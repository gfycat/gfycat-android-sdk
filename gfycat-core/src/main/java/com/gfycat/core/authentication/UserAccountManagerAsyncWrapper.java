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
import io.reactivex.subjects.ReplaySubject;

/**
 * Created by dgoliy on 4/11/17.
 */

public class UserAccountManagerAsyncWrapper implements UserAccountManager {
    private ReplaySubject<UserAccountManager> subject = ReplaySubject.create();

    public void init(UserAccountManager userAccountManager) {
        if (subject.hasComplete()) {
            return;
        }
        subject.onNext(userAccountManager);
        subject.onComplete();
    }

    @Override
    public UserInfo getUserInfo() {
        UserAccountManager userAccountManager = subject.getValue();
        return userAccountManager != null ? userAccountManager.getUserInfo() : null;
    }

    @Override
    public Observable<UserInfo> observe() {
        return subject.flatMap(UserAccountManager::observe);
    }

    @Override
    public boolean isSignedIn() {
        UserAccountManager userAccountManager = subject.getValue();
        return userAccountManager != null && userAccountManager.isSignedIn();
    }

    @Override
    public Completable observeUserNameAvailability(String username) {
        return subject.flatMap(manager -> manager.observeUserNameAvailability(username).toObservable()).ignoreElements();
    }

    @Override
    public Completable resetPassword(String email) {
        return subject.flatMap(manager -> manager.resetPassword(email).toObservable()).ignoreElements();
    }

    @Override
    public Single<String> signIn(String accountOrEmail, String password) {
        return subject.singleOrError().flatMap(manager -> manager.signIn(accountOrEmail, password));
    }

    @Override
    public Single<String> signUp(String username, String password) {
        return subject.singleOrError().flatMap(manager -> manager.signUp(username, password));
    }

    @Override
    public Single<String> signUp(String email, String username, String password) {
        return subject.singleOrError().flatMap(manager -> manager.signUp(email, username, password));
    }

    @Override
    public Single<String> facebookSignIn(String facebookToken, @Nullable String facebookId, @Nullable String facebookName) {
        return subject.singleOrError().flatMap(manager -> manager.facebookSignIn(facebookToken, facebookId, facebookName));
    }

    @Override
    public Single<String> facebookSignUp(String login, String facebookToken) {
        return subject.singleOrError().flatMap(manager -> manager.facebookSignUp(login, facebookToken));
    }

    @Override
    public void signOut() {
        UserAccountManager userAccountManager = subject.getValue();
        if (userAccountManager != null) {
            userAccountManager.signOut();
        }
    }

    @Override
    public Completable validateUserEmail() {
        return subject.flatMap(userAccountManager -> userAccountManager.validateUserEmail().toObservable()).ignoreElements();
    }

    @Override
    public Completable updateUserProfile(UpdateUserInfo body) {
        return subject.flatMap(manager -> manager.updateUserProfile(body).toObservable()).ignoreElements();
    }

    @Override
    public Completable uploadUserAvatar(InputStream is) {
        return subject.flatMap(manager -> manager.uploadUserAvatar(is).toObservable()).ignoreElements();
    }
}
