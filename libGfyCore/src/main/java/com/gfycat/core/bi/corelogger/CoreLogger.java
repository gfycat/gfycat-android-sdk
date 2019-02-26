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
import com.gfycat.core.bi.analytics.BILogger;
import com.gfycat.core.gfycatapi.pojo.Gfycat;
import com.gfycat.core.storage.MediaType;

/**
 * Created by anton on 2/7/17.
 */

public interface CoreLogger extends BILogger {
    String ACCOUNT_CREATED_EVENT = "account_created";
    String ACCOUNT_LOGGED_IN_EVENT = "account_logged_in";
    String FORBIDDEN_CONTENT = "forbidden_content";
    String BROKEN_CONTENT = "broken_content";
    String MEDIA_TYPE_KEY = "media_type";
    String MEDIA_URL_KEY = "media_url";
    String USERNAME_KEY = "username";
    String CHANNEL_KEY = "channel";
    String FBID_KEY = "fb_id";
    String EMAIL_KEY = "email";

    void logAccountCreated(BIContext context, String userName, String email, String fb_id);

    void logAccountLoggedIn(BIContext context, String userName, String email, String fb_id);

    void logBrokenContent(Gfycat gfycat, MediaType mediaType);

    void logForbidden(Gfycat gfycat, MediaType mediaType);
}
