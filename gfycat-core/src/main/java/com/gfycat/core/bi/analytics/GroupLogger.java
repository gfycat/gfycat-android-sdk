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

import com.gfycat.common.utils.Logging;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dekalo on 18.10.16.
 */

public class GroupLogger<T extends BILogger> {
    private static final String LOG_TAG = "GroupLogger";
    private List<T> loggers = new ArrayList<>();
    private final InvocationHandler invocationHandler = new BILoggerInvocationHandler();
    private final T proxyLogger;

    public GroupLogger(Class<T> loggerClass) {
        this.proxyLogger = loggerClass.cast(Proxy.newProxyInstance(
                loggerClass.getClassLoader(),
                new Class[]{loggerClass},
                invocationHandler));
    }

    public void add(T logger) {
        loggers.add(logger);
    }

    public T getProxyLogger() {
        return proxyLogger;
    }

    private class BILoggerInvocationHandler implements InvocationHandler {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            for (T logger : loggers) {
                Logging.d(LOG_TAG, logger.getClass().getSimpleName() + "." + method.getName() + "()");
                method.invoke(logger, args);
            }
            return null;
        }
    }
}
