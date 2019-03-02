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

import com.gfycat.common.utils.Logging;
import com.gfycat.core.FeedIdentifier;

import java.util.concurrent.TimeUnit;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;

/**
 * Data saving interface for Database.
 * <p>
 * Created by dekalo on 10.09.15.
 */
public class FeedCacheUriContract {

    private static final String LOG_TAG = "FeedCacheUriContract";

    private static final long MINIMUM_CHANGES_EMIT_PERIOD_MILLIS = 100;

    private static final FeedChangeEventBus FEED_CHANGE_EVENT_BUS = new FeedChangeEventBus();

    public static FeedChangeEventBus getFeedChangeEventBus() {
        return FEED_CHANGE_EVENT_BUS;
    }

    public static Flowable<FeedIdentifier> observeChanges(FeedIdentifier identifier) {

        return Flowable.<FeedIdentifier>create(emitter -> {
            FeedChangeEventBus.FeedObserver feedObserver = feedIdentifier -> {
                Logging.d(LOG_TAG, "onChange(", identifier, ")");
                emitter.onNext(identifier);
            };

            FEED_CHANGE_EVENT_BUS.registerFeedObserver(identifier, feedObserver);

            emitter.setCancellable(() -> FEED_CHANGE_EVENT_BUS.unregisterFeedObserver(feedObserver));
        }, BackpressureStrategy.BUFFER)
                .throttleLatest(MINIMUM_CHANGES_EMIT_PERIOD_MILLIS, TimeUnit.MILLISECONDS);
    }
}
