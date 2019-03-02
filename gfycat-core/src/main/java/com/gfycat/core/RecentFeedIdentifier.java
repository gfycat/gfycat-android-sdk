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

import android.net.Uri;

/**
 * Created by dgoliy on 03.30.17
 */

public class RecentFeedIdentifier implements FeedIdentifier {

    public static final String RECENT = "recent";
    public static final FeedType RECENT_FEED_TYPE = () -> RECENT;
    private static final RecentFeedIdentifier INSTANCE = new RecentFeedIdentifier();

    private RecentFeedIdentifier() {}

    @Override
    public FeedType getType() {
        return RECENT_FEED_TYPE;
    }

    @Override
    public String toName() {
        return RECENT;
    }

    @Override
    public String toUniqueIdentifier() {
        return new Uri.Builder()
                .scheme(RECENT_FEED_TYPE.getName())
                .authority(RECENT)
                .build()
                .toString();
    }

    public static FeedIdentifier recent() {
        return INSTANCE;
    }
}
