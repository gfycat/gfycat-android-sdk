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

import com.gfycat.core.authentication.pojo.AuthenticationToken;
import com.gfycat.core.authentication.pojo.TokenRequest;

import io.reactivex.Single;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Gfycat api for getting access token.
 * <p>
 * Created by dekalo on 14.09.15.
 */
public interface AuthenticationAPI {

    String PATH = "oauth/token";

    @POST(PATH)
    Call<AuthenticationToken> requestTokenCall(@Body TokenRequest tokenRequest);

    @POST(PATH)
    Single<AuthenticationToken> requestToken(@Body TokenRequest tokenRequest);
}
