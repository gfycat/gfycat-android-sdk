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

package com.gfycat.ads.remote;

import com.google.gson.annotations.SerializedName;

/**
 * Created by dgoliy on 9/26/17.
 */

public class FbAdsConfig {
    public static class FbAdApplication {
        public String application;
        @SerializedName("is_enabled")
        public boolean isEnabled;
        public FbAdPlacementId[] ads;
    }

    public static class FbAdPlacementId {
        public String type;
        public String adId;
        public int width;
        public int height;
    }

    public String name;
    public FbAdApplication[] ids;
    public int version;
    public long updated;
}
