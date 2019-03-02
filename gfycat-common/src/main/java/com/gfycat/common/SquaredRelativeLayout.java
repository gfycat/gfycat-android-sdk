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

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * Created by anton on 10/14/16.
 */

public class SquaredRelativeLayout extends RelativeLayout {
    private boolean isFlattenByHeight = false;

    public SquaredRelativeLayout(Context context) {
        super(context);
    }

    public SquaredRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        applyAttrs(attrs);
    }

    public SquaredRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        applyAttrs(attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SquaredRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        applyAttrs(attrs);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int size = isFlattenByHeight ? heightMeasureSpec : widthMeasureSpec;
        super.onMeasure(size, size);
    }

    public void setFlattenByHeight(boolean flattenByHeight) {
        isFlattenByHeight = flattenByHeight;
    }

    private void applyAttrs(AttributeSet attrs) {
        TypedArray attrArray = getContext().getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.SquaredRelativeLayout,
                0, 0);

        try {
            setFlattenByHeight(attrArray.getBoolean(R.styleable.SquaredRelativeLayout_flattenByHeight, false));
        } finally {
            attrArray.recycle();
        }
    }
}
