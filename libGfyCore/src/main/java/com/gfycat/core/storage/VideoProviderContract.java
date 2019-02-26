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

package com.gfycat.core.storage;

import android.net.Uri;

import com.gfycat.core.ApplicationIDHelperLib;
import com.gfycat.common.utils.Assertions;

/**
 * Provide possibility to share File stream, showing real file path.
 * <p/>
 * /mp4/gfyId - to get stream of gfyId.mp4 file.
 * <p/>
 * Created by dekalo on 22.09.15.
 */
public class VideoProviderContract {

    static final String SCHEMA = "content";

    static final String AUTHORITY = ApplicationIDHelperLib.getAppId() + ".videoprovider";

    public enum SharingType {
        CACHED,
        REMOTE
    }

    /**
     * Generate uri for client.
     */
    public static Uri build(String gfyId, SharingType sharingType, MediaType mediaType) {
        Assertions.assertNotEmpty(gfyId, () -> new IllegalArgumentException("gfyId is empty"));
        return new Uri.Builder().scheme(SCHEMA).authority(AUTHORITY).appendPath(sharingType.name()).appendPath(mediaType.getName()).appendPath(gfyId).build();
    }
}
