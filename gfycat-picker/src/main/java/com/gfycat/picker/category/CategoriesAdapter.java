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

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.ViewGroup;

import com.gfycat.common.Action1;
import com.gfycat.common.Recyclable;
import com.gfycat.core.gfycatapi.pojo.GfycatCategory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by anton on 10/14/16.
 */

public class CategoriesAdapter extends RecyclerView.Adapter<TextCategoryViewHolder> {
    private final Set<Recyclable> weakRecyclableItemsForRelease;
    private final Action1<GfycatCategory> onCategoryClickListener;
    private List<GfycatCategory> filteredData = new ArrayList<>();
    private List<GfycatCategory> data;
    private String currentFiler = "";
    private float aspectRatio;
    private final int orientation;
    private float cornerRadius;

    public CategoriesAdapter(Action1<GfycatCategory> onCategoryClickListener, Set<Recyclable> weakRecyclableItemsForRelease, float aspectRatio, int orientation, float cornerRadius) {
        this.weakRecyclableItemsForRelease = weakRecyclableItemsForRelease;
        this.onCategoryClickListener = onCategoryClickListener;
        this.aspectRatio = aspectRatio;
        this.orientation = orientation;
        this.cornerRadius = cornerRadius;
        filterData();
    }

    @Override
    public TextCategoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        TextCategoryViewHolder holder = new TextCategoryViewHolder(new TextCategoryView(parent.getContext()), aspectRatio, orientation, cornerRadius);
        weakRecyclableItemsForRelease.add(holder);
        return holder;
    }

    @Override
    public void onBindViewHolder(TextCategoryViewHolder holder, int position) {
        GfycatCategory category = filteredData.get(position);
        holder.itemView.setOnClickListener(v -> onCategoryClickListener.call(category));
        holder.bind(category);
    }

    @Override
    public int getItemCount() {
        return filteredData.size();
    }

    public void filter(String filter) {
        filter = filter.trim();
        if (currentFiler.equals(filter))
            return;

        currentFiler = filter;
        filterData();
    }

    public void updateData(List<GfycatCategory> data) {
        this.data = data;
        filterData();
    }

    private void filterData() {
        filteredData.clear();

        if (data != null) {
            if (TextUtils.isEmpty(currentFiler))
                filteredData.addAll(data);
            else
                for (GfycatCategory cat : data)
                    if (cat.getTagText().toLowerCase().contains(currentFiler.toLowerCase()))
                        filteredData.add(cat);
        }

        notifyDataSetChanged();
    }
}
