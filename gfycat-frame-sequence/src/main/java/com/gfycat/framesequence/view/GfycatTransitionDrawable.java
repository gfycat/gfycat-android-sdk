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

package com.gfycat.framesequence.view;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.gfycat.common.ContextDetails;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by dekalo on 06.04.17.
 */

public class GfycatTransitionDrawable extends Drawable implements Drawable.Callback {

    private Drawable first;
    private Drawable second;

    private ColorFilter colorFilter;

    private Queue<DrawableToAnimate> drawableQueue = new LinkedList<>();

    private long startTime;
    private long duration;
    private ContextDetails contextDetails;
    private Rect bounds = new Rect();

    public GfycatTransitionDrawable(Drawable drawable) {
        assignToFirst(drawable);
    }

    public void setDrawable(Drawable drawable) {

        assignToFirst(drawable);
        assignToSecond(null);

        drawableQueue.clear();
    }

    private void assignToFirst(Drawable drawable) {
        if (first != null) first.setCallback(null);
        first = drawable;
        if (first != null) {
            first.setBounds(bounds);
            first.setCallback(this);
            first.setColorFilter(colorFilter);
        }
    }

    private void assignToSecond(Drawable drawable) {
        if (second != null) second.setCallback(null);
        second = drawable;
        if (second != null) {
            second.setBounds(bounds);
            second.setCallback(this);
            second.setColorFilter(colorFilter);
        }
    }

    public void setContextDetails(ContextDetails contextDetails) {
        this.contextDetails = contextDetails;
    }

    public void addDrawable(Drawable drawable, int durationMs) {
        if (first == null) {
            assignToFirst(drawable);
            invalidateSelf();
        } else if (second == null) {
            assignToSecond(drawable);
            startAnimation(durationMs);
        } else {
            drawableQueue.add(new DrawableToAnimate(drawable, durationMs));
        }
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        this.bounds = bounds;
        if (first != null) first.setBounds(bounds);
        if (second != null) second.setBounds(bounds);
    }

    private void startAnimation(int durationMs) {
        startTime = SystemClock.uptimeMillis();
        duration = durationMs;
        invalidateSelf();
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        if (first == null) {
            return;
        }

        if (second == null) {
            first.draw(canvas);
            return;
        }

        int normalized = (int) ((255 * (SystemClock.uptimeMillis() - startTime)) / duration);

        if (normalized >= 255) {
            second.setAlpha(255);
            second.draw(canvas);
            evictFirstDrawable();
        } else {
            first.setAlpha(255);
            first.draw(canvas);
            second.setAlpha(normalized);
            second.draw(canvas);
            invalidateSelf();
        }
    }

    private void evictFirstDrawable() {
        assignToFirst(second);

        if (!drawableQueue.isEmpty()) {
            DrawableToAnimate drawableToAnimate = drawableQueue.poll();
            assignToSecond(drawableToAnimate.drawable);
            startAnimation(drawableToAnimate.duration);
        } else {
            second = null;
        }
    }

    @Override
    public int getIntrinsicWidth() {
        return first.getIntrinsicWidth();
    }

    @Override
    public int getIntrinsicHeight() {
        return first.getIntrinsicHeight();
    }

    @Override
    public void setAlpha(@IntRange(from = 0, to = 255) int alpha) {
        // alpha is controlled internally
    }

    private void applyColorFilter() {
        if (first != null) first.setColorFilter(colorFilter);
        if (second != null) second.setColorFilter(colorFilter);
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        this.colorFilter = colorFilter;
        applyColorFilter();
    }

    @Override
    public void clearColorFilter() {
        this.colorFilter = null;
        applyColorFilter();
    }

    @Override
    public int getOpacity() {
        return first.getOpacity();
    }

    @Override
    public void invalidateDrawable(@NonNull Drawable who) {
        invalidateSelf();
    }

    @Override
    public void scheduleDrawable(@NonNull Drawable who, @NonNull Runnable what, long when) {
        scheduleSelf(what, when);
    }

    @Override
    public void unscheduleDrawable(@NonNull Drawable who, @NonNull Runnable what) {
        unscheduleSelf(what);
    }

    private static class DrawableToAnimate {

        private final Drawable drawable;
        private final int duration;

        public DrawableToAnimate(Drawable drawable, int durationMs) {
            this.drawable = drawable;
            this.duration = durationMs;
        }
    }
}
