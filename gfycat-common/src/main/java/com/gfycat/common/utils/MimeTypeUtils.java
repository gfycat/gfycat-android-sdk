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

package com.gfycat.common.utils;

/**
 * Created by dekalo on 14.10.16.
 */

public class MimeTypeUtils {
    public static final String MP4_VIDEO_MIME_TYPE = "video/mp4";
    public static final String MP4_EXT = "mp4";
    public static final String GIF_MIME_TYPE = "image/gif";
    public static final String GIF_EXT = "gif";
    public static final String WEBP_MIME_TYPE = "image/webp";
    public static final String WEBP_EXT = "webp";
    public static final String JPG_MIME_TYPE = "image/jpg";
    public static final String JPG_EXT = "jpg";
    public static final String PNG_MIME_TYPE = "image/png";
    public static final String PNG_EXT = "png";
    public static final String TEXT_MIME_TYPE = "plain/text";

    public static String buildFileName(String name, String extension) {
        return name + "." + extension;
    }
}
