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
public class FeedFactoryTests {

    private static void testCorrectFeedFactoryDeserializatoin(FeedIdentifier expectedIdentifier) {
        String expectedUniqueIdentifier = expectedIdentifier.toUniqueIdentifier();

        FeedIdentifier resultIdentifier = FeedIdentifierFactory.fromUniqueIdentifier(expectedUniqueIdentifier);

        Assert.assertEquals(expectedIdentifier, resultIdentifier);
        Assert.assertEquals(expectedUniqueIdentifier, resultIdentifier.toUniqueIdentifier());
    }

    @Test
    public void trendingFeedDeserialization() {
        testCorrectFeedFactoryDeserializatoin(PublicFeedIdentifier.trending());
    }

    @Test
    public void trendingTagFeedDeserialization() {
        testCorrectFeedFactoryDeserializatoin(PublicFeedIdentifier.fromTagName("awesome TAG !1!1!"));
    }

    @Test
    public void searchFeedDeserialization() {
        testCorrectFeedFactoryDeserializatoin(PublicFeedIdentifier.fromSearch("what's new in gfycat 12@!#21 GFYCAT"));
    }

    @Test
    public void reactionsFeedDeserialization() {
        testCorrectFeedFactoryDeserializatoin(PublicFeedIdentifier.fromReaction("hey bro"));
    }

    @Test
    public void userFeedDeserialization() {
        testCorrectFeedFactoryDeserializatoin(PublicFeedIdentifier.fromUsername("richard"));
    }

    @Test
    public void myFeedDeserialization() {
        testCorrectFeedFactoryDeserializatoin(PublicFeedIdentifier.myGfycats());
    }

    @Test
    public void soundFeedDeserialization() {
        testCorrectFeedFactoryDeserializatoin(PublicFeedIdentifier.soundTrending());
    }

    @Test
    public void soundSearchFeedDeserialization() {
        testCorrectFeedFactoryDeserializatoin(PublicFeedIdentifier.fromSoundSearch("bu buz 123 *()# HA"));
    }

    @Test
    public void singleFeedTypeDeserialization() {
        testCorrectFeedFactoryDeserializatoin(PublicFeedIdentifier.fromSingleItem("gyIdHereShouldBe"));
    }

    @Test
    public void recentFeedDeserialization() {
        testCorrectFeedFactoryDeserializatoin(RecentFeedIdentifier.recent());
    }
}
