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

package com.gfycat.picker.onecategory;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.gfycat.common.utils.Utils;
import com.gfycat.core.FeedIdentifier;
import com.gfycat.core.gfycatapi.pojo.Gfycat;
import com.gfycat.picker.feed.CellController;
import com.gfycat.picker.feed.GfyWebpViewHolder;

/**
 * Created by dekalo on 03.02.17.
 */

public class SingleGfycatAdapter extends RecyclerView.Adapter<GfyWebpViewHolder> {

    private final CellController cellController;
    private final FeedIdentifier feedidentifier;
    private final int orientation;
    private final float cornerRadius;
    private Gfycat gfycat;

    public SingleGfycatAdapter(FeedIdentifier feedidentifier, Gfycat gfycat, int orientation, float cornerRadius, CellController cellController) {
        this.feedidentifier = feedidentifier;
        this.gfycat = gfycat;
        this.orientation = orientation;
        this.cornerRadius = cornerRadius;
        this.cellController = cellController;
    }

    @Override
    public GfyWebpViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new GfyWebpViewHolder(parent.getContext(), cellController, orientation, cornerRadius);
    }

    @Override
    public void onBindViewHolder(GfyWebpViewHolder holder, int position) {
        holder.bind(gfycat, feedidentifier);
    }

    public void setGfycat(Gfycat gfycat) {
        boolean notify = !Utils.equals(gfycat, this.gfycat);
        this.gfycat = gfycat;
        if (notify) notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return gfycat == null ? 0 : 1;
    }
}
