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

import com.gfycat.common.Func1;
import com.gfycat.core.gfycatapi.pojo.Gfycat;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class ParameterizedRemoteFeedTests {

    @Before
    public void prepare() {
        CoreTestHelper.setupCoreWithTestJsonInterceptor();
    }

    @After
    public void tearDown() {
        CoreTestHelper.tearDownCore();
    }

    @Test
    public void soundFeedSingleParameterDeserialization() {
        FeedIdentifier doubleSerializedFeed = PublicFeedIdentifierTests.testFeedWithDoubleSerialization(
                FeedIdentifier.Type.SOUND_TRENDING,
                "",
                new ParameterizedFeedIdentifierBuilder(PublicFeedIdentifier.soundTrending()).withContentRating(Gfycat.ContentRating.PG13).build());

        Assert.assertTrue(doubleSerializedFeed instanceof PublicFeedIdentifier);
        Assert.assertEquals(Gfycat.ContentRating.PG13.urlEncodedValue, ((PublicFeedIdentifier) doubleSerializedFeed).getParameter(FeedIdentifierParameters.CONTENT_RATING));
    }

    @Test
    public void soundSearchFeedSingleParameterDeserialization() {
        FeedIdentifier doubleSerializedFeed = PublicFeedIdentifierTests.testFeedWithDoubleSerialization(
                FeedIdentifier.Type.SOUND_SEARCH,
                "music and dance",
                new ParameterizedFeedIdentifierBuilder(PublicFeedIdentifier.fromSoundSearch("music and dance")).withMinAspectRatio(0.5f).build());

        Assert.assertTrue(doubleSerializedFeed instanceof PublicFeedIdentifier);
        Assert.assertEquals("0.5", ((PublicFeedIdentifier) doubleSerializedFeed).getParameter(FeedIdentifierParameters.MIN_ASPECT_RATIO));
    }

    @Test
    public void searchFeedSingleParameterDeserialization() {
        FeedIdentifier doubleSerializedFeed = PublicFeedIdentifierTests.testFeedWithDoubleSerialization(
                FeedIdentifier.Type.SEARCH,
                "where is Victor ?",
                new ParameterizedFeedIdentifierBuilder(PublicFeedIdentifier.fromSearch("where is Victor ?")).withMaxAspectRatio(100f).build());

        Assert.assertTrue(doubleSerializedFeed instanceof PublicFeedIdentifier);
        Assert.assertEquals("10", ((PublicFeedIdentifier) doubleSerializedFeed).getParameter(FeedIdentifierParameters.MAX_ASPECT_RATIO));
    }

    @Test
    public void soundFeedAllParametersDeserialization() {
        FeedIdentifier doubleSerializedFeed = PublicFeedIdentifierTests.testFeedWithDoubleSerialization(
                FeedIdentifier.Type.SOUND_TRENDING,
                "",
                new ParameterizedFeedIdentifierBuilder(PublicFeedIdentifier.soundTrending())
                        .withContentRating(Gfycat.ContentRating.G)
                        .withMinAspectRatio(0.5f)
                        .withMaxAspectRatio(2)
                        .withMinLength(0.33f)
                        .withMaxLength(10)
                        .build());

        Assert.assertTrue(doubleSerializedFeed instanceof PublicFeedIdentifier);
        Assert.assertEquals(Gfycat.ContentRating.G.urlEncodedValue, ((PublicFeedIdentifier) doubleSerializedFeed).getParameter(FeedIdentifierParameters.CONTENT_RATING));
        Assert.assertEquals("0.5", ((PublicFeedIdentifier) doubleSerializedFeed).getParameter(FeedIdentifierParameters.MIN_ASPECT_RATIO));
        Assert.assertEquals("2", ((PublicFeedIdentifier) doubleSerializedFeed).getParameter(FeedIdentifierParameters.MAX_ASPECT_RATIO));
        Assert.assertEquals("0.33", ((PublicFeedIdentifier) doubleSerializedFeed).getParameter(FeedIdentifierParameters.MIN_LENGTH));
        Assert.assertEquals("10", ((PublicFeedIdentifier) doubleSerializedFeed).getParameter(FeedIdentifierParameters.MAX_LENGTH));
    }

    @Test
    public void soundSearchFeedAllParametersDeserialization() {
        FeedIdentifier doubleSerializedFeed = PublicFeedIdentifierTests.testFeedWithDoubleSerialization(
                FeedIdentifier.Type.SOUND_SEARCH,
                "rhythms are awesome",
                new ParameterizedFeedIdentifierBuilder(PublicFeedIdentifier.fromSoundSearch("rhythms are awesome"))
                        .withContentRating(Gfycat.ContentRating.G)
                        .withMinAspectRatio(0.5f)
                        .withMaxAspectRatio(2)
                        .withMinLength(0.33f)
                        .withMaxLength(10)
                        .build());

        Assert.assertTrue(doubleSerializedFeed instanceof PublicFeedIdentifier);
        Assert.assertEquals(Gfycat.ContentRating.G.urlEncodedValue, ((PublicFeedIdentifier) doubleSerializedFeed).getParameter(FeedIdentifierParameters.CONTENT_RATING));
        Assert.assertEquals("0.5", ((PublicFeedIdentifier) doubleSerializedFeed).getParameter(FeedIdentifierParameters.MIN_ASPECT_RATIO));
        Assert.assertEquals("2", ((PublicFeedIdentifier) doubleSerializedFeed).getParameter(FeedIdentifierParameters.MAX_ASPECT_RATIO));
        Assert.assertEquals("0.33", ((PublicFeedIdentifier) doubleSerializedFeed).getParameter(FeedIdentifierParameters.MIN_LENGTH));
        Assert.assertEquals("10", ((PublicFeedIdentifier) doubleSerializedFeed).getParameter(FeedIdentifierParameters.MAX_LENGTH));
    }

    @Test
    public void searchFeedAllParametersDeserialization() {
        FeedIdentifier doubleSerializedFeed = PublicFeedIdentifierTests.testFeedWithDoubleSerialization(
                FeedIdentifier.Type.SEARCH,
                "where I am",
                new ParameterizedFeedIdentifierBuilder(PublicFeedIdentifier.fromSearch("where I am"))
                        .withContentRating(Gfycat.ContentRating.G)
                        .withMinAspectRatio(0.5f)
                        .withMaxAspectRatio(2)
                        .withMinLength(0.33f)
                        .withMaxLength(10)
                        .build());

        Assert.assertTrue(doubleSerializedFeed instanceof PublicFeedIdentifier);
        Assert.assertEquals(Gfycat.ContentRating.G.urlEncodedValue, ((PublicFeedIdentifier) doubleSerializedFeed).getParameter(FeedIdentifierParameters.CONTENT_RATING));
        Assert.assertEquals("0.5", ((PublicFeedIdentifier) doubleSerializedFeed).getParameter(FeedIdentifierParameters.MIN_ASPECT_RATIO));
        Assert.assertEquals("2", ((PublicFeedIdentifier) doubleSerializedFeed).getParameter(FeedIdentifierParameters.MAX_ASPECT_RATIO));
        Assert.assertEquals("0.33", ((PublicFeedIdentifier) doubleSerializedFeed).getParameter(FeedIdentifierParameters.MIN_LENGTH));
        Assert.assertEquals("10", ((PublicFeedIdentifier) doubleSerializedFeed).getParameter(FeedIdentifierParameters.MAX_LENGTH));
    }

    private static void generalBoundariesTest(Func1<ParameterizedFeedIdentifierBuilder, ParameterizedFeedIdentifierBuilder> paramsApplier,
                                              String expectedMinAspect,
                                              String expectedMaxAspect,
                                              String expectedMinLength,
                                              String expectedMaxLength) {

        FeedIdentifier doubleSerializedFeed = PublicFeedIdentifierTests.testFeedWithDoubleSerialization(
                FeedIdentifier.Type.SEARCH,
                "searchQuery",
                paramsApplier.call(new ParameterizedFeedIdentifierBuilder(PublicFeedIdentifier.fromSearch("searchQuery"))).build());

        Assert.assertEquals(expectedMinAspect, ((PublicFeedIdentifier) doubleSerializedFeed).getParameter(FeedIdentifierParameters.MIN_ASPECT_RATIO));
        Assert.assertEquals(expectedMaxAspect, ((PublicFeedIdentifier) doubleSerializedFeed).getParameter(FeedIdentifierParameters.MAX_ASPECT_RATIO));
        Assert.assertEquals(expectedMinLength, ((PublicFeedIdentifier) doubleSerializedFeed).getParameter(FeedIdentifierParameters.MIN_LENGTH));
        Assert.assertEquals(expectedMaxLength, ((PublicFeedIdentifier) doubleSerializedFeed).getParameter(FeedIdentifierParameters.MAX_LENGTH));
    }

    @Test
    public void searchFeedMinAspectBelowLowerLimit() {
        generalBoundariesTest(parameterizedFeedIdentifierBuilder -> parameterizedFeedIdentifierBuilder.withMinAspectRatio(0.0000001f),
                "0.1", null, null, null);
    }

    @Test
    public void searchFeedMinAspectAboveHigherLimit() {
        generalBoundariesTest(parameterizedFeedIdentifierBuilder -> parameterizedFeedIdentifierBuilder.withMinAspectRatio(100000f),
                "10", null, null, null);
    }

    @Test
    public void searchFeedMaxAspectBelowLowerLimit() {
        generalBoundariesTest(parameterizedFeedIdentifierBuilder -> parameterizedFeedIdentifierBuilder.withMaxAspectRatio(0.00001f),
                null, "0.1", null, null);
    }

    @Test
    public void searchFeedMaxAspectAboveHigherLimit() {
        generalBoundariesTest(parameterizedFeedIdentifierBuilder -> parameterizedFeedIdentifierBuilder.withMaxAspectRatio(1000000f),
                null, "10", null, null);
    }

    @Test
    public void searchFeedMinLengthBelowLowerLimit() {
        generalBoundariesTest(parameterizedFeedIdentifierBuilder -> parameterizedFeedIdentifierBuilder.withMinLength(-100),
                null, null, "0", null);
    }

    @Test
    public void searchFeedMinLengthAboveHigherLimit() {
        generalBoundariesTest(parameterizedFeedIdentifierBuilder -> parameterizedFeedIdentifierBuilder.withMinLength(100.2f),
                null, null, "60", null);
    }

    @Test
    public void searchFeedMaxLengthBelowLowerLimit() {
        generalBoundariesTest(parameterizedFeedIdentifierBuilder -> parameterizedFeedIdentifierBuilder.withMaxLength(-1),
                null, null, null, "0");
    }

    @Test
    public void searchFeedMaxLengthAboveHigherLimit() {
        generalBoundariesTest(parameterizedFeedIdentifierBuilder -> parameterizedFeedIdentifierBuilder.withMaxLength(60.1f),
                null, null, null, "60");
    }

    @Test
    public void soundFeedAllParametersLoading() {
        FeedLoadingTests.testFeedLoadingWithParams(
                new ParameterizedFeedIdentifierBuilder(PublicFeedIdentifier.soundTrending())
                        .withContentRating(Gfycat.ContentRating.R)
                        .withMinLength(1f)
                        .withMaxLength(2f)
                        .withMinAspectRatio(1)
                        .withMaxAspectRatio(2)
                        .build(),
                "https://api.gfycat.com/v1/sound?count=100&minLength=1&maxLength=2&minAspectRatio=1&maxAspectRatio=2&rating=R",
                "https://api.gfycat.com/v1/sound?digest=first_digest&count=100&minLength=1&maxLength=2&minAspectRatio=1&maxAspectRatio=2&rating=R");
    }

    @Test
    public void soundSearchFeedAllParametersLoading() {
        FeedLoadingTests.testFeedLoadingWithParams(
                new ParameterizedFeedIdentifierBuilder(PublicFeedIdentifier.fromSoundSearch("music in me"))
                        .withContentRating(Gfycat.ContentRating.R)
                        .withMinLength(1f)
                        .withMaxLength(2f)
                        .withMinAspectRatio(1)
                        .withMaxAspectRatio(2)
                        .build(),
                "https://api.gfycat.com/v1/sound/search?search_text=music%20in%20me&count=100&minLength=1&maxLength=2&minAspectRatio=1&maxAspectRatio=2&rating=R",
                "https://api.gfycat.com/v1/sound/search?search_text=music%20in%20me&cursor=first_digest&count=100&minLength=1&maxLength=2&minAspectRatio=1&maxAspectRatio=2&rating=R");
    }

    @Test
    public void searchFeedAllParametersLoading() {
        FeedLoadingTests.testFeedLoadingWithParams(
                new ParameterizedFeedIdentifierBuilder(PublicFeedIdentifier.fromSearch("Hey google!"))
                        .withContentRating(Gfycat.ContentRating.R)
                        .withMinLength(1f)
                        .withMaxLength(2f)
                        .withMinAspectRatio(1)
                        .withMaxAspectRatio(2)
                        .build(),
                "https://api.gfycat.com/v1/gfycats/search?search_text=Hey%20google%21&count=100&minLength=1&maxLength=2&minAspectRatio=1&maxAspectRatio=2&rating=R",
                "https://api.gfycat.com/v1/gfycats/search?search_text=Hey%20google%21&cursor=first_digest&count=100&minLength=1&maxLength=2&minAspectRatio=1&maxAspectRatio=2&rating=R");
    }
}
