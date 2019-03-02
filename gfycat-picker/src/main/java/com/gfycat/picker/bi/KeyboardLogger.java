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

package com.gfycat.picker.bi;

import android.support.annotation.IntDef;
import android.support.annotation.StringDef;

import com.gfycat.core.bi.analytics.BILogger;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Created by Madden on 06.02.2017.
 */

public interface KeyboardLogger extends BILogger {
    String KEYWORD_KEY = "keyword";
    String TAP_CATEGORY_EVENT = "tap_category";
    String SEND_GFYCAT_EVENT = "send_video";
    String SEARCH_VIDEOS_EVENT = "search_videos";

    String SOURCE_KEY = "source";
    String SOURCE_VALUE_SEARCH = "search";
    String SOURCE_VALUE_CATEGORY = "category";

    @Retention(SOURCE)
    @StringDef({
            SOURCE_VALUE_SEARCH,
            SOURCE_VALUE_CATEGORY
    })
    @interface SendGfycatSource {}

    void logTapCategory(String keyword);

    void logSendGfycat(String gfyID, String searchKeyword, @SendGfycatSource String source);

    void logSearchVideos(String keyword);
}