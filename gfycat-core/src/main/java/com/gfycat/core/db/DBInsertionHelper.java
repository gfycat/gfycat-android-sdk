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

import android.content.ContentValues;

import com.gfycat.common.Func1;
import com.gfycat.common.utils.Sugar;
import com.gfycat.common.utils.Utils;
import com.gfycat.core.gfycatapi.pojo.Gfycat;

import java.util.Date;

/**
 * Class for manipulation with content values.
 * <p>
 * Created by dekalo on 26.08.15.
 */
class DBInsertionHelper {

    static ContentValues gfycatToCV(com.gfycat.core.gfycatapi.pojo.Gfycat gfycat) {
        ContentValues cv = new ContentValues();
        cv.put(GfycatDatabaseContracts.GfycatContract.GFY_ITEM_ID, gfycat.getGfyId());
        cv.put(GfycatDatabaseContracts.GfycatContract.GFY_NAME, gfycat.getGfyName());
        cv.put(GfycatDatabaseContracts.GfycatContract.GFY_NUMBER, gfycat.getGfyNumber());
        cv.put(GfycatDatabaseContracts.GfycatContract.WIDTH, gfycat.getWidth());
        cv.put(GfycatDatabaseContracts.GfycatContract.HEIGHT, gfycat.getHeight());

        cv.put(GfycatDatabaseContracts.GfycatContract.POSTERURL, gfycat.getPosterUrl());
        cv.put(GfycatDatabaseContracts.GfycatContract.PNGPOSTERURL, gfycat.getPosterPngUrl());
        cv.put(GfycatDatabaseContracts.GfycatContract.MOBILEPOSTERURL, gfycat.getPosterMobileUrl());
        cv.put(GfycatDatabaseContracts.GfycatContract.MINIPOSTERURL, gfycat.getPosterMiniUrl());
        cv.put(GfycatDatabaseContracts.GfycatContract.THUMB100POSTERURL, gfycat.getPosterThumb100Url());
        cv.put(GfycatDatabaseContracts.GfycatContract.MP4URL, gfycat.getMp4Url());
        cv.put(GfycatDatabaseContracts.GfycatContract.MOBILEURL, gfycat.getMp4MobileUrl());
        cv.put(GfycatDatabaseContracts.GfycatContract.MINIURL, gfycat.getMp4MiniUrl());
        cv.put(GfycatDatabaseContracts.GfycatContract.GIFURL, gfycat.getGifLargeUrl());
        cv.put(GfycatDatabaseContracts.GfycatContract.WEBMURL, gfycat.getWebMUrl());
        cv.put(GfycatDatabaseContracts.GfycatContract.WEBPURL, gfycat.getWebPUrl());
        cv.put(GfycatDatabaseContracts.GfycatContract.GIF100URL, gfycat.getGif100pxUrl());
        cv.put(GfycatDatabaseContracts.GfycatContract.MAX1MBGIFURL, gfycat.getGif1mbUrl());
        cv.put(GfycatDatabaseContracts.GfycatContract.MAX2MBGIFURL, gfycat.getGif2mbUrl());
        cv.put(GfycatDatabaseContracts.GfycatContract.MAX5MBGIFURL, gfycat.getGif5mbUrl());

        cv.put(GfycatDatabaseContracts.GfycatContract.MP4SIZE, gfycat.getMp4Size());
        cv.put(GfycatDatabaseContracts.GfycatContract.WEBMSIZE, gfycat.getWebMSize());
        cv.put(GfycatDatabaseContracts.GfycatContract.USER_NAME, gfycat.getUserName());
        cv.put(GfycatDatabaseContracts.GfycatContract.SERVER_CREATE_DATE, gfycat.getCreateDate());
        cv.put(GfycatDatabaseContracts.GfycatContract.LOCAL_CREATE_DATE, Utils.ISO8601.format(new Date()));
        cv.put(GfycatDatabaseContracts.GfycatContract.VIEWS, gfycat.getViews());
        cv.put(GfycatDatabaseContracts.GfycatContract.TITLE, gfycat.getTitle());
        cv.put(GfycatDatabaseContracts.GfycatContract.DESCRIPTION, gfycat.getDescription());
        cv.put(GfycatDatabaseContracts.GfycatContract.PROJECTION_TYPE, gfycat.getProjectionType());
        cv.put(GfycatDatabaseContracts.GfycatContract.TAGS, Utils.serializeListOfStrings(gfycat.getTags()));
        cv.put(GfycatDatabaseContracts.GfycatContract.NSFW, gfycat.getNsfw());
        cv.put(GfycatDatabaseContracts.GfycatContract.PUBLISHED, gfycat.getPublished());
        cv.put(GfycatDatabaseContracts.GfycatContract.AVG_COLOR, gfycat.getAvgColor());
        cv.put(GfycatDatabaseContracts.GfycatContract.HAS_TRANSPARENCY, gfycat.hasTransparency() ? 1 : 0);
        cv.put(GfycatDatabaseContracts.GfycatContract.HAS_AUDIO, gfycat.hasAudio() ? 1 : 0);
        cv.put(GfycatDatabaseContracts.GfycatContract.CONTENT_RATING, gfycat.getContentRating() == null ? null : gfycat.getContentRating().responseValue);
        cv.put(GfycatDatabaseContracts.GfycatContract.NUM_FRAMES, gfycat.getNumFrames());
        cv.put(GfycatDatabaseContracts.GfycatContract.FRAME_RATE, gfycat.getFrameRate());
        return cv;
    }

    static ContentValues feedDigestCV(String nextPartIdentifier, boolean isClosed) {
        ContentValues cv = new ContentValues();
        cv.put(GfycatDatabaseContracts.FeedContract.IS_CLOSED, isClosed);
        cv.put(GfycatDatabaseContracts.FeedContract.FEED_NEXT_PART_IDENTIFIER, nextPartIdentifier);
        return cv;
    }

    static ContentValues closeFeedCV() {
        ContentValues cv = new ContentValues();
        cv.put(GfycatDatabaseContracts.FeedContract.IS_CLOSED, true);
        return cv;
    }

    static ContentValues feedCV(String uniqueName, String nextPartIdentifier, boolean isClosed) {
        ContentValues cv = feedDigestCV(nextPartIdentifier, isClosed);
        cv.put(GfycatDatabaseContracts.FeedContract.CREATE_DATE_TIME, Utils.ISO8601.format(new Date()));
        cv.put(GfycatDatabaseContracts.FeedContract.FEED_UNIQUE_NAME, Utils.safeEncode(uniqueName));
        return cv;
    }

    static ContentValues feedGfycatItemsRelationsToCV(long gfyId, long feedId, int indexInFeed) {
        ContentValues cv = new ContentValues();
        cv.put(GfycatDatabaseContracts.FeedToGfycatRelation.FEED_ID, feedId);
        cv.put(GfycatDatabaseContracts.FeedToGfycatRelation.GFYCAT_ITEM_ID, gfyId);
        cv.put(GfycatDatabaseContracts.FeedToGfycatRelation.INDEX_IN_FEED, indexInFeed);
        return cv;
    }

    static ContentValues deletedCV(boolean deleted) {
        ContentValues cv = new ContentValues();
        cv.put(GfycatDatabaseContracts.GfycatContract.DELETED, deleted ? 1 : 0);
        return cv;
    }
}
