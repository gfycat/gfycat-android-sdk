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

package com.gfycat.webp.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.gfycat.common.ContextDetails;
import com.gfycat.core.gfycatapi.pojo.Gfycat;
import com.gfycat.framesequence.view.FrameSequenceView;
import com.gfycat.framesequence.view.OnStartAnimationListener;
import com.gfycat.player.GfycatPlayer;
import com.gfycat.webp.GfycatWebpFrameSequenceSource;

/**
 * Displays a Gfycat using animated webp image from gfycat.
 */
public class GfycatWebpView extends FrameSequenceView implements GfycatPlayer {

    public GfycatWebpView(Context context) {
        super(context);
    }

    public GfycatWebpView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GfycatWebpView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * Setup view to display mentioned {@link Gfycat}.
     */
    public void setupGfycat(Gfycat gfycat, ContextDetails details) {
        setup(new GfycatWebpFrameSequenceSource(getContext(), gfycat, details));
    }

    /**
     * Setup view to display mentioned {@link Gfycat}.
     */
    public void setupGfycat(Gfycat gfycat) {
        setup(new GfycatWebpFrameSequenceSource(getContext(), gfycat));
    }

    @Override
    public void setOnStartAnimationListener(Runnable onStartAnimationListener) {
        setOnStartAnimationListener((OnStartAnimationListener) onStartAnimationListener::run);
    }

    @Override
    public View getView() {
        return this;
    }
}
