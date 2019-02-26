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

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;

import com.gfycat.common.utils.Assertions;

/**
 * Helper class for getting items on screen positions from any LayoutManger.
 * <p/>
 * Created by dekalo on 02.10.15.
 */
public abstract class LayoutManagerHelper {

    public static LayoutManagerHelper getHelper(RecyclerView recyclerView) {
        if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) {
            return new LinearLayoutManagerHelper((LinearLayoutManager) recyclerView.getLayoutManager());
        } else if (recyclerView.getLayoutManager() instanceof StaggeredGridLayoutManager) {
            return new StaggeredGridLayoutManagerHelper((StaggeredGridLayoutManager) recyclerView.getLayoutManager());
        } else {
            Assertions.fail(new IllegalArgumentException("Unsupported layout manager " + recyclerView.getLayoutManager()));
            return new EmptyLayoutManager();
        }
    }

    /**
     * Count of items in layout manager.
     *
     * @return
     */
    public abstract int getItemCount();

    /**
     * @return position of first visible item on scree,
     */
    public abstract int findFirstVisibleItemPosition();

    /**
     * @return position of last visible item on screen.
     */
    public abstract int findLastVisibleItemPosition();

    private static class EmptyLayoutManager extends LayoutManagerHelper {
        @Override
        public int getItemCount() {
            return 0;
        }

        @Override
        public int findFirstVisibleItemPosition() {
            return 0;
        }

        @Override
        public int findLastVisibleItemPosition() {
            return 0;
        }
    }

    private static class StaggeredGridLayoutManagerHelper extends LayoutManagerHelper {

        private final StaggeredGridLayoutManager layoutManager;

        public StaggeredGridLayoutManagerHelper(StaggeredGridLayoutManager layoutManager) {
            this.layoutManager = layoutManager;
        }

        @Override
        public int getItemCount() {
            return layoutManager.getItemCount();
        }

        @Override
        public int findFirstVisibleItemPosition() {
            int[] firstItems = layoutManager.findFirstVisibleItemPositions(null);
            int minItem = Integer.MAX_VALUE;
            for (int value : firstItems) {
                if (value < minItem) minItem = value;
            }
            return minItem;
        }

        @Override
        public int findLastVisibleItemPosition() {
            int[] firstItems = layoutManager.findLastVisibleItemPositions(null);
            int maxItem = Integer.MIN_VALUE;
            for (int value : firstItems) {
                if (value > maxItem) maxItem = value;
            }
            return maxItem;
        }
    }

    private static class LinearLayoutManagerHelper extends LayoutManagerHelper {

        private final LinearLayoutManager layoutManager;

        public LinearLayoutManagerHelper(LinearLayoutManager layoutManager) {
            this.layoutManager = layoutManager;
        }

        @Override
        public int getItemCount() {
            return layoutManager.getItemCount();
        }

        @Override
        public int findFirstVisibleItemPosition() {
            return layoutManager.findFirstVisibleItemPosition();
        }

        @Override
        public int findLastVisibleItemPosition() {
            return layoutManager.findLastVisibleItemPosition();
        }
    }
}
