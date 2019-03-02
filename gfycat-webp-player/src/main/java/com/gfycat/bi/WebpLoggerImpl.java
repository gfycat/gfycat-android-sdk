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

package com.gfycat.bi;

import com.gfycat.core.bi.CommonKeys;
import com.gfycat.core.bi.analytics.EngineBasedLogger;
import com.gfycat.core.gfycatapi.pojo.Gfycat;
import com.gfycat.core.storage.MediaType;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by anton on 2/7/17.
 */

public class WebpLoggerImpl extends EngineBasedLogger implements WebpLogger {
    @Override
    public void logBadContent(Gfycat gfycat, MediaType mediaType) {

        Map<String, String> params = new HashMap<>();
        params.put(CommonKeys.GFYID_KEY, gfycat.getGfyName());
        params.put("url", mediaType.getUrl(gfycat));

        track(BAD_CONTENT_EVENT, params);
    }
}
