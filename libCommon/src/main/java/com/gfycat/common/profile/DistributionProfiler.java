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

package com.gfycat.common.profile;

import com.gfycat.common.utils.Logging;
import com.gfycat.common.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by dekalo on 22/02/18.
 */

public class DistributionProfiler extends Profiler {

    private volatile AtomicLong totalCount = new AtomicLong();
    private Map<String, AtomicLong> sections = new HashMap<>();

    public long incTotal() {
        return totalCount.incrementAndGet();
    }

    public long incSection(String section) {
        AtomicLong counter = sections.get(section);
        if (counter == null) {
            sections.put(section, counter = new AtomicLong());
        }
        return counter.incrementAndGet();
    }

    public long inc(String section) {
        incTotal();
        return incSection(section);
    }

    /**
     * Dump all sections to log with logTag.
     */
    public void dump(String logTag) {
        if (isEnabled()) {
            Logging.d(logTag, "TOTAL : ", totalCount.get());

            for (Map.Entry<String, AtomicLong> entry : sections.entrySet()) {
                Logging.d(logTag,
                        entry.getKey(),
                        " : ",
                        entry.getValue().get(),
                        String.format(Locale.US, " (%3.1f)", 100 * entry.getValue().get() / (float) totalCount.get()));
            }
        }
    }

    public void dumpAndClear(String logTag) {
        dump(logTag);
        clear();
    }

    public void clear() {
        totalCount.set(0);
        sections.clear();
    }
}
