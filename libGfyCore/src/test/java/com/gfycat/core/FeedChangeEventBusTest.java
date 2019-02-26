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

package com.gfycat.core;

import com.gfycat.core.db.FeedChangeEventBus;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

/**
 * Created by dgoliy on 11/9/17.
 */
@RunWith(RobolectricTestRunner.class)
public class FeedChangeEventBusTest {

    private int countFirstRemainingCalls = 3;
    private int countSecondRemainingCalls = 1;

    private static class CountingFeedObserver implements FeedChangeEventBus.FeedObserver {

        private int counter = 0;

        @Override
        public void onChange(FeedIdentifier feedIdentifier) {
            counter++;
        }
    }

    @Test
    public void testRootChangeNotification() {

        CountingFeedObserver observer = new CountingFeedObserver();
        FeedChangeEventBus eventBus = new FeedChangeEventBus();

        eventBus.registerFeedObserver(PublicFeedIdentifier.trending(), observer);
        eventBus.notifyRootChange();

        Assert.assertEquals(1, observer.counter);
    }

    @Test
    public void testFeedChangeEventBus() {
        FeedIdentifier firstIdentifier = PublicFeedIdentifier.trending();
        FeedIdentifier secondIdentifier = PublicFeedIdentifier.fromReaction("wow");
        FeedIdentifier thirdIdentifier = PublicFeedIdentifier.fromSearch("gfycat");

        FeedChangeEventBus.FeedObserver feedObserverFirst = feedIdentifier -> {
            Assert.assertTrue("More changes notified then expected for the first observer", countFirstRemainingCalls > 0);
            countFirstRemainingCalls--;
        };
        FeedChangeEventBus.FeedObserver feedObserverSecond = feedIdentifier -> {
            Assert.assertTrue("More changes notified then expected for the second observer", countSecondRemainingCalls > 0);
            countSecondRemainingCalls--;
        };
        FeedChangeEventBus eventBus = new FeedChangeEventBus();

        eventBus.registerFeedObserver(firstIdentifier, feedObserverFirst);
        eventBus.registerFeedObserver(secondIdentifier, feedObserverSecond);

        eventBus.notifyChange(firstIdentifier);
        eventBus.notifyChange(secondIdentifier);

        Assert.assertTrue("First observer remaining calls count is incorrect " + countFirstRemainingCalls, countFirstRemainingCalls == 2);
        Assert.assertTrue("Second observer remaining calls count is incorrect " + countSecondRemainingCalls, countSecondRemainingCalls == 0);

        eventBus.registerFeedObserver(secondIdentifier, feedObserverFirst);

        eventBus.unregisterFeedObserver(feedObserverSecond);

        eventBus.notifyChange(firstIdentifier);
        eventBus.notifyChange(secondIdentifier);

        eventBus.notifyChange(thirdIdentifier);

        eventBus.unregisterFeedObserver(feedObserverFirst);
        eventBus.notifyChange(firstIdentifier);
        eventBus.notifyChange(secondIdentifier);

        Assert.assertTrue("Not all notifications for first observer arrived. Calls remaining: " + countFirstRemainingCalls, countFirstRemainingCalls == 0);
        Assert.assertTrue("Not all notifications for second observer arrived. Calls remaining: " + countSecondRemainingCalls, countSecondRemainingCalls == 0);
    }
}
