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

import com.gfycat.common.ChainedException;
import com.gfycat.common.utils.Assertions;

import java.io.InterruptedIOException;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

/**
 * Network related utility methods.
 */
public class NetworkUtils {

    public static String getPublicUrlForGfyName(String gfyName) {
        return "https://gfycat.com/" + gfyName;
    }

    public static boolean isAcceptableNetworkException(Throwable throwable) {
        if (throwable == null) return false;
        return throwable instanceof ConnectException
                || throwable instanceof UnknownHostException
                || throwable instanceof SocketTimeoutException
                || throwable instanceof SocketException
                || isAcceptableNetworkException(throwable.getCause());
    }

    public static void reportIfNotAcceptable(Throwable throwable) {
        if (!isAcceptableNetworkException(throwable)) {
            Assertions.fail(new ChainedException(throwable));
        }
    }


}
