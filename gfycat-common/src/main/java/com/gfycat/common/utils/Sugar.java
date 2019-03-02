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

import com.gfycat.common.Action1;
import com.gfycat.common.Func1;

/**
 * Created by dekalo on 27.01.17.
 */

public final class Sugar {
    public static <T> void doIfNotNull(T t, Action1<T> action) {
        if (t != null) action.call(t);
    }

    public static <T, R> R callIfNotNull(T t, Func1<T, R> action) {
        return (t != null) ? action.call(t) : null;
    }
}
