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
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.gfycat.common.utils.UIUtils;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

/**
 * Created by dgoliy on 5/15/17.
 * <p>
 * A layout containing two child views and operating their transparency with {@link #setOffset} method call from outside
 */

public class TransitionLayout extends FrameLayout {

    private static final String TAG = "TransitionLayout";

    private View mFirstView, mSecondView;

    public TransitionLayout(@NonNull Context context) {
        super(context);
    }

    public TransitionLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public TransitionLayout(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        // check if we have a correct amount of children in this ViewGroup
        if (getChildCount() != 2) {
            throw new IllegalStateException("TransitionLayout should have 2 child views. Not more and not less.");
        }

        mFirstView = getChildAt(0);
        mSecondView = getChildAt(1);

        if (mFirstView.getLayoutParams().width != MATCH_PARENT ||
                mFirstView.getLayoutParams().height != MATCH_PARENT ||
                mSecondView.getLayoutParams().width != MATCH_PARENT ||
                mSecondView.getLayoutParams().height != MATCH_PARENT) {
            throw new IllegalStateException("TransitionLayout children should have both dimensions set to MATCH_PARENT");
        }

        mFirstView.setVisibility(View.VISIBLE);
        mSecondView.setVisibility(View.INVISIBLE);
    }

    public void setOffset(float offset) {
        UIUtils.updateViewAlphaAndVisibility(mFirstView, offset);
        UIUtils.updateViewAlphaAndVisibility(mSecondView, 1.0f - offset);
    }
}
