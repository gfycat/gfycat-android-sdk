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

import android.os.Handler;
import android.support.v7.widget.RecyclerView;

/**
 * RecyclerView scroll listener that will fire onLoadMore() callback when RecyclerView is near end.
 */
public abstract class EndlessScrollListener extends RecyclerView.OnScrollListener {
    public static String TAG = "EndlessScrollListener";

    private static final int RESET_LOADING_STATE_MAX_FREQUENCY_MS = 3000;

    private int previousTotal = 0; // The total number of items in the dataset after the last loadAsResponse
    private boolean loading = true; // True if we are still waiting for the last set of data to loadAsResponse.
    private static final int ITEMS_AFTER_LAST_THRESHOLD = 5; // Amount of items that after last item that should trigger update.
    private int lastVisibleItem, totalItemCount;

    private int currentPage = 1;

    private long lastOnLoadMoreTimestamp;

    private Handler resetLoadingStateHandler = new Handler();

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        if (dy < 0) return; //check for scroll down

        LayoutManagerHelper helper = LayoutManagerHelper.getHelper(recyclerView);

        totalItemCount = helper.getItemCount();
        lastVisibleItem = helper.findLastVisibleItemPosition();

        if (loading) {
            if (totalItemCount > previousTotal) {
                loading = false;
                previousTotal = totalItemCount;
            }
        }
        if (!loading && (totalItemCount - lastVisibleItem) < ITEMS_AFTER_LAST_THRESHOLD) {
            // End has been reached
            // Do loadAsResponse more
            currentPage++;

            onLoadMore(currentPage);
            lastOnLoadMoreTimestamp = System.currentTimeMillis();

            loading = true;
            resetLoadingStateHandler.removeCallbacksAndMessages(null);
        }
    }

    public abstract void onLoadMore(int currentPage);

    public void reInit() {
        loading = true;
        previousTotal = lastVisibleItem = totalItemCount = 0;
    }

    public void forceUpdate(RecyclerView recyclerView) {
        onScrolled(recyclerView, 0, 0);
    }

    public void resetLoadingState() {
        long resetLoadingStateDelay = RESET_LOADING_STATE_MAX_FREQUENCY_MS - (System.currentTimeMillis() - lastOnLoadMoreTimestamp);
        if (resetLoadingStateDelay < 0) {
            resetLoadingStateDelay = 0;
        }

        resetLoadingStateHandler.postDelayed(() -> loading = false, resetLoadingStateDelay);
    }
}