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

package com.gfycat.core.db;

import com.gfycat.core.FeedIdentifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by dgoliy on 11/8/17.
 */

public class FeedChangeEventBus {

    public interface FeedObserver {
        void onChange(FeedIdentifier feedIdentifier);
    }

    private HashMap<FeedIdentifier, List<FeedObserver>> feedObservers = new HashMap<>();

    private List<FeedObserver> getOrInsert(FeedIdentifier feedIdentifier) {

        List<FeedObserver> result = feedObservers.get(feedIdentifier);

        if (result == null) {
            feedObservers.put(feedIdentifier, result = new ArrayList<>());
        }

        return result;
    }

    public void registerFeedObserver(FeedIdentifier feedIdentifier, FeedObserver feedObserver) {
        if (feedObserver == null || feedIdentifier == null) return;
        getOrInsert(feedIdentifier).add(feedObserver);
    }

    public void unregisterFeedObserver(FeedObserver feedObserver) {
        if (feedObserver == null) return;

        for (List<FeedObserver> observers : feedObservers.values()) {
            observers.remove(feedObserver);
        }
    }

    public void notifyRootChange() {
        for (Map.Entry<FeedIdentifier, List<FeedObserver>> entry : feedObservers.entrySet()) {
            for (FeedObserver observer : entry.getValue()) {
                observer.onChange(entry.getKey());
            }
        }
    }

    public void notifyChange(FeedIdentifier feedIdentifier) {
        List<FeedObserver> observers = feedObservers.get(feedIdentifier);
        if (observers != null) {
            for (FeedObserver observer : observers) {
                observer.onChange(feedIdentifier);
            }
        }
    }
}
