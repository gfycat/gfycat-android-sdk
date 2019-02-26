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

package com.gfycat.core.suit;

import com.gfycat.core.FeedChangeEventBusTest;
import com.gfycat.core.FeedFactoryTests;
import com.gfycat.core.FeedLoadingTests;
import com.gfycat.core.ParameterizedRemoteFeedTests;
import com.gfycat.core.RecentFeedIdentifierTests;
import com.gfycat.core.PublicFeedIdentifierTests;
import com.gfycat.core.SingleFeedIdentifierTests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)


@Suite.SuiteClasses({
        FeedFactoryTests.class,
        FeedLoadingTests.class,
        ParameterizedRemoteFeedTests.class,
        RecentFeedIdentifierTests.class,
        PublicFeedIdentifierTests.class,
        SingleFeedIdentifierTests.class,
        FeedChangeEventBusTest.class
})

public class FeedIdentifierSuit {
}
