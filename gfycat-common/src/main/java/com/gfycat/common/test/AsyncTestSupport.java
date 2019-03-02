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

import android.util.Log;

import com.gfycat.common.Action1;
import com.gfycat.common.utils.Logging;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.reactivex.functions.Consumer;

/**
 * Class provides asynchronous support for tests.
 */
public class AsyncTestSupport {

    private final CountDownLatch countDownLatch;
    private final String logTag;
    private final Action1<String> fail;
    private final long timeoutMs;

    private volatile List<Throwable> throwables = new ArrayList<>();

    public AsyncTestSupport(String logTag, int count, long timeoutMs, Action1<String> fail) {
        Logging.d(logTag, "init<>(", count, ")");
        this.logTag = logTag;
        this.countDownLatch = new CountDownLatch(count);
        this.fail = fail;
        this.timeoutMs = timeoutMs;
    }

    public AsyncTestSupport(String logTag, int count, Action1<String> fail) {
        Logging.d(logTag, "init<>(", count, ")");
        this.logTag = logTag;
        this.countDownLatch = new CountDownLatch(count);
        this.fail = fail;
        this.timeoutMs = TimeUnit.SECONDS.toMillis(1);
    }

    public synchronized void report(Throwable throwable) {
        throwables.add(throwable);
    }

    public synchronized void countDown() {
        Logging.d(logTag, "countDown()");
        countDownLatch.countDown();
    }

    public void complete() throws Throwable {
        if (!countDownLatch.await(timeoutMs, TimeUnit.MILLISECONDS)) {
            assertThrowables();
            fail.call(logTag + " timeout");
        } else {
            assertThrowables();
        }
        Logging.d(logTag, "completed()");
    }

    private void assertThrowables() {
        if (!throwables.isEmpty()) {
            for (Throwable throwable : throwables) {
                Log.e(logTag, logTag + " test failed because of this exception.", throwable);
            }
            fail.call(logTag + " failed because of: " + throwablesToString() + " (exceptions logged logcat).");
        }
    }

    private String throwablesToString() {
        String[] arr = new String[throwables.size()];
        int index = 0;
        for (Throwable throwable : throwables) {
            arr[index++] = throwable.getClass().getSimpleName();
        }
        return Arrays.toString(arr);
    }
}
