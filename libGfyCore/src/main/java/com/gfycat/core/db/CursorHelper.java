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

import android.database.Cursor;

import com.gfycat.common.utils.Assertions;
import com.gfycat.common.utils.Utils;
import com.gfycat.core.FeedIdentifierFactory;
import com.gfycat.core.downloading.FeedDescription;
import com.gfycat.core.gfycatapi.pojo.Gfycat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by dekalo on 25.01.17.
 */
public class CursorHelper {
    private static final String LOG_TAG = "CursorHelper";

    static FeedDescription getFeedDescriptionFromCursor(Cursor cursor) {
        return new FeedDescription(
                cursor.getInt(cursor.getColumnIndex(GfycatDatabaseContracts.FeedContract.IS_CLOSED)) == 1,
                FeedIdentifierFactory.fromUniqueIdentifier(Utils.safeDecode(cursor.getString(cursor.getColumnIndex(GfycatDatabaseContracts.FeedContract.FEED_UNIQUE_NAME)))),
                cursor.getString(cursor.getColumnIndex(GfycatDatabaseContracts.FeedContract.FEED_NEXT_PART_IDENTIFIER)),
                Utils.parseDateSafe(cursor.getString(cursor.getColumnIndex(GfycatDatabaseContracts.FeedContract.CREATE_DATE_TIME)), Utils.ISO8601, new Date(0)).getTime());
    }

    /**
     * Get Gfycat from Cursor that contains gfycat table rows only.
     */
    public static Gfycat getGfycatFromCursor(Cursor cursor) {
        com.gfycat.core.gfycatapi.pojo.Gfycat gfycat = new com.gfycat.core.gfycatapi.pojo.Gfycat();
        gfycat.setGfyId(cursor.getString(cursor.getColumnIndex(GfycatDatabaseContracts.GfycatContract.GFY_ITEM_ID)));
        gfycat.setGfyName(cursor.getString(cursor.getColumnIndex(GfycatDatabaseContracts.GfycatContract.GFY_NAME)));
        gfycat.setGfyNumber(cursor.getString(cursor.getColumnIndex(GfycatDatabaseContracts.GfycatContract.GFY_NUMBER)));
        gfycat.setWidth(cursor.getInt(cursor.getColumnIndex(GfycatDatabaseContracts.GfycatContract.WIDTH)));
        gfycat.setHeight(cursor.getInt(cursor.getColumnIndex(GfycatDatabaseContracts.GfycatContract.HEIGHT)));

        gfycat.setPosterUrl(cursor.getString(cursor.getColumnIndex(GfycatDatabaseContracts.GfycatContract.POSTERURL)));
        gfycat.setPosterPngUrl(cursor.getString(cursor.getColumnIndex(GfycatDatabaseContracts.GfycatContract.PNGPOSTERURL)));
        gfycat.setPosterMobileUrl(cursor.getString(cursor.getColumnIndex(GfycatDatabaseContracts.GfycatContract.MOBILEPOSTERURL)));
        gfycat.setPosterMiniUrl(cursor.getString(cursor.getColumnIndex(GfycatDatabaseContracts.GfycatContract.MINIPOSTERURL)));
        gfycat.setPosterThumb100Url(cursor.getString(cursor.getColumnIndex(GfycatDatabaseContracts.GfycatContract.THUMB100POSTERURL)));
        gfycat.setMp4Url(cursor.getString(cursor.getColumnIndex(GfycatDatabaseContracts.GfycatContract.MP4URL)));
        gfycat.setMp4MobileUrl(cursor.getString(cursor.getColumnIndex(GfycatDatabaseContracts.GfycatContract.MOBILEURL)));
        gfycat.setMp4MiniUrl(cursor.getString(cursor.getColumnIndex(GfycatDatabaseContracts.GfycatContract.MINIURL)));
        gfycat.setGifLargeUrl(cursor.getString(cursor.getColumnIndex(GfycatDatabaseContracts.GfycatContract.GIFURL)));
        gfycat.setWebMUrl(cursor.getString(cursor.getColumnIndex(GfycatDatabaseContracts.GfycatContract.WEBMURL)));
        gfycat.setWebPUrl(cursor.getString(cursor.getColumnIndex(GfycatDatabaseContracts.GfycatContract.WEBPURL)));
        gfycat.setGif100pxUrl(cursor.getString(cursor.getColumnIndex(GfycatDatabaseContracts.GfycatContract.GIF100URL)));
        gfycat.setGif1mbUrl(cursor.getString(cursor.getColumnIndex(GfycatDatabaseContracts.GfycatContract.MAX1MBGIFURL)));
        gfycat.setGif2mbUrl(cursor.getString(cursor.getColumnIndex(GfycatDatabaseContracts.GfycatContract.MAX2MBGIFURL)));
        gfycat.setGif5mbUrl(cursor.getString(cursor.getColumnIndex(GfycatDatabaseContracts.GfycatContract.MAX5MBGIFURL)));

        gfycat.setMp4Size(cursor.getInt(cursor.getColumnIndex(GfycatDatabaseContracts.GfycatContract.MP4SIZE)));
        gfycat.setWebMSize(cursor.getInt(cursor.getColumnIndex(GfycatDatabaseContracts.GfycatContract.WEBMSIZE)));
        gfycat.setUserName(cursor.getString(cursor.getColumnIndex(GfycatDatabaseContracts.GfycatContract.USER_NAME)));
        gfycat.setCreateDate(cursor.getString(cursor.getColumnIndex(GfycatDatabaseContracts.GfycatContract.SERVER_CREATE_DATE)));
        gfycat.setViews(cursor.getInt(cursor.getColumnIndex(GfycatDatabaseContracts.GfycatContract.VIEWS)));
        gfycat.setTitle(cursor.getString(cursor.getColumnIndex(GfycatDatabaseContracts.GfycatContract.TITLE)));
        gfycat.setDescription(cursor.getString(cursor.getColumnIndex(GfycatDatabaseContracts.GfycatContract.DESCRIPTION)));
        gfycat.setProjectionType(cursor.getString(cursor.getColumnIndex(GfycatDatabaseContracts.GfycatContract.PROJECTION_TYPE)));
        gfycat.setTags(Utils.deSerializeListOfStrings(cursor.getBlob(cursor.getColumnIndex(GfycatDatabaseContracts.GfycatContract.TAGS))));
        gfycat.setNsfw(cursor.getInt(cursor.getColumnIndex(GfycatDatabaseContracts.GfycatContract.NSFW)));
        gfycat.setPublished(cursor.getInt(cursor.getColumnIndex(GfycatDatabaseContracts.GfycatContract.PUBLISHED)));
        gfycat.setAvgColor(cursor.getString(cursor.getColumnIndex(GfycatDatabaseContracts.GfycatContract.AVG_COLOR)));
        gfycat.setHasTransparency(cursor.getInt(cursor.getColumnIndex(GfycatDatabaseContracts.GfycatContract.HAS_TRANSPARENCY)) == 1);
        gfycat.setHasAudio(cursor.getInt(cursor.getColumnIndex(GfycatDatabaseContracts.GfycatContract.HAS_AUDIO)) == 1);
        gfycat.setContentRating(cursor.getString(cursor.getColumnIndex(GfycatDatabaseContracts.GfycatContract.CONTENT_RATING)));
        gfycat.setFrameRate(cursor.getFloat(cursor.getColumnIndex(GfycatDatabaseContracts.GfycatContract.FRAME_RATE)));
        gfycat.setNumFrames(cursor.getInt(cursor.getColumnIndex(GfycatDatabaseContracts.GfycatContract.NUM_FRAMES)));
        return gfycat;
    }

    public static List<Gfycat> getGfycatsFromCursor(Cursor cursor) {
        Assertions.assertNotUIThread(IllegalStateException::new);

        List<Gfycat> result = new ArrayList<>(cursor.getCount());

        while (cursor.moveToNext()) {
            result.add(getGfycatFromCursor(cursor));
        }

        return result;
    }
}
