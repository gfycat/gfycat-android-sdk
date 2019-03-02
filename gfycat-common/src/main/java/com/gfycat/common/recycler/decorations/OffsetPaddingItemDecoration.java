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
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by anton on 11/3/16.
 */

public class OffsetPaddingItemDecoration extends PaddingItemDecoration {
    private int offset = 0;

    public OffsetPaddingItemDecoration(int padding, int columnsCount, int offset) {
        super(() -> columnsCount, padding, columnsCount);
        this.offset = offset;
    }

    @Override
    protected void applyPaddingToPosition(Rect outRect, View view, RecyclerView recycler, int position) {
        if (position < offset)
            return;

        position -= offset;
        super.applyPaddingToPosition(outRect, view, recycler, position);
    }
}
