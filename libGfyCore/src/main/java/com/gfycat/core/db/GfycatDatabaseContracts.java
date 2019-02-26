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

package com.gfycat.core.db;

import android.provider.BaseColumns;

/**
 * Base Gfycat DB contract.
 * <p/>
 * /feed/trending - returns trending feed item, see Feed schema.
 * /feed/trending/gfycats - returns list of trending gfyItems, see Gfycat schema
 * <p/>
 * /gfycats - here you may query any gfycat item that you want, Gfycat table in digest.
 * Access it like if you have raw access to this table, so you may query any parameters.
 * <p/>
 * /feed/tags/<tagName> - returns Feed associated with mentioned tag name, see Gfycat schema.
 * /feed/tags/<tagName>/gfycats  - returns list of Gfycats for Feed associated with mentioned tag name, see Gfycat schema.
 * <p/>
 * /feed/search/<search_text> - returns Feed associated with search.
 * /feed/search/<search_text>/gfycats - returns list of Gfycats for Feed associated with mentioned search query, see Gfycat schema.
 * <p/>
 * /feed/single/<gfyId> - returns Feed associated with search.
 * /feed/single/<gfyId>/gfycats - returns list of Gfycats for Feed associated with mentioned search, see Gfycat schema.
 * <p/>
 * /trending_tags - returns list of trending tags, see TrendingTags schema.
 * /navigation_items - returns list of navigation items, NavigationItem schema.
 */
final class GfycatDatabaseContracts {

    /**
     * Representing 1 gfycat item.
     */
    static final class GfycatContract implements BaseColumns {
        static final String TABLE_NAME = "gfyList"; // string
        static final String GFY_ITEM_ID = "gfyId"; // string
        static final String GFY_NAME = "gfyName"; // string
        static final String WIDTH = "width"; // int
        static final String HEIGHT = "height"; // int
        static final String POSTERURL = "posterUrl"; // string
        static final String PNGPOSTERURL = "pngPosterUrl"; // string
        static final String MOBILEPOSTERURL = "mobilePosterUrl"; // string
        static final String MINIPOSTERURL = "miniPosterUrl"; // string
        static final String THUMB100POSTERURL = "thumb100PosterUrl"; // string
        static final String MP4URL = "mp4Url"; // string
        static final String MOBILEURL = "mobileUrl"; // string
        static final String MINIURL = "miniUrl"; // string
        static final String GIFURL = "gifUrl"; // string
        static final String WEBMURL = "webmUrl"; // string
        static final String WEBPURL = "webpUrl"; // string
        static final String GIF100URL = "gif100px"; // string
        static final String MAX1MBGIFURL = "max1mbGif"; // string
        static final String MAX2MBGIFURL = "max2mbGif"; // string
        static final String MAX5MBGIFURL = "max5mbGif"; // string
        static final String MP4SIZE = "mp4Size"; // int
        static final String WEBMSIZE = "webmSize"; // int
        static final String GFY_NUMBER = "gfyNumber"; // int
        static final String USER_NAME = "userName"; // String
        static final String SERVER_CREATE_DATE = "servertCreateDate"; // string
        static final String LOCAL_CREATE_DATE = "localCreateDate"; // string
        static final String VIEWS = "views"; // long
        static final String TITLE = "title"; // string
        static final String DESCRIPTION = "description"; // string
        static final String PROJECTION_TYPE = "projectionType"; // string
        static final String TAGS = "tags"; // byte []
        static final String DELETED = "deleted"; // deleted integer as boolean 0  - false and 1 - true
        static final String NSFW = "nsfw"; // nsfw integer 0  - safe for work and 1 - not safe for work
        static final String PUBLISHED = "published"; // published integer 0  - not published and 1 - published
        static final String AVG_COLOR = "avgColor"; // average gfycat color, String #RRGGBB
        static final String HAS_TRANSPARENCY = "has_transparency"; // indicates if gfycat has transparency
        static final String HAS_AUDIO = "has_audio"; // indicates if gfycat has audio
        static final String CONTENT_RATING = "content_rating"; // indicates if gfycat has audio
        static final String NUM_FRAMES = "num_frames"; // indicates if gfycat has audio
        static final String FRAME_RATE = "frame_rate"; // indicates if gfycat has audio

        /**
         * Needed to map fields in complex multiple tables query.
         */
        static final String[] BASE_PROJECTION = new String[]{_ID, GFY_ITEM_ID, GFY_NAME, GFY_NUMBER, WIDTH, HEIGHT, POSTERURL, PNGPOSTERURL, MOBILEPOSTERURL, MINIPOSTERURL, THUMB100POSTERURL, MP4URL, MOBILEURL, MINIURL, GIFURL, WEBMURL, WEBPURL, GIF100URL, MAX1MBGIFURL, MAX2MBGIFURL, MAX5MBGIFURL, MP4SIZE, WEBMSIZE, USER_NAME, SERVER_CREATE_DATE, LOCAL_CREATE_DATE, VIEWS, TITLE, DESCRIPTION, TAGS, DELETED, PROJECTION_TYPE, NSFW, PUBLISHED, AVG_COLOR, HAS_TRANSPARENCY, HAS_AUDIO, CONTENT_RATING, FRAME_RATE, NUM_FRAMES};
    }

    /**
     * Representing 1 Feed item.
     */
    static final class FeedContract implements BaseColumns {
        static final String TABLE_NAME = "gfyFeed"; // string
        static final String FEED_NEXT_PART_IDENTIFIER = "digest"; // string
        static final String FEED_UNIQUE_NAME = "feedUniqueName"; // string unique "tag:<tagName>" for taf feeds or "trending" for trending feeds.
        static final String CREATE_DATE_TIME = "createDate"; // string
        static final String IS_CLOSED = "local_isClosed"; // boolean
    }

    /**
     * Feed and Gfycats mapping n to n.
     */
    static final class FeedToGfycatRelation implements BaseColumns {
        static final String TABLE_NAME = "gfycatFeedRelations";
        static final String FEED_ID = "feed_Id"; // int
        static final String GFYCAT_ITEM_ID = "gfycat_Id"; // int
        static final String INDEX_IN_FEED = "indexInFeed"; // int could be less than 0
    }

    static final class BlockedUsersContract implements BaseColumns {
        static final String TABLE_NAME = "blocked_users";
        static final String USERNAME = "username"; // string
    }

    static final class BlockedGfycatsContract implements BaseColumns {
        static final String TABLE_NAME = "blocked_gfycats";
        static final String GFY_ID = "gfy_id"; // string
    }
}
