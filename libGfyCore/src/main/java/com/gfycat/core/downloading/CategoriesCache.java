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

package com.gfycat.core.downloading;

import android.content.Context;
import android.support.v4.util.Pair;

import com.gfycat.common.utils.Assertions;
import com.gfycat.common.utils.Logging;
import com.gfycat.core.SingleObjectRepository;
import com.gfycat.core.downloading.pojo.GfycatCategoriesData;
import com.gfycat.core.gfycatapi.pojo.GfycatCategoriesList;

/**
 * Created by dekalo on 02.02.17.
 */

public class CategoriesCache {

    private static final String LOG_TAG = "CategoriesCache";

    private static final String PREFERENCES_NAME = "gfycat_categories";
    private final SingleObjectRepository<GfycatCategoriesData> categoriesCache;

    private final static long CATEGORIES_EXPIRATION_TIME = 0;

    public CategoriesCache(Context context) {
        this.categoriesCache = new SingleObjectRepository<>(context, PREFERENCES_NAME, GfycatCategoriesData.class, new GfycatCategoriesData());
    }

    private void guard() {
        Assertions.assertNotUIThread(IllegalAccessException::new);
    }

    private boolean isCacheExpired(long lastUpdateTime) {
        return lastUpdateTime + CATEGORIES_EXPIRATION_TIME < System.currentTimeMillis();
    }

    /**
     * @return if item is cached - pair with first = GfycatCategoriesList, and second  isCacheExpired boolean, null otherwise.
     */
    public Pair<GfycatCategoriesList, Boolean> get() {
        guard();
        GfycatCategoriesData cachedData = categoriesCache.get();
        if (cachedData != null && cachedData.version != GfycatCategoriesData.CURRENT_CATEGORIES_DATA_VERSION) {
            // data format changed
            Logging.d(LOG_TAG, "dropCategories cache");
            cachedData = null;
            drop();
        }
        return cachedData != null ? Pair.create(cachedData.categoriesList, isCacheExpired(cachedData.lastUpdateTime)) : null;
    }

    /**
     * @param newCategoriesList from server.
     * @return if data in db is changed.
     */
    public boolean update(GfycatCategoriesList newCategoriesList) {
        guard();
        Pair<GfycatCategoriesList, Boolean> previousList = get();
        categoriesCache.put(new GfycatCategoriesData(newCategoriesList, System.currentTimeMillis(), GfycatCategoriesData.CURRENT_CATEGORIES_DATA_VERSION));
        return previousList == null || !newCategoriesList.equals(previousList.first);
    }

    void drop() {
        guard();
        categoriesCache.remove();
    }
}
