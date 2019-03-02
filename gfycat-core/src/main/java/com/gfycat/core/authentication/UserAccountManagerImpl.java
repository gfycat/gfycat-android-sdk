/*
 * Copyright (c) 2015-present, Gfycat, Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gfycat.core.authentication;

import android.content.Context;
import android.text.TextUtils;
import android.webkit.URLUtil;

import com.gfycat.common.ChainedException;
import com.gfycat.common.utils.Assertions;
import com.gfycat.common.utils.Logging;
import com.gfycat.common.utils.Utils;
import com.gfycat.core.GfyCore;
import com.gfycat.core.NoAuthAPI;
import com.gfycat.core.bi.BIContext;
import com.gfycat.core.bi.analytics.GfycatAnalytics;
import com.gfycat.core.bi.corelogger.CoreLogger;
import com.gfycat.core.gfycatapi.GfycatAPI;
import com.gfycat.core.gfycatapi.pojo.ErrorMessage;
import com.gfycat.core.gfycatapi.pojo.ResetPasswordRequest;
import com.gfycat.core.gfycatapi.pojo.TransferGfycatsRequest;
import com.gfycat.core.gfycatapi.pojo.UpdateUserInfo;
import com.gfycat.core.gfycatapi.pojo.UserInfo;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiConsumer;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.HttpException;
import retrofit2.Response;

/**
 * Created by dekalo on 20.01.16.
 */
public class UserAccountManagerImpl implements UserAccountManager {

    private static final String LOG_TAG = "UserAccountManagerImpl";

    private static final long WAIT_AVATAR_DURATION = 3;
    private static final long TRIALS_COUNT = 10;

    private static final String GHOST_USERNAME_PREFIX = "ghost_";
    private static final int USERNAME_SIZE = 20;
    private static final int PASSWORD_SIZE = 20;

    private UserInfoStorage userInfoStorage;
    private final TokenAuthenticator authenticator;
    private final GfycatAPI gfycatAPI;
    private final NoAuthAPI noAuthApi;
    private final Disposable tokenChangeSubscription;
    private final Runnable dropUserRelatedContent;
    private final GhostUserPreference ghostUserPreference;

    public UserAccountManagerImpl(Context context, TokenAuthenticator authenticator, GfycatAPI gfycatAPI, NoAuthAPI noAuthApi, Runnable dropUserRelatedContent) {
        this.authenticator = authenticator;
        this.gfycatAPI = gfycatAPI;
        this.noAuthApi = noAuthApi;
        this.userInfoStorage = new UserInfoStorage(context);
        this.tokenChangeSubscription = authenticator.observeToken().observeOn(Schedulers.io()).subscribe(new RefreshAction(), throwable -> Assertions.fail(new ChainedException(throwable)));
        this.dropUserRelatedContent = dropUserRelatedContent;
        this.ghostUserPreference = new GhostUserPreference(context);
    }

    public boolean isUserReal() {
        return isSignedIn() && !ghostUserPreference.isUserGhost();
    }

    public boolean isUserGhost() {
        return isSignedIn() && ghostUserPreference.isUserGhost();
    }

    @Override
    public Single<String> facebookSignUp(String login, String facebookToken) {
        return authenticator.facebookSignUp(login, facebookToken, new TransferGfycatsIfNeeded());
    }

    @Override
    public Completable validateUserEmail() {
        return gfycatAPI
                .verifyUserEmail("")
                .onErrorResumeNext(throwable -> {
                    if (throwable instanceof IOException) {
                        return Completable.complete();
                    } else if (throwable instanceof HttpException) {
                        HttpException httpException = (HttpException) throwable;
                        if (httpException.code() == 400)
                            return Completable.error(new ErrorMessageException(new ErrorMessage(ErrorMessage.Client.EMAIL_NOT_VERIFIED, ErrorMessage.ClientMessage.EMAIL_UNABLE_TO_VERIFY)));
                        if (httpException.code() == 404)
                            return Completable.error(new ErrorMessageException(new ErrorMessage(ErrorMessage.Client.EMAIL_NOT_VERIFIED, ErrorMessage.ClientMessage.EMAIL_UNABLE_TO_VERIFY)));
                        Assertions.fail(new IllegalStateException(LOG_TAG + " Unknown code for observeUserNameAvailability " + httpException.code(), throwable));
                        return Completable.complete();
                    } else {
                        Assertions.fail(new IllegalStateException(LOG_TAG + " Class cast in observeUserNameAvailability throwable instanceof " + throwable, throwable));
                        return Completable.complete();
                    }
                });
    }

    @Override
    public Single<String> facebookSignIn(String facebookToken, String facebookId, String facebookName) {
        Logging.d(LOG_TAG, "facebookSignIn(", facebookId, ", ", facebookName, ")");
        return authenticator.facebookSignIn(facebookToken, new TransferGfycatsIfNeeded())
                .onErrorResumeNext(new MapFacebookUserNotExistsToCreateUserError())
                .doOnSuccess(username -> GfycatAnalytics.getLogger(CoreLogger.class).logAccountLoggedIn(
                        new BIContext(BIContext.FACEBOOK_FLOW),
                        facebookName,
                        "",
                        facebookId))
                .doOnSuccess(username -> ghostUserPreference.setUserReal());
    }

    @Override
    public Single<String> signIn(String accountOrEmail, String password) {
        return authenticator
                .signIn(accountOrEmail, password, new TransferGfycatsIfNeeded())
                .doOnSuccess(username -> GfycatAnalytics.getLogger(CoreLogger.class).logAccountLoggedIn(new BIContext(BIContext.EMAIL_FLOW), username, accountOrEmail, ""))
                .doOnSuccess(s -> ghostUserPreference.setUserReal());

    }

    public Completable signUpGhost() {

        if (isSignedIn())
            throw new IllegalStateException("Can not create ghost account with already signed user.");

        String username;
        String password = Utils.randomString(PASSWORD_SIZE);

        Throwable availabilityException;
        int tryCount = 0;

        do {
            username = GHOST_USERNAME_PREFIX + Utils.randomString(USERNAME_SIZE - GHOST_USERNAME_PREFIX.length());
            availabilityException = GfyCore.getUserAccountManager().observeUserNameAvailability(username).blockingGet();
        } while (availabilityException != null && tryCount++ < 5);


        return authenticator
                .signUp(username, password, new TransferGfycatsIfNeeded())
                .doOnSuccess(localUsername -> GfycatAnalytics.getLogger(CoreLogger.class).logAccountCreated(new BIContext(BIContext.GHOST_FLOW), localUsername, "", ""))
                .flatMap(this::waitForUserInfoUpdate)
                .ignoreElement()
                .doOnComplete(ghostUserPreference::setUserGhost);
    }

    private Single<String> waitForUserInfoUpdate(String username) {
        return observe()
                .filter(userInfo -> userInfo != null && username.equals(userInfo.getUserid()))
                .firstOrError()
                .map(UserInfo::getUserid);
    }

    @Override
    public Single<String> signUp(String username, String password) {
        return authenticator
                .signUp(username, password, new TransferGfycatsIfNeeded())
                .doOnSuccess(s -> GfycatAnalytics.getLogger(CoreLogger.class).logAccountCreated(new BIContext(BIContext.GHOST_FLOW), username, "", ""))
                .doOnSuccess(s -> ghostUserPreference.setUserReal());
    }

    @Override
    public Single<String> signUp(final String email, final String username, final String password) {
        return authenticator
                .signUp(username, email, password, new TransferGfycatsIfNeeded())
                .doOnSuccess(s -> GfycatAnalytics.getLogger(CoreLogger.class).logAccountCreated(new BIContext(BIContext.EMAIL_FLOW), username, email, ""))
                .doOnSuccess(s -> ghostUserPreference.setUserReal());
    }

    @Override
    public Completable resetPassword(final String email) {
        return gfycatAPI.resetPassword(new ResetPasswordRequest(email))
                .onErrorResumeNext(throwable -> Completable.error(ErrorMessageException.fromRawThrowable(throwable)));
    }

    @Override
    public void signOut() {
        Logging.c(LOG_TAG, "signOut()");
        authenticator.signOut();
        /*
         * Drop userInfo and user content will happen in RefreshAction callback.
         */
    }

    @Override
    public boolean isSignedIn() {
        return userInfoStorage.get() != UserInfoStorage.NO_USER;
    }

    @Override
    public Completable observeUserNameAvailability(String username) {
        return gfycatAPI
                .checkAvailability(username)
                .flatMapCompletable(response -> {
                    if (response.code() == 204) {
                        return Completable.error(new ErrorMessageException(new ErrorMessage(ErrorMessage.Server.USERNAME_UNAVAILABLE, ErrorMessage.ClientMessage.USERNAME_IS_UNAVAILABLE)));
                    } else if (response.code() == 422) {
                        return Completable.error(new ErrorMessageException(new ErrorMessage(ErrorMessage.Server.INVALID_USERNAME, ErrorMessage.ClientMessage.USERNAME_IS_INVALID)));
                    } else if (response.code() == 404) {
                        return Completable.complete();
                    } else {
                        Logging.c(LOG_TAG, "unknown error code for response", new RuntimeException());
                        return Completable.complete();
                    }
                });
    }

    @Override
    public UserInfo getUserInfo() {
        return userInfoStorage.get();
    }

    @Override
    public Observable<UserInfo> observe() {
        return userInfoStorage.observe();
    }

    @Override
    public Completable updateUserProfile(UpdateUserInfo body) {
        return gfycatAPI
                .updateUserProfile(body)
                .andThen(refreshUserProfile())
                .onErrorResumeNext(throwable -> Completable.error(new ErrorMessageException(new ErrorMessage("-1", throwable.getMessage()))))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Completable uploadUserAvatar(InputStream is) {
        return gfycatAPI
                .getUploadURL()
                .flatMap((Function<Response<ResponseBody>, SingleSource<String>>) responseBodyResponse -> {
                    String destinationUrl = "";
                    try {
                        destinationUrl = responseBodyResponse.body().string();
                        destinationUrl = destinationUrl.replace("\\", "");
                        destinationUrl = destinationUrl.replace("\"", "");
                    } catch (IOException ignored) {
                    }

                    if (!URLUtil.isValidUrl(destinationUrl) || !destinationUrl.contains("/"))
                        return Single.error(new Throwable("Invalid upload URL!"));

                    final String ticket = destinationUrl.substring(destinationUrl.lastIndexOf('/') + 1);

                    RequestBody requestBody = new TypedImageRequestBody(MediaType.parse("multipart/form-data"), is);

                    return noAuthApi
                            .uploadAvatarImage(destinationUrl, requestBody)
                            .flatMap(response -> waitAvatarChanged(0, ticket, 0))
                            .ignoreElements()
                            .andThen(refreshUserProfile())
                            .onErrorResumeNext(throwable -> Completable.error(new ErrorMessageException(new ErrorMessage("-1", throwable.getMessage()))))
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .toSingle(() -> "");
                })
                .toCompletable()
                .onErrorResumeNext(throwable -> Completable.error(new ErrorMessageException(new ErrorMessage("-1", throwable.getMessage()))))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    private Observable<Response<ResponseBody>> waitAvatarChanged(long delay, String ticket, int count) {
        return Observable
                .timer(delay, TimeUnit.SECONDS)
                .flatMap((Function<Long, Observable<Response<ResponseBody>>>) aLong ->
                        gfycatAPI.getAvatarStatus(ticket).flatMap((Function<Response<ResponseBody>, Observable<Response<ResponseBody>>>) response -> {
                            String result = "";
                            try {
                                result = response.body().string();
                            } catch (IOException ex) {
                                return Observable.error(ex);
                            }

                            if (response.isSuccessful() &&
                                    result.equalsIgnoreCase("\"succeeded\"")) {
                                return Observable.just(response);
                            } else if (!response.isSuccessful()) {
                                return Observable.error(new Throwable(response.message()));
                            } else if (count >= TRIALS_COUNT) {
                                return Observable.error(new Throwable("Timeout exception."));
                            } else {
                                return waitAvatarChanged(WAIT_AVATAR_DURATION, ticket, count + 1);
                            }
                        }));
    }

    private Completable refreshUserProfile() {
        return gfycatAPI.getMyInfo().doOnNext(userInfo -> userInfoStorage.putSync(userInfo)).ignoreElements();
    }

    private class RefreshAction implements Consumer<Token> {
        @Override
        public void accept(Token token) {
            UserInfo userInfo = userInfoStorage.get();

            Logging.d(LOG_TAG, "::RefreshAction called with token = ", token, " userInfo = ", userInfo);

            /**
             * Drop user info if user is not logged anymore.
             */
            if (token == null || TextUtils.isEmpty(token.getUserid())) {

                if (userInfo != UserInfoStorage.NO_USER) {
                    Logging.c(LOG_TAG, "user logged out, drop it's data");
                    dropUserContent();
                    Logging.setUserId(null);
                    userInfoStorage.putSync(UserInfoStorage.NO_USER);
                    ghostUserPreference.setUserGhost();
                } else {
                    Logging.d(LOG_TAG, "application token refresh happens");
                }

            } else {
                String newUserId = token.getUserid();
                Logging.setUserId(newUserId);
                /**
                 * New user just logged in
                 */
                if (userInfo == UserInfoStorage.NO_USER) {
                    Logging.c(LOG_TAG, "new user logged in");
                    userInfoStorage.putSync(UserInfo.from(newUserId));
                }

                /**
                 * This is another user.
                 */
                if (userInfo != UserInfoStorage.NO_USER && !userInfo.getUserid().equals(newUserId)) {
                    Logging.c(LOG_TAG, "another user logged in, drop db");
                    dropUserContent();
                    userInfoStorage.putSync(UserInfo.from(newUserId));
                }

                gfycatAPI.getMyInfo().subscribe(updatedUserInfo -> {
                    Logging.d(LOG_TAG, "successfully updated userInfo = " + updatedUserInfo);
                    userInfoStorage.putSync(updatedUserInfo);
                }, throwable -> {
                    Logging.e(LOG_TAG, "onErrorHappens in RefreshAction gfycatAPI.getMyInfo()", throwable);
                });
            }
        }
    }

    private void dropUserContent() {
        Logging.d(LOG_TAG, "dropUserContent()");
        if (dropUserRelatedContent != null) dropUserRelatedContent.run();
    }

    private class MapFacebookUserNotExistsToCreateUserError implements Function<Throwable, Single<String>> {

        @Override
        public Single<String> apply(Throwable throwable) {
            if (throwable instanceof ErrorMessageException) {
                ErrorMessageException authException = (ErrorMessageException) throwable;
                if (ErrorMessage.Server.USER_NOT_EXISTS.equals(authException.getErrorMessage().getCode())) {
                    return Single.error(new FacebookCreateUserException(authException.getErrorMessage().getDescription()));
                } else {
                    return Single.error(new FacebookSignInException());
                }
            } else {
                return Single.error(throwable);
            }
        }
    }

    @SuppressWarnings("unused")
    public void release() {
        if (tokenChangeSubscription != null) tokenChangeSubscription.dispose();
    }

    public static class FacebookSignInException extends Throwable {
    }

    public static class FacebookCreateUserException extends Throwable {
        public FacebookCreateUserException(String description) {
            super(description);
        }
    }

    private class TransferGfycatsIfNeeded implements BiConsumer<Token, Token> {
        @Override
        public void accept(Token prev, Token next) {
            if (prev != null && !TextUtils.isEmpty(prev.getUserid()) && isUserGhost()) {
                try {
                    Logging.d(LOG_TAG, "TransferGfycatsIfNeeded from ", prev.getUserid(), " to ", next.getUserid());
                    gfycatAPI.transferGfycats(next.getUserid(), new TransferGfycatsRequest(prev.getAccessToken(), next.getAccessToken())).blockingAwait();
                } catch (Throwable throwable) {
                    collectTransferIssueDetails(prev, next, throwable);
                }
            }
        }
    }

    private static void collectTransferIssueDetails(Token prev, Token next, Throwable throwable) {
        StringBuilder sb = new StringBuilder();

        sb.append("from:[").append(prev.getUserid()).append(" ").append(prev.getAccessToken()).append("]");
        sb.append("to:[").append(next.getUserid()).append(" ").append(next.getAccessToken()).append("]");

        if (throwable instanceof HttpException) {
            HttpException httpException = (HttpException) throwable;
            sb.append("code:").append(httpException.code()).append(" ");
            sb.append("message:").append(httpException.message()).append(" ");
            try {
                sb.append("body:").append(httpException.response().errorBody().string());
            } catch (IOException e) {
                sb.append("[IOException] while reading body");
            }
        }

        Assertions.fail(new ChainedException(sb.toString(), throwable));

    }
}
