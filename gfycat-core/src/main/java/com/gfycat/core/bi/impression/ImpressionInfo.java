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

package com.gfycat.core.bi.impression;

import android.text.TextUtils;

import com.gfycat.common.utils.Assertions;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by dekalo on 27.07.17.
 */

public class ImpressionInfo {

    public static final String CATEGORY_LIST_CONTEXT = "category_list";
    public static final String CATEGORY_CONTEXT = "category";
    public static final String RECENT_CONTEXT = "recent";
    public static final String SEARCH_CONTEXT = "search";
    public static final String PROFILE_CONTEXT = "profile";
    public static final String USER_CONTEXT = "user";
    public static final String CHAT_CONTEXT = "chat";
    public static final String STICKER_LIST_CONTEXT = "sticker_list";
    public static final String SOUND_SEARCH_CONTEXT = "sound_search";
    public static final String SOUND_TRENDING_CONTEXT = "sound_trending";

    public static final String HALF_SCREEN_FLOW = "half";
    public static final String FULL_SCREEN_FLOW = "full";
    public static final String CREATION_SCREEN_FLOW = "creation";

    private static final String GFY_ID_KEY = "gfyid";
    private static final String CONTEXT_KEY = "context";
    private static final String KEYWORD_KEY = "keyword";
    private static final String FLOW_KEY = "flow";

    public static final String KEYWORD_NOT_AVAILABLE = "no-keyword-provided";

    private String gfyId;
    private String context;
    private String keyword;
    private String flow;

    public String getGfyId() {
        return gfyId;
    }

    public ImpressionInfo setGfyId(String gfyId) {
        this.gfyId = gfyId;
        return this;
    }

    public String getContext() {
        return context;
    }

    public ImpressionInfo setContext(String context) {
        this.context = context;
        return this;
    }

    public String getKeyword() {
        return keyword;
    }

    public ImpressionInfo setKeyword(String keyword) {
        this.keyword = keyword;
        return this;
    }

    public String getFlow() {
        return flow;
    }

    public ImpressionInfo setFlow(String flow) {
        this.flow = flow;
        return this;
    }

    public void ensureInfoSetCorrectly() {
        if (TextUtils.isEmpty(gfyId))
            Assertions.fail(new IllegalStateException("gfyId was not provided for " + this));
        if (TextUtils.isEmpty(context))
            Assertions.fail(new IllegalStateException("context was not provided for " + this));
        if (TextUtils.isEmpty(keyword))
            Assertions.fail(new IllegalStateException("keyword was not provided for " + this));
        if (TextUtils.isEmpty(flow))
            Assertions.fail(new IllegalStateException("flow was not provided for " + this));
    }

    public Map<String, String> asParams() {
        Map<String, String> params = new HashMap<>();
        if (!TextUtils.isEmpty(gfyId)) params.put(GFY_ID_KEY, gfyId);
        if (!TextUtils.isEmpty(context)) params.put(CONTEXT_KEY, context);
        if (!TextUtils.isEmpty(keyword)) params.put(KEYWORD_KEY, keyword);
        if (!TextUtils.isEmpty(flow)) params.put(FLOW_KEY, flow);
        return params;
    }

    @Override
    public String toString() {
        return "ImpressionInfo{" +
                "gfyId='" + gfyId + '\'' +
                ", context='" + context + '\'' +
                ", keyword='" + keyword + '\'' +
                ", flow='" + flow + '\'' +
                '}';
    }
}
