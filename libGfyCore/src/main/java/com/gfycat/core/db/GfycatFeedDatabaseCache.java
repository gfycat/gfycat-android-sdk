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
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.gfycat.common.ContentValuesBuilder;
import com.gfycat.common.utils.Assertions;
import com.gfycat.common.utils.Logging;
import com.gfycat.common.utils.Sugar;
import com.gfycat.common.utils.ThreadUtils;
import com.gfycat.common.utils.Utils;
import com.gfycat.core.FeedIdentifier;
import com.gfycat.core.GfyUtils;
import com.gfycat.core.PublicFeedIdentifier;
import com.gfycat.core.RecentFeedIdentifier;
import com.gfycat.core.downloading.FeedData;
import com.gfycat.core.downloading.FeedDescription;
import com.gfycat.core.gfycatapi.pojo.Gfycat;
import com.gfycat.core.gfycatapi.pojo.GfycatList;

import org.apache.commons.io.IOUtils;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

import io.reactivex.functions.Function;

/**
 * Created by dekalo on 25.01.17.
 */

public class GfycatFeedDatabaseCache implements GfycatFeedCache {

    private static final String LOG_TAG = "GfycatFeedDatabaseCache";

    private static final boolean VERBOSE = false && Logging.ENABLED;

    private static final String ITEMS_ORDER_COLUMN_NAME = "ITEMS_ORDER_COLUMN_NAME";

    private final Context context;
    private final GfycatDBHelper dbHelper;
    private SQLiteDatabase readableDatabase;
    private SQLiteDatabase writableDatabase;

    public GfycatFeedDatabaseCache(Context context) {
        Logging.d(LOG_TAG, "onCreate()");
        this.context = context;
        dbHelper = new GfycatDBHelper(context);
    }

    private SQLiteDatabase getWritableDatabase() {
        if (writableDatabase == null) {
            writableDatabase = dbHelper.getWritableDatabase();
        }
        return writableDatabase;
    }

    private SQLiteDatabase getReadableDatabase() {
        if (readableDatabase == null) {
            readableDatabase = dbHelper.getReadableDatabase();
        }
        return readableDatabase;
    }

    private void guard() {
        Assertions.assertNotUIThread(IllegalAccessException::new);
    }

    private <T> T mapGfycatCursor(String gfyId, Function<Cursor, T> func) throws CanNotFindGfyIdException {
        Cursor cursor = null;

        try {
            cursor = getReadableDatabase().query(
                    GfycatDatabaseContracts.GfycatContract.TABLE_NAME,
                    null,
                    GfycatDatabaseContracts.GfycatContract.GFY_ITEM_ID + " = \"" + gfyId + "\"",
                    null,
                    null,
                    null,
                    null);

            if (cursor.getCount() == 0) {
                throw new CanNotFindGfyIdException();
            }

            if (cursor.getCount() > 1) {
                Assertions.fail(new IllegalStateException("Multiple gfycats by id = " + gfyId));
            }

            cursor.moveToFirst();

            return func.apply(cursor);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(cursor);
        }
    }

    private long getGfyDBId(String gfyId) {
        try {
            return mapGfycatCursor(gfyId, cursor -> cursor.getLong(cursor.getColumnIndex(GfycatDatabaseContracts.GfycatContract._ID)));
        } catch (CanNotFindGfyIdException e) {
            return -1;
        }
    }

    @Override
    public com.gfycat.core.gfycatapi.pojo.Gfycat getGfycat(String gfyId) {
        try {
            return mapGfycatCursor(gfyId, CursorHelper::getGfycatFromCursor);
        } catch (CanNotFindGfyIdException e) {
            return null;
        }
    }

    @Override
    public void insertFeed(FeedIdentifier identifier, GfycatList data, CloseMode closeMode) {
        guard();
        Logging.d(LOG_TAG, "insertFeed(", identifier, ") nextPart = " + data.getNextDataPartIdentifier());
        insertFeed(identifier, data, closeMode, false);
    }

    @Override
    public void insertFeed(FeedIdentifier identifier, GfycatList gfycatList, CloseMode closeMode, boolean append) {
        guard();
        Logging.d(LOG_TAG, "insertFeed(", identifier, ") nextPart = " + gfycatList.getNextDataPartIdentifier());
        String feedUniqueName = identifier.toUniqueIdentifier();
        SQLiteDatabase db = getWritableDatabase();

        if (isSameAsInDB(feedUniqueName, gfycatList)) {
            Logging.d(LOG_TAG, "Feed is same as DB, skip update.");
            return;
        }

        db.beginTransaction();
        try {
            // remove previous trending (on delete cascade should happens)
            long feed_Id = insertWithPolicy(feedUniqueName, gfycatList, closeMode, append);

            if (feed_Id == -1)
                throw new InternalOperationException("Can not insert feed. feedUniqueName = " + feedUniqueName);

            saveFeedData(db, feed_Id, gfycatList);

            db.setTransactionSuccessful();
        } catch (SQLException | InternalOperationException e) {
            Assertions.fail(e);
            return;
        } catch (Exception e) {
            Assertions.fail(e);
            return;
        } finally {
            db.endTransaction();
        }

        notifyIdentifierChange(identifier);
    }

    @Override
    public void updateFeed(FeedIdentifier identifier, String previousDigest, GfycatList gfycatList) {
        guard();
        Logging.w(LOG_TAG, "updateFeed(", identifier, ") previousDigest = " + previousDigest + " digest = " + gfycatList.getNextDataPartIdentifier());
        String feedUniqueName = identifier.toUniqueIdentifier();
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {

            if (!updateFeedDigest(db, feedUniqueName, gfycatList.getNextDataPartIdentifier(), previousDigest)) {
                throw new WrongDigestException("Can not update digest for feedUniqueName = " + feedUniqueName);
            }

            long feed_Id = getFeedIdByUniqueName(feedUniqueName);
            if (feed_Id == -1)
                throw new InternalOperationException("Can not find feed for feedUniqueName = " + feedUniqueName);

            saveFeedData(db, feed_Id, gfycatList);

            db.setTransactionSuccessful();
        } catch (InternalOperationException e) {
            Assertions.fail(e);
            return;
        } catch (WrongDigestException e) {
            /**
             * This is possible when to loadMore was called in parallel.
             * So first update would be successful, second would fail with this exception.
             */
            Logging.d(LOG_TAG, e, "Wrong digest exception happens.");
            return;
        } finally {
            db.endTransaction();
        }

        notifyIdentifierChange(identifier);
    }

    @Override
    public void closeFeed(FeedIdentifier identifier, String previousDigest) {
        guard();
        Logging.d(LOG_TAG, "closeFeed(", identifier, ")");

        String feedUniqueName = identifier.toUniqueIdentifier();

        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            if (!updateFeedDigest(db, feedUniqueName, "", previousDigest)) {
                throw new InternalOperationException("Can not close feed for feedUniqueName = " + feedUniqueName);
            }

            db.setTransactionSuccessful();
        } catch (InternalOperationException e) {
            /**
             * Possible if someone deleted this feed moment ago.
             * Like with swipe to refresh.
             */
            Logging.d(LOG_TAG, "Insertion exception happens. ", e);
            return;
        } finally {
            db.endTransaction();
        }

        notifyIdentifierChange(identifier);
    }

    @Override
    public boolean markDeleted(com.gfycat.core.gfycatapi.pojo.Gfycat item, boolean deleted) {
        Logging.d(LOG_TAG, "markDeleted(", item, ", ", deleted, ")");
        if (deleted)
            removeSingleItemFeed(item, false);

        return updateGfycatAndNotify(item.getGfyId(), DBInsertionHelper.deletedCV(deleted));
    }

    @Override
    public void removeFromRecent(Gfycat item) {
        guard();
        SQLiteDatabase db = getWritableDatabase();

        String feedUniqueName = RecentFeedIdentifier.recent().toUniqueIdentifier();
        long feedId = getFeedIdByUniqueName(feedUniqueName);
        long gfyDbId = getGfyDBId(item.getGfyId());

        db.beginTransaction();
        try {
            removeRelation(db, feedId, gfyDbId);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public boolean markPublished(com.gfycat.core.gfycatapi.pojo.Gfycat item, boolean published) {
        return updateGfycatAndNotify(item.getGfyId(), new ContentValuesBuilder().put(GfycatDatabaseContracts.GfycatContract.PUBLISHED, published ? 1 : 0).build());
    }

    @Override
    public boolean markNsfw(com.gfycat.core.gfycatapi.pojo.Gfycat item, boolean nsfw) {
        return updateGfycatAndNotify(item.getGfyId(), new ContentValuesBuilder().put(GfycatDatabaseContracts.GfycatContract.NSFW, nsfw ? 1 : 0).build());
    }

    @Override
    public boolean delete(FeedIdentifier feedIdentifier) {
        boolean result = internalDelete(feedIdentifier, true);
        notifyIdentifierChange(feedIdentifier);
        return result;
    }

    public boolean internalDelete(FeedIdentifier feedIdentifier, boolean notify) {
        try {
            return getWritableDatabase().delete(GfycatDatabaseContracts.FeedContract.TABLE_NAME, GfycatDatabaseContracts.FeedContract.FEED_UNIQUE_NAME + " = ?", new String[]{Utils.safeEncode(feedIdentifier.toUniqueIdentifier())}) == 1;
        } finally {
            if (notify) notifyIdentifierChange(feedIdentifier);
        }
    }

    @Override
    public FeedData getFeedData(FeedIdentifier feedIdentifier) {

        FeedDescription feedDescription = getFeed(getReadableDatabase(), feedIdentifier);

        if (feedDescription == null) {
            return new FeedData(new FeedDescription(feedIdentifier));
        } else {
            long id = getFeedIdByUniqueName(feedIdentifier.toUniqueIdentifier());
            return new FeedData(feedDescription, getGfycatsForFeed(id));
        }
    }

    @Override
    public boolean blockItem(com.gfycat.core.gfycatapi.pojo.Gfycat gfcycat, boolean block) {

        long result;

        if (block) {
            ContentValues cv = new ContentValues();
            cv.put(GfycatDatabaseContracts.BlockedGfycatsContract.GFY_ID, gfcycat.getGfyId());
            result = getWritableDatabase().insert(GfycatDatabaseContracts.BlockedGfycatsContract.TABLE_NAME, null, cv);
        } else {
            result = getWritableDatabase().delete(GfycatDatabaseContracts.BlockedGfycatsContract.TABLE_NAME, GfycatDatabaseContracts.BlockedGfycatsContract.GFY_ID + " = ?", new String[]{gfcycat.getGfyId()});
        }

        notifyRootChange();

        Assertions.assertEquals(1L, result, IllegalStateException::new);
        return result == 1;
    }

    @Override
    public boolean blockUser(String userName, boolean block) {

        long result;

        if (block) {
            ContentValues cv = new ContentValues();
            cv.put(GfycatDatabaseContracts.BlockedUsersContract.USERNAME, userName);
            result = getWritableDatabase().insert(GfycatDatabaseContracts.BlockedUsersContract.TABLE_NAME, null, cv);
        } else {
            result = getWritableDatabase().delete(GfycatDatabaseContracts.BlockedUsersContract.TABLE_NAME, GfycatDatabaseContracts.BlockedUsersContract.USERNAME + " = ?", new String[]{userName});
        }

        notifyRootChange();

        Assertions.assertEquals(1L, result, IllegalStateException::new);
        return result == 1;
    }

    private long insertWithPolicy(String feedUniqueName, GfycatList gfycatList, CloseMode closeMode, boolean append) throws InternalOperationException {
        if (append) {
            return insertIfNotExistFeed(getWritableDatabase(), feedUniqueName, gfycatList.getNextDataPartIdentifier(), closeMode);
        } else {
            return insertOrReplaceFeed(getWritableDatabase(), feedUniqueName, gfycatList.getNextDataPartIdentifier(), closeMode);
        }
    }

    private int getIndexInFeedWithAggregation(SQLiteDatabase db, long feedId, String aggregationFunction) {
        Cursor cursor = db.rawQuery(
                "SELECT " + aggregationFunction + "(" + GfycatDatabaseContracts.FeedToGfycatRelation.INDEX_IN_FEED + ") as " + GfycatDatabaseContracts.FeedToGfycatRelation.INDEX_IN_FEED + " " +
                        "FROM " + GfycatDatabaseContracts.FeedToGfycatRelation.TABLE_NAME + " " +
                        "WHERE " + GfycatDatabaseContracts.FeedToGfycatRelation.FEED_ID + " = ?", new String[]{String.valueOf(feedId)});

        int index = 0;
        try {
            if (cursor.getCount() != 0) {
                cursor.moveToFirst();
                index = cursor.getInt(cursor.getColumnIndex(GfycatDatabaseContracts.FeedToGfycatRelation.INDEX_IN_FEED));
            }
        } finally {
            IOUtils.closeQuietly(cursor);
        }

        return index;
    }

    private int getMaxIndexInFeed(SQLiteDatabase db, long feedId) {
        return getIndexInFeedWithAggregation(db, feedId, "max");
    }

    private int getMinIndexInFeed(SQLiteDatabase db, long feedId) {
        return getIndexInFeedWithAggregation(db, feedId, "min");
    }

    /**
     * Insert Gfycat in mentioned DB.
     */
    private long insertGfycat(SQLiteDatabase writableDB, com.gfycat.core.gfycatapi.pojo.Gfycat value) {

        long gfyDbId = getGfyDBId(value.getGfyId());

        if (gfyDbId < 0) {
            return writableDB.insert(GfycatDatabaseContracts.GfycatContract.TABLE_NAME, null, DBInsertionHelper.gfycatToCV(value));
        } else {
            writableDB.update(GfycatDatabaseContracts.GfycatContract.TABLE_NAME, DBInsertionHelper.gfycatToCV(value), GfycatDatabaseContracts.GfycatContract._ID + " = " + gfyDbId, null);
            return gfyDbId;
        }
    }

    private long insertRelation(SQLiteDatabase db, long feedId, long gfyDbId, FeedIndexer indexer, boolean replaceIfExists) {

        long feedGfycatRelationId = -1;
        Cursor cursor = getReadableDatabase().query(
                GfycatDatabaseContracts.FeedToGfycatRelation.TABLE_NAME,
                null,
                GfycatDatabaseContracts.FeedToGfycatRelation.GFYCAT_ITEM_ID + " = " + gfyDbId + " AND " + GfycatDatabaseContracts.FeedToGfycatRelation.FEED_ID + " = " + feedId,
                null,
                null,
                null,
                null);

        if (cursor != null && cursor.moveToFirst()) {
            if (replaceIfExists) {
                long relationIdToRemove = cursor.getLong(cursor.getColumnIndex(GfycatDatabaseContracts.FeedToGfycatRelation._ID));
                db.delete(
                        GfycatDatabaseContracts.FeedToGfycatRelation.TABLE_NAME,
                        GfycatDatabaseContracts.FeedToGfycatRelation._ID + " = ?",
                        new String[]{String.valueOf(relationIdToRemove)});
            } else {
                feedGfycatRelationId = cursor.getLong(cursor.getColumnIndex(GfycatDatabaseContracts.FeedToGfycatRelation._ID));
            }
        }

        if (feedGfycatRelationId == -1) {
            feedGfycatRelationId = db.insert(
                    GfycatDatabaseContracts.FeedToGfycatRelation.TABLE_NAME,
                    null,
                    DBInsertionHelper.feedGfycatItemsRelationsToCV(gfyDbId, feedId, indexer.nextIndex()));
        }

        IOUtils.closeQuietly(cursor);
        return feedGfycatRelationId;
    }

    private void removeRelation(SQLiteDatabase db, long feedId, long gfyDbId) {
        db.delete(
                GfycatDatabaseContracts.FeedToGfycatRelation.TABLE_NAME,
                GfycatDatabaseContracts.FeedToGfycatRelation.GFYCAT_ITEM_ID + " = " + gfyDbId
                        + " AND " + GfycatDatabaseContracts.FeedToGfycatRelation.FEED_ID + " = " + feedId,
                null);
    }

    private void saveGfyListFeedData(SQLiteDatabase db, long feedId, List<com.gfycat.core.gfycatapi.pojo.Gfycat> items, FeedIndexer indexer, boolean replaceFeedGfycatRelationIfExists) throws InternalOperationException {
        for (com.gfycat.core.gfycatapi.pojo.Gfycat gfycat : items) {
            long gfyDbId = insertGfycat(db, gfycat);
            if (gfyDbId == -1)
                throw new InternalOperationException("Can not insert gfycat with gfyId = " + gfycat.getGfyId());
            long feedGfycatRelation_Id = insertRelation(db, feedId, gfyDbId, indexer, replaceFeedGfycatRelationIfExists);
            if (feedGfycatRelation_Id == -1)
                throw new InternalOperationException("Can not insert relation = " + gfycat.getGfyId());
            if (VERBOSE) Logging.d(LOG_TAG, "save to db: " + gfycat.getGfyId());
        }
    }

    /**
     * @throws InternalOperationException - if operation is not possible.
     */
    private void saveFeedData(SQLiteDatabase db, long feedId, GfycatList gfycatList) throws InternalOperationException {
        // save all gfycats and relations to feed

        int minIndex = getMinIndexInFeed(db, feedId);
        int maxIndex = getMaxIndexInFeed(db, feedId);

        db.beginTransaction();
        try {
            saveGfyListFeedData(db, feedId, gfycatList.getGfycats(), new FeedIndexer(maxIndex, true), false);
            saveGfyListFeedData(db, feedId, gfycatList.getNewGfycats(), new FeedIndexer(minIndex, false), true);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    private boolean isSameAsInDB(String feedUniqueName, GfycatList gfycatList) {

        Cursor feedCursor = null, gfycatsCursor = null;

        try {

            if (!gfycatList.getNewGfycats().isEmpty()) return false;

            feedCursor = getFeedByUniqueName(feedUniqueName);

            if (!feedCursor.moveToFirst()) return false;
            if (isFeedOutdated(feedCursor)) return false;

            gfycatsCursor = getGfycatsCursorForFeed(feedCursor.getLong(feedCursor.getColumnIndex(GfycatDatabaseContracts.FeedContract._ID)));

            if (gfycatsCursor.getCount() < gfycatList.getGfycats().size()) return false;

            for (int i = 0; i < gfycatList.getGfycats().size(); i++) {
                if (!gfycatsCursor.moveToPosition(i)) return false;
                if (!Utils.equals(gfycatList.getGfycats().get(i).getGfyId(), gfycatsCursor.getString(gfycatsCursor.getColumnIndex(GfycatDatabaseContracts.GfycatContract.GFY_ITEM_ID)))) {
                    return false;
                }
            }

            return true;
        } finally {
            ThreadUtils.with(feedCursor, Cursor::close);
            ThreadUtils.with(gfycatsCursor, Cursor::close);
        }
    }

    private List<com.gfycat.core.gfycatapi.pojo.Gfycat> getGfycatsForFeed(long feed_id) {
        Cursor cursor = null;
        try {
            return CursorHelper.getGfycatsFromCursor(cursor = getGfycatsCursorForFeed(feed_id));
        } finally {
            IOUtils.closeQuietly(cursor);
        }
    }

    private Cursor getGfycatsCursorForFeed(long feed_id) {
        String query = "SELECT " + buildProjectionForGfycatForFeed() +
                " FROM " + GfycatDatabaseContracts.FeedContract.TABLE_NAME + ", " + GfycatDatabaseContracts.FeedToGfycatRelation.TABLE_NAME + ", " + GfycatDatabaseContracts.GfycatContract.TABLE_NAME +
                " WHERE " + GfycatDatabaseContracts.FeedContract.TABLE_NAME + "." + GfycatDatabaseContracts.FeedContract._ID + " = " + GfycatDatabaseContracts.FeedToGfycatRelation.TABLE_NAME + "." + GfycatDatabaseContracts.FeedToGfycatRelation.FEED_ID +
                " AND " + GfycatDatabaseContracts.GfycatContract.TABLE_NAME + "." + GfycatDatabaseContracts.GfycatContract._ID + " = " + GfycatDatabaseContracts.FeedToGfycatRelation.TABLE_NAME + "." + GfycatDatabaseContracts.FeedToGfycatRelation.GFYCAT_ITEM_ID +
                " AND " + GfycatDatabaseContracts.FeedContract.TABLE_NAME + "." + GfycatDatabaseContracts.FeedContract._ID + " = " + feed_id +
                " AND " + GfycatDatabaseContracts.GfycatContract.TABLE_NAME + "." + GfycatDatabaseContracts.GfycatContract.DELETED + " = 0 " +
                " AND " + GfycatDatabaseContracts.GfycatContract.TABLE_NAME + "." + GfycatDatabaseContracts.GfycatContract.GFY_ITEM_ID + " NOT IN " + "(" + "SELECT " + GfycatDatabaseContracts.BlockedGfycatsContract.GFY_ID + " FROM " + GfycatDatabaseContracts.BlockedGfycatsContract.TABLE_NAME + ")" +
                " AND " + GfycatDatabaseContracts.GfycatContract.USER_NAME + " NOT IN " + "(" + "SELECT " + GfycatDatabaseContracts.BlockedUsersContract.USERNAME + " FROM " + GfycatDatabaseContracts.BlockedUsersContract.TABLE_NAME + ")" +
                " ORDER BY " + ITEMS_ORDER_COLUMN_NAME + ";";

        if (VERBOSE) {
            Logging.d(LOG_TAG, "getGfycatsForFeed query = ", query);
        }

        return getReadableDatabase().rawQuery(query, null);
    }

    private String buildProjectionForGfycatForFeed() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < GfycatDatabaseContracts.GfycatContract.BASE_PROJECTION.length; i++) {
            String field = GfycatDatabaseContracts.GfycatContract.BASE_PROJECTION[i];
            sb.append(GfycatDatabaseContracts.GfycatContract.TABLE_NAME).append(".").append(field).append(" as ").append(field);
            if (i < GfycatDatabaseContracts.GfycatContract.BASE_PROJECTION.length - 1) {
                sb.append(", ");
            }
        }

        // add ordering item
        sb.append(", ")
                .append(GfycatDatabaseContracts.FeedToGfycatRelation.TABLE_NAME).append(".").append(GfycatDatabaseContracts.FeedToGfycatRelation.INDEX_IN_FEED)
                .append(" as ").append(ITEMS_ORDER_COLUMN_NAME);

        return sb.toString();
    }

    private boolean isFeedOutdated(Cursor cursor) {
        Date createDate = null;
        try {
            createDate = Utils.ISO8601.parse(cursor.getString(cursor.getColumnIndex(GfycatDatabaseContracts.FeedContract.CREATE_DATE_TIME)));
        } catch (ParseException e) {/* skip */}

        return GfyUtils.isFeedOutdated(createDate);
    }

    private Cursor getFeedByUniqueName(String uniqueName) {
        return getReadableDatabase().query(
                GfycatDatabaseContracts.FeedContract.TABLE_NAME,
                null,
                GfycatDatabaseContracts.FeedContract.FEED_UNIQUE_NAME + " = ?",
                new String[]{Utils.safeEncode(uniqueName)},
                null,
                null,
                null);
    }

    private long insertOrReplaceFeed(SQLiteDatabase db, String feedUniqueName, String nextPartIdentifier, CloseMode closeMode) throws InternalOperationException {
        long feedId = -1;

        db.beginTransaction();
        try {
            db.delete(
                    GfycatDatabaseContracts.FeedContract.TABLE_NAME,
                    GfycatDatabaseContracts.FeedContract.FEED_UNIQUE_NAME + " = ?",
                    new String[]{Utils.safeEncode(feedUniqueName)});

            // insert new feed
            feedId = db.insert(
                    GfycatDatabaseContracts.FeedContract.TABLE_NAME,
                    null, DBInsertionHelper.feedCV(
                            feedUniqueName,
                            nextPartIdentifier,
                            !closeMode.isOpen(nextPartIdentifier)));

            if (feedId == -1)
                throw new InternalOperationException("::insertFeed() can not insert feed, feedUniqueName = " + feedUniqueName);

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        return feedId;
    }

    private long insertIfNotExistFeed(SQLiteDatabase db, String feedUniqueName, String nextPartIdentifier, CloseMode closeMode) throws InternalOperationException {
        long feedId = -1;

        db.beginTransaction();
        try {
            feedId = getFeedIdByUniqueName(feedUniqueName);

            if (feedId == -1) {
                feedId = db.insert(
                        GfycatDatabaseContracts.FeedContract.TABLE_NAME,
                        null, DBInsertionHelper.feedCV(
                                feedUniqueName,
                                nextPartIdentifier,
                                !closeMode.isOpen(nextPartIdentifier)));
                Logging.d(LOG_TAG, "New feed " + feedUniqueName + " inserted with rowId = " + feedId);
            }

            if (feedId == -1)
                throw new InternalOperationException("::insertFeed() can not insert feed, feedUniqueName = " + feedUniqueName);

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        return feedId;
    }

    private long getFeedIdByUniqueName(String uniqueName) {
        Cursor cursor = getFeedByUniqueName(uniqueName);

        if (cursor.getCount() != 0 && cursor.moveToFirst()) {
            try {
                return cursor.getLong(cursor.getColumnIndex(GfycatDatabaseContracts.FeedContract._ID));
            } finally {
                IOUtils.closeQuietly(cursor);
            }
        }
        return -1;
    }

    /**
     * Update digest for feed.
     *
     * @return return true if successfully happens.
     */
    private boolean updateFeedDigest(SQLiteDatabase db, String feedUniqueName, String digest, String previousDigest) {
        return db.update(
                GfycatDatabaseContracts.FeedContract.TABLE_NAME,
                DBInsertionHelper.feedDigestCV(digest, false),
                GfycatDatabaseContracts.FeedContract.FEED_UNIQUE_NAME + " = ? AND " + GfycatDatabaseContracts.FeedContract.FEED_NEXT_PART_IDENTIFIER + " = ?",
                new String[]{Utils.safeEncode(feedUniqueName), previousDigest}) == 1;
    }

    private void removeSingleItemFeed(com.gfycat.core.gfycatapi.pojo.Gfycat item, boolean notify) {
        internalDelete(PublicFeedIdentifier.fromSingleItem(item.getGfyId()), notify);
    }

    private boolean updateGfycatAndNotify(String gfyId, ContentValues contentValues) {
        guard();
        Logging.d(LOG_TAG, "updateGfycatAndNotify(", gfyId, ")");
        SQLiteDatabase db = getWritableDatabase();
        int result = db.update(GfycatDatabaseContracts.GfycatContract.TABLE_NAME, contentValues, GfycatDatabaseContracts.GfycatContract.GFY_ITEM_ID + " = ?; ", new String[]{gfyId});
        Assertions.assertEquals(1, result, () -> new IllegalStateException(LOG_TAG + "::updateGfycatAndNotify(" + gfyId + ") wrong updated count"));

        notifyRootChange();

        return result == 1;
    }

    private void notifyRootChange() {
        Logging.d(LOG_TAG, "notifyRootChange()");
        FeedCacheUriContract.getFeedChangeEventBus().notifyRootChange();
    }

    private void notifyIdentifierChange(FeedIdentifier identifier) {
        Logging.d(LOG_TAG, "notifyIdentifierChange(", identifier, ")");
        FeedCacheUriContract.getFeedChangeEventBus().notifyChange(identifier);
    }

    /**
     * Get feed by uniqueFeedName.
     */
    private static FeedDescription getFeed(SQLiteDatabase readableDB, FeedIdentifier feedIdentifier) {
        Cursor cursor = null;
        try {
            cursor = readableDB.query(
                    GfycatDatabaseContracts.FeedContract.TABLE_NAME,
                    null,
                    GfycatDatabaseContracts.FeedContract.FEED_UNIQUE_NAME + " = ?",
                    new String[]{String.valueOf(Utils.safeEncode(feedIdentifier.toUniqueIdentifier()))},
                    null,
                    null,
                    null);
            if (cursor == null || cursor.getCount() != 1) {
                // Possible if feed was deleted or there was no such.
                return null;
            }
            cursor.moveToFirst();
            return CursorHelper.getFeedDescriptionFromCursor(cursor);
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    public void close() {
        Sugar.doIfNotNull(readableDatabase, SQLiteDatabase::close);
        readableDatabase = null;
        Sugar.doIfNotNull(writableDatabase, SQLiteDatabase::close);
        writableDatabase = null;
        Sugar.doIfNotNull(dbHelper, GfycatDBHelper::close);
        dbHelper.close();
    }

    private static class InternalOperationException extends Exception {
        InternalOperationException(String s) {
            super(s);
        }
    }

    private static class WrongDigestException extends Exception {
        WrongDigestException(String message) {
            super(message);
        }
    }

    private static class CanNotFindGfyIdException extends Throwable {
    }
}
