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

import android.support.v7.widget.GridLayoutManager;

import com.gfycat.common.Func1;

/**
 * Created by dekalo on 11.11.16.
 */

public class GridSpanSizeLookup extends GridLayoutManager.SpanSizeLookup {

    private final Func1<Integer, Integer> lookup;

    public GridSpanSizeLookup(Func1<Integer, Integer> lookup) {
        this.lookup = lookup;
    }

    @Override
    public int getSpanSize(int position) {
        return lookup.call(position);
    }
}
