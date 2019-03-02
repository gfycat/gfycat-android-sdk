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

package com.gfycat.core.downloading;

import android.content.Context;

import com.gfycat.core.FeedIdentifier;
import com.gfycat.core.PublicFeedIdentifier;
import com.gfycat.core.gfycatapi.pojo.Gfycat;
import com.gfycat.core.gfycatapi.pojo.GfycatCategoriesList;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;

/**
 * Responsible for downloading Gfycat items.
 */
public interface FeedManager {

    /**
     * Request categories list from cache. If none found, network request will be performed.
     * <p>
     * If found in cache but expired - will return cached one and perform network request for updates.
     * <p>
     * So, onNext may be called more than once!!!
     *
     * @return Returns {@link GfycatCategoriesList} observable.
     */
    Observable<GfycatCategoriesList> getCategories();

    /**
     * Subscribe for Gfycat changes in local DB.
     * <p>
     * !!! Important: You should NOT forget to unsubscribe, otherwise memory leak will occur.
     *
     * @param feedIdentifier - to subscribe for.
     * @return Returns {@link FeedData} flowable.
     */
    Flowable<FeedData> observeGfycats(FeedIdentifier feedIdentifier);


    /**
     * See {@link #observeGfycats(FeedIdentifier)}.
     * <p>
     * Deprecation reason: Context not required anymore.
     * Target deprecation version: 0.9.0
     */
    @Deprecated
    Flowable<FeedData> observeGfycats(Context context, FeedIdentifier feedIdentifier);

    /**
     * Perform a network request of Gfycat list for a specified {@param feedIdentifier}.
     * {@link FeedData} containing this request result will be returned via {@link #observeGfycats(Context, FeedIdentifier)} observable.
     * <p>
     *
     * @param feedIdentifier - to get gfycats for.
     * @return Returns {@link Completable} calling onError in case of failure or onComplete in case of success.
     */
    Completable getGfycats(FeedIdentifier feedIdentifier);

    /**
     * Perform a network request of new Gfycats for a specified {@param feedDescription}.
     * {@link FeedData} containing this request result will be returned via {@link #observeGfycats(Context, FeedIdentifier)} observable.
     * <p>
     * NOTE: This works only with {@link FeedDescription} obtained via {@link PublicFeedIdentifier#trending()}.
     *
     * @param feedDescription from the latest {@link FeedData} received with {@link #observeGfycats(Context, FeedIdentifier)}
     * @return Returns {@link Completable} calling onError in case of failure or onComplete in case of success.
     */
    Completable getNewGfycats(FeedDescription feedDescription);

    /**
     * Perform a network request of more Gfycats for corresponding feed. Will download Gfycats and save to local DB if needed.
     * <p>
     * {@link FeedData} containing this request result will be returned via {@link #observeGfycats(Context, FeedIdentifier)} observable.
     *
     * @param feedDescription from latest {@link FeedData} received with {@link #observeGfycats(Context, FeedIdentifier)}.
     * @return Returns {@link Completable} calling onError in case of failure or onComplete in case of success
     */
    Completable getMoreGfycats(FeedDescription feedDescription);

    /**
     * Look for a Gfycat in cache. Request one from network, if not found.
     *
     * @return Returns {@link Single} emitting a Gfycat object or null, if not found neither in cache nor during network request.
     */
    Single<Gfycat> getGfycat(String gfyId);

    /**
     * Indicates that there are no [more] results for requested search.
     */
    class NoSearchResultException extends RuntimeException {
    }

    /**
     * Indicate that there is an internal unhandled error inside Gfycat SDK.
     */
    class InternalGfycatException extends RuntimeException {
        InternalGfycatException(String errorMessage) {
            super("Unknown error message: " + errorMessage);
        }
    }
}