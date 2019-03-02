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

package com.gfycat.webp;

import android.graphics.Bitmap;

import com.gfycat.framesequence.FrameSequence;
import com.gfycat.framesequence.view.FrameSequenceMemoryUsage;

/**
 * Created by dekalo on 05.11.15.
 */
public class WebPOldFrameSequence implements FrameSequence {

    private final WebPImage image;
    private int[] durations;
    private final int size;
    private boolean released;

    public WebPOldFrameSequence(byte[] source) {
        FrameSequenceMemoryUsage.add(size = source.length);
        image = WebPImage.create(source);
    }

    @Override
    public boolean mayHaveBlending() {
        return true;
    }

    @Override
    public long getFrameDuration(int frame) {
        if (durations == null) {
            durations = image.getFrameDurations();
        }
        return durations[frame];
    }

    @Override
    public void drawFrame(int nextFrame, Bitmap bitmap) {
        WebPFrame frame = image.getFrame(nextFrame);
        frame.renderFrame(bitmap, true);
    }

    @Override
    public int getWidth() {
        return image.getWidth();
    }

    @Override
    public int getHeight() {
        return image.getHeight();
    }

    @Override
    public void release() {
        if (!released) {
            image.dispose();
            FrameSequenceMemoryUsage.remove(size);
        }
        released = true;
    }

    @Override
    protected void finalize() throws Throwable {
        if (!released) {
            release();
        }
    }

    @Override
    public int getFrameCount() {
        return image.getFrameCount();
    }

    @Override
    public boolean isOpaque() {
        return true;
    }

    @Override
    public int lastKeyFrameInRange(int start, int end) {
        return image.lastKeyFrameInRange(start, end);
    }
}
