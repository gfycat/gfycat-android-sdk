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

package com.gfycat.core.bi;

import android.os.Bundle;

import com.gfycat.common.utils.Assertions;
import com.gfycat.core.ApplicationIDHelperLib;

/**
 * Created by anton on 12/12/16.
 */

public class BIContext {
    public static final String KEY = ApplicationIDHelperLib.getAppId() + "bicontext_key";

    public static final String LOCAL_VIDEO_FLOW = "Local video";
    public static final String LINK_URL_FLOW = "Link url";
    public static final String CATEGORY_FLOW = "category";
    public static final String PROFILE_FLOW = "profile";
    public static final String CREATION_PREVIEW_FLOW = "creation_preview";
    public static final String SCREEN_CAPTURE_FLOW = "Screen capture";
    public static final String CREATION_FLOW = "creation";
    public static final String REAR_CAMERA_PREFIX = "rear-";
    public static final String FRONT_CAMERA_PREFIX = "front-";
    public static final String CAMERA_FULLSCREEN_FLOW = "camera-fullscreen";
    public static final String CAMERA_HALFSCREEN_FLOW = "camera-halfscreen";
    public static final String PORTRAIT_POSTFIX = "-portrait";
    public static final String LANDSCAPE_POSTFIX = "-landscape";
    public static final String THIRD_PARTY_FLOW = "third-party";
    public static final String FACEBOOK_FLOW = "facebook";
    public static final String EMAIL_FLOW = "email";
    public static final String GHOST_FLOW = "ghost";
    public static final String PHOTOMOMENTS_FLOW = "photomoments";

    public static final String PLAY_IN_CHAT = "chat";
    public static final String PLAY_IN_SEARCH = "search";
    public static final String PLAY_IN_CATEGORY = "category";


    private static final String FLOW_KEY = "flow_key";

    private final Bundle bundle;

    public BIContext() {
        this(new Bundle());
    }

    public BIContext(String flow) {
        bundle = new Bundle();
        bundle.putString(FLOW_KEY, flow);
    }

    public BIContext(Bundle bundle) {

        if (bundle == null) {
            Assertions.fail(new IllegalArgumentException("Trying to create BIContext with bundle == null."));
            bundle = new Bundle();
        }

        this.bundle = bundle;
    }

    public String getFlow() {
        return bundle.getString(FLOW_KEY);
    }

    public Bundle getBundle() {
        return bundle;
    }
}