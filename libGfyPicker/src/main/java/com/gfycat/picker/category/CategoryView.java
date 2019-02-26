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

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.gfycat.player.GfycatPlayer;
import com.gfycat.picker.R;
import com.gfycat.picker.feed.GfyCardView;
import com.gfycat.player.GfycatPlayerWrapper;

/**
 * Created by anton on 11/28/16.
 */

public abstract class CategoryView extends GfyCardView {

    private GfycatPlayer playerView;

    public CategoryView(Context context) {
        super(context);
    }

    public CategoryView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CategoryView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public GfycatPlayer getPlayerView() {
        return playerView;
    }

    @Override
    protected void sharedConstructor(AttributeSet attrs) {
        super.sharedConstructor(attrs);
        setCardBackgroundColor(ContextCompat.getColor(getContext(), android.R.color.transparent));
        playerView = (GfycatPlayerWrapper) findViewById(R.id.video_view_wrapper);
        playerView.setScaleType(ImageView.ScaleType.CENTER_CROP);
    }
}