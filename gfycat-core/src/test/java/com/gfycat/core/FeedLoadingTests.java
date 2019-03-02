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

import com.gfycat.core.downloading.FeedData;
import com.gfycat.core.gfycatapi.pojo.Gfycat;
import com.google.gson.Gson;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import io.reactivex.schedulers.Schedulers;

@RunWith(RobolectricTestRunner.class)
public class FeedLoadingTests {

    private static final int TIMEOUT_SECONDS = 4;

    private static final String LOAD_RESPONSE = "{\"cursor\":\"first_cursor\",\"gfycats\":[{\"tags\":[],\"views\":977325,\"userName\":\"panstanislav\",\"gfyNumber\":\"1\",\"gfyId\":\"gfycatnumber1\",\"gfyName\":\"GfycatNumber1\",\"createDate\":1549224774}],\"digest\":\"first_digest\",\"tag\":\"_gfycat_all_trending\",\"tagText\":\"_gfycat_all_trending\"}";
    private static final String LOAD_MORE_RESPONSE = "{\"gfycats\":[{\"tags\":[],\"views\":977325,\"userName\":\"panstanislav\",\"gfyNumber\":\"2\",\"gfyId\":\"gfycatnumber2\",\"gfyName\":\"GfycatNumber2\",\"createDate\":1549224774}],\"tag\":\"_gfycat_all_trending\",\"tagText\":\"_gfycat_all_trending\"}";
    public static final String GFYCAT_JSON_MODEL = "{\"published\":1,\"nsfw\":\"0\",\"gatekeeper\":0,\"mp4Url\":\"https://giant.gfycat.com/ChillyFlashyLeonberger.mp4\",\"gifUrl\":\"https://thumbs.gfycat.com/ChillyFlashyLeonberger-size_restricted.gif\",\"webmUrl\":\"https://giant.gfycat.com/ChillyFlashyLeonberger.webm\",\"webpUrl\":\"https://thumbs.gfycat.com/ChillyFlashyLeonberger.webp\",\"mobileUrl\":\"https://thumbs.gfycat.com/ChillyFlashyLeonberger-mobile.mp4\",\"mobilePosterUrl\":\"https://thumbs.gfycat.com/ChillyFlashyLeonberger-mobile.jpg\",\"extraLemmas\":\"\",\"thumb100PosterUrl\":\"https://thumbs.gfycat.com/ChillyFlashyLeonberger-mobile.jpg\",\"miniUrl\":\"https://thumbs.gfycat.com/ChillyFlashyLeonberger-mobile.mp4\",\"gif100px\":\"https://thumbs.gfycat.com/ChillyFlashyLeonberger-max-1mb.gif\",\"miniPosterUrl\":\"https://thumbs.gfycat.com/ChillyFlashyLeonberger-mobile.jpg\",\"max5mbGif\":\"https://thumbs.gfycat.com/ChillyFlashyLeonberger-size_restricted.gif\",\"title\":\"Tree FML\",\"max2mbGif\":\"https://thumbs.gfycat.com/ChillyFlashyLeonberger-small.gif\",\"max1mbGif\":\"https://thumbs.gfycat.com/ChillyFlashyLeonberger-max-1mb.gif\",\"posterUrl\":\"https://thumbs.gfycat.com/ChillyFlashyLeonberger-poster.jpg\",\"languageText2\":\"\",\"views\":236305,\"userName\":\"happydeathdaymovie\",\"description\":\"\",\"hasTransparency\":true,\"hasAudio\":false,\"likes\":\"3\",\"dislikes\":0,\"gfyNumber\":\"251515992\",\"pngPosterUrl\":\"https://thumbs.gfycat.com/ChillyFlashyLeonberger-transparent.png\",\"userDisplayName\":\"Happy Death Day 2U\",\"userProfileImageUrl\":\"https://profiles.gfycat.com/22c8a15dfde9a15e93e55764351eea3c9980951162d163b95aeccaa1324b8e10.png\",\"gfyId\":\"chillyflashyleonberger\",\"gfyName\":\"ChillyFlashyLeonberger\",\"avgColor\":\"#FFFFFF\",\"rating\":\"G\",\"width\":1200,\"height\":1200,\"frameRate\":25,\"numFrames\":132,\"mp4Size\":745877,\"webmSize\":1260349,\"createDate\":1549308133,\"md5\":\"5d8b49b189d590e337f77811a1fc7916\",\"source\":4}";

    @Before
    public void prepare() {
        CoreTestHelper.setupCoreWithTestJsonInterceptor();
    }

    @After
    public void tearDownTest() {
        CoreTestHelper.tearDownCore();
    }

    private static void assertNoThrowable(Throwable throwable) {
        if (throwable != null) {
            throw new AssertionError(throwable);
        }
    }

    private static void assertFirstFeedData(FeedIdentifier feedIdentifier, FeedData feedData) {
        Assert.assertEquals(1, feedData.getCount());
        Assert.assertEquals("gfycatnumber1", feedData.getGfycats().get(0).getGfyId());
        Assert.assertEquals(feedIdentifier, feedData.getFeedDescription().getIdentifier());
        Assert.assertFalse(feedData.getFeedDescription().isClosed());
        Assert.assertEquals("first_digest", feedData.getFeedDescription().getDigest());
    }

    private static void assertSecondFeedData(FeedIdentifier feedIdentifier, FeedData feedData) {
        Assert.assertEquals(2, feedData.getCount());
        Assert.assertEquals("gfycatnumber1", feedData.getGfycats().get(0).getGfyId());
        Assert.assertEquals("gfycatnumber2", feedData.getGfycats().get(1).getGfyId());
        Assert.assertEquals(feedIdentifier, feedData.getFeedDescription().getIdentifier());
    }

    public static void testFeedLoadingWithParams(FeedIdentifier identifier, String firstRequest, String secondRequest) {

        CoreTestHelper.expectNextRequestAndResponse(firstRequest, LOAD_RESPONSE);
        Throwable throwable = GfyCore.getFeedManager().getGfycats(identifier).subscribeOn(Schedulers.computation()).blockingGet(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        assertNoThrowable(throwable);
        FeedData firstFeedData = GfyCore.getFeedManager()
                .observeGfycats(identifier)
                .skipWhile(feedData -> feedData.getGfycats().isEmpty())
                .timeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .blockingFirst();
        assertFirstFeedData(identifier, firstFeedData);

        CoreTestHelper.expectNextRequestAndResponse(secondRequest, LOAD_MORE_RESPONSE);
        throwable = GfyCore.getFeedManager().getMoreGfycats(firstFeedData.getFeedDescription()).blockingGet(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        assertNoThrowable(throwable);
        FeedData secondFeedData = GfyCore.getFeedManager()
                .observeGfycats(identifier)
                .skipWhile(feedData -> feedData.getGfycats().isEmpty())
                .timeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .blockingFirst();
        assertSecondFeedData(identifier, secondFeedData);
    }

    @Test
    public void trendingFeedLoading() {
        testFeedLoadingWithParams(
                PublicFeedIdentifier.trending(),
                "https://api.gfycat.com/v1/gfycats/trending?count=100",
                "https://api.gfycat.com/v1/gfycats/trending?digest=first_digest&count=100");
    }

    @Test
    public void trendingTagFeedLoading() {
        testFeedLoadingWithParams(
                PublicFeedIdentifier.fromTagName("hiBob"),
                "https://api.gfycat.com/v1/gfycats/trending?tagName=hiBob&count=100",
                "https://api.gfycat.com/v1/gfycats/trending?tagName=hiBob&digest=first_digest&count=100");
    }

    @Test
    public void searchFeedLoading() {
        testFeedLoadingWithParams(
                PublicFeedIdentifier.fromSearch("where is gfycat ?"),
                "https://api.gfycat.com/v1/gfycats/search?search_text=where%20is%20gfycat%20%3F&count=100",
                "https://api.gfycat.com/v1/gfycats/search?search_text=where%20is%20gfycat%20%3F&cursor=first_digest&count=100");
    }

    @Test
    public void reactionsFeedLoading() {
        testFeedLoadingWithParams(
                PublicFeedIdentifier.fromReaction("Love"),
                "https://api.gfycat.com/v1/reactions/populated?tagName=Love&gfyCount=100",
                "https://api.gfycat.com/v1/reactions/populated?tagName=Love&digest=first_digest&gfyCount=100");
    }

    @Test
    public void userFeedLoading() {
        testFeedLoadingWithParams(
                PublicFeedIdentifier.fromUsername("panstanislav"),
                "https://api.gfycat.com/v1/users/panstanislav/gfycats?count=100",
                "https://api.gfycat.com/v1/users/panstanislav/gfycats?cursor=first_digest&count=100");
    }

    @Test
    public void soundFeedLoading() {
        testFeedLoadingWithParams(
                PublicFeedIdentifier.soundTrending(),
                "https://api.gfycat.com/v1/sound?count=100",
                "https://api.gfycat.com/v1/sound?digest=first_digest&count=100");
    }

    @Test
    public void soundSearchFeedLoading() {
        testFeedLoadingWithParams(
                PublicFeedIdentifier.fromSoundSearch("music bam bam"),
                "https://api.gfycat.com/v1/sound/search?search_text=music%20bam%20bam&count=100",
                "https://api.gfycat.com/v1/sound/search?search_text=music%20bam%20bam&cursor=first_digest&count=100");
    }

    @Test
    public void singleFeedTypeLoadingWhenNotSigned() {

        GfyCore.getFeedManager().getGfycats(PublicFeedIdentifier.myGfycats()).blockingGet(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        try {
            GfyCore.getFeedManager()
                    .observeGfycats(PublicFeedIdentifier.myGfycats())
                    .skipWhile(feedData -> feedData.getGfycats().isEmpty())
                    .timeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .blockingFirst();

            Assert.fail("TimeoutException is expected.");
        } catch (RuntimeException e) {
            Assert.assertTrue(e.getCause() instanceof TimeoutException);
        }
    }

    @Ignore("Implement test with proper authentication")
    @Test
    public void singleFeedTypeLoadingWhenSigned() {
        Assert.fail("TODO");
    }

    @Test
    public void recentFeedLoading() {

        try {
            GfyCore.getFeedManager()
                    .observeGfycats(RecentFeedIdentifier.recent())
                    .skipWhile(feedData -> feedData.getGfycats().isEmpty())
                    .timeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    .blockingFirst();

            Assert.fail("TimeoutException is expected.");
        } catch (RuntimeException e) {
            Assert.assertTrue(e.getCause() instanceof TimeoutException);
        }

        Gfycat gfycat = new Gson().fromJson(GFYCAT_JSON_MODEL, Gfycat.class);
        GfyPrivate.get().getFeedManager().addRecentGfycat(gfycat).blockingAwait(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        FeedData feed = GfyCore.getFeedManager()
                .observeGfycats(RecentFeedIdentifier.recent())
                .skipWhile(feedData -> feedData.getGfycats().isEmpty())
                .timeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .blockingFirst();

        Assert.assertTrue(feed.isClosed());
        Assert.assertFalse(feed.isEmpty());
        Assert.assertEquals(1, feed.getCount());
        Assert.assertEquals(gfycat, feed.getGfycats().get(0));
    }
}
