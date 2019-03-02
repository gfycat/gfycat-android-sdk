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

import android.text.TextUtils;

import com.gfycat.common.Func1;
import com.gfycat.common.utils.Logging;
import com.gfycat.common.utils.MimeTypeUtils;
import com.gfycat.common.utils.Utils;
import com.gfycat.core.GfyPrivate;
import com.gfycat.core.gfycatapi.pojo.Gfycat;

/**
 * Gfycat SDK provided media types.
 */
public enum MediaType {

    /**
     * Mobile optimized MP4 format.
     */
    MP4("MP4", MimeTypeUtils.MP4_VIDEO_MIME_TYPE, MimeTypeUtils.MP4_EXT, "https://thumbs.{domainName}/{gfyName}-mobile.mp4", -1L, true, Gfycat::getMp4MobileUrl),

    /**
     * GIF format with maximum 1 MB size.
     */
    GIF1("GIF1", MimeTypeUtils.GIF_MIME_TYPE, MimeTypeUtils.GIF_EXT, "https://thumbs.{domainName}/{gfyName}-max-1mb.gif ", Utils.MB, false, Gfycat::getGif1mbUrl),

    /**
     * GIF format with maximum 2 MB size.
     */
    GIF2("GIF2", MimeTypeUtils.GIF_MIME_TYPE, MimeTypeUtils.GIF_EXT, "https://thumbs.{domainName}/{gfyName}-small.gif", 2 * Utils.MB, false, Gfycat::getGif2mbUrl),

    /**
     * GIF format with maximum 5 MB size.
     */
    GIF5("GIF5", MimeTypeUtils.GIF_MIME_TYPE, MimeTypeUtils.GIF_EXT, "https://thumbs.{domainName}/{gfyName}-size_restricted.gif", 5 * Utils.MB, false, Gfycat::getGif5mbUrl),

    /**
     * Animated WEBP image.
     */
    WEBP("WEBP", MimeTypeUtils.WEBP_MIME_TYPE, MimeTypeUtils.WEBP_EXT, "https://thumbs.{domainName}/{gfyName}.webp", -1L, false, Gfycat::getWebPUrl),

    /**
     * Transparent poster image for gfycat.
     */
    TRANSPARENT_POSTER("TRANSPARENT_POSTER", MimeTypeUtils.PNG_MIME_TYPE, MimeTypeUtils.PNG_EXT, "https://thumbs.{domainName}/{gfyName}-transparent.png", -1L, false, Gfycat::getPosterPngUrl),

    /**
     * Poster image for gfycat.
     */
    POSTER("POSTER", MimeTypeUtils.JPG_MIME_TYPE, MimeTypeUtils.JPG_EXT, "https://thumbs.{domainName}/{gfyName}-mobile.jpg", -1L, false, Gfycat::getPosterMobileUrl);

    private static final String LOG_TAG = "MediaType";

    private static final String DOMAIN_NAME_PART = "{domainName}";
    private static final String GFY_NAME_PART = "{gfyName}";

    private final String name; // protect from proguard
    private final String mimeType;
    private final String extension;
    private final String urlTemplate;
    private final long sizeHint;
    private final boolean isVideo;
    private final Func1<Gfycat, String> getUrlFromGfycat;

    MediaType(String name, String mimeType, String extension, String urlTemplate, long sizeHint, boolean isVideo, Func1<Gfycat, String> getFromGfycat) {
        this.name = name;
        this.mimeType = mimeType;
        this.extension = extension;
        this.urlTemplate = urlTemplate;
        this.sizeHint = sizeHint;
        this.isVideo = isVideo;
        this.getUrlFromGfycat = getFromGfycat;
    }

    /**
     * @return Returns {@link MediaType} URL to {@link Gfycat} provided.
     */
    public String getUrl(Gfycat gfycat) {
        return getUrlSafe(getUrlFromGfycat.call(gfycat), gfycat.getGfyName());
    }

    private String getUrlSafe(String gfycatUrl, String gfyName) {
        if (TextUtils.isEmpty(gfycatUrl) && !TextUtils.isEmpty(urlTemplate)) {
            Logging.e(LOG_TAG, "Using hardcoded url for " + mimeType + " of " + gfyName);
            return urlTemplate.replace(DOMAIN_NAME_PART, GfyPrivate.get().getDomainName()).replace(GFY_NAME_PART, gfyName);
        }
        return gfycatUrl;
    }

    /**
     * File type possible size hint in bytes.
     *
     * @return Returns max size byte count, or -1 if not available.
     */
    public long getSizeHint() {
        return sizeHint;
    }

    /**
     * @return Returns true if this type is video, false otherwise.
     */
    public boolean isVideo() {
        return isVideo;
    }

    /**
     * Same as {@link Enum#name()} but safe from proguard.
     */
    public String getName() {
        return name;
    }

    String getVideoStorageId(Gfycat gfycat) {
        return gfycat.getGfyId() + '_' + name.toLowerCase() + '.' + extension;
    }

    /**
     * @return Returns MediaType's MimeType
     */
    public String getMimeType() {
        return mimeType;
    }

    public static MediaType posterType(Gfycat gfycat) {
        return gfycat.hasTransparency() ? MediaType.TRANSPARENT_POSTER : MediaType.POSTER;
    }
}
