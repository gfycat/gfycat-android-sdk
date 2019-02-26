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

package com.gfycat.core.gfycatapi.pojo;

import java.util.Arrays;
import java.util.List;

/**
 * Gfycat categories list container
 */
public class GfycatCategoriesList {
    private String cursor;
    private List<GfycatCategory> tags;
    private String digest;

    public GfycatCategoriesList() {
    }

    /**
     * Fot test purposes.
     */
    public GfycatCategoriesList(String cursorOrDigest, GfycatCategory... gfycatCategories) {
        cursor = cursorOrDigest;
        digest = cursorOrDigest;
        tags = Arrays.asList(gfycatCategories);
    }

    /**
     * @return Returns cursor which is a next page identifier.
     */
    public String getCursor() {
        return cursor;
    }

    /**
     * List of categories returned from server or cache.
     *
     * @return Returns list of categories.
     */
    public List<GfycatCategory> getTags() {
        return tags;
    }

    /**
     * @return Returns digest which is a next page identifier.
     */
    public String getDigest() {
        return digest;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GfycatCategoriesList that = (GfycatCategoriesList) o;

        return tags != null ? tags.equals(that.tags) : that.tags == null;
    }

    @Override
    public int hashCode() {
        return tags != null ? tags.hashCode() : 0;
    }
}
