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

/**
 * While inserting FdyCat items in DB.
 * This class will generate it's indexes in Feed.
 * Based on initial order and lastPosition.
 * <p/>
 * Created by dekalo on 23.10.15.
 */
class FeedIndexer {

    private final boolean forward;
    private int lastValue;

    FeedIndexer(int lastValue, boolean forward) {
        this.lastValue = lastValue;
        this.forward = forward;
    }

    int nextIndex() {
        lastValue += forward ? 1 : -1;
        return lastValue;
    }
}
