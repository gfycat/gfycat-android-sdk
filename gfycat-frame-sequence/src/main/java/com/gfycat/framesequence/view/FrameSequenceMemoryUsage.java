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

package com.gfycat.framesequence.view;

import com.gfycat.common.utils.Utils;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by dekalo on 07.12.16.
 */

public class FrameSequenceMemoryUsage {

    public static AtomicLong count = new AtomicLong();
    public static AtomicLong totalSize = new AtomicLong();

    public static void add(int size) {
        count.incrementAndGet();
        totalSize.addAndGet(size);
    }

    public static void remove(int size) {
        count.decrementAndGet();
        totalSize.addAndGet(-size);
    }

    public static String getWebPMemoryUsage() {
        return String.format(Locale.US, "Allocated %d framesequences for %s memory", count.longValue(), Utils.humanReadableByteCount(totalSize.longValue()));
    }
}
