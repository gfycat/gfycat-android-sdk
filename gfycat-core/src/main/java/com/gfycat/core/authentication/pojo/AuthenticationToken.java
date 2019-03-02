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

import com.gfycat.core.authentication.Token;
import com.gfycat.core.gfycatapi.pojo.ErrorMessage;
import com.google.gson.annotations.SerializedName;

/**
 * Authentication token. Used to authenticate application.
 * <p>
 * Created by dekalo on 14.09.15.
 */
public class AuthenticationToken implements Token {

    public final static String ACCESS_DENIED = "access_denied";
    public final static String INVALID_CLIENT = "invalid_client";

    @SerializedName("access_token")
    String accessToken;

    @SerializedName("expires_in")
    long expiresIn;

    @SerializedName("token_type")
    String tokenType;

    @SerializedName("scope")
    String scope;

    @SerializedName("error")
    ErrorMessage errorMessage;

    @SerializedName("resource_owner")
    String userid;

    @SerializedName("refresh_token")
    String refreshToken;

    @SerializedName("refresh_token_expires_in")
    long refreshTokenExpiresIn;


    @Override
    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }


    public void setRefreshTokenExpiresIn(long refreshTokenExpiresIn) {
        this.refreshTokenExpiresIn = refreshTokenExpiresIn;
    }

    @Override
    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    @Override
    public ErrorMessage getError() {
        return errorMessage;
    }

    public void setError(ErrorMessage error) {
        this.errorMessage = errorMessage;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    @Override
    public String toString() {
        return "AuthenticationToken{" +
                "accessToken='" + accessToken + '\'' +
                ", expiresIn=" + expiresIn +
                ", tokenType='" + tokenType + '\'' +
                ", scope='" + scope + '\'' +
                ", errorMessage=" + errorMessage +
                ", userid='" + userid + '\'' +
                ", refreshToken='" + refreshToken + '\'' +
                ", refreshTokenExpiresIn=" + refreshTokenExpiresIn +
                '}';
    }
}
