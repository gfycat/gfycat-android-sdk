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

package com.gfycat.common.media;

import com.gfycat.common.utils.Assertions;
import com.gfycat.common.utils.Logging;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

/**
 * Created by dekalo on 22.06.16.
 */
public class MediaCodecHint {

    private static String CRASHLYTICS_KEY = "MediaCodecHint";

    private static Map<String, String> usedItems = new HashMap<>();
    private static BehaviorSubject<Integer> subject = BehaviorSubject.createDefault(0);

    public static String purposesAsString() {
        String result[] = new String[usedItems.size()];
        int index = 0;
        for (Map.Entry<String, String> stringStringEntry : usedItems.entrySet())
            result[index++] = stringStringEntry.getValue();
        return Arrays.toString(result);
    }

    public static synchronized String acquire(String purpose) {
        String key = UUID.randomUUID().toString();
        usedItems.put(key, purpose);
        subject.onNext(usedItems.size());
        Logging.setVariable(CRASHLYTICS_KEY, purposesAsString());

        return key;
    }

    public static synchronized void release(String key) {

        String removedItem = usedItems.remove(key);

        if (removedItem == null) {
            Assertions.fail(new IllegalStateException("Trying to release key that was not produced by acquire function. Or already released."));
        }

        subject.onNext(usedItems.size());

        Logging.setVariable(CRASHLYTICS_KEY, purposesAsString());
    }

    public static int get() {
        return subject.getValue();
    }

    public static Observable<Integer> observeHint() {
        return subject;
    }
}
