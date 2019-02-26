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

package com.gfycat.picker.feed;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;

import com.gfycat.player.GfycatPlayer;
import com.gfycat.picker.R;
import com.gfycat.player.GfycatPlayerWrapper;

/**
 * Created by dekalo on 10.03.17.
 */

public class WebpCellView extends GfyCardView {

    private GfycatPlayer playerView;

    public WebpCellView(Context context) {
        super(context);
    }

    public WebpCellView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WebpCellView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onCreateView() {
        inflate(getContext(), R.layout.gfycat_feed_view_cell_layout, this);
        setCardBackgroundColor(ContextCompat.getColor(getContext(), android.R.color.transparent));
    }

    public GfycatPlayer gfycatWebpView() {
        if (playerView == null) {
            playerView = (GfycatPlayerWrapper) findViewById(R.id.feed_cell_webp_view);
        }
        return playerView;
    }
}
