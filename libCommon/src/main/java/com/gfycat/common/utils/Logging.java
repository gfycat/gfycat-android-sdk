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

package com.gfycat.common.utils;

import android.content.Context;
import android.util.Log;

/**
 * Main purpose, switch off logging in production.
 * <p>
 * Created by dekalo on 25.08.15.
 */
public class Logging {

    private static final CrashReporter EMPTY_REPORTER = new CrashReporter() {
        @Override
        public void log(String message) {
        }

        @Override
        public void setUserId(String userId) {
        }

        @Override
        public void setVariable(String key, String value) {
        }

        public void logPermissionsState(Context context) {
        }
    };

    private static CrashReporter crashReporter = EMPTY_REPORTER;

    public static boolean ENABLED = false;


    public static void setEnabled(boolean enabled) {
        Logging.ENABLED = enabled;
    }

    public static void setCrashReporter(CrashReporter crashReporter) {
        Logging.crashReporter = crashReporter == null ? EMPTY_REPORTER : crashReporter;
    }

    public static void setVariable(String key, String value) {
        Logging.d(key, "setVariable(", value, ")");
        crashReporter.setVariable(key, value);
    }

    public static void logPermissions(Context context) {
        crashReporter.logPermissionsState(context);
    }

    public static void setUserId(String userId) {
        crashReporter.setUserId(userId);
    }

    public interface CrashReporter {
        void log(String message);

        void setUserId(String userId);

        void setVariable(String key, String value);

        void logPermissionsState(Context context);
    }


    /**
     * Will print to log if Logging.ENABLED == true.
     * Will report to Crashlytics anyway.
     */
    public static void c(String logTag, Object... messages) {
        String message = StringUtils.concatAsString(messages);
        crashReporter.log(StringUtils.concatAsString(logTag, " : ", message));
        d(logTag, message);
    }

    public static void d(String logTag, String logMessage) {
        if (ENABLED) {
            Log.d(logTag, logMessage);
        }
    }

    public static void d(String logTag, Object... messages) {
        if (ENABLED) {
            Log.d(logTag, StringUtils.concatAsString(messages));
        }
    }

    public static void d(String logTag, Throwable e, Object... messages) {
        if (ENABLED) {
            Log.d(logTag, StringUtils.concatAsString(messages), e);
        }
    }


    public static void i(String logTag, String logMessage) {
        if (ENABLED) {
            Log.i(logTag, logMessage);
        }
    }

    public static void i(String logTag, Object... messages) {
        if (ENABLED) {
            Log.i(logTag, StringUtils.concatAsString(messages));
        }
    }


    public static void w(String logTag, Object... messages) {
        if (ENABLED) {
            Log.w(logTag, StringUtils.concatAsString(messages));
        }
    }

    public static void w(String logTag, Throwable e, Object... messages) {
        if (ENABLED) {
            Log.w(logTag, StringUtils.concatAsString(messages), e);
        }
    }


    public static void e(String logTag, Object... messages) {
        if (ENABLED) {
            Log.e(logTag, StringUtils.concatAsString(messages));
        }
    }

    public static void e(String logTag, Throwable e, Object... messages) {
        if (ENABLED) {
            Log.e(logTag, StringUtils.concatAsString(messages), e);
        }
    }
}