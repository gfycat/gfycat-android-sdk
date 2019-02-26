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

package com.gfycat.core.bi.corelogger;

import com.gfycat.core.bi.BIContext;
import com.gfycat.core.bi.analytics.EngineBasedLogger;
import com.gfycat.core.gfycatapi.pojo.Gfycat;
import com.gfycat.core.storage.MediaType;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by anton on 2/7/17.
 */

public class CoreLoggerImpl extends EngineBasedLogger implements CoreLogger {

    @Override
    public void logAccountCreated(BIContext context, String userName, String email, String fb_id) {
        Map<String, String> params = new HashMap<>();
        params.put(CHANNEL_KEY, context.getFlow());
        params.put(USERNAME_KEY, userName);
        params.put(EMAIL_KEY, email);
        params.put(FBID_KEY, fb_id);

        track(ACCOUNT_CREATED_EVENT, params);
    }

    @Override
    public void logAccountLoggedIn(BIContext context, String userName, String email, String fb_id) {
        Map<String, String> params = new HashMap<>();
        params.put(CHANNEL_KEY, context.getFlow());
        params.put(USERNAME_KEY, userName);
        params.put(EMAIL_KEY, email);
        params.put(FBID_KEY, fb_id);

        track(ACCOUNT_LOGGED_IN_EVENT, params);
    }

    @Override
    public void logBrokenContent(Gfycat gfycat, MediaType mediaType) {
        Map<String, String> params = new HashMap<>();
        params.put(MEDIA_TYPE_KEY, mediaType.getName());
        params.put(MEDIA_URL_KEY, mediaType.getUrl(gfycat));

        track(BROKEN_CONTENT, params);
    }

    @Override
    public void logForbidden(Gfycat gfycat, MediaType mediaType) {
        Map<String, String> params = new HashMap<>();
        params.put(MEDIA_TYPE_KEY, mediaType.getName());
        params.put(MEDIA_URL_KEY, mediaType.getUrl(gfycat));

        track(FORBIDDEN_CONTENT, params);
    }
}
