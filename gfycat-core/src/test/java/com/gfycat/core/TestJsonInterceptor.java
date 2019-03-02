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

package com.gfycat.core;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Response;
import okhttp3.ResponseBody;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_OK;

class TestJsonInterceptor implements Interceptor {

    private static final String MIME_TYPE_JSON = "text/json";

    private String expectedUrl;
    private String targetResponse;

    @Override
    public Response intercept(Chain chain) {
        if (chain.request().url().toString().equals(expectedUrl)) {
            return new Response.Builder()
                    .code(HTTP_OK)
                    .body(ResponseBody.create(MediaType.get(MIME_TYPE_JSON), targetResponse))
                    .protocol(Protocol.HTTP_2)
                    .request(chain.request())
                    .message("SUCCESS")
                    .build();
        }
        return new Response.Builder()
                .code(HTTP_BAD_REQUEST)
                .message("Url(" + chain.request().url() + ") was not expected, should be " + expectedUrl)
                .protocol(Protocol.HTTP_2)
                .request(chain.request())
                .body(ResponseBody.create(MediaType.get(MIME_TYPE_JSON), ""))
                .build();
    }

    public void setExpectedUrlAndResponse(String expectedUrl, String targetResponse) {
        this.expectedUrl = expectedUrl;
        this.targetResponse = targetResponse;
    }

    public void tearDown() {
        expectedUrl = null;
        targetResponse = null;
    }
}
