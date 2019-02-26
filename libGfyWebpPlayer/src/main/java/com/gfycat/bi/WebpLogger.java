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

import com.gfycat.core.bi.analytics.BILogger;
import com.gfycat.core.gfycatapi.pojo.Gfycat;
import com.gfycat.core.storage.MediaType;

/**
 * Created by anton on 2/7/17.
 */

public interface WebpLogger extends BILogger {
    String BAD_CONTENT_EVENT = "BadContent";

    void logBadContent(Gfycat gfycat, MediaType mediaType);
}
