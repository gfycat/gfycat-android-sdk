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

package com.gfycat.player;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.gfycat.common.ContextDetails;
import com.gfycat.core.gfycatapi.pojo.Gfycat;

/**
 * Wrapper for {@link GfycatPlayer}, instantiate actual player via {@link MainPlayerFactory} class.
 */
public class GfycatPlayerWrapper extends FrameLayout implements GfycatPlayer {

    private GfycatPlayer player;

    public GfycatPlayerWrapper(@NonNull Context context) {
        super(context);
        sharedCtor(context);
    }

    public GfycatPlayerWrapper(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        sharedCtor(context);
        internalAttrsInit(context, attrs);
    }

    public GfycatPlayerWrapper(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        sharedCtor(context);
        internalAttrsInit(context, attrs);
    }

    private void sharedCtor(Context context) {
        player = MainPlayerFactory.get().create(context);
        addView(player.getView());
    }

    private void internalAttrsInit(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, com.gfycat.framesequence.R.styleable.FrameSequenceView);

        boolean shouldLoadPreview = a.getBoolean(com.gfycat.framesequence.R.styleable.FrameSequenceView_shouldLoadPreview, false);
        boolean shouldPlay = a.getBoolean(com.gfycat.framesequence.R.styleable.FrameSequenceView_autoplay, false);

        player.setShouldLoadPreview(shouldLoadPreview);
        if (shouldPlay) player.play();

        a.recycle();
    }

    @Override
    public View getView() {
        return this;
    }

    @Override
    public void setShouldLoadPreview(boolean shouldLoadPreview) {
        player.setShouldLoadPreview(shouldLoadPreview);
    }

    @Override
    public void setupPreview(Drawable drawable, boolean animate) {
        player.setupPreview(drawable, animate);
    }

    @Override
    public void setupGfycat(Gfycat gfycat, ContextDetails contextDetails) {
        player.setupGfycat(gfycat, contextDetails);
    }

    @Override
    public void setupGfycat(Gfycat gfycat) {
        player.setupGfycat(gfycat);
    }

    @Override
    public void play() {
        player.play();
    }

    @Override
    public void pause() {
        player.pause();
    }

    @Override
    public void setScaleType(ImageView.ScaleType scaleType) {
        player.setScaleType(scaleType);
    }

    @Override
    public void release() {
        player.release();
    }

    @Override
    public void setOnStartAnimationListener(Runnable onStartAnimationListener) {
        player.setOnStartAnimationListener(onStartAnimationListener);
    }
}
