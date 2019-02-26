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

import java.io.Serializable;

/**
 * There are different Feeds: Trending, Tag, Search, Reactions, Single item and User.
 * <p>
 * See {@link PublicFeedIdentifier} for public available feed types.
 */
public interface FeedIdentifier extends Serializable {
    /*
     * FeedActivity and ColumnFeedFragment will work with them in a safe, consistent way.
     * Network client based on type of FeedIdentifier switches to an appropriate api.
     * In DB all Feeds stored in same table, but have different URI to access them from application.
     */

    /**
     * Public available feed types declaration.
     * <p>
     * See public available {@link FeedIdentifier} at {@link PublicFeedIdentifier} class.
     */
    enum Type implements FeedType {

        /**
         * Feed of currently trending gfycats.
         * See <a href="http://developers.gfycat.com/api/#trending-feeds">http://developers.gfycat.com/api/#trending-feeds</a>
         */
        TRENDING("trending"),

        /**
         * Feed of currently trending gfycats by tag name.
         * See <a href="http://developers.gfycat.com/api/#trending-feeds">http://developers.gfycat.com/api/#trending-feeds</a> with tagName
         */
        TAG("tag"),

        /**
         * Search for gfycat by keyword.
         * See <a href="http://developers.gfycat.com/api/#search">http://developers.gfycat.com/api/#search</a>
         */
        SEARCH("search"),

        /**
         * Allows to represent a Gfycat as single feed.
         */
        SINGLE("single"),

        /**
         * Reaction feeds.
         */
        REACTIONS("reactions"),

        /**
         * Public user feed.
         * See <a href="http://developers.gfycat.com/api/#user-feeds">http://developers.gfycat.com/api/#user-feeds</a>
         */
        USER("user"),

        /**
         * Signed user feed list.
         */
        ME("me"),

        /**
         * Trending Gfycats that contains sound.
         * Endpoint: https://api.gfycat.com/v1/sound
         */
        SOUND_TRENDING("sound_trending"),

        /**
         * Search for gfycat by keyword, contains only gfycats with sound.
         * https://api.gfycat.com/v1/sound/search?search_text=$keyword
         */
        SOUND_SEARCH("sound_search");

        /**
         * We would like to protect name from proguard in SDK.
         */
        private final String name;

        Type(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        /* package */ static FeedType fromName(String name) {
            for (FeedType type : values()) {
                if (type.getName().equals(name)) {
                    return type;
                }
            }
            return null;
        }
    }

    /**
     * @return Returns feed type.
     */
    FeedType getType();

    /**
     * @return Returns feed name.
     */
    String toName();

    /**
     * Get a unique feed identifier representation.
     * Obtain {@link FeedIdentifier} using returned string with {@link FeedIdentifierFactory#fromUniqueIdentifier(String)}
     *
     * @return Unique string representation of {@link FeedIdentifier}.
     */
    String toUniqueIdentifier();
}
