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

/**
 * Created by dekalo on 28.04.16.
 */
public interface SQLCreationScripts {
    String CREATE_TABLE = " CREATE TABLE IF NOT EXISTS ";
    String INTEGER_TYPE = " INTEGER ";
    String FLOAT_TYPE = " REAL ";
    String NOT_NULL = " NOT NULL ";
    String UNIQUE = " UNIQUE ";
    String NOT_NULL_UNIQUE = NOT_NULL + UNIQUE;
    String TEXT_TYPE = " TEXT ";
    String BLOB_TYPE = " BLOB ";
    String REFERENCES = " REFERENCES ";
    String ON_DELETE_CASCADE = " ON DELETE CASCADE ";
    String COMMA_SEP = ", ";
    String DEFAULT = " DEFAULT ";
    String DROP_TABLE_IF_EXISTS = "DROP TABLE IF EXISTS ";

    String CREATE_GFYCAT_ITEM_TABLE_SQL = CREATE_TABLE + GfycatDatabaseContracts.GfycatContract.TABLE_NAME + " (" +
            GfycatDatabaseContracts.GfycatContract._ID + INTEGER_TYPE + " PRIMARY KEY" + COMMA_SEP +
            GfycatDatabaseContracts.GfycatContract.GFY_ITEM_ID + TEXT_TYPE + NOT_NULL_UNIQUE + COMMA_SEP +
            GfycatDatabaseContracts.GfycatContract.GFY_NAME + TEXT_TYPE + NOT_NULL + COMMA_SEP +
            GfycatDatabaseContracts.GfycatContract.GFY_NUMBER + INTEGER_TYPE + NOT_NULL + COMMA_SEP +
            GfycatDatabaseContracts.GfycatContract.WIDTH + INTEGER_TYPE + COMMA_SEP +
            GfycatDatabaseContracts.GfycatContract.HEIGHT + INTEGER_TYPE + COMMA_SEP +

            GfycatDatabaseContracts.GfycatContract.POSTERURL + TEXT_TYPE + COMMA_SEP +
            GfycatDatabaseContracts.GfycatContract.PNGPOSTERURL + TEXT_TYPE + COMMA_SEP +
            GfycatDatabaseContracts.GfycatContract.MOBILEPOSTERURL + TEXT_TYPE + COMMA_SEP +
            GfycatDatabaseContracts.GfycatContract.MINIPOSTERURL + TEXT_TYPE + COMMA_SEP +
            GfycatDatabaseContracts.GfycatContract.THUMB100POSTERURL + TEXT_TYPE + COMMA_SEP +
            GfycatDatabaseContracts.GfycatContract.MP4URL + TEXT_TYPE + COMMA_SEP +
            GfycatDatabaseContracts.GfycatContract.MOBILEURL + TEXT_TYPE + COMMA_SEP +
            GfycatDatabaseContracts.GfycatContract.MINIURL + TEXT_TYPE + COMMA_SEP +
            GfycatDatabaseContracts.GfycatContract.GIFURL + TEXT_TYPE + COMMA_SEP +
            GfycatDatabaseContracts.GfycatContract.WEBMURL + TEXT_TYPE + COMMA_SEP +
            GfycatDatabaseContracts.GfycatContract.WEBPURL + TEXT_TYPE + COMMA_SEP +
            GfycatDatabaseContracts.GfycatContract.GIF100URL + TEXT_TYPE + COMMA_SEP +
            GfycatDatabaseContracts.GfycatContract.MAX1MBGIFURL + TEXT_TYPE + COMMA_SEP +
            GfycatDatabaseContracts.GfycatContract.MAX2MBGIFURL + TEXT_TYPE + COMMA_SEP +
            GfycatDatabaseContracts.GfycatContract.MAX5MBGIFURL + TEXT_TYPE + COMMA_SEP +

            GfycatDatabaseContracts.GfycatContract.MP4SIZE + INTEGER_TYPE + COMMA_SEP +
            GfycatDatabaseContracts.GfycatContract.WEBMSIZE + INTEGER_TYPE + COMMA_SEP +
            GfycatDatabaseContracts.GfycatContract.USER_NAME + TEXT_TYPE + NOT_NULL + COMMA_SEP +
            GfycatDatabaseContracts.GfycatContract.SERVER_CREATE_DATE + TEXT_TYPE + NOT_NULL + COMMA_SEP +
            GfycatDatabaseContracts.GfycatContract.LOCAL_CREATE_DATE + TEXT_TYPE + NOT_NULL + COMMA_SEP +
            GfycatDatabaseContracts.GfycatContract.VIEWS + INTEGER_TYPE + COMMA_SEP +
            GfycatDatabaseContracts.GfycatContract.TITLE + TEXT_TYPE + COMMA_SEP +
            GfycatDatabaseContracts.GfycatContract.DESCRIPTION + TEXT_TYPE + COMMA_SEP +
            GfycatDatabaseContracts.GfycatContract.PROJECTION_TYPE + TEXT_TYPE + COMMA_SEP +
            GfycatDatabaseContracts.GfycatContract.TAGS + BLOB_TYPE + COMMA_SEP +
            GfycatDatabaseContracts.GfycatContract.DELETED + INTEGER_TYPE + DEFAULT + 0 + COMMA_SEP +
            GfycatDatabaseContracts.GfycatContract.NSFW + INTEGER_TYPE + DEFAULT + 0 + COMMA_SEP +
            GfycatDatabaseContracts.GfycatContract.PUBLISHED + INTEGER_TYPE + DEFAULT + 0 + COMMA_SEP +
            GfycatDatabaseContracts.GfycatContract.HAS_TRANSPARENCY + INTEGER_TYPE + DEFAULT + 0 + COMMA_SEP +
            GfycatDatabaseContracts.GfycatContract.HAS_AUDIO + INTEGER_TYPE + DEFAULT + 0 + COMMA_SEP +
            GfycatDatabaseContracts.GfycatContract.CONTENT_RATING + TEXT_TYPE + COMMA_SEP +
            GfycatDatabaseContracts.GfycatContract.NUM_FRAMES + INTEGER_TYPE + DEFAULT + 0 + COMMA_SEP +
            GfycatDatabaseContracts.GfycatContract.FRAME_RATE + FLOAT_TYPE + DEFAULT + "0.0" + COMMA_SEP +
            GfycatDatabaseContracts.GfycatContract.AVG_COLOR + TEXT_TYPE +
            " );";

    String CREATE_FEED_TABLE_SQL = CREATE_TABLE + GfycatDatabaseContracts.FeedContract.TABLE_NAME + " (" +
            GfycatDatabaseContracts.FeedContract._ID + INTEGER_TYPE + " PRIMARY KEY" + COMMA_SEP +
            GfycatDatabaseContracts.FeedContract.FEED_NEXT_PART_IDENTIFIER + TEXT_TYPE + COMMA_SEP +
            GfycatDatabaseContracts.FeedContract.CREATE_DATE_TIME + TEXT_TYPE + NOT_NULL + COMMA_SEP +
            GfycatDatabaseContracts.FeedContract.FEED_UNIQUE_NAME + TEXT_TYPE + NOT_NULL_UNIQUE + COMMA_SEP +
            GfycatDatabaseContracts.FeedContract.IS_CLOSED + INTEGER_TYPE + NOT_NULL + DEFAULT + "0" +
            " );";

    String CREATE_FEED_ITEM_TABLE_SQL = CREATE_TABLE + GfycatDatabaseContracts.FeedToGfycatRelation.TABLE_NAME + " (" +
            GfycatDatabaseContracts.FeedToGfycatRelation._ID + INTEGER_TYPE + " PRIMARY KEY" + COMMA_SEP +
            GfycatDatabaseContracts.FeedToGfycatRelation.FEED_ID + INTEGER_TYPE + REFERENCES + GfycatDatabaseContracts.FeedContract.TABLE_NAME + "(" + GfycatDatabaseContracts.FeedContract._ID + ")" + ON_DELETE_CASCADE + COMMA_SEP +
            GfycatDatabaseContracts.FeedToGfycatRelation.GFYCAT_ITEM_ID + INTEGER_TYPE + REFERENCES + GfycatDatabaseContracts.GfycatContract.TABLE_NAME + "(" + GfycatDatabaseContracts.GfycatContract._ID + ")" + ON_DELETE_CASCADE + COMMA_SEP +
            GfycatDatabaseContracts.FeedToGfycatRelation.INDEX_IN_FEED + INTEGER_TYPE + NOT_NULL +
            " );";

    String CREATE_BLOCKED_USERS = CREATE_TABLE + GfycatDatabaseContracts.BlockedUsersContract.TABLE_NAME + " (" +
            GfycatDatabaseContracts.BlockedUsersContract.USERNAME + TEXT_TYPE + UNIQUE + NOT_NULL +
            " );";

    String CREATE_BLOCKED_GFYCATS = CREATE_TABLE + GfycatDatabaseContracts.BlockedGfycatsContract.TABLE_NAME + " (" +
            GfycatDatabaseContracts.BlockedGfycatsContract.GFY_ID + TEXT_TYPE + UNIQUE + NOT_NULL +
            " );";
}
