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

package com.gfycat.core.contentmanagement;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;

import com.gfycat.common.utils.Logging;
import com.gfycat.common.utils.Sugar;
import com.gfycat.core.creation.CreationAPI;
import com.gfycat.core.creation.pojo.UpdateGfycat;
import com.gfycat.core.db.GfycatFeedCache;
import com.gfycat.core.gfycatapi.GfycatAPI;
import com.gfycat.core.gfycatapi.pojo.Gfycat;
import com.gfycat.core.gfycatapi.pojo.NSFWUpdateRequest;
import com.gfycat.core.gfycatapi.pojo.PublishedUpdateRequest;

import java.util.List;

import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;

/**
 * Created by dekalo on 19.09.16.
 */
public class UserOwnedContentManagerImpl implements UserOwnedContentManager {

    private static final String LOG_TAG = "UserOwnedContentManagerImpl";

    private final GfycatAPI gfycatApi;
    private final CreationAPI creationApi;
    private final GfycatFeedCache gfycatFeedCache;
    private final Handler handler;

    public UserOwnedContentManagerImpl(GfycatAPI gfycatApi, CreationAPI creationApi, GfycatFeedCache gfycatFeedCache) {
        this.gfycatApi = gfycatApi;
        this.creationApi = creationApi;
        this.gfycatFeedCache = gfycatFeedCache;
        this.handler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void delete(Gfycat gfycat) {
        Runnable deleteLocally = () -> AsyncTask.execute(() -> gfycatFeedCache.markDeleted(gfycat, true));
        Runnable undoDeleteLocally = () -> AsyncTask.execute(() -> gfycatFeedCache.markDeleted(gfycat, false));
        Runnable networkOperation = () -> gfycatApi.delete(gfycat.getGfyId())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        responseBodyResponse -> {
                            if (!responseBodyResponse.isSuccessful() && responseBodyResponse.code() != 404)
                                undoDeleteLocally.run();
                        },
                        throwable -> undoDeleteLocally.run());

        networkOperation.run();
        deleteLocally.run();
    }

    public void markPublishedLocally(String gfyId, boolean published) {
        Sugar.doIfNotNull(
                gfycatFeedCache.getGfycat(gfyId),
                gfycat -> gfycatFeedCache.markPublished(gfycat, published));

    }

    private Runnable performPublished(Gfycat gfycat, long undoDuration, boolean publish) {
        Runnable privateLocally = () -> AsyncTask.execute(() -> gfycatFeedCache.markPublished(gfycat, publish));
        Runnable undoPrivateLocally = () -> AsyncTask.execute(() -> gfycatFeedCache.markPublished(gfycat, !publish));
        Runnable networkOperation = () -> gfycatApi.updatePublishedState(gfycat.getGfyId(), PublishedUpdateRequest.makePublic(publish))
                .subscribeOn(Schedulers.io())
                .subscribe(
                        responseBodyResponse -> {
                        },
                        throwable -> Logging.d(LOG_TAG, "makePrivate error", throwable));

        return perform(undoDuration, privateLocally, undoPrivateLocally, networkOperation);
    }

    @Override
    public Runnable makePrivate(Gfycat gfycat, long undoDuration) {
        return performPublished(gfycat, undoDuration, false);
    }

    @Override
    public Runnable makePublic(Gfycat gfycat, long undoDuration) {
        return performPublished(gfycat, undoDuration, true);
    }

    private Runnable performNSFW(Gfycat gfycat, long undoDuration, boolean nsfw) {
        Runnable privateLocally = () -> AsyncTask.execute(() -> gfycatFeedCache.markNsfw(gfycat, nsfw));
        Runnable undoPrivateLocally = () -> AsyncTask.execute(() -> gfycatFeedCache.markNsfw(gfycat, !nsfw));
        Runnable networkOperation = () -> gfycatApi.updateNSFW(gfycat.getGfyId(), NSFWUpdateRequest.notSafeForWork(nsfw))
                .subscribeOn(Schedulers.io())
                .subscribe(
                        responseBodyResponse -> {
                        },
                        throwable -> Logging.d(LOG_TAG, "makePrivate error", throwable));

        return perform(undoDuration, privateLocally, undoPrivateLocally, networkOperation);
    }

    @Override
    public Runnable suitableForAllAges(Gfycat gfycat, long undoDuration) {
        return performNSFW(gfycat, undoDuration, false);
    }

    @Override
    public Runnable markAs18Only(Gfycat gfycat, long undoDuration) {
        return performNSFW(gfycat, undoDuration, true);
    }

    @Override
    public Single<Boolean> updatePublishState(Gfycat gfycat, boolean published) {
        return creationApi
                .updatePublishState(gfycat.getGfyId(), UpdateGfycat.publishState(published))
                .map(Response::isSuccessful)
                .singleOrError();
    }

    @Override
    public Single<Boolean> updateDescription(Gfycat gfycat, String description) {
        return creationApi
                .updateDescription(gfycat.getGfyId(), UpdateGfycat.description(description))
                .map(Response::isSuccessful)
                .singleOrError();
    }

    @Override
    public Single<Boolean> updateTitle(Gfycat gfycat, String title) {
        return creationApi
                .updateTitle(gfycat.getGfyId(), UpdateGfycat.title(title))
                .map(Response::isSuccessful)
                .singleOrError();
    }

    @Override
    public Single<Boolean> addTags(Gfycat gfycat, List<String> tags) {
        return creationApi
                .addTags(gfycat.getGfyId(), UpdateGfycat.tags(tags))
                .map(Response::isSuccessful)
                .singleOrError();
    }

    private Runnable perform(long undoTime, Runnable localComplete, Runnable localUndo, Runnable remoteComplete) {
        handler.postDelayed(remoteComplete, undoTime);
        localComplete.run();

        return () -> {
            handler.removeCallbacks(remoteComplete);
            localUndo.run();
        };
    }
}
