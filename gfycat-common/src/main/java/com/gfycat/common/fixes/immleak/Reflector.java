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

package com.gfycat.common.fixes.immleak;

import java.lang.reflect.Method;

/**
 * From:
 * http://stackoverflow.com/questions/5038158/main-activity-is-not-garbage-collected-after-destruction-because-it-is-reference
 *
 * Created by dekalo on 03.03.16.
 */
public class Reflector {

    public static final class TypedObject {
        private final Object object;
        private final Class type;

        public TypedObject(final Object object, final Class type) {
            this.object = object;
            this.type = type;
        }

        Object getObject() {
            return object;
        }

        Class getType() {
            return type;
        }
    }

    public static void invokeMethodExceptionSafe(final Object methodOwner, final String method, final TypedObject... arguments) {
        if (null == methodOwner) {
            return;
        }

        try {
            final Class<?>[] types = null == arguments ? new Class[0] : new Class[arguments.length];
            final Object[] objects = null == arguments ? new Object[0] : new Object[arguments.length];

            if (null != arguments) {
                for (int i = 0, limit = types.length; i < limit; i++) {
                    types[i] = arguments[i].getType();
                    objects[i] = arguments[i].getObject();
                }
            }

            final Method declaredMethod = methodOwner.getClass().getDeclaredMethod(method, types);

            declaredMethod.setAccessible(true);
            declaredMethod.invoke(methodOwner, objects);
        } catch (final Throwable ignored) {
        }
    }
}
