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

package com.gfycat.gif;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Movie;

import com.gfycat.framesequence.FrameSequence;

/**
 * Created by dekalo on 20/02/18.
 */

public class GifFrameSequence implements FrameSequence {

    private static final int DEFAULT_FRAME_DURATION = 100;
    private final Movie movie;

    public GifFrameSequence(byte[] data) {
        movie = Movie.decodeByteArray(data, 0, data.length);
    }

    @Override
    public boolean mayHaveBlending() {
        return false;
    }

    @Override
    public long getFrameDuration(int frame) {
        return DEFAULT_FRAME_DURATION;
    }

    @Override
    public void drawFrame(int frame, Bitmap bitmap) {
        movie.setTime(Math.min(frame * DEFAULT_FRAME_DURATION, movie.duration()));
        bitmap.eraseColor(Color.TRANSPARENT);
        movie.draw(new Canvas(bitmap), 0, 0);
    }

    @Override
    public int getWidth() {
        return movie.width();
    }

    @Override
    public int getHeight() {
        return movie.height();
    }

    @Override
    public void release() {
    }

    @Override
    public int getFrameCount() {
        return Math.max(movie.duration() / DEFAULT_FRAME_DURATION, 1);
    }

    @Override
    public boolean isOpaque() {
        return true;
    }

    @Override
    public int lastKeyFrameInRange(int start, int end) {
        return end;
    }
}
