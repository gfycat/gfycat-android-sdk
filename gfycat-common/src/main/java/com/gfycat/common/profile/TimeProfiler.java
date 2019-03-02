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

import java.sql.Time;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TimeProfiler that allow profiling by sections, supporting multithreading.
 */
public class TimeProfiler extends Profiler {

    private Map<String, Section> sections = new ConcurrentHashMap<>();

    /**
     * Start measurement, after it you should call {@link Measurement#end()} to end measurement and add it.
     *
     * @param sectionName of measurement.
     */
    public Measurement start(String sectionName) {
        if (!isEnabled()) return new DisabledMeasurement();
        Section section = getOrInsert(sectionName);
        long startTime = System.nanoTime();
        return () -> section.add(System.nanoTime() - startTime);
    }

    private synchronized Section getOrInsert(String tag) {
        Section section = sections.get(tag);
        if (section == null) {
            sections.put(tag, section = new Section(tag));
        }
        return section;
    }

    /**
     * Dump all sections to log with logTag.
     */
    public void dump(String logTag) {
        if (isEnabled()) {
            List<Section> list = new ArrayList<>(sections.values());
            Collections.sort(list, (o1, o2) -> (int) (o2.getTotalDurationAsMs() - o1.getTotalDurationAsMs()));
            for (Section section : list) {
                Logging.d(logTag, section);
            }
        }
    }

    /**
     * Clear all previous measurements.
     */
    public void clear() {
        sections.clear();
    }

    private static class DisabledMeasurement implements Measurement {
        @Override
        public void end() {
        }
    }

    @Override
    public TimeProfiler disable() {
        return (TimeProfiler) super.disable();
    }

    @Override
    public TimeProfiler enable() {
        return (TimeProfiler) super.enable();
    }
}
