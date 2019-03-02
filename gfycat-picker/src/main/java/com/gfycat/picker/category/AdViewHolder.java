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

package com.gfycat.picker.category;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.gfycat.common.Recyclable;

/**
 * Created by dgoliy on 9/18/17.
 */

public class AdViewHolder extends RecyclerView.ViewHolder implements Recyclable {

    static class AdContainer extends FrameLayout {
        private int adHeight;

        public AdContainer(@NonNull Context context, int height) {
            super(context);
            adHeight = height;
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            if (adHeight > 0) {
                heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(adHeight, View.MeasureSpec.EXACTLY);
            }
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    private AdContainer adContainer;

    public static AdViewHolder create(Context context, boolean isStaggeredLayoutManager, int height) {
        AdContainer parentAdView = new AdContainer(context, height);
        if (isStaggeredLayoutManager) {
            StaggeredGridLayoutManager.LayoutParams params =
                    new StaggeredGridLayoutManager.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height);
            params.setFullSpan(true);
            parentAdView.setLayoutParams(params);
        }

        return new AdViewHolder(parentAdView);
    }

    private AdViewHolder(AdContainer itemView) {
        super(itemView);
        adContainer = itemView;
    }

    public void bind(@NonNull View adView) {
        adContainer.removeAllViews();
        adContainer.addView(adView);
        adView.setAlpha(0.0f);
        adView.animate().alpha(1.0f).start();
    }

    @Override
    public void recycle() {
        adContainer.removeAllViews();
    }
}
