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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.gfycat.common.utils.Logging;

/**
 * As usual all initialization / upgrading functionality here.
 * <p>
 * Created by dekalo on 25.08.15.
 */
public class GfycatDBHelper extends SQLiteOpenHelper implements SQLCreationScripts {

    private static final String LOG_TAG = "GfyDB";
    private static String DB_NAME = "Gfycat.db";
    private static int DB_VERSION = 31;

    public GfycatDBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Logging.d(LOG_TAG, "onCreate()");
        SQLUtils.performTransactionWith(db, sqLiteDatabase -> createTables(db));
    }

    private void createTables(SQLiteDatabase db) {
        Logging.d(LOG_TAG, "createTables()");
        db.execSQL(CREATE_GFYCAT_ITEM_TABLE_SQL);
        db.execSQL(CREATE_FEED_TABLE_SQL);
        db.execSQL(CREATE_FEED_ITEM_TABLE_SQL);
        db.execSQL(CREATE_BLOCKED_USERS);
        db.execSQL(CREATE_BLOCKED_GFYCATS);
    }

    private void dropAllTables(SQLiteDatabase db) {
        Logging.d(LOG_TAG, "dropAllTables()");
        db.execSQL("DROP TABLE IF EXISTS " + GfycatDatabaseContracts.BlockedGfycatsContract.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + GfycatDatabaseContracts.BlockedUsersContract.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + GfycatDatabaseContracts.FeedToGfycatRelation.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + GfycatDatabaseContracts.GfycatContract.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + GfycatDatabaseContracts.FeedContract.TABLE_NAME);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Logging.d(LOG_TAG, "onUpgrade", " oldVersion = ", oldVersion, " newVersion = ", newVersion);
        dropAllTables(db);
        createTables(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Logging.d(LOG_TAG, "onDowngrade", " oldVersion = ", oldVersion, " newVersion = ", newVersion);
        dropAllTables(db);
        createTables(db);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        Logging.d(LOG_TAG, "onConfigure() " + DB_VERSION);
        db.execSQL("PRAGMA foreign_keys = ON;");
    }
}
