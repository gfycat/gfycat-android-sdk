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

import com.gfycat.core.GfycatApplicationInfo;
import com.google.gson.annotations.SerializedName;

/**
 * Represent Json objects that would be sent for various authentication requests.
 * <p>
 * Created by dekalo on 10.02.16.
 */
public class TokenRequest {

    public static TokenRequest applicationTokenRequest(GfycatApplicationInfo gfycatApplicationInfo) {
        TokenRequest request = new TokenRequest(gfycatApplicationInfo);
        request.grantType = "client_credentials";
        return request;
    }

    public static TokenRequest facebookTokenRequest(GfycatApplicationInfo gfycatApplicationInfo, String token) {
        TokenRequest request = new TokenRequest(gfycatApplicationInfo);
        request.grantType = "convert_token";
        request.provider = "facebook";
        request.token = token;
        return request;
    }

    public static TokenRequest userTokenRequest(GfycatApplicationInfo gfycatApplicationInfo, String username, String password) {
        TokenRequest request = new TokenRequest(gfycatApplicationInfo);
        request.grantType = "password";
        request.scope = "basicProfile,publishPost,listPublications,create";
        request.username = username;
        request.password = password;
        return request;
    }

    public static TokenRequest refreshUserTokenRequest(GfycatApplicationInfo gfycatApplicationInfo, String refreshToken) {
        TokenRequest request = new TokenRequest(gfycatApplicationInfo);
        request.grantType = "refresh";
        request.refreshToken = refreshToken;
        return request;
    }

    @SerializedName("client_id")
    private final String clientId;
    @SerializedName("client_secret")
    private final String clientSecret;
    @SerializedName("grant_type")
    private String grantType;
    private String scope;
    private String username;
    private String password;
    @SerializedName("refresh_token")
    private String refreshToken;
    private String provider;
    private String token;

    public TokenRequest(GfycatApplicationInfo gfycatApplicationInfo) {
        this.clientId = gfycatApplicationInfo.clientId;
        this.clientSecret = gfycatApplicationInfo.clientSecret;
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }

    public String getProvider() {
        return provider;
    }

    public String getToken() {
        return token;
    }
}
