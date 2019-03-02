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

package com.gfycat.core.bi.analytics;

import java.util.HashMap;
import java.util.Map;

/**
 * Root class for GfycatAnalytics events reporting.
 * <p>
 * Responsible for providing correct logger for GfycatAnalytics events.
 * + utility methods for GfycatAnalytics events reporting.
 * <p>
 * Created by dekalo on 16.10.15.
 */
public class GfycatAnalytics {

    private static Map<Class, GroupLogger> loggers = new HashMap<>();
    private static GroupEngine groupEngine = new GroupEngine();

    public static void addLogger(Class keyClass, BILogger logger) {
        if (loggers.get(keyClass) == null)
            loggers.put(keyClass, new GroupLogger(keyClass));

        logger.setupEngine(groupEngine);

        loggers.get(keyClass).add(logger);
    }

    public static <T extends BILogger> T getLogger(Class<T> loggerClass) {
        if (loggers.get(loggerClass) == null)
            loggers.put(loggerClass, new GroupLogger(loggerClass));

        return loggerClass.cast(loggers.get(loggerClass).getProxyLogger());
    }

    public static void addEngine(BIEngine engine) {
        groupEngine.add(engine);
    }
}