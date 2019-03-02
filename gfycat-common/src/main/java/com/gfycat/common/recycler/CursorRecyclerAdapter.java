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

package com.gfycat.common.recycler;

import android.database.Cursor;
import android.support.v7.widget.RecyclerView;

import com.gfycat.common.utils.Logging;

/**
 * This class is used as is from http://quanturium.github.io/2015/04/19/using-cursors-with-the-new-recyclerview/ OR https://gist.githubusercontent.com/quanturium/46541c81aae2a916e31d/raw/cb2d28f76b5603249a7430012794bb05634273ab/CursorRecyclerAdapter.java
 *
 * @param <VH>
 */
public abstract class CursorRecyclerAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {
    private static final String LOG_TAG = "CursorRecyclerAdapter";

    private boolean mDataValid;
    protected Cursor mCursor;
    private int mRowIDColumn;

    private NewItemsListener itemsListener;

    public interface NewItemsListener {
        void onTopRangeInserted(int count);

        void onBottomRangeInserted(int count);
    }


    public CursorRecyclerAdapter(Cursor c) {
        init(c);
    }

    private void init(Cursor c) {
        boolean cursorPresent = c != null;
        mCursor = c;
        mDataValid = cursorPresent;
        mRowIDColumn = cursorPresent ? c.getColumnIndexOrThrow("_id") : -1;
        setHasStableIds(true);
    }

    public void setItemsListener(NewItemsListener itemsListener) {
        this.itemsListener = itemsListener;
    }

    @Override
    public final void onBindViewHolder(VH holder, int position) {
        if (!mDataValid) {
            throw new IllegalStateException("this should only be called when the cursor is valid");
        }
        if (!mCursor.moveToPosition(position)) {
            throw new IllegalStateException("couldn't move cursor to position " + position);
        }

        onBindViewHolder(holder, mCursor);
    }

    public abstract void onBindViewHolder(VH holder, Cursor cursor);

    public Cursor getCursor() {
        return mCursor;
    }

    @Override
    public int getItemCount() {
        if (mDataValid && mCursor != null) {
            return mCursor.getCount();
        } else {
            return 0;
        }
    }

    @Override
    public long getItemId(int position) {
        if (hasStableIds() && mDataValid && mCursor != null) {
            if (mCursor.moveToPosition(position)) {
                return mCursor.getLong(mRowIDColumn);
            } else {
                return RecyclerView.NO_ID;
            }
        } else {
            return RecyclerView.NO_ID;
        }
    }

    /**
     * Change the underlying cursor to a new cursor. If there is an existing cursor it will be
     * closed.
     *
     * @param cursor The new cursor to be used
     */
    public void changeGfycats(Cursor cursor) {
        Cursor old = swapCursor(cursor);
        if (old != null) {
            old.close();
        }
    }

    /**
     * Swap in a new Cursor, returning the old Cursor.  Unlike
     * {@link #changeGfycats(Cursor)}, the returned old Cursor is <em>not</em>
     * closed.
     *
     * @param newCursor The new cursor to be used.
     * @return Returns the previously set Cursor, or null if there wasa not one.
     * If the given new Cursor is the same instance is the previously set
     * Cursor, null is also returned.
     */
    private Cursor swapCursor(Cursor newCursor) {
        if (newCursor == mCursor) {
            return null;
        }
        Cursor oldCursor = mCursor;
        mCursor = newCursor;
        if (newCursor != null) {
            mRowIDColumn = newCursor.getColumnIndexOrThrow("_id");
            mDataValid = true;
            if (oldCursor == null || !notifyUpdates(newCursor, oldCursor)) {
                notifyDataSetChanged();
            }
        } else {
            mRowIDColumn = -1;
            mDataValid = false;
            // notify the observers about the lack of a data set
            notifyItemRangeRemoved(0, oldCursor.getCount());
        }
        return oldCursor;
    }

    private boolean notifyUpdates(Cursor newCursor, Cursor oldCursor) {

        // let's say that content could be inserted at oldCursor beginning and after oldCursor ending
        // there could not be any reordering in old cursor if so - let's

        if (oldCursor.getCount() == 0 && newCursor.getCount() == 0) return true;

        if (oldCursor.getCount() == 0) return false;
        if (newCursor.getCount() < oldCursor.getCount()) return false;

        int firstRowIndex = findFirstSameRow(newCursor, oldCursor);
        if (firstRowIndex == -1) return false;
        int addedAtEnd = findAddedAtEndCount(newCursor, oldCursor);
        if (addedAtEnd == -1) return false;

        boolean middleAreaChanged = oldAreaChanged(newCursor, oldCursor, firstRowIndex);

        if (middleAreaChanged) return false;

        if (addedAtEnd > 0) {
            Logging.d(LOG_TAG, "notifyItemRangeInserted at the end, count = ", addedAtEnd);
            notifyItemRangeInserted(oldCursor.getCount(), addedAtEnd);
            if (itemsListener != null) itemsListener.onBottomRangeInserted(addedAtEnd);
        }

        if (firstRowIndex > 0) {
            Logging.d(LOG_TAG, "notifyItemRangeInserted at the beginning, count = ", firstRowIndex);
            notifyItemRangeInserted(0, firstRowIndex);
            if (itemsListener != null) itemsListener.onTopRangeInserted(addedAtEnd);
        }

        return true;
    }

    private boolean oldAreaChanged(Cursor newCursor, Cursor oldCursor, int firstRowIndex) {
        for (int i = 0; i < oldCursor.getCount(); i++) {
            oldCursor.moveToPosition(i);
            newCursor.moveToPosition(firstRowIndex + i);
            if (oldCursor.getLong(oldCursor.getColumnIndex("_id")) != newCursor.getLong(newCursor.getColumnIndex("_id"))) {
                return true;
            }
        }
        return false;
    }


    private int findAddedAtEndCount(Cursor newCursor, Cursor oldCursor) {
        oldCursor.moveToLast();
        int count = 0;
        for (int i = newCursor.getCount() - 1; i >= 0; i--) {
            newCursor.moveToPosition(i);
            if (oldCursor.getLong(oldCursor.getColumnIndex("_id")) == newCursor.getLong(newCursor.getColumnIndex("_id"))) {
                return count;
            }
            count++;
        }
        return -1;
    }

    private int findFirstSameRow(Cursor newCursor, Cursor oldCursor) {
        oldCursor.moveToFirst();
        for (int i = 0; i < newCursor.getCount(); i++) {
            newCursor.moveToPosition(i);
            if (oldCursor.getLong(oldCursor.getColumnIndex("_id")) == newCursor.getLong(newCursor.getColumnIndex("_id"))) {
                return i;
            }
        }
        return -1;
    }

    /**
     * <p>Converts the cursor into a CharSequence. Subclasses should override this
     * method to convert their results. The default implementation returns an
     * empty String for null values or the default String representation of
     * the value.</p>
     *
     * @param cursor the cursor to convert to a CharSequence
     * @return a CharSequence representing the value
     */
    public CharSequence convertToString(Cursor cursor) {
        return cursor == null ? "" : cursor.toString();
    }
}