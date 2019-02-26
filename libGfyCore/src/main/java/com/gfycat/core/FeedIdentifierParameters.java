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
package com.gfycat.core;

import java.text.DecimalFormat;

/* package */
public class FeedIdentifierParameters {

    public static final String NAME = "name";
    public static final String TAG_PARAMETER = "tagName";
    public static final String SEARCH_TEXT_PARAMETER = "search_text";
    public static final String USERNAME_PARAMETER = "username";

    public static final String MIN_LENGTH = "minLength";
    public static final String MAX_LENGTH = "maxLength";
    public static final float MIN_LENGTH_VALUE = 0;
    public static final float MAX_LENGTH_VALUE = 60;

    public static final String MIN_ASPECT_RATIO = "minAspectRation";
    public static final String MAX_ASPECT_RATIO = "maxAspectRation";
    public static final float MIN_ASPECT_RATIO_VALUE = 0.1f;
    public static final float MAX_ASPECT_RATIO_VALUE = 10f;

    public static final String CONTENT_RATING = "contentRating";

    private static final DecimalFormat ASPECT_RATIO_FORMAT = new DecimalFormat("#.###");
    private static final DecimalFormat LENGTH_FORMAT = new DecimalFormat("#.##");

    /* package */
    static String formatAspect(float aspect) {
        return ASPECT_RATIO_FORMAT.format(aspect);
    }

    /* package */
    static String formatLength(float length) {
        return LENGTH_FORMAT.format(length);
    }
}
