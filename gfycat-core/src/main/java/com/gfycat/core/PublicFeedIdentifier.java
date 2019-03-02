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
import android.text.TextUtils;

import com.gfycat.common.utils.Assertions;
import com.gfycat.common.utils.Utils;
import com.gfycat.core.gfycatapi.pojo.Gfycat;

/**
 * Public available feed identifiers available here.
 */
public class PublicFeedIdentifier implements FeedIdentifier {

    static final String PUBLIC_IDENTIFIER_SCHEME = "public";

    static final String TRENDING_ENDPOINT = "/gfycats/trending";
    static final String SEARCH_ENDPOINT = "/gfycats/search";
    static final String SOUND_ENDPOINT = "/sound";
    static final String SOUND_SEARCH_ENDPOINT = "/sound/search";
    static final String REACTIONS_ENDPOINT = "/reactions/populated";
    static final String ME_ENDPOINT = "/me/gfycats";

    // to not bother URI parsing we remove username from path and added it as parameter
    static final String USER_ENDPOINT = "/users/gfycats";

    private static final FeedIdentifier TRENDING = new PublicFeedIdentifier(new Uri.Builder()
            .scheme(PUBLIC_IDENTIFIER_SCHEME)
            .path(TRENDING_ENDPOINT)
            .build());

    private static final FeedIdentifier SOUND_TRENDING = new PublicFeedIdentifier(new Uri.Builder()
            .scheme(PUBLIC_IDENTIFIER_SCHEME)
            .path(SOUND_ENDPOINT)
            .build());

    private static final FeedIdentifier ME = new PublicFeedIdentifier(new Uri.Builder()
            .scheme(PUBLIC_IDENTIFIER_SCHEME)
            .path(ME_ENDPOINT)
            .build());

    /* package */
    static FeedIdentifier create(String uniqueFeedIdentifier) {
        return new PublicFeedIdentifier(Uri.parse(uniqueFeedIdentifier));
    }


    private static FeedType resolveFeedType(Uri uri) {

        String uriPath = uri.getPath();

        if (SOUND_ENDPOINT.equals(uriPath)) {
            return Type.SOUND_TRENDING;
        } else if (SOUND_SEARCH_ENDPOINT.equals(uriPath) && !TextUtils.isEmpty(uri.getQueryParameter(FeedIdentifierParameters.SEARCH_TEXT_PARAMETER))) {
            return Type.SOUND_SEARCH;
        } else if (SEARCH_ENDPOINT.equals(uriPath) && !TextUtils.isEmpty(uri.getQueryParameter(FeedIdentifierParameters.SEARCH_TEXT_PARAMETER))) {
            return Type.SEARCH;
        } else if (REACTIONS_ENDPOINT.equals(uriPath) && !TextUtils.isEmpty(uri.getQueryParameter(FeedIdentifierParameters.TAG_PARAMETER))) {
            return Type.REACTIONS;
        } else if (TRENDING_ENDPOINT.equals(uriPath) && !TextUtils.isEmpty(uri.getQueryParameter(FeedIdentifierParameters.TAG_PARAMETER))) {
            return Type.TAG;
        } else if (TRENDING_ENDPOINT.equals(uriPath)) {
            return Type.TRENDING;
        } else if (ME_ENDPOINT.equals(uriPath)) {
            return Type.ME;
        } else if (USER_ENDPOINT.equals(uriPath) && !TextUtils.isEmpty(uri.getQueryParameter(FeedIdentifierParameters.USERNAME_PARAMETER))) {
            return Type.USER;
        }


        Assertions.fail(new IllegalStateException("Feed resolution failed from uri = " + uri));
        return null;
    }

    /**
     * See {@link FeedIdentifier.Type#ME}
     */
    public static FeedIdentifier myGfycats() {
        return ME;
    }

    /**
     * See {@link FeedIdentifier.Type#TRENDING}
     */
    public static FeedIdentifier trending() {
        return TRENDING;
    }

    /**
     * See {@link FeedIdentifier.Type#SOUND_TRENDING}
     */
    public static FeedIdentifier soundTrending() {
        return SOUND_TRENDING;
    }

    /**
     * See {@link FeedIdentifier.Type#SINGLE}
     *
     * @param gfyId from {@link Gfycat#getGfyId()}.
     */
    public static FeedIdentifier fromSingleItem(String gfyId) {
        return new SingleFeedIdentifier(gfyId);
    }

    /**
     * See {@link FeedIdentifier.Type#SOUND_SEARCH}
     */
    public static FeedIdentifier fromSoundSearch(String searchQuery) {
        return new PublicFeedIdentifier(new Uri.Builder()
                .scheme(PUBLIC_IDENTIFIER_SCHEME)
                .path(SOUND_SEARCH_ENDPOINT)
                .appendQueryParameter(FeedIdentifierParameters.SEARCH_TEXT_PARAMETER, searchQuery)
                .appendQueryParameter(FeedIdentifierParameters.NAME, searchQuery)
                .build());
    }

    /**
     * See {@link FeedIdentifier.Type#TAG}
     */
    public static FeedIdentifier fromTagName(String tagName) {
        return new PublicFeedIdentifier(new Uri.Builder()
                .scheme(PUBLIC_IDENTIFIER_SCHEME)
                .path(TRENDING_ENDPOINT)
                .appendQueryParameter(FeedIdentifierParameters.TAG_PARAMETER, tagName)
                .appendQueryParameter(FeedIdentifierParameters.NAME, tagName)
                .build());
    }

    /**
     * See {@link FeedIdentifier.Type#REACTIONS}
     */
    public static FeedIdentifier fromReaction(String reactionName) {
        return new PublicFeedIdentifier(new Uri.Builder()
                .scheme(PUBLIC_IDENTIFIER_SCHEME)
                .path(REACTIONS_ENDPOINT)
                .appendQueryParameter(FeedIdentifierParameters.TAG_PARAMETER, reactionName)
                .appendQueryParameter(FeedIdentifierParameters.NAME, reactionName)
                .build());
    }

    /**
     * See See {@link FeedIdentifier.Type#USER}
     */
    public static FeedIdentifier fromUsername(String username) {
        return new PublicFeedIdentifier(new Uri.Builder()
                .scheme(PUBLIC_IDENTIFIER_SCHEME)
                .path(USER_ENDPOINT)
                .appendQueryParameter(FeedIdentifierParameters.NAME, username)
                .appendQueryParameter(FeedIdentifierParameters.USERNAME_PARAMETER, username)
                .build());
    }

    /**
     * See {@link FeedIdentifier.Type#SEARCH}
     */
    public static FeedIdentifier fromSearch(String searchQuery) {
        return new PublicFeedIdentifier(new Uri.Builder()
                .scheme(PUBLIC_IDENTIFIER_SCHEME)
                .path(SEARCH_ENDPOINT)
                .appendQueryParameter(FeedIdentifierParameters.SEARCH_TEXT_PARAMETER, searchQuery)
                .appendQueryParameter(FeedIdentifierParameters.NAME, searchQuery)
                .build());
    }

    private final FeedType feedType;
    private final Uri uri;

    /* package */ PublicFeedIdentifier(Uri uri) {

        if (!PUBLIC_IDENTIFIER_SCHEME.equals(uri.getScheme())) {
            throw new IllegalStateException("Unuspported uri = " + uri.toString());
        }

        this.uri = uri;
        this.feedType = resolveFeedType(uri);

        if (feedType == null) {
            throw new IllegalArgumentException("Feed uri = " + uri + " not supported.");
        }
    }

    /* package */ Uri getUri() {
        return uri;
    }

    @Override
    public FeedType getType() {
        return feedType;
    }

    @Override
    public String toName() {
        String nameParameter = uri.getQueryParameter(FeedIdentifierParameters.NAME);
        return nameParameter != null ? nameParameter : "";
    }

    @Override
    public String toUniqueIdentifier() {
        return uri.toString();
    }

    /**
     * Returns parameters value previously set via {@link ParameterizedFeedIdentifierBuilder}.
     */
    public String getParameter(String parameterName) {
        return uri.getQueryParameter(parameterName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PublicFeedIdentifier that = (PublicFeedIdentifier) o;
        return Utils.equals(uri, that.uri);
    }

    @Override
    public int hashCode() {
        return Utils.hash(uri);
    }
}
