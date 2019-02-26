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

import android.support.annotation.Nullable;

import com.gfycat.core.gfycatapi.pojo.UpdateUserInfo;
import com.gfycat.core.gfycatapi.pojo.UserInfo;

import java.io.InputStream;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

/**
 * User account management. Sign up, sign in and sign out actions. Update user data.
 */
public interface UserAccountManager {

    /**
     * @return UserInfo if user is already signed in, null otherwise.
     */
    UserInfo getUserInfo();

    /**
     * UserInfo changes Observable.
     * <p>
     * This is an infinite Observable - onComplete() and onError() will never be called.
     * <p>
     * Note: This call will not perform any network requests.
     *
     * @return UserInfo observable.
     */
    Observable<UserInfo> observe();

    /**
     * @return true if user is signed in, false otherwise.
     */
    boolean isSignedIn();

    /**
     * Network request for username availability status.
     * <p>
     * See https://developers.gfycat.com/api/#checking-if-the-username-is-available-username-exists-username-is-valid for more information
     */
    Completable observeUserNameAvailability(String username);

    /**
     * Reset password by providing email address.
     * <p>
     * See http://developers.gfycat.com/api/#send-a-password-reset-email
     */
    Completable resetPassword(String email);

    /**
     * Sign in existing user.
     *
     * @param accountOrEmail user's email or account.
     * @param password       user's password.
     */
    Single<String> signIn(String accountOrEmail, String password);

    /**
     * Register new user.
     *
     * @param username username.
     * @param password user's password.
     */
    Single<String> signUp(String username, String password);

    /**
     * Register new user.
     *
     * @param email    user's email.
     * @param username username.
     * @param password user's password.
     */
    Single<String> signUp(String email, String username, String password);

    /**
     * Sign in with facebook token.
     *
     * @param facebookToken facebook token for authentication.
     * @param facebookId    facebook user id for GfycatAnalytics. Can be null.
     * @param facebookName  facebook user name for GfycatAnalytics. Can be null.
     */
    Single<String> facebookSignIn(String facebookToken, @Nullable String facebookId, @Nullable String facebookName);

    /**
     * Register new user with facebook token and login.
     *
     * @param login         to create account with.
     * @param facebookToken from users's facebook account.
     */
    Single<String> facebookSignUp(String login, String facebookToken);

    /**
     * Sign out from current account.
     */
    void signOut();

    /**
     * Request email verification
     * <p>
     * See http://developers.gfycat.com/api/#sending-an-email-verification-request
     */
    Completable validateUserEmail();

    /**
     * Update user profile with UpdateUserInfo data.
     * <p>
     * See http://developers.gfycat.com/api/#updating-user-39-s-details for details.
     */
    Completable updateUserProfile(UpdateUserInfo body);

    /**
     * Update user's profile picture.
     * <p>
     * See http://developers.gfycat.com/api/#uploading-user-39-s-profile-image
     *
     * @param is should be a stream of valid image data, bitmap or png.
     */
    Completable uploadUserAvatar(InputStream is);
}