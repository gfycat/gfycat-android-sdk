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

import com.gfycat.common.utils.Utils;
import com.gfycat.core.gfycatapi.pojo.Gfycat;

import java.util.Arrays;

/**
 * Provides possibility to parameterize feeds with specific content, such as aspect ratio, length or content rating.
 * <p>
 * Supported feed types are:
 * {@link FeedIdentifier.Type#SEARCH},
 * {@link FeedIdentifier.Type#SOUND_SEARCH},
 * {@link FeedIdentifier.Type#SOUND_TRENDING}
 */
public class ParameterizedFeedIdentifierBuilder {

    private static FeedType[] SUPPORTED_FEED_TYPES = new FeedType[]{FeedIdentifier.Type.SEARCH, FeedIdentifier.Type.SOUND_SEARCH, FeedIdentifier.Type.SOUND_TRENDING};

    private final Uri.Builder baseUriBuilder;

    public static boolean supportsParametrization(FeedIdentifier feedIdentifier) {
        return Utils.contains(SUPPORTED_FEED_TYPES, feedIdentifier) && feedIdentifier instanceof PublicFeedIdentifier;
    }

    /**
     * Supported feed types for now are
     * {@link FeedIdentifier.Type#SEARCH},
     * {@link FeedIdentifier.Type#SOUND_SEARCH},
     * {@link FeedIdentifier.Type#SOUND_TRENDING}
     *
     * @param baseFeedIdentifier - should be one of types listed above.
     */
    public ParameterizedFeedIdentifierBuilder(FeedIdentifier baseFeedIdentifier) {
        if (!Utils.contains(SUPPORTED_FEED_TYPES, baseFeedIdentifier.getType())) {
            throw new IllegalArgumentException("Please provide feed type with one of next types: " + Arrays.toString(SUPPORTED_FEED_TYPES));
        }

        if (!(baseFeedIdentifier instanceof PublicFeedIdentifier)) {
            throw new IllegalArgumentException("ParameterizedFeedIdentifierBuilder not supports custom feed identifier.");
        }

        baseUriBuilder = ((PublicFeedIdentifier) baseFeedIdentifier).getUri().buildUpon();
    }

    /**
     * Feed will contain gfycats with aspect ration above minAspectRatio
     * <p>
     * Note that @minAspectRatio should be in range
     * from {@link FeedIdentifierParameters#MIN_ASPECT_RATIO} to {@link FeedIdentifierParameters#MAX_ASPECT_RATIO},
     * if not it will be converted to nearest acceptable.
     */
    public ParameterizedFeedIdentifierBuilder withMinAspectRatio(float minAspectRatio) {
        if (minAspectRatio < FeedIdentifierParameters.MIN_ASPECT_RATIO_VALUE)
            minAspectRatio = FeedIdentifierParameters.MIN_ASPECT_RATIO_VALUE;
        if (minAspectRatio > FeedIdentifierParameters.MAX_ASPECT_RATIO_VALUE)
            minAspectRatio = FeedIdentifierParameters.MAX_ASPECT_RATIO_VALUE;

        baseUriBuilder.appendQueryParameter(
                FeedIdentifierParameters.MIN_ASPECT_RATIO,
                FeedIdentifierParameters.formatAspect(minAspectRatio));
        return this;
    }

    /**
     * Feed will contain gfycats with aspect ration below maxAspectRatio.
     * <p>
     * Note that @maxAspectRatio should be in range
     * from {@link FeedIdentifierParameters#MIN_ASPECT_RATIO_VALUE} to {@link FeedIdentifierParameters#MAX_ASPECT_RATIO},
     * if not it will be converted to nearest acceptable.
     */
    public ParameterizedFeedIdentifierBuilder withMaxAspectRatio(float maxAspectRatio) {
        if (maxAspectRatio < FeedIdentifierParameters.MIN_ASPECT_RATIO_VALUE)
            maxAspectRatio = FeedIdentifierParameters.MIN_ASPECT_RATIO_VALUE;
        if (maxAspectRatio > FeedIdentifierParameters.MAX_ASPECT_RATIO_VALUE)
            maxAspectRatio = FeedIdentifierParameters.MAX_ASPECT_RATIO_VALUE;

        baseUriBuilder.appendQueryParameter(
                FeedIdentifierParameters.MAX_ASPECT_RATIO,
                FeedIdentifierParameters.formatAspect(maxAspectRatio));
        return this;
    }

    /**
     * Feed will contain gfycats with length of GIF above minLength.
     * <p>
     * Note that @minLength should be in range
     * from {@link FeedIdentifierParameters#MIN_LENGTH_VALUE} to {@link FeedIdentifierParameters#MAX_LENGTH_VALUE},
     * if not it will be converted to nearest acceptable.
     */
    public ParameterizedFeedIdentifierBuilder withMinLength(float minLength) {

        if (minLength < FeedIdentifierParameters.MIN_LENGTH_VALUE)
            minLength = FeedIdentifierParameters.MIN_LENGTH_VALUE;
        if (minLength > FeedIdentifierParameters.MAX_LENGTH_VALUE)
            minLength = FeedIdentifierParameters.MAX_LENGTH_VALUE;

        baseUriBuilder.appendQueryParameter(
                FeedIdentifierParameters.MIN_LENGTH,
                FeedIdentifierParameters.formatLength(minLength));
        return this;
    }

    /**
     * Feed will contain gfycats with length of GIF below maxLength.
     * <p>
     * Note that @maxLength should be in range
     * from {@link FeedIdentifierParameters#MIN_LENGTH_VALUE} to {@link FeedIdentifierParameters#MAX_LENGTH_VALUE},
     * if not it will be converted to nearest acceptable.
     */
    public ParameterizedFeedIdentifierBuilder withMaxLength(float maxLength) {

        if (maxLength < FeedIdentifierParameters.MIN_LENGTH_VALUE)
            maxLength = FeedIdentifierParameters.MIN_LENGTH_VALUE;
        if (maxLength > FeedIdentifierParameters.MAX_LENGTH_VALUE)
            maxLength = FeedIdentifierParameters.MAX_LENGTH_VALUE;

        baseUriBuilder.appendQueryParameter(
                FeedIdentifierParameters.MAX_LENGTH,
                FeedIdentifierParameters.formatLength(maxLength));
        return this;
    }

    /**
     * Feed will contain gfycats with content rating below or equal to provided contentRating.
     */
    public ParameterizedFeedIdentifierBuilder withContentRating(Gfycat.ContentRating contentRating) {
        baseUriBuilder.appendQueryParameter(
                FeedIdentifierParameters.CONTENT_RATING,
                contentRating.urlEncodedValue);
        return this;
    }

    /**
     * Build new {@link FeedIdentifier} with applied changes.
     */
    public FeedIdentifier build() {
        return new PublicFeedIdentifier(baseUriBuilder.build());
    }
}
