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

package com.gfycat.framesequence;

import android.graphics.Bitmap;

/**
 * Provider for frames for FrameSequenceDrawable.
 * <p>
 * Created by dekalo on 05.11.15.
 */
public interface FrameSequence {

    /**
     * @return true if format may need blending frames.
     */
    boolean mayHaveBlending();

    /**
     * @return duration of mentioned frame.
     */
    long getFrameDuration(int frame);

    /**
     * @param frame  - that should be drawn.
     * @param bitmap - where to render image.
     */
    void drawFrame(int frame, Bitmap bitmap);

    /**
     * @return with of frame sequence.
     */
    int getWidth();

    /**
     * Height of frame sequence.
     */
    int getHeight();

    /**
     * Release associated resources.
     */
    void release();

    /**
     * @return count of frames in sequence.
     */
    int getFrameCount();

    /**
     * @return true if is opaque, false otherwise.
     */
    boolean isOpaque();

    /**
     * @return last frame that can be treated as keyframe in range [start, end].
     */
    int lastKeyFrameInRange(int start, int end);
}
