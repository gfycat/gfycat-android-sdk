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
import android.content.res.TypedArray;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.gfycat.common.AspectRatioDelegate;
import com.gfycat.common.utils.VersionUtils;
import com.gfycat.core.ApiWrapper;
import com.gfycat.core.gfycatapi.pojo.Gfycat;
import com.gfycat.picker.R;


/**
 * Created by anton on 1/19/17.
 */

public class GfyCardView extends CardView {

    private static final String LOG_TAG = "GfyCardView";

    private static final float MAX_ASPECT_RATIO = 4f;
    private static final float MIN_ASPECT_RATIO = 0.25f;

    private AspectRatioDelegate aspectRatioDelegate = new AspectRatioDelegate(1f, MIN_ASPECT_RATIO, MAX_ASPECT_RATIO);
    private float cornerRadius;

    public GfyCardView(Context context) {
        super(context);
        sharedConstructor(null);
    }

    public GfyCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        sharedConstructor(attrs);
    }

    public GfyCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        sharedConstructor(attrs);
    }

    @Override
    public void setRadius(float radius) {
        super.setRadius(radius);
        this.cornerRadius = radius;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        setRadius(cornerRadius);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(
                aspectRatioDelegate.onMeasureWidth(widthMeasureSpec, heightMeasureSpec),
                aspectRatioDelegate.onMeasureHeight(widthMeasureSpec, heightMeasureSpec));

    }

    public void setAspectRatioFromGfycat(Gfycat gfycat) {
        setAspectRatioFromSize(gfycat.getWidth(), gfycat.getHeight());
    }

    public void setFlattenByWidth(boolean flattenByWidth) {
        aspectRatioDelegate.setFlattenByWidth(flattenByWidth);
    }

    public void setAspectRatioFromSize(int width, int height) {
        setAspectRatio(width / (float) height);
    }

    public void setAspectRatio(float aspectRatio) {
        aspectRatioDelegate.setAspectRatio(aspectRatio);
    }

    protected void onCreateView() {
    }

    protected void sharedConstructor(AttributeSet attrs) {

        onCreateView();

        setContentPadding(0, 0, 0, 0);
        if (VersionUtils.isAtLeastLollipop())
            ApiWrapper.setElevation(this, 0);
        else {
            setMaxCardElevation(0);
            setPreventCornerOverlap(false);

            View roundedMask = new View(getContext());
            roundedMask.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            roundedMask.setBackgroundResource(R.drawable.card_mask);
            this.addView(roundedMask);
        }

        aspectRatioDelegate.fromXml(getContext(), attrs, com.gfycat.common.R.styleable.GfyCardView, com.gfycat.common.R.styleable.GfyCardView_gfyFlattenByWidth);

        TypedArray attrArray = getContext().getTheme().obtainStyledAttributes(
                attrs,
                com.gfycat.common.R.styleable.GfyCardView,
                0, 0);

        try {
            cornerRadius = attrArray.getDimension(com.gfycat.common.R.styleable.GfyCardView_gfyCornerRadius, getResources().getDimension(R.dimen.gfycat_video_preview_rounded_corner_radius));
        } finally {
            attrArray.recycle();
        }
    }
}