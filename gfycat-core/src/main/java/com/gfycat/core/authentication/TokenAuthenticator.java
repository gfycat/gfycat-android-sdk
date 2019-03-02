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

import com.gfycat.common.utils.Assertions;
import com.gfycat.common.utils.Logging;
import com.gfycat.core.GfycatApplicationInfo;
import com.gfycat.core.authentication.pojo.AuthenticationToken;
import com.gfycat.core.authentication.pojo.SignUpRequest;
import com.gfycat.core.authentication.pojo.TokenRequest;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.functions.BiConsumer;
import io.reactivex.functions.Function;
import okhttp3.Authenticator;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

/**
 * Responsible for signing request with token and renewing it, when it is expired.
 * <p>
 * Created by dekalo on 14.09.15.
 */
public class TokenAuthenticator implements Authenticator, Interceptor {

    private static final String LOG_TAG = "TokenAuthenticator";

    public static final String AUTHORIZATION_HEADER = "Authorization";
    private static final int MAX_AUTHENTIFICATION_TRIES = 3;


    private final AtomicBoolean needRenewToken = new AtomicBoolean(false);

    private GfycatApplicationInfo gfycatApplicationInfo;
    private final AuthenticationAPI authenticationApi;
    private final TokenRenewer tokenRenewer = new TokenRenewerImpl();
    private final TokenStorage tokenStorage;
    private volatile Token lastToken = Token.NO_TOKEN;
    private SignUpAPI signUpAPI;

    public TokenAuthenticator(Context context, GfycatApplicationInfo gfycatApplicationInfo, AuthenticationAPI authenticationApi) {
        this.tokenStorage = new TokenStorage(context);
        this.authenticationApi = authenticationApi;
        this.gfycatApplicationInfo = gfycatApplicationInfo;
        lastToken = tokenStorage.get();
    }

    @Override
    public Request authenticate(Route route, Response response) throws IOException {
        Logging.d(LOG_TAG, "::authenticate(", response.request().url().toString(), ") ", responseCount(response));

        if (responseCount(response) > MAX_AUTHENTIFICATION_TRIES) {
            Logging.c(LOG_TAG, "Failed to authenticate url: " + response.request().url().toString());
            broadcastAuthenticationProblems();
            return null;
        }

        if (reNewToken()) {
            return response.request().newBuilder()
                    .removeHeader(AUTHORIZATION_HEADER)
                    .addHeader(AUTHORIZATION_HEADER, bearer(lastToken.getAccessToken()))
                    .build();
        } else {
            return null;
        }
    }

    private String bearer(String accessToken) {
        return "Bearer " + accessToken;
    }

    private boolean isValid(Token token) {
        return token != null && token.getError() == null;
    }

    private boolean reNewToken() {

        needRenewToken.set(true);

        synchronized (this) {

            if (needRenewToken.get()) {

                try {
                    retrofit2.Response<AuthenticationToken> tokenReponse = getNewToken();
                    if (tokenReponse.isSuccessful()) {
                        boolean result = changeToken(tokenReponse.body());
                        if (!result) signOut();
                        return result;
                    } else {
                        signOut();
                        return false;
                    }
                } catch (IOException e) {
                    signOut();
                    return false;
                } finally {
                    needRenewToken.set(false);
                }
            } else {
                Logging.d(LOG_TAG, "Other thread updated token while we was waiting synchronization.");
                return true;
            }
        }
    }

    /**
     * @param token - new token.
     * @return true if it is valid token, false otherwise.
     */
    private synchronized boolean changeToken(Token token) {
        Assertions.assertNull(token.getError(), () -> new Exception("token.getError() = " + token.getError()));
        Logging.d(LOG_TAG, "changeToken(", token, ") from: ", lastToken);
        if (isValid(token)) {
            lastToken = token;
            tokenStorage.putSync(token);
            return true;
        }
        return false;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Logging.d(LOG_TAG, "::intercept(", chain.request().url().toString(), ") lastToken = ", lastToken);
        Request request = chain.request();

        if (Token.NO_TOKEN.equals(lastToken)) reNewToken();

        if (!Token.NO_TOKEN.equals(lastToken)) {
            request = request.newBuilder().addHeader(AUTHORIZATION_HEADER, bearer(lastToken.getAccessToken())).build();
        }

        return chain.proceed(request);
    }

    private static int responseCount(Response response) {
        int result = 1;
        while ((response = response.priorResponse()) != null) {
            result++;
        }
        return result;
    }

    private void broadcastAuthenticationProblems() {
        Logging.d(LOG_TAG, "::broadcastAuthenticationProblems()");
        tokenStorage.putSync(Token.NO_TOKEN);
    }

    private retrofit2.Response<AuthenticationToken> getNewToken() throws IOException {
        Assertions.assertNotUIThread(IllegalAccessException::new);
        Logging.d(LOG_TAG, "getNewToken() called with lastToken = ", lastToken);
        return tokenRenewer.reNew(gfycatApplicationInfo, authenticationApi, lastToken);
    }

    public Single<String> facebookSignUp(String login, String facebookToken, BiConsumer<Token, Token> onTokenChange) {
        Logging.d(LOG_TAG, "facebookSignUp(...) with facebook token");
        Assertions.assertNotNull(signUpAPI, () -> new NullPointerException("signUpAPI was not set."));
        return authenticate(signUpAPI.signUp(SignUpRequest.signUpWithFacebook(login, facebookToken)), onTokenChange);
    }

    public Single<String> signUp(String login, String email, String password, BiConsumer<Token, Token> onTokenChange) {
        Logging.d(LOG_TAG, "signUp(...) with password");
        Assertions.assertNotNull(signUpAPI, () -> new NullPointerException("signUpAPI was not set."));
        return authenticate(signUpAPI.signUp(SignUpRequest.signUpWithEmailAndPassword(login, email, password)), onTokenChange);
    }

    public Single<String> signUp(String login, String password, BiConsumer<Token, Token> onTokenChange) {
        Logging.d(LOG_TAG, "signUp(...) with password");
        Assertions.assertNotNull(signUpAPI, () -> new NullPointerException("signUpAPI was not set."));
        return authenticate(signUpAPI.signUp(SignUpRequest.signUpWithPassword(login, password)), onTokenChange);
    }

    public Single<String> signIn(String login, String password, BiConsumer<Token, Token> onTokenChange) {
        Logging.d(LOG_TAG, "signIn(...)");
        return authenticate(authenticationApi.requestToken(TokenRequest.userTokenRequest(gfycatApplicationInfo, login, password)), onTokenChange);
    }

    public Single<String> facebookSignIn(String token, BiConsumer<Token, Token> onTokenChange) {
        Logging.d(LOG_TAG, "signIn(...)");
        return authenticate(authenticationApi.requestToken(TokenRequest.facebookTokenRequest(gfycatApplicationInfo, token)), onTokenChange);
    }

    private Single<String> authenticate(final Single<AuthenticationToken> single, BiConsumer<Token, Token> onTokenChange) {

        TokenChangeHelper helper = new TokenChangeHelper(onTokenChange);

        return single
                .onErrorResumeNext(throwable -> Single.error(ErrorMessageException.fromRawThrowable(throwable)))
                .doOnSuccess(authenticationToken -> helper.preTokenChange(lastToken, authenticationToken))
                .flatMap(new Function<AuthenticationToken, SingleSource<String>>() {
                    @Override
                    public SingleSource<String> apply(AuthenticationToken authenticationToken) throws Exception {
                        if (changeToken(authenticationToken)) {
                            return Single.just(authenticationToken.getUserid());
                        } else {
                            return Single.error(new ErrorMessageException(authenticationToken.getError()));
                        }
                    }
                })
                .doOnSuccess(username -> helper.postTokenChange(lastToken))
                .doOnError(throwable -> Logging.d(LOG_TAG, throwable, " onError while authenticate", throwable));
    }

    public void signOut() {
        dropToken();
    }

    private void dropToken() {
        lastToken = Token.NO_TOKEN;
        tokenStorage.removeSync();
    }

    public void setSignUpAPI(SignUpAPI signUpAPI) {
        this.signUpAPI = signUpAPI;
    }

    Observable<Token> observeToken() {
        return tokenStorage.observe();
    }

    private class TokenChangeHelper {

        private final BiConsumer<Token, Token> onTokenChange;
        private Token previousToken;

        public TokenChangeHelper(BiConsumer<Token, Token> onTokenChange) {
            this.onTokenChange = onTokenChange;
        }

        private void preTokenChange(Token previousToken, Token nextToken) {
            this.previousToken = previousToken;
        }

        private void postTokenChange(Token nextToken) {
            try {
                onTokenChange.accept(previousToken, nextToken);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
