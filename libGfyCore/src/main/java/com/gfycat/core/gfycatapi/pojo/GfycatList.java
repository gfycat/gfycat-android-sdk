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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by dekalo on 10.09.15.
 */
public class GfycatList {

    private String cursor; // user for search
    private String digest; // used for tags and trending
    private List<Gfycat> gfycats;
    private List<Gfycat> newGfycats;
    private String errorMessage;

    public GfycatList() {
    }

    public GfycatList(Gfycat item) {
        gfycats = new ArrayList<>();
        gfycats.add(item);
    }

    public GfycatList(Gfycat item, String digest) {
        gfycats = new ArrayList<>();
        gfycats.add(item);
        this.digest = digest;
    }

    /**
     * Constructor used for Recent category flow
     *
     * @param newGfycats
     */
    public GfycatList(List<Gfycat> newGfycats) {
        this.gfycats = Collections.emptyList();
        this.newGfycats = newGfycats;
    }

    public GfycatList(List<Gfycat> gfycats, String digest) {
        this.gfycats = gfycats;
        this.digest = digest;
    }

    public List<Gfycat> getGfycats() {
        return gfycats;
    }

    public String getCursor() {
        return cursor;
    }

    public String getDigest() {
        return digest;
    }

    public List<Gfycat> getNewGfycats() {
        return newGfycats == null ? Collections.<Gfycat>emptyList() : newGfycats;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getNextDataPartIdentifier() {
        if (digest != null) return digest;
        return cursor;
    }
}