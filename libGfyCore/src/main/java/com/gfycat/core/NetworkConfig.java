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

/**
 * Networking config.
 * <p/>
 * Created by dekalo on 14.12.15.
 */
public class NetworkConfig {

    public static final String DEFAULT_DOMAIN = "gfycat.com";

    public static String buildApiUrl(String domainName) {
        return "https://api." + domainName + "/v1/";
    }

    public static String buildConfigApiUrl(String domainName) {
        return "https://appdata." + domainName + "/";
    }

    public static String buildUploadUrl(String domainName) {
        return "https://filedrop." + domainName + "/";
    }
}
