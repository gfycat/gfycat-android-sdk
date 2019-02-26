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

package com.gfycat.core.gfycatapi.pojo;

import android.graphics.Color;
import android.text.TextUtils;

import com.gfycat.common.EPS;
import com.gfycat.common.utils.Assertions;
import com.gfycat.common.utils.Utils;
import com.gfycat.core.GfyPrivate;
import com.gfycat.core.NetworkConfig;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * Contains gfycat identity data, sizes and urls to all the types of files stored on Gfycat servers
 */
public class Gfycat implements Serializable {

    /**
     * Projection type of 360 gfycats.
     */
    public enum ProjectionType {
        Plain,
        Equirectangular("equirectangular"),
        Facebookcube("facebook-cube");

        private final String[] values;

        ProjectionType(String... values) {
            this.values = values;
        }

        ProjectionType() {
            this("", null);
        }

        public boolean isEqual(String projectionType) {
            for (String value : values) {
                if (Utils.equals(value, projectionType)) {
                    return true;
                }
            }
            return false;
        }
    }

    public enum ContentRating {
        R("R"), PG13("PG13", "PG-13"), PG("PG"), G("G");

        /**
         * Protect from proguard.
         */
        public final String urlEncodedValue;
        public final String responseValue;

        ContentRating(String value) {
            this(value, value);
        }

        ContentRating(String urlEncodedValue, String responseValue) {
            this.urlEncodedValue = urlEncodedValue;
            this.responseValue = responseValue;
        }

        public static ContentRating fromResponesValue(String contentRating) {
            for (ContentRating rating : values()) {
                if (Utils.equals(rating.responseValue, contentRating)) {
                    return rating;
                }
            }
            return null;
        }
    }

    private String gfyId;
    private String gfyName;
    private String gfyNumber;
    private int width;
    private int height;
    private String userName;
    private String createDate;
    private int views;
    private String title;
    private String description;
    private String projectionType;
    private int nsfw;
    private int published;
    private String avgColor;
    private boolean hasTransparency;
    private boolean hasAudio;
    private List<String> tags = Collections.emptyList();
    private float frameRate;
    private int numFrames;
    private String rating;

    private int mp4Size;
    private int webmSize;

    private String posterUrl;
    private String pngPosterUrl;
    private String mobilePosterUrl;
    private String miniPosterUrl;
    private String thumb100PosterUrl;

    private String mp4Url;
    private String mobileUrl;
    private String miniUrl;
    private String gifUrl;
    private String webmUrl;
    private String webpUrl;

    private String gif100px;
    private String max1mbGif;
    private String max2mbGif;
    private String max5mbGif;

    /**
     * Color of first frame.
     *
     * @return Returns average color (int value) of the first frame.
     */
    public int getAvgColorInt() {
        if (avgColor != null)
            return Color.parseColor(avgColor);

        return Color.WHITE;
    }

    private String getUrlInternal(String url) {
        if (!NetworkConfig.DEFAULT_DOMAIN.equals(GfyPrivate.get().getDomainName()) && !TextUtils.isEmpty(url)) {
            return url.replace(NetworkConfig.DEFAULT_DOMAIN, GfyPrivate.get().getDomainName());
        }
        return url;
    }

    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }

    public String getPosterUrl() {
        return getUrlInternal(posterUrl);
    }

    public void setPosterPngUrl(String posterPngUrl) {
        this.pngPosterUrl = posterPngUrl;
    }

    public String getPosterPngUrl() {
        return getUrlInternal(pngPosterUrl);
    }

    public void setPosterMobileUrl(String posterMobileUrl) {
        this.mobilePosterUrl = posterMobileUrl;
    }

    public String getPosterMobileUrl() {
        return getUrlInternal(mobilePosterUrl);
    }

    public void setPosterMiniUrl(String posterMiniUrl) {
        this.miniPosterUrl = posterMiniUrl;
    }

    public String getPosterMiniUrl() {
        return getUrlInternal(miniPosterUrl);
    }

    public void setPosterThumb100Url(String posterThumb100PosterUrl) {
        this.thumb100PosterUrl = posterThumb100PosterUrl;
    }

    public String getPosterThumb100Url() {
        return getUrlInternal(thumb100PosterUrl);
    }

    /**
     * Url to raw mp4 file.
     */
    public String getDesktopMp4Url() {
        return getUrlInternal(mp4Url);
    }

    public String getWebMUrl() {
        return getUrlInternal(webmUrl);
    }

    public int getWebMSize() {
        return webmSize;
    }

    public void setWebMUrl(String webmUrl) {
        this.webmUrl = webmUrl;
    }

    public void setWebMSize(int webpmSize) {
        this.webmSize = webpmSize;
    }

    public void setMp4MobileUrl(String mobileUrl) {
        this.mobileUrl = mobileUrl;
    }

    /**
     * Url to mobile friendly mp4 file.
     */
    public String getMp4MobileUrl() {
        return getUrlInternal(mobileUrl);
    }

    public void setWebPUrl(String webpUrl) {
        this.webpUrl = webpUrl;
    }

    /**
     * Url to animated webp file.
     */
    public String getWebPUrl() {
        return getUrlInternal(webpUrl);
    }

    public void setGif100pxUrl(String gif100pxUrl) {
        this.gif100px = gif100pxUrl;
    }

    /**
     * Url to 100px GIF.
     */
    public String getGif100pxUrl() {
        return getUrlInternal(gif100px);
    }

    public void setGifLargeUrl(String gifUrl) {
        this.gifUrl = gifUrl;
    }

    /**
     * Url to Large GIF.
     */
    public String getGifLargeUrl() {
        return getUrlInternal(gifUrl);
    }

    public void setGif5mbUrl(String max5mbGifUrl) {
        this.max5mbGif = max5mbGifUrl;
    }

    /**
     * Url to GIF of 5 MB maximum size.
     */
    public String getGif5mbUrl() {
        return getUrlInternal(max5mbGif);
    }

    public void setGif2mbUrl(String max2mbGifUrl) {
        this.max2mbGif = max2mbGifUrl;
    }

    /**
     * Url to GIF of 2 MB maximum size.
     */
    public String getGif2mbUrl() {
        return getUrlInternal(max2mbGif);
    }

    public void setGif1mbUrl(String max1mbGifUrl) {
        this.max1mbGif = max1mbGifUrl;
    }

    /**
     * Url to GIF of 1 MB maximum size.
     */
    public String getGif1mbUrl() {
        return getUrlInternal(max1mbGif);
    }

    /**
     * @return Returns average color of first frame.
     */
    public String getAvgColor() {
        return avgColor;
    }

    public void setAvgColor(String avgColor) {
        this.avgColor = avgColor;
    }

    public boolean hasTransparency() {
        return hasTransparency;
    }

    public void setHasTransparency(boolean hasTransparency) {
        this.hasTransparency = hasTransparency;
    }

    public boolean hasAudio() {
        return hasAudio;
    }

    public void setHasAudio(boolean hasAudio) {
        this.hasAudio = hasAudio;
    }

    /**
     * @return Returns unique gfycat identifier (lowercase only).
     */
    public String getGfyId() {
        return gfyId;
    }

    public void setGfyId(String gfyId) {
        this.gfyId = gfyId;
    }

    /**
     * @return Returns gfycat name (upper and lower case).
     */
    public String getGfyName() {
        return gfyName;
    }

    public void setGfyName(String gfyName) {
        this.gfyName = gfyName;
    }

    public String getGfyNumber() {
        return gfyNumber;
    }

    public void setGfyNumber(String gfyNumber) {
        this.gfyNumber = gfyNumber;
    }

    /**
     * @return Returns width of gfycat in pixels
     */
    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    /**
     * @return Returns height of gfycat in pixels.
     */
    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getMp4MiniUrl() {
        return getUrlInternal(miniUrl);
    }

    public void setMp4MiniUrl(String mp4MiniUrl) {
        this.miniUrl = mp4MiniUrl;
    }

    /**
     * Use {@link #getDesktopMp4Url()} instead
     */
    @Deprecated
    public String getMp4Url() {
        return getUrlInternal(mp4Url);
    }

    public void setMp4Url(String mp4Url) {
        this.mp4Url = mp4Url;
    }

    /**
     * @return Returns size of desktop version of mp4 file.
     */
    public int getMp4Size() {
        return mp4Size;
    }

    public void setMp4Size(int mp4Size) {
        this.mp4Size = mp4Size;
    }

    /**
     * @return Returns Gfycat's owner name.
     */
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public long getCreateDateMilliseconds() {
        return Long.valueOf(createDate) * 1000;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public int getViews() {
        return views;
    }

    public void setViews(int views) {
        this.views = views;
    }

    /**
     * @return Returns Gfycat title.
     */
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return Returns Gfycat description.
     */
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return Returns Gfycat associated tags.
     */
    public List<String> getTags() {
        return tags != null ? tags : Collections.emptyList();
    }

    public void setTags(List<String> tags) {
        tags = tags == null ? Collections.emptyList() : tags;
        this.tags = tags;
    }

    public float getFrameRate() {
        return frameRate;
    }

    public void setFrameRate(float frameRate) {
        this.frameRate = frameRate;
    }

    public int getNumFrames() {
        return numFrames;
    }

    public void setNumFrames(int numFrames) {
        this.numFrames = numFrames;
    }

    public void setContentRating(String contentRating) {
        this.rating = contentRating;
    }

    public void setProjectionType(String projectionType) {
        this.projectionType = projectionType;
    }

    /**
     * Use {@link #projectionType()} instead.
     */
    public String getProjectionType() {
        return projectionType;
    }

    /**
     * Only 360 videos
     *
     * @return Returns Gfycat projection type.
     */
    public ProjectionType projectionType() {

        for (ProjectionType type : ProjectionType.values()) {
            if (type.isEqual(projectionType))
                return type;
        }

        Assertions.fail(new UnsupportedOperationException("Unknown projection type + " + projectionType));
        return ProjectionType.Plain;
    }

    /**
     * Returns content rating according to https://en.wikipedia.org/wiki/Motion_Picture_Association_of_America_film_rating_system.
     * See {@link ContentRating} enum here.
     */
    public ContentRating getContentRating() {
        return ContentRating.fromResponesValue(rating);
    }

    /**
     * Returns length of Gfycat in seconds or -1 if unknown.
     */
    public float getLength() {
        if (EPS.isAboutZero(frameRate)) return -1;
        return numFrames / frameRate;
    }

    public boolean isValid() {
        return !TextUtils.isEmpty(gfyId)
                && !TextUtils.isEmpty(gfyName)
                && width > 0 && height > 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Gfycat gfycat = (Gfycat) o;

        if (!Utils.equals(gfyId, gfycat.gfyId)) return false;
        return Utils.equals(gfyName, gfycat.gfyName);
    }

    @Override
    public int hashCode() {
        int result = 0;
        if (gfyId != null) result += gfyId.hashCode();
        if (gfyName != null) result = 31 * result + gfyName.hashCode();
        return result;
    }

    /**
     * Use {@link #isSafeForWork()} instead.
     */
    public int getNsfw() {
        return nsfw;
    }

    @Override
    public String toString() {
        return "Gfycat{" +
                "gfyId='" + gfyId + '\'' +
                ", gfyName='" + gfyName + '\'' +
                ", gfyNumber='" + gfyNumber + '\'' +
                ", width=" + width +
                ", height=" + height +
                ", mp4Url='" + mp4Url + '\'' +
                ", mp4Size=" + mp4Size +
                ", userName='" + userName + '\'' +
                ", createDate='" + createDate + '\'' +
                ", views=" + views +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", projectionType='" + projectionType + '\'' +
                ", nsfw=" + nsfw +
                ", published=" + published +
                ", avgColor='" + avgColor + '\'' +
                ", tags=" + tags +
                ", webmUrl='" + webmUrl + '\'' +
                ", webmSize='" + webmSize + '\'' +
                ", webpUrl='" + webpUrl + '\'' +
                ", posterUrl='" + posterUrl + '\'' +
                '}';
    }

    public void setNsfw(int nsfw) {
        this.nsfw = nsfw;
    }

    /**
     * Use {@link #isPublished()} instead.
     */
    public int getPublished() {
        return published;
    }

    public void setPublished(int published) {
        this.published = published;
    }

    /**
     * Indicates if content is publicly available.
     * Usually used for user owned content.
     *
     * @return Returns true if content is publicly available, false otherwise.
     */
    public boolean isPublished() {
        return published == 1;
    }

    /**
     * Indicates if content is safe for work.
     * <p>
     * This is part of API, however api.gfycat.com should not return any nsfw content for mobile clients.
     *
     * @return Returns true for safe content, false otherwise.
     */
    public boolean isSafeForWork() {
        return nsfw == 0;
    }

    /**
     * @return Returns true if there are associated tags, false otherwise.
     */
    public boolean hasTags() {
        return getTags() != null && !getTags().isEmpty();
    }
}