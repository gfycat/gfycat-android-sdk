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

package com.gfycat.core.downloading.pojo;

import com.gfycat.core.gfycatapi.pojo.GfycatCategoriesList;

/**
 * This pojo is used only locally.
 */
public class GfycatCategoriesData {

    /**
     * Same as with database we should be able to drop categories when Gfycat format significantly changed.
     */
    public static final int CURRENT_CATEGORIES_DATA_VERSION = 1;

    public long version;
    public long lastUpdateTime;
    public GfycatCategoriesList categoriesList;

    public GfycatCategoriesData() {
        /**
         * For jackson
         */
    }

    public GfycatCategoriesData(GfycatCategoriesList categoriesList, long updateTime, long version) {
        this.version = version;
        this.lastUpdateTime = updateTime;
        this.categoriesList = categoriesList;
    }

    public GfycatCategoriesList getCategoriesList() {
        return categoriesList;
    }

    public void setCategoriesList(GfycatCategoriesList categoriesList) {
        this.categoriesList = categoriesList;
    }

    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }
}
