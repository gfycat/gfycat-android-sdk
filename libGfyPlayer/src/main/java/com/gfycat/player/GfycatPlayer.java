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

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;

import com.gfycat.common.ContextDetails;
import com.gfycat.core.gfycatapi.pojo.Gfycat;
import com.gfycat.framesequence.view.FrameSequenceSource;
import com.gfycat.framesequence.view.FrameSequenceView;

/**
 * Gfycat player interface.
 */
public interface GfycatPlayer {

    /**
     * If set to true preview image will be loaded and displayed while larger animated file is being loaded.
     * <p>
     * Default value is <b>false</b>.
     * <p>
     * IMPORTANT: Call this method before {@link #setupGfycat(Gfycat)}, otherwise preview image will not be loaded.
     * <p>
     * Note:
     * Use {@link FrameSequenceView#setupPreview(Drawable, boolean)} instead if you need to display your own preview.
     * <p>
     * You can also download preview image manually and set it as a preview with {@link FrameSequenceView#setupPreview(Drawable, boolean)}
     * if you already have one, or there are other needs.
     */
    void setShouldLoadPreview(boolean shouldLoadPreview);

    /**
     * Set preview manually by calling this method.
     * <p>
     * Note: If {@link #setShouldLoadPreview(boolean)} is set to true then preview image will be replaced by the one loaded from {@link FrameSequenceSource#loadPoster()}.
     *
     * @param drawable to use as preview, until actual animation will be loaded is being loaded.
     * @param animate  set to true if transition animation needed.
     */
    void setupPreview(Drawable drawable, boolean animate);

    /**
     * @param gfycat
     * @param contextDetails
     */
    void setupGfycat(Gfycat gfycat, ContextDetails contextDetails);

    void setupGfycat(Gfycat gfycat);

    /**
     * Start animation playback.
     */
    void play();

    /**
     * Pause animation playback.
     */
    void pause();

    /**
     * Set scale type, see #ImageView.ScaleType
     */
    void setScaleType(ImageView.ScaleType scaleType);

    /**
     * You MUST call this method when @{link {@link GfycatPlayer} is no longer needed in order to release resources and avoid memory leak.
     * <p>
     * Note: We are working on automatic resource release encapsulation. This method will be marked as Deprecated when done.
     */
    void release();

    /**
     * Set callback that would be invoked on first animation start.
     */
    void setOnStartAnimationListener(Runnable onStartAnimationListener);

    /**
     * Returns view that represents this player.
     */
    View getView();
}
