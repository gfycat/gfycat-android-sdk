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
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import com.gfycat.common.utils.Assertions;
import com.gfycat.common.utils.Logging;

/**
 * Created by dekalo on 27.11.17.
 */
public class GfycatPluginInitializer {

    private static final String LOG_TAG = "GfycatPluginInitializer";

    /**
     * !!! IMPORTANT !!!
     * Plugin classes are usually marked as unused since they are being used via reflection.
     * Protect your plugin classes from being removed by Proguard!
     */
    private static final String PLUGIN_KEYS[] = {
            "picker_categories_prefetch_plugin",
            "creation_finalization_plugin",
            "photo_moments_initialization_plugin"
    };

    public static void initialize(Context context) {
        Logging.d(LOG_TAG, "initialize()");
        for (String plugin : PLUGIN_KEYS) {
            initializePlugin(plugin, context);
        }
    }

    private static void initializePlugin(String pluginKey, Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            ApplicationInfo appInfo = packageManager.getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            if (appInfo != null && appInfo.metaData != null) {
                String className = appInfo.metaData.getString(pluginKey);

                if (!TextUtils.isEmpty(className)) {
                    Class<?> clazz = Class.forName(className);
                    GfycatPlugin gfycatPlugin = (GfycatPlugin) clazz.newInstance();
                    try {
                        gfycatPlugin.initialize(context.getApplicationContext());
                    } catch (Throwable throwable) {
                        Logging.e(LOG_TAG, throwable, "Failed to initialize plugin(" + pluginKey + ")");
                        Assertions.fail(new IllegalStateException("Failed to initialize " + pluginKey, throwable));
                    }
                } else {
                    Logging.d(LOG_TAG, "Plugin [", pluginKey, "] was not provided.");
                }
            }

        } catch (InstantiationException | PackageManager.NameNotFoundException | IllegalAccessException | ClassNotFoundException e) {
            Logging.e(LOG_TAG, e, "Failed to initialize plugin(" + pluginKey + ")");
        }
    }
}
