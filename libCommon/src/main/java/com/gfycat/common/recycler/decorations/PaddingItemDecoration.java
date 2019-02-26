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

package com.gfycat.common.recycler.decorations;

import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;

import com.gfycat.common.Func0;

/**
 * Created by dekalo on 12.10.16.
 */
public class PaddingItemDecoration extends RecyclerView.ItemDecoration {
    private final Func0<Integer> itemsCountInTheFirstRow;
    private final int columnsCount;
    private final int padding;

    public static PaddingItemDecoration defaultDecoration(int bottomPadding, int columnCount) {
        return new PaddingItemDecoration(() -> columnCount, bottomPadding, columnCount);
    }

    public static PaddingItemDecoration dynamicFirstRowDecoration(@NonNull Func0<Integer> itemsCountInTheFirstRow, int padding, int columnCount) {
        return new PaddingItemDecoration(itemsCountInTheFirstRow, padding, columnCount);
    }

    protected PaddingItemDecoration(Func0<Integer> itemsCountInTheFirstRow, int padding, int columnsCount) {
        this.itemsCountInTheFirstRow = itemsCountInTheFirstRow;
        this.columnsCount = columnsCount;
        this.padding = padding;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view);
        applyPaddingToPosition(outRect, view, parent, position);
    }

    protected void applyPaddingToPosition(Rect outRect, View view, RecyclerView recycler, int position) {
        int leftPadding = padding;
        int rightPadding = padding;

        if (view.getLayoutParams() instanceof StaggeredGridLayoutManager.LayoutParams) {
            StaggeredGridLayoutManager.LayoutParams params = (StaggeredGridLayoutManager.LayoutParams) view.getLayoutParams();
            if (params.getSpanIndex() == 0)
                leftPadding = padding * 2;
            if (params.getSpanIndex() == columnsCount - 1 ||
                    (isFirstRow(position) && params.getSpanIndex() == itemsCountInTheFirstRow.call() - 1))
                rightPadding = padding * 2;
        }

        outRect.set(leftPadding, padding, rightPadding, padding);
    }

    private boolean isFirstRow(int position) {
        return position < itemsCountInTheFirstRow.call();
    }
}