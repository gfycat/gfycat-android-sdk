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

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class PublicFeedIdentifierTests {

    static FeedIdentifier testFeedWithDoubleSerialization(FeedType expectedType, String expectedName, FeedIdentifier feedIdentifier) {
        String firstUniqueIdentifier = feedIdentifier.toUniqueIdentifier();

        FeedIdentifier recreated = PublicFeedIdentifier.create(firstUniqueIdentifier);
        String secondUniqueIdentifier = recreated.toUniqueIdentifier();

        FeedIdentifier result = PublicFeedIdentifier.create(secondUniqueIdentifier);

        FeedIdentifierTestUtils.assertSameFeed(expectedType, expectedName, result);
        Assert.assertEquals(feedIdentifier.toUniqueIdentifier(), result.toUniqueIdentifier());

        return result;
    }

    @Test
    public void testTrending() {
        testFeedWithDoubleSerialization(FeedIdentifier.Type.TRENDING, "", PublicFeedIdentifier.trending());
    }

    @Test
    public void testTrendingTag() {
        testFeedWithDoubleSerialization(FeedIdentifier.Type.TAG, "awesome TAG", PublicFeedIdentifier.fromTagName("awesome TAG"));
    }

    @Test
    public void testSearch() {
        testFeedWithDoubleSerialization(FeedIdentifier.Type.SEARCH, "search query !@#$%^&*(", PublicFeedIdentifier.fromSearch("search query !@#$%^&*("));
    }

    @Test
    public void testReaction() {
        testFeedWithDoubleSerialization(FeedIdentifier.Type.REACTIONS, "HI", PublicFeedIdentifier.fromReaction("HI"));
    }

    @Test
    public void testUser() {
        testFeedWithDoubleSerialization(FeedIdentifier.Type.USER, "panstanislav", PublicFeedIdentifier.fromUsername("panstanislav"));
    }

    @Test
    public void testMe() {
        testFeedWithDoubleSerialization(FeedIdentifier.Type.ME, "", PublicFeedIdentifier.myGfycats());
    }

    @Test
    public void testSound() {
        testFeedWithDoubleSerialization(FeedIdentifier.Type.SOUND_TRENDING, "", PublicFeedIdentifier.soundTrending());
    }

    @Test
    public void testSoundSearch() {
        testFeedWithDoubleSerialization(FeedIdentifier.Type.SOUND_SEARCH, "bum bum bum 12397*&^", PublicFeedIdentifier.fromSoundSearch("bum bum bum 12397*&^"));
    }
}
