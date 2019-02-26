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

package com.gfycat.core.authentication.pojo;


import com.google.gson.annotations.SerializedName;

/**
 * Created by dekalo on 05.02.16.
 */
public class SignUpRequest {

    public SignUpRequest() {
    }

    public static SignUpRequest signUpWithFacebook(String username, String facebookToken) {
        SignUpRequest result = new SignUpRequest();
        result.username = username;
        result.facebookToken = facebookToken;
        result.provider = "facebook";
        return result;
    }

    public static SignUpRequest signUpWithPassword(String username, String password) {
        SignUpRequest result = new SignUpRequest();
        result.username = username;
        result.password = password;
        return result;
    }

    public static SignUpRequest signUpWithEmailAndPassword(String username, String email, String password) {
        SignUpRequest result = new SignUpRequest();
        result.username = username;
        result.email = email;
        result.password = password;
        return result;
    }

    String provider;
    String username;
    String email;
    String password;

    @SerializedName("access_token")
    String facebookToken;

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getProvider() {
        return provider;
    }
}
