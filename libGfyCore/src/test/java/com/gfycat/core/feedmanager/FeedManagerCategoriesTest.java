/*
 * Copyright (c) 2015-present, Gfycat, Inc. All rights reserved.
 *
 * You are hereby granted a non-exclusive, worldwide, royalty-free license to use,
 * copy, modify, and distribute this software in source code or binary form for use
 * in connection with the web services and APIs provided by Gfycat.
 *
 * As with any software that integrates with the Gfycat platform, your use of
 * this software is subject to the Gfycat Terms of Service [https://gfycat.com/terms]
 * and Partner Terms of Service [https://gfycat.com/partners/terms]. This copyright
 * notice shall be included in all copies or substantial portions of the software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.gfycat.core.feedmanager;

import android.util.Log;

import com.gfycat.common.utils.Utils;
import com.gfycat.core.CoreTestHelper;
import com.gfycat.core.FeedLoadingTests;
import com.gfycat.core.GfyCore;
import com.gfycat.core.downloading.CategoriesTestHelper;
import com.gfycat.core.gfycatapi.pojo.Gfycat;
import com.gfycat.core.gfycatapi.pojo.GfycatCategoriesList;
import com.gfycat.core.gfycatapi.pojo.GfycatCategory;
import com.google.gson.Gson;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.List;

/**
 * Created by dekalo on 24.02.17.
 */

@RunWith(RobolectricTestRunner.class)
public class FeedManagerCategoriesTest {

    private static final String CATEGORIES_REQUEST = "https://api.gfycat.com/v1/reactions/populated?gfyCount=1&locale=en";

    private static Gfycat GFYCAT_MODEL = new Gson().fromJson(FeedLoadingTests.GFYCAT_JSON_MODEL, Gfycat.class);
    private static final String FIRST_CATEGORIES_RESPONSE;
    private static final String SECOND_CATEGORIES_RESPONSE;

    static {
        Gson gson = new Gson();

        FIRST_CATEGORIES_RESPONSE = gson.toJson(new GfycatCategoriesList(
                "first_cursor",
                new GfycatCategory("first_cursor", "first_tag", "first_tag_text", GFYCAT_MODEL)));

        SECOND_CATEGORIES_RESPONSE = gson.toJson(new GfycatCategoriesList(
                "second_cursor",
                new GfycatCategory("second_cursor", "second_tag_1", "second_tag_text_1", GFYCAT_MODEL),
                new GfycatCategory("second_cursor", "second_tag_2", "second_tag_text_2", GFYCAT_MODEL)));
    }

    @Before
    public void setup() {
        CoreTestHelper.setupCoreWithTestJsonInterceptor();
        CategoriesTestHelper.dropCategoriesCache(RuntimeEnvironment.application.getApplicationContext());
    }

    @After
    public void restoreCore() {
        CategoriesTestHelper.dropCategoriesCache(RuntimeEnvironment.application.getApplicationContext());
        CoreTestHelper.tearDownCore();
    }

    @Test
    public void testLoadCategoriesFromNetwork() throws Throwable {

        CoreTestHelper.expectNextRequestAndResponse(CATEGORIES_REQUEST, FIRST_CATEGORIES_RESPONSE);

        Iterable<GfycatCategoriesList> categoriesIterable = GfyCore.getFeedManager().getCategories().blockingIterable();
        List<GfycatCategoriesList> categoriesResponses = Utils.collect(categoriesIterable.iterator());

        Assert.assertEquals(1, categoriesResponses.size());
        Assert.assertEquals("first_cursor", categoriesResponses.get(0).getCursor());
        Assert.assertEquals("first_cursor", categoriesResponses.get(0).getDigest());
        Assert.assertEquals(1, categoriesResponses.get(0).getTags().size());
        Assert.assertEquals("first_tag", categoriesResponses.get(0).getTags().get(0).getTag());
        Assert.assertEquals("first_tag_text", categoriesResponses.get(0).getTags().get(0).getTagText());
        Assert.assertEquals("first_cursor", categoriesResponses.get(0).getTags().get(0).getCursor());
        Assert.assertEquals("first_cursor", categoriesResponses.get(0).getTags().get(0).getDigest());
        Assert.assertEquals(GFYCAT_MODEL, categoriesResponses.get(0).getTags().get(0).getGfycat());
    }

    @Test
    public void testLoadCategoriesFromCacheIfNetworkNotChanged() throws Throwable {

        CategoriesTestHelper.putCategoriesToCache(
                RuntimeEnvironment.application.getApplicationContext(),
                new Gson().fromJson(FIRST_CATEGORIES_RESPONSE, GfycatCategoriesList.class));

        // It is possible that next read from cache will happen same millisecond as read.
        // To avoid it we will ad 1 ms delay
        Thread.sleep(1);

        CoreTestHelper.expectNextRequestAndResponse(CATEGORIES_REQUEST, FIRST_CATEGORIES_RESPONSE);

        Iterable<GfycatCategoriesList> categoriesIterable = GfyCore.getFeedManager().getCategories().blockingIterable();
        List<GfycatCategoriesList> categoriesResponses = Utils.collect(categoriesIterable.iterator());

        Assert.assertEquals(1, categoriesResponses.size());
        Assert.assertEquals("first_cursor", categoriesResponses.get(0).getCursor());
        Assert.assertEquals("first_cursor", categoriesResponses.get(0).getDigest());
        Assert.assertEquals(1, categoriesResponses.get(0).getTags().size());
        Assert.assertEquals("first_tag", categoriesResponses.get(0).getTags().get(0).getTag());
        Assert.assertEquals("first_tag_text", categoriesResponses.get(0).getTags().get(0).getTagText());
        Assert.assertEquals("first_cursor", categoriesResponses.get(0).getTags().get(0).getCursor());
        Assert.assertEquals("first_cursor", categoriesResponses.get(0).getTags().get(0).getDigest());
        Assert.assertEquals(GFYCAT_MODEL, categoriesResponses.get(0).getTags().get(0).getGfycat());
    }

    @Test
    public void testLoadCategoriesFromCacheIfNetworkChanged() throws Throwable {

        CategoriesTestHelper.putCategoriesToCache(
                RuntimeEnvironment.application.getApplicationContext(),
                new Gson().fromJson(FIRST_CATEGORIES_RESPONSE, GfycatCategoriesList.class));

        // It is possible that next read from cache will happen same millisecond as read.
        // To avoid it we will ad 1 ms delay
        Thread.sleep(1);

        CoreTestHelper.expectNextRequestAndResponse(CATEGORIES_REQUEST, SECOND_CATEGORIES_RESPONSE);

        Iterable<GfycatCategoriesList> categoriesIterable = GfyCore.getFeedManager().getCategories().blockingIterable();
        List<GfycatCategoriesList> categoriesResponses = Utils.collect(categoriesIterable.iterator());

        Assert.assertEquals(2, categoriesResponses.size());

        //fist notify
        Assert.assertEquals("first_cursor", categoriesResponses.get(0).getCursor());
        Assert.assertEquals("first_cursor", categoriesResponses.get(0).getDigest());
        Assert.assertEquals(1, categoriesResponses.get(0).getTags().size());
        Assert.assertEquals("first_tag", categoriesResponses.get(0).getTags().get(0).getTag());
        Assert.assertEquals("first_tag_text", categoriesResponses.get(0).getTags().get(0).getTagText());
        Assert.assertEquals("first_cursor", categoriesResponses.get(0).getTags().get(0).getCursor());
        Assert.assertEquals("first_cursor", categoriesResponses.get(0).getTags().get(0).getDigest());
        Assert.assertEquals(GFYCAT_MODEL, categoriesResponses.get(0).getTags().get(0).getGfycat());

        // second notify
        Assert.assertEquals("second_cursor", categoriesResponses.get(1).getCursor());
        Assert.assertEquals("second_cursor", categoriesResponses.get(1).getDigest());
        Assert.assertEquals(2, categoriesResponses.get(1).getTags().size());
        // first item in second request
        Assert.assertEquals("second_tag_1", categoriesResponses.get(1).getTags().get(0).getTag());
        Assert.assertEquals("second_tag_text_1", categoriesResponses.get(1).getTags().get(0).getTagText());
        Assert.assertEquals("second_cursor", categoriesResponses.get(1).getTags().get(0).getCursor());
        Assert.assertEquals("second_cursor", categoriesResponses.get(1).getTags().get(0).getDigest());
        Assert.assertEquals(GFYCAT_MODEL, categoriesResponses.get(1).getTags().get(0).getGfycat());
        // second item in second request
        Assert.assertEquals("second_tag_2", categoriesResponses.get(1).getTags().get(1).getTag());
        Assert.assertEquals("second_tag_text_2", categoriesResponses.get(1).getTags().get(1).getTagText());
        Assert.assertEquals("second_cursor", categoriesResponses.get(1).getTags().get(1).getCursor());
        Assert.assertEquals("second_cursor", categoriesResponses.get(1).getTags().get(1).getDigest());
        Assert.assertEquals(GFYCAT_MODEL, categoriesResponses.get(1).getTags().get(1).getGfycat());

    }
}
