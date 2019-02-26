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

package com.gfycat.picker.category;

import android.support.annotation.DrawableRes;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.gfycat.common.Action1;
import com.gfycat.common.Recyclable;
import com.gfycat.core.gfycatapi.pojo.GfycatCategory;

import java.util.Set;

/**
 * Created by dgoliy on 3/31/17.
 */

public class SingleCategoryAdapter extends RecyclerView.Adapter<IconCategoryViewHolder> {

    private final Set<Recyclable> weakRecyclableItemsForRelease;
    private final Action1<GfycatCategory> onCategoryClickListener;
    private GfycatCategory gfycatCategory;
    @DrawableRes
    private int iconResId;
    private String currentFiler = "";
    private float aspectRatio;
    private int orientation;
    private final float cornerRadius;

    private boolean shouldHideRecentCategory = false;

    public SingleCategoryAdapter(Action1<GfycatCategory> onCategoryClickListener, Set<Recyclable> weakRecyclableItemsForRelease, float aspectRatio, int orientation, float cornerRadius) {
        this.weakRecyclableItemsForRelease = weakRecyclableItemsForRelease;
        this.onCategoryClickListener = onCategoryClickListener;
        this.aspectRatio = aspectRatio;
        this.orientation = orientation;
        this.cornerRadius = cornerRadius;
    }

    public void update(GfycatCategory gfycatCategory, @DrawableRes int iconResId) {
        this.gfycatCategory = gfycatCategory;
        this.iconResId = iconResId;
        notifyDataSetChanged();
    }

    public void filter(String filter) {
        if (filter == null)
            filter = filter.trim();
        if (currentFiler.equals(filter.toLowerCase()))
            return;

        currentFiler = filter.toLowerCase();
        filterData();
    }

    private void filterData() {
        shouldHideRecentCategory = gfycatCategory == null || !gfycatCategory.getTag().contains(currentFiler);
        notifyDataSetChanged();
    }

    @Override
    public IconCategoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        IconCategoryViewHolder holder = new IconCategoryViewHolder(new IconCategoryView(parent.getContext()), aspectRatio, orientation, cornerRadius);
        weakRecyclableItemsForRelease.add(holder);
        return holder;
    }

    @Override
    public void onBindViewHolder(IconCategoryViewHolder holder, int position) {
        holder.itemView.setOnClickListener(v -> onCategoryClickListener.call(gfycatCategory));
        holder.bind(gfycatCategory, iconResId);
    }

    @Override
    public int getItemCount() {
        return gfycatCategory != null && gfycatCategory.getGfycats().size() > 0 && !shouldHideRecentCategory ? 1 : 0;
    }
}
