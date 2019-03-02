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
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.gfycat.common.utils.Logging;

/**
 * Created by Andrew Khloponin
 * <p/>
 * FrameLayout that would change it size depends on possible space and aspect ratio provided
 */
public class AspectRatioViewContainer extends FrameLayout {
    private static final String LOG_TAG = AspectRatioViewContainer.class.getSimpleName();

    private float aspectRatio = -1;


    public AspectRatioViewContainer(Context context) {
        super(context);
    }

    public AspectRatioViewContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AspectRatioViewContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    public void setAspectRatio(float aspectRatio) {
        this.aspectRatio = aspectRatio;
        requestLayout();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (aspectRatio == -1) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }

        int maxWidth = MeasureSpec.getSize(widthMeasureSpec);
        int maxHeight = MeasureSpec.getSize(heightMeasureSpec);
        float maxAspectRatio = maxWidth / (float) maxHeight;
        int width = 0;
        int height = 0;
        if (maxAspectRatio <= aspectRatio) {
            width = MeasureSpec.getSize(widthMeasureSpec);
            height = (int) (width / aspectRatio);
        } else {
            height = MeasureSpec.getSize(heightMeasureSpec);
            width = (int) (height * aspectRatio);
        }
        Logging.d(LOG_TAG, "::onMeasure(...) ", " ", width, " ", height);
        super.onMeasure(
                MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
    }
}