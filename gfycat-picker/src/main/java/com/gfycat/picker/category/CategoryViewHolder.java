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

import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;

import com.gfycat.common.ContextDetails;
import com.gfycat.common.utils.Assertions;
import com.gfycat.common.utils.Logging;
import com.gfycat.core.bi.impression.GfycatImpression;
import com.gfycat.core.bi.impression.ImpressionInfo;
import com.gfycat.core.gfycatapi.pojo.Gfycat;
import com.gfycat.core.gfycatapi.pojo.GfycatCategory;
import com.gfycat.player.GfycatPlayer;
import com.gfycat.picker.ContextDetailsValues;
import com.gfycat.picker.feed.GfyItemHolder;

/**
 * Created by anton on 11/28/16.
 */

public abstract class CategoryViewHolder extends RecyclerView.ViewHolder implements GfyItemHolder {
    private static final String LOG_TAG = "CategoryViewHolder";

    private ContextDetails details;
    protected Gfycat currentGfycat;
    private String categoryTag;
    protected CategoryView categoryView;
    private boolean isAutoPlay;
    private float aspectRatio;
    private final int orientation;

    public CategoryViewHolder(CategoryView itemView, float aspectRatio, int orientation, float cornerRadius) {
        super(itemView);
        this.categoryView = itemView;
        this.aspectRatio = aspectRatio;
        this.orientation = orientation;
        itemView.setRadius(cornerRadius);
    }

    @Override
    public void recycle() {
        Logging.d(LOG_TAG, "recycle() for ", details);
        isAutoPlay = false;
        currentGfycat = null;

        categoryView.getPlayerView().release();
    }

    @Override
    public void autoPause() {
        if (currentGfycat == null) return;
        Logging.d(LOG_TAG, "autoPause() ", details);
        isAutoPlay = false;

        categoryView.getPlayerView().pause();
    }

    @Override
    public void autoPlay() {
        if (currentGfycat == null) return;
        Logging.d(LOG_TAG, "autoPlay() ", details);

        isAutoPlay = true;
        categoryView.getPlayerView().play();
    }

    @Override
    public boolean isAutoPlay() {
        return isAutoPlay;
    }

    @Override
    public Gfycat getItem() {
        return currentGfycat;
    }

    public void bind(GfycatCategory categoryData) {

        Logging.d(LOG_TAG, "bind(", categoryData, ")");

        if (currentGfycat != null) recycle();
        currentGfycat = categoryData.getGfycat();
        categoryTag = categoryData.getTag();

        if (currentGfycat == null) {
            Assertions.fail(new Throwable("Gfycat can\'t be null! Category \'" + categoryData.getTagText() + "\' " + categoryData.toString()));
            return;
        }

        details = new ContextDetails(
                Pair.create("category", categoryData.getTag()),
                Pair.create(ContextDetailsValues.SOURCE, "CategoryViewHolder"),
                Pair.create(ContextDetailsValues.HASH_CODE, String.valueOf(hashCode())));

        if (aspectRatio <= 0)
            categoryView.setAspectRatioFromGfycat(currentGfycat);
        else
            categoryView.setAspectRatio(aspectRatio);

        categoryView.setFlattenByWidth(orientation == OrientationHelper.VERTICAL);

        initVideoView(categoryView.getPlayerView(), currentGfycat);
    }

    private void initVideoView(GfycatPlayer webpView, final Gfycat gfycat) {
        webpView.setShouldLoadPreview(true);
        webpView.setupGfycat(currentGfycat, details);
        webpView.setOnStartAnimationListener(() -> {
            if (gfycat.equals(currentGfycat)) {
                Logging.d(LOG_TAG, "onStart() ", details);
                GfycatImpression.logImpression(
                        new ImpressionInfo()
                                .setGfyId(gfycat.getGfyId())
                                .setContext(ImpressionInfo.CATEGORY_LIST_CONTEXT)
                                .setFlow(ImpressionInfo.HALF_SCREEN_FLOW)
                                .setKeyword(categoryTag));
            }
        });
    }
}
