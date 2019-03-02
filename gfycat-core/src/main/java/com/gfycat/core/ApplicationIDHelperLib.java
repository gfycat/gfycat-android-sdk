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

import com.gfycat.common.utils.Logging;

import java.lang.reflect.Field;

/**
 * Created by dekalo on 14.10.16.
 */

public class ApplicationIDHelperLib {

    public static String getAppId() {


        String appId = "com.gfycat";

        try {
            ClassLoader loader = ApplicationIDHelperLib.class.getClassLoader();

            Class<?> clz = loader.loadClass("com.gfycat.app.ApplicationIDHelperApp");
            Field declaredField = clz.getDeclaredField("APP_ID");

            appId = declaredField.get(null).toString();
        } catch (ClassNotFoundException e) {
            Logging.d("ApplicationIDHelperLib", "Failed to obtain real applicationId in runtime", e);
        } catch (NoSuchFieldException e) {
            Logging.d("ApplicationIDHelperLib", "Failed to obtain real applicationId in runtime", e);
        } catch (IllegalArgumentException e) {
            Logging.d("ApplicationIDHelperLib", "Failed to obtain real applicationId in runtime", e);
        } catch (IllegalAccessException e) {
            Logging.d("ApplicationIDHelperLib", "Failed to obtain real applicationId in runtime", e);
        }

        return appId;
    }
}
