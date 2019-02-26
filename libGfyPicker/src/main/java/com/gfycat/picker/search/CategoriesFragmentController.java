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

package com.gfycat.picker.search;

import com.gfycat.core.FeedIdentifier;
import com.gfycat.core.gfycatapi.pojo.Gfycat;
import com.gfycat.core.gfycatapi.pojo.GfycatCategory;

/**
 * Created by dekalo on 26.01.17.
 */
public interface CategoriesFragmentController {

    /**
     * @return aspect ration for gfycats. if 0f - gfycats will have native aspect ratio.
     * acceptable range [0.25, 4]
     */
    float getCategoryAspectRatio();
    /**
     * @return categories column count.
     */
    int getCategoriesColumnCount();

    /**
     * @return columns count for gfycats.
     */
    int getGfycatsColumnCount();

    /**
     * @return scrollable padding from content to top edge of fragment.
     */
    int getContentTopPadding();

    /**
     * @return scrollable padding from content to bottom edge of fragment.
     */
    int getContentBottomPadding();

    /**
     * @return scrollable padding from left screen end to content.
     */
    int getContentLeftPadding();

    /**
     * @param category click happens.
     */
    @Deprecated
    void onCategoryClick(GfycatCategory category);

    void onPhotoMomentsCategoryClick();

    /**
     * @param identifier
     * @param gfycat     user clicked on.
     * @param position   gfycat is in list.
     */
    void onGfycatClick(FeedIdentifier identifier, Gfycat gfycat, int position);

    /**
     * Returns orientation as {@link android.support.v7.widget.OrientationHelper#HORIZONTAL}
     * or {@link android.support.v7.widget.OrientationHelper#VERTICAL}
     */
    int getOrientation();


    /**
     * Returns category corner radius.
     */
    float getCategoryCornerRadius();

    /**
     * Returns gfycat corner radius.
     */
    float getGfycatCornerRadius();
}
