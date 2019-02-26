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

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.gfycat.common.Func0;


/**
 * Main purpose is to add various assertions in IS_PRODUCTION mode.
 * And switch it off in production.
 * <p>
 * Created by dekalo on 25.08.15.
 */
public class Assertions {

    private static final CrashReporter EMPTY_REPORTER = throwable -> {
    };


    private static boolean CRASH_ON_FAIL = false;
    private static Handler UI_HANDLER;
    private static CrashReporter CRASH_REPORTER = EMPTY_REPORTER;

    public interface CrashReporter {
        void report(Throwable throwable);
    }

    public static void initializeUIHandler() {
        UI_HANDLER = new Handler();
    }

    private static boolean isUIThread() {
        return Looper.getMainLooper() == Looper.myLooper();
    }

    public static boolean isEnabled() {
        return CRASH_ON_FAIL;
    }

    public static void setEnabled(boolean enabled) {
        CRASH_ON_FAIL = enabled;
    }

    public static CrashReporter getCrashReporter() {
        return CRASH_REPORTER;
    }

    public static void setCrashReporter(CrashReporter crashReporter) {
        CRASH_REPORTER = crashReporter == null ? EMPTY_REPORTER : crashReporter;
    }

    public static void fail(final Throwable e) {
        CRASH_REPORTER.report(e);
        if (CRASH_ON_FAIL) {
            performOnUIThreadIfPossible(new ThrowDelegateRunnable(e));
        }
    }

    public static void fail(final Func0<Throwable> descriptionGenerator) {
        fail(descriptionGenerator.call());
    }

    private static void performOnUIThreadIfPossible(Runnable runnable) {
        if (!isUIThread() && UI_HANDLER != null) {
            UI_HANDLER.post(runnable);
        } else {
            runnable.run();
        }
    }

    public static void assertNull(Object shouldBeNull, Func0<Throwable> descriptionGenerator) {
        if (shouldBeNull != null) {
            fail(descriptionGenerator.call());
        }
    }

    public static void assertNotNull(Object obj, Func0<Throwable> descriptionGenerator) {
        if (obj == null) {
            fail(descriptionGenerator.call());
        }
    }

    public static void assertNotEmpty(String string, Func0<Throwable> descriptionGenerator) {
        if (TextUtils.isEmpty(string)) {
            fail(descriptionGenerator.call());
        }
    }

    public static void assertEquals(Object desired, Object real, Func0<Throwable> descriptionGenerator) {
        if (!desired.equals(real)) {
            fail(descriptionGenerator.call());
        }
    }

    public static void assertEquals(int desired, int real, Func0<Throwable> descriptionGenerator) {
        if (desired != real) {
            fail(descriptionGenerator.call());
        }
    }

    public static void assertEquals(long desired, long real, Func0<Throwable> descriptionGenerator) {
        if (desired != real) {
            fail(descriptionGenerator.call());
        }
    }

    public static void assertNotEquals(int value, int shouldBeNotEqualTo, Func0<Throwable> descriptionGenerator) {
        if (value == shouldBeNotEqualTo) {
            fail(descriptionGenerator.call());
        }
    }

    public static void assertTrue(boolean shouldBeTrue, Func0<Throwable> descriptionGenerator) {
        if (!shouldBeTrue) {
            fail(descriptionGenerator.call());
        }
    }

    public static void assertFalse(boolean shouldBeFalse, Func0<Throwable> descriptionGenerator) {
        if (shouldBeFalse) {
            fail(descriptionGenerator.call());
        }
    }

    public static void assertUIThread(Func0<Throwable> descriptionGenerator) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            fail(descriptionGenerator.call());
        }
    }

    public static void assertNotUIThread(Func0<Throwable> descriptionGenerator) {
        if (CRASH_ON_FAIL && Looper.myLooper() == Looper.getMainLooper()) {
            fail(descriptionGenerator.call());
        }
    }

    private static class ThrowDelegateRunnable implements Runnable {

        private final RuntimeException runtimeException;

        private ThrowDelegateRunnable(Throwable throwable) {
            this.runtimeException = new RuntimeException(throwable);
        }

        @Override
        public void run() {
            throw runtimeException;
        }
    }

    public static void unreachable() {
        fail(new UnsupportedOperationException("Unreachable source code."));
    }
}