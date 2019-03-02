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

import android.os.Binder;

import com.gfycat.common.Action1;
import com.gfycat.common.Func1;

import java.util.concurrent.Callable;

/**
 * Created by dekalo on 12.10.15.
 */
public class ThreadUtils {

    public static <T> T withClearIdentity(Callable<T> callable) throws Exception {
        long token = Binder.clearCallingIdentity();
        try {
            return callable.call();
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    public static <T> T withClearIdentitySafe(Callable<T> callable) {
        try {
            return withClearIdentity(callable);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> void with(T value, Action1<T> action1) {
        if (value != null) action1.call(value);
    }

    public static <T> void withSilently(T value, Action1<T> action1) {
        try {
            if (value != null) action1.call(value);
        } catch (Throwable ignored) {
        }
    }

    public static <R, P> R with(P param, Func1Unsafe<P, R> operator, Action1<Throwable> failHandler) {
        try {
            return operator.call(param);
        } catch (Throwable throwable) {
            failHandler.call(throwable);
            return null;
        }
    }

    public static <R, P> R withSilently(P param, Func1<P, R> operator) {
        return param == null ? null : operator.call(param);
    }

    public interface Func1Unsafe<P, R> {
        R call(P param) throws Throwable;
    }
}
