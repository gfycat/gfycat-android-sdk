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

import android.content.Context;

import com.gfycat.core.downloading.FeedData;
import com.gfycat.core.downloading.FeedDescription;
import com.gfycat.core.downloading.FeedManager;
import com.gfycat.core.gfycatapi.pojo.Gfycat;
import com.gfycat.core.gfycatapi.pojo.GfycatCategoriesList;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.subjects.ReplaySubject;

/**
 * Created by dgoliy on 4/11/17.
 */

public class FeedManagerAsyncWrapper implements FeedManager {
    private ReplaySubject<FeedManager> subject = ReplaySubject.create();

    public void init(FeedManager feedManager) {
        if (subject.hasComplete()) {
            return;
        }
        subject.onNext(feedManager);
        subject.onComplete();
    }

    public Single<FeedManager> observeFeedManager() {
        return subject.singleOrError();
    }

    @Override
    public Observable<GfycatCategoriesList> getCategories() {
        return subject.singleOrError().flatMapObservable(FeedManager::getCategories);
    }

    @Override
    public Flowable<FeedData> observeGfycats(FeedIdentifier feedIdentifier) {
        return subject.singleOrError().toFlowable().flatMap(feedManager -> feedManager.observeGfycats(feedIdentifier));
    }

    @Override
    public Flowable<FeedData> observeGfycats(Context context, FeedIdentifier feedIdentifier) {
        return subject
                .singleOrError()
                .toFlowable()
                .flatMap(feedManager -> feedManager.observeGfycats(context, feedIdentifier));
    }

    @Override
    public Completable getGfycats(FeedIdentifier feedIdentifier) {
        return subject.singleOrError().flatMapCompletable(feedManager -> feedManager.getGfycats(feedIdentifier));
    }

    @Override
    public Completable getNewGfycats(FeedDescription feedDescription) {
        return subject.singleOrError().flatMapCompletable(feedManager -> feedManager.getNewGfycats(feedDescription));
    }

    @Override
    public Completable getMoreGfycats(FeedDescription feedDescription) {
        return subject.singleOrError().flatMapCompletable(feedManager -> feedManager.getMoreGfycats(feedDescription));
    }

    @Override
    public Single<Gfycat> getGfycat(String gfyId) {
        return subject.singleOrError().flatMap(feedManager -> feedManager.getGfycat(gfyId));
    }
}
