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

package com.gfycat.common.utils;

import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;

import java.util.List;

/**
 * Created by dekalo on 22.02.17.
 */

public class Algorithms {

    public static <T> boolean isIn(T[] array, T value) {
        for (T t : array) {
            if (Utils.equals(t, value))
                return true;
        }
        return false;
    }

    public static class IsOrderedSubsetResult {
        public final boolean isSubset;
        public final int subsetStartIndex;
        public final int subsetEndIndex;

        public IsOrderedSubsetResult(boolean isSubset, int subsetStartIndex, int subsetEndIndex) {
            this.isSubset = isSubset;
            this.subsetStartIndex = subsetStartIndex;
            this.subsetEndIndex = subsetEndIndex;
        }

        private static IsOrderedSubsetResult fail() {
            return new IsOrderedSubsetResult(false, -1, -1);
        }

        private static IsOrderedSubsetResult success(int subsetStartIndex, int subsetEndIndex) {
            return new IsOrderedSubsetResult(true, subsetStartIndex, subsetEndIndex);
        }
    }

    /**
     * Find ordered inclusion of subset into superset.
     */
    public static <T> IsOrderedSubsetResult isOrderedSubset(List<T> subset, List<T> superset) {
        if (superset.size() < subset.size()) return IsOrderedSubsetResult.fail();
        if (subset.isEmpty()) return IsOrderedSubsetResult.success(0, 0);

        int subsetStartIndex = -1;
        int subsetEndIndex;

        int subsetIndex = 0;
        int index = 0;

        while (index < superset.size()) {

            if (subsetStartIndex == -1) {
                if (Utils.equals(superset.get(index), subset.get(subsetIndex))) {
                    /**
                     * Start found.
                     */
                    subsetStartIndex = index;
                } else {
                    /**
                     * Look forward for start
                     */
                    index++;
                }
            } else {

                boolean isEqual = Utils.equals(superset.get(index), subset.get(subsetIndex));
                boolean subsetEndReached = (subsetIndex == subset.size() - 1);

                if (isEqual && subsetEndReached) {
                    /**
                     * We reached subset end and all items are part of superset.
                     */
                    subsetEndIndex = index + 1;
                    return IsOrderedSubsetResult.success(subsetStartIndex, subsetEndIndex);
                } else if (!isEqual && !subsetEndReached) {
                    /**
                     * Difference is the middle found.
                     */
                    return IsOrderedSubsetResult.fail();
                } else {
                    subsetIndex++;
                    index++;
                }
            }
        }

        return IsOrderedSubsetResult.fail();
    }

    public static <T> void prepareDiffAndNotify(List<T> oldList, List<T> newList, RecyclerView.Adapter adapter) {
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return oldList.size();
            }

            @Override
            public int getNewListSize() {
                return newList.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                return Utils.equals(oldList.get(oldItemPosition), newList.get(newItemPosition));
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                return Utils.equals(oldList.get(oldItemPosition), newList.get(newItemPosition));
            }
        }, false);
        result.dispatchUpdatesTo(adapter);
    }
}
