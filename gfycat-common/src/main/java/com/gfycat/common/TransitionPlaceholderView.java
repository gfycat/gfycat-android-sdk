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
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.gfycat.common.utils.Logging;
import com.gfycat.common.utils.UIUtils;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

/**
 * Created by Stanislav on 5/18/17.
 * <p>
 * {@link TransitionLayout} placeholder, holds target offset for {@link TransitionLayout}
 */
public class TransitionPlaceholderView extends View {

    private static final String TAG = "TransitionPlaceHolderView";

    private int offsetState;

    public TransitionPlaceholderView(@NonNull Context context) {
        super(context);
    }

    public TransitionPlaceholderView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        readAttrs(attrs);
    }

    public TransitionPlaceholderView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        readAttrs(attrs);
    }

    private void readAttrs(AttributeSet attrs) {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.TransitionPlaceholderView);
        offsetState = a.getInt(R.styleable.TransitionPlaceholderView_offsetState, 0);
        a.recycle();
    }

    public int getOffsetState() {
        return offsetState;
    }
}
