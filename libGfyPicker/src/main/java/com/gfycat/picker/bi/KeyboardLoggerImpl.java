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

import com.gfycat.core.bi.CommonKeys;
import com.gfycat.core.bi.analytics.EngineBasedLogger;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Madden on 06.02.2017.
 */

public class KeyboardLoggerImpl extends EngineBasedLogger implements KeyboardLogger {

    @Override
    public void logTapCategory(String keyword) {
        Map<String, String> params = new HashMap<>();
        params.put(KEYWORD_KEY, keyword);

        track(TAP_CATEGORY_EVENT, params);
    }

    @Override
    public void logSendGfycat(String gfyID, String searchKeyword, @SendGfycatSource String source) {
        Map<String, String> params = new HashMap<>();
        params.put(CommonKeys.GFYID_KEY, gfyID);
        params.put(KEYWORD_KEY, searchKeyword);
        params.put(SOURCE_KEY, searchKeyword);

        track(SEND_GFYCAT_EVENT, params);
    }

    @Override
    public void logSearchVideos(String keyword) {
        Map<String, String> params = new HashMap<>();
        params.put(KEYWORD_KEY, keyword);

        track(SEARCH_VIDEOS_EVENT, params);
    }
}