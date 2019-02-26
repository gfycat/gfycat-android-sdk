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

import android.support.annotation.DrawableRes;

import com.gfycat.core.gfycatapi.pojo.GfycatCategory;

/**
 * Created by dgoliy on 4/5/17.
 */

public class IconCategoryViewHolder extends CategoryViewHolder {
    public IconCategoryViewHolder(IconCategoryView itemView, float aspectRatio, int orientation, float cornerRadius) {
        super(itemView, aspectRatio, orientation, cornerRadius);
    }

    public void bind(GfycatCategory categoryData, @DrawableRes int iconResId) {
        super.bind(categoryData);

        ((IconCategoryView) categoryView).getIconView().setImageResource(iconResId);
    }
}
