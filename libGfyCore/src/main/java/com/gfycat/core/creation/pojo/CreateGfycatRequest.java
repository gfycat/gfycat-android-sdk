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

package com.gfycat.core.creation.pojo;


import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link com.gfycat.core.gfycatapi.pojo.Gfycat} creation request metadata.
 */
public class CreateGfycatRequest {

    @SerializedName("private")
    boolean privateState;

    int nsfw;

    List<Caption> captions;

    List<String> tags;

    String title;

    boolean noMd5;

    private CreateGfycatRequest() {}

    /**
     * {@link CreateGfycatRequest} builder.
     */
    public static class Builder {
        private List<Caption> captions;
        private List<String> tags;
        private String title;

        /**
         * Add a {@link com.gfycat.core.gfycatapi.pojo.Gfycat} tag.
         */
        public Builder addTag(String tag) {
            if (tags == null) {
                tags = new ArrayList<>();
            }
            tags.add(tag);
            return this;
        }

        /**
         * Add a list of {@link com.gfycat.core.gfycatapi.pojo.Gfycat} tags.
         */
        public Builder addTags(List<String> tags) {
            if (tags == null) {
                tags = new ArrayList<>();
            }
            this.tags.addAll(tags);
            return this;
        }

        /**
         * Add captions list to be drawn over Gfycat.
         *
         * @param captions to be drawn over Gfycat.
         */
        public Builder addCaptions(List<Caption> captions) {
            this.captions = captions;
            return this;
        }

        /**
         * Add {@link com.gfycat.core.gfycatapi.pojo.Gfycat} title.
         *
         * @param title of {@link com.gfycat.core.gfycatapi.pojo.Gfycat}.
         */
        public Builder addTitle(String title) {
            this.title = title;
            return this;
        }

        /**
         * @return Returns {@link CreateGfycatRequest} instance.
         */
        public CreateGfycatRequest build() {
            CreateGfycatRequest result = new CreateGfycatRequest();
            result.nsfw = 0;
            result.privateState = true;
            result.noMd5 = true;
            result.captions = captions;
            result.tags = tags;
            result.title = title;
            return result;
        }
    }
}
