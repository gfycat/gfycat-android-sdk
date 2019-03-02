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

/**
 * Search View controller to be used by GfycatPickerFragment.
 */
public interface SearchController {

    /**
     * @param query A query string to show in Search View.
     */
    void setSearchQuery(String query);

    /**
     * @return Returns current search query.
     */
    String getSearchQuery();

    /**
     * @param searchControllerListener Search View events listener.
     */
    void setSearchControllerListener(SearchControllerListener searchControllerListener);

    /**
     * Used as part of contentTopPadding distance.
     *
     * @return Returns a height of search view.
     */
    int getSearchHeight();

    /**
     * Set accent tint color called by GfycatPickerFragment when accent tinting is being applied.
     */
    void setAccentTintColor(int color);

    /**
     * Set search view visibility.
     */
    void setSearchViewVisible(boolean isVisible);

    /**
     * Check if search view is visible.
     */
    boolean isSearchViewVisible();
}
