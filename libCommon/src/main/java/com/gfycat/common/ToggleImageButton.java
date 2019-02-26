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
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Checkable;

import java.util.ArrayList;
import java.util.List;

public class ToggleImageButton extends android.support.v7.widget.AppCompatImageButton implements Checkable {
    private static final int STATE_CHECKED = android.R.attr.state_checked;
    private static final int STATE_NEW = R.attr.state_new;

    private final String EXTRA_INSTANCE_STATE = "EXTRA_INSTANCE_STATE";
    private final String EXTRA_CHECKED_STATE = "EXTRA_CHECKED_STATE";
    private final String EXTRA_IS_NEW_STATE = "EXTRA_IS_NEW_STATE";

    private boolean isChecked;
    private boolean isBroadCasting;
    private OnCheckedChangeListener onCheckedChangeListener;

    private boolean isNewState;

    public ToggleImageButton(Context context) {
        super(context);
    }

    public ToggleImageButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttr(context, attrs, 0);
    }

    public ToggleImageButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initAttr(context, attrs, defStyle);
    }

    private void initAttr(Context context, AttributeSet attrs, int defStyle) {
        if (attrs == null) return;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ToogleImageButton, defStyle, 0);
        boolean checked = a.getBoolean(R.styleable.ToogleImageButton_android_checked, false);
        setChecked(checked);
        a.recycle();
    }

    @Override
    public boolean isChecked() {
        return isChecked;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setChecked(boolean checked) {
        if (isChecked == checked) {
            return;
        }
        isChecked = checked;
        refreshDrawableState();
        if (isBroadCasting) {
            return;
        }
        isBroadCasting = true;
        if (onCheckedChangeListener != null) {
            onCheckedChangeListener.onCheckedChanged(this, isChecked);
        }
        isBroadCasting = false;
    }

    public void setNewState(boolean isNew) {
        if (isNewState == isNew) {
            return;
        }
        isNewState = isNew;
        refreshDrawableState();
    }

    public boolean isNewState() {
        return isNewState;
    }

    @Override
    public int[] onCreateDrawableState(int extraSpace) {
        List<Integer> additionalState = new ArrayList<>();
        if (isChecked()) {
            additionalState.add(STATE_CHECKED);
        }
        if (isNewState) {
            additionalState.add(STATE_NEW);
        }
        int[] drawableState = super.onCreateDrawableState(extraSpace + additionalState.size());
        if (additionalState.size() > 0) {
            int[] states = new int[additionalState.size()];
            for (int i = 0; i < additionalState.size(); ++i) {
                states[i] = additionalState.get(i);
            }
            mergeDrawableStates(drawableState, states);
        }

        return drawableState;
    }

    public void setOnCheckedChangeListener(OnCheckedChangeListener onCheckedChangeListener) {
        this.onCheckedChangeListener = onCheckedChangeListener;
    }

    @Override
    public void setOnClickListener(View.OnClickListener outerClickListener) {
        super.setOnClickListener(new InternalClickListener(outerClickListener));
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        setOnClickListener(null);
    }

    @Override
    public void toggle() {
        setChecked(!isChecked);
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable(EXTRA_INSTANCE_STATE, super.onSaveInstanceState());
        bundle.putBoolean(EXTRA_CHECKED_STATE, isChecked);
        bundle.putBoolean(EXTRA_IS_NEW_STATE, isNewState);
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle outState = (Bundle) state;
            setChecked(outState.getBoolean(EXTRA_CHECKED_STATE));
            setNewState(outState.getBoolean(EXTRA_IS_NEW_STATE));
            state = outState.getParcelable(EXTRA_INSTANCE_STATE);
        }
        super.onRestoreInstanceState(state);
    }

    private class InternalClickListener implements View.OnClickListener {
        private final View.OnClickListener outerClickListener;

        private InternalClickListener(View.OnClickListener outerClickListener) {
            this.outerClickListener = outerClickListener;
        }

        @Override
        public void onClick(View v) {
            if (outerClickListener == null) return;

            outerClickListener.onClick(v);
            if (outerClickListener instanceof OnClickListener)
                if (((OnClickListener) outerClickListener).toggleOnClick()) toggle();
        }
    }

    public abstract static class OnClickListener implements View.OnClickListener {
        public boolean toggleOnClick() {
            return true;
        }
    }

    public interface OnCheckedChangeListener {
        void onCheckedChanged(ToggleImageButton buttonView, boolean isChecked);
    }
}