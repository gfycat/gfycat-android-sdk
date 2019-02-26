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

import android.content.Context;

import com.gfycat.common.utils.Utils;
import com.gfycat.core.bi.impression.GfycatImpressionTestHelper;

import org.junit.Assert;
import org.robolectric.RuntimeEnvironment;

import okhttp3.Interceptor;

public class CoreTestHelper {

    private static final long INITIALIZATION_POLLING_TIMEOUT = 10;

    private static volatile TestJsonInterceptor TEST_JSON_INTERCEPTOR = new TestJsonInterceptor();

    public static void setupCoreWithTestJsonInterceptor() {
        CoreTestHelper.initializeCore(TEST_JSON_INTERCEPTOR);
    }

    public static void expectNextRequestAndResponse(String requestUrl, String response) {
        Assert.assertNotNull(TEST_JSON_INTERCEPTOR);
        TEST_JSON_INTERCEPTOR.setExpectedUrlAndResponse(requestUrl, response);
    }

    public static void tearDownCore() {
        CoreTestHelper.deInitializeCore();
    }

    public static void initializeCore(Interceptor interceptor) {

        Assert.assertFalse(GfyCoreInitializer.initializePerformed());

        Context context = RuntimeEnvironment.application.getApplicationContext();

        GfyCoreInitializer.initialize(
                new GfyCoreInitializationBuilder(context, new GfycatApplicationInfo("appId", "appSecret"))
                        .setJsonInterceptor(interceptor));

        while (!GfyCoreInitializer.initializationCompleted) {
            try {
                Thread.sleep(INITIALIZATION_POLLING_TIMEOUT);
            } catch (InterruptedException e) {
                Assert.fail("Core preparation interrupted.");
            }
        }
    }

    public static void deInitializeCore() {
        GfyCoreInitializer.deInitialize();
        GfycatImpressionTestHelper.deInitialize();
        AdsManager.deInitialize();
    }
}
