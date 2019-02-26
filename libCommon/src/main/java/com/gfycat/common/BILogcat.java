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

package com.gfycat.common;

import com.gfycat.common.utils.Logging;

import java.util.Map;

/**
 * Created by dgoliy on 2/10/17.
 */

public class BILogcat {
    private static final String LOG_TAG = "BILogcat";
    private static boolean isEnabled = false;

    public static void setEnabled(boolean isEnabled) {
        BILogcat.isEnabled = isEnabled;
    }

    public static void log(String engine, String eventName, Map<String, String> params) {
        if (isEnabled) {
            Logging.d(LOG_TAG, String.format("engine : %s | event : %s | params : %s", engine, eventName, params));
        }
    }
}
