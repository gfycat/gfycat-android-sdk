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

import android.text.TextUtils;

import com.gfycat.core.GfycatApplicationInfo;
import com.gfycat.core.authentication.pojo.AuthenticationToken;
import com.gfycat.core.authentication.pojo.TokenRequest;

import java.io.IOException;

import retrofit2.Response;

/**
 * Map for token refreshing.
 * <p>
 * Created by dekalo on 20.01.16.
 */
public class TokenRenewerImpl implements TokenRenewer {

    @Override
    public Response<AuthenticationToken> reNew(GfycatApplicationInfo gfycatApplicationInfo, AuthenticationAPI api, Token oldToken) throws IOException {
        // no token case
        if (oldToken == null || Token.NO_TOKEN.equals(oldToken)) {
            return api.requestTokenCall(TokenRequest.applicationTokenRequest(gfycatApplicationInfo)).execute();
        } else if (!TextUtils.isEmpty(oldToken.getRefreshToken())) {
            return api.requestTokenCall(TokenRequest.refreshUserTokenRequest(gfycatApplicationInfo, oldToken.getRefreshToken())).execute();
        } else {
            return api.requestTokenCall(TokenRequest.applicationTokenRequest(gfycatApplicationInfo)).execute();
        }
    }
}
