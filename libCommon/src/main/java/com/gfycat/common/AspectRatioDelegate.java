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

package com.gfycat.common;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by dekalo on 08.03.17.
 */

public class AspectRatioDelegate {

    private final float MIN_ASPECT;
    private final float MAX_ASPECT;

    private float aspectRatio;
    private boolean isFlattenByWidth;

    public AspectRatioDelegate(float aspectRatio) {
        this.aspectRatio = aspectRatio;
        MIN_ASPECT = Float.MIN_VALUE;
        MAX_ASPECT = Float.MAX_VALUE;
    }

    public AspectRatioDelegate(float aspectRatio, float minAspectRatio, float maxAspectRatio) {
        this.aspectRatio = aspectRatio;
        MIN_ASPECT = minAspectRatio;
        MAX_ASPECT = maxAspectRatio;
    }

    private int onMeasure(int widthMeasureSpec, int heightMeasureSpec, boolean returnWidth) {
        int width;
        int height;

        if (isFlattenByWidth) {
            width = View.MeasureSpec.getSize(widthMeasureSpec);
            height = (int) (width / aspectRatio);
        } else {
            height = View.MeasureSpec.getSize(heightMeasureSpec);
            width = (int) (height * aspectRatio);
        }

        return returnWidth
                ? View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY)
                : View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY);
    }

    public int onMeasureWidth(int widthMeasureSpec, int heightMeasureSpec) {
        return onMeasure(widthMeasureSpec, heightMeasureSpec, true);
    }

    public int onMeasureHeight(int widthMeasureSpec, int heightMeasureSpec) {
        return onMeasure(widthMeasureSpec, heightMeasureSpec, false);
    }

    public void fromXml(Context context, AttributeSet attrs, int[] stylableAtributes, int attribute) {

        TypedArray attrArray = context.getTheme().obtainStyledAttributes(attrs,
                stylableAtributes,
                0, 0);

        try {
            isFlattenByWidth = attrArray.getBoolean(attribute, true);
        } finally {
            attrArray.recycle();
        }

    }

    public void setFlattenByWidth(boolean flattenByWidth) {
        this.isFlattenByWidth = flattenByWidth;
    }

    public void setAspectRatio(int width, int height) {
        setAspectRatio(width / (float) height);
    }

    public void setAspectRatio(float aspectRatio) {
        aspectRatio = Math.min(aspectRatio, MAX_ASPECT);
        aspectRatio = Math.max(aspectRatio, MIN_ASPECT);
        this.aspectRatio = aspectRatio;
    }
}
