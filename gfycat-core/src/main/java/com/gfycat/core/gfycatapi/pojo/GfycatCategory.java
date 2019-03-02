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


import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

/**
 * Contains Gfycat category data: gfycats list, tag text, etc.
 */
public class GfycatCategory {

    protected String tag;
    protected List<Gfycat> gfycats;
    protected String cursor;
    protected String digest;
    protected String tagText;

    public GfycatCategory() {
    }

    public GfycatCategory(String cursorOrDigest, String tag, String tagText, Gfycat... gfycats) {
        this.tag = tag;
        this.cursor = cursorOrDigest;
        this.digest = cursorOrDigest;
        this.tagText = tagText;
        this.gfycats = Arrays.asList(gfycats);
    }

    /**
     * @return Returns category tag.
     */
    public String getTag() {
        return tag;
    }

    /**
     * List of gfycats in category.
     * Usually it is one element.
     *
     * @return Returns a list of gfycats in category.
     */
    public List<Gfycat> getGfycats() {
        return gfycats;
    }

    /**
     * @return Returns a cursor which is used as next page identifier.
     */
    public String getCursor() {
        return cursor;
    }

    /**
     * @return Returns digest which is used as next page identifier.
     */
    public String getDigest() {
        return digest;
    }

    /**
     * @return Returns a localized category name.
     */
    public String getTagText() {
        return tagText;
    }

    public boolean isValid() {
        return gfycats.size() == 1 && gfycats.get(0).isValid();
    }

    @Override
    public String toString() {
        return "GfycatCategory{" +
                "tag='" + tag + '\'' +
                ", gfycats=" + Arrays.toString(gfycats.toArray()) +
                ", cursor='" + cursor + '\'' +
                ", digest='" + digest + '\'' +
                ", tagText='" + tagText + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GfycatCategory that = (GfycatCategory) o;

        if (tag != null ? !tag.equals(that.tag) : that.tag != null) return false;
        if (gfycats != null ? !gfycats.equals(that.gfycats) : that.gfycats != null) return false;
        return tagText != null ? tagText.equals(that.tagText) : that.tagText == null;

    }

    @Override
    public int hashCode() {
        int result = tag != null ? tag.hashCode() : 0;
        result = 31 * result + (gfycats != null ? gfycats.hashCode() : 0);
        result = 31 * result + (tagText != null ? tagText.hashCode() : 0);
        return result;
    }

    public Gfycat getGfycat() {
        return gfycats == null || gfycats.isEmpty() ? null : gfycats.get(0);
    }
}
