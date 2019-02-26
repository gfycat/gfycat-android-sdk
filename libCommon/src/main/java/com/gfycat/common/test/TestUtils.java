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

package com.gfycat.common.test;


import com.gfycat.common.Func0;

public class TestUtils {

    private static final long DEFAULT_PING_PERIOD = 200;

    public static void waitForCondition(Func0<Boolean> condition, long maxTime) {
        waitForCondition(condition, DEFAULT_PING_PERIOD, maxTime);
    }

    public static void waitForCondition(Func0<Boolean> condition, long pingPeriod, long maxTime) {

        long startTime = System.currentTimeMillis();

        while (!condition.call() && startTime + maxTime > System.currentTimeMillis()) {
            waitForMs(pingPeriod);
        }
    }

    public static void waitForMs(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
