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

import java.util.List;

/**
 * Used for Recent category flow
 */
public class GfycatRecentCategory extends GfycatCategory {

    /**
     * Constructor used for Recent category flow
     * @param tag
     * @param gfycats
     */
    public GfycatRecentCategory(String tag, List<Gfycat> gfycats) {
        this.tag = tag;
        this.gfycats = gfycats;
    }

    public void setTagText(String title) {
        this.tagText = title;
    }
}
