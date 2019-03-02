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
import android.support.annotation.NonNull;
import android.support.v4.util.Pair;
import android.text.TextUtils;

import com.gfycat.common.Function;
import com.gfycat.common.utils.Assertions;
import com.gfycat.common.utils.Logging;
import com.gfycat.common.utils.Sugar;
import com.gfycat.common.utils.Utils;
import com.gfycat.core.FeedIdentifier;
import com.gfycat.core.RecentFeedIdentifier;
import com.gfycat.core.db.CloseMode;
import com.gfycat.core.db.FeedCacheUriContract;
import com.gfycat.core.db.GfycatFeedCache;
import com.gfycat.core.gfycatapi.GfycatAPI;
import com.gfycat.core.gfycatapi.pojo.Gfycat;
import com.gfycat.core.gfycatapi.pojo.GfycatCategoriesList;
import com.gfycat.core.gfycatapi.pojo.GfycatCategory;
import com.gfycat.core.gfycatapi.pojo.GfycatList;
import com.gfycat.core.gfycatapi.pojo.GfycatRecentCategory;
import com.gfycat.core.gfycatapi.pojo.OneGfyItem;

import java.nio.channels.IllegalSelectorException;
import java.util.Collections;
import java.util.Iterator;
import java.util.Locale;
import java.util.concurrent.Callable;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;

/**
 * Created by dekalo on 20.01.16.
 */
public class FeedManagerImpl implements FeedManager {

    private static final String LOG_TAG = "FeedManagerImpl";

    private static final int DEFAULT_GET_NEW_ITEMS_VALUE = 1;
    public static final int DEFAULT_GET_ITEMS_VALUE = 100;
    public static final int DEFAULT_GET_MORE_ITEMS_VALUE = DEFAULT_GET_ITEMS_VALUE;

    private static final int RECENT_GFYCATS_LIMIT = 100;

    private BehaviorSubject<GfycatCategoriesList> ongoingCategoriesRequest;
    private final CategoriesCache categoriesCache;

    private final GfycatFeedCache gfycatFeedCache;
    private final GfycatAPI gfycatApi;

    private GetGfycatsObservableFactory getGfycatsObservableFactory = new GetGfycatsObservableFactoryMap();
    private GetMoreGfycatsObservableFactory getMoreGfycatsObservableFactory = new GetMoreGfycatsObservableFactoryMap();

    public FeedManagerImpl(CategoriesCache categoriesCache, GfycatAPI gfycatApi, GfycatFeedCache gfycatFeedCache) {
        this.categoriesCache = categoriesCache;
        this.gfycatApi = gfycatApi;
        this.gfycatFeedCache = gfycatFeedCache;
    }

    @Override
    public Single<Gfycat> getGfycat(String gfyId) {
        Assertions.assertNotUIThread(IllegalSelectorException::new);
        return Single.create(subscriber -> {
            Gfycat gfycat = gfycatFeedCache.getGfycat(gfyId);
            if (gfycat != null) {
                subscriber.onSuccess(gfycat);
            } else {
                Disposable localDisposable = gfycatApi.getOneGfycatItemObservable(gfyId)
                        .map(OneGfyItem::getGfyItem)
                        .subscribe(subscriber::onSuccess, subscriber::tryOnError);

                subscriber.setCancellable(localDisposable::dispose);
            }
        });
    }

    @Override
    public Observable<GfycatCategoriesList> getCategories() {
        if (ongoingCategoriesRequest == null) {
            ongoingCategoriesRequest = BehaviorSubject.create();
            doLoadCategories()
                    .subscribeOn(Schedulers.io())
                    .doAfterTerminate(() -> ongoingCategoriesRequest = null)
                    .subscribe(
                            item -> ongoingCategoriesRequest.onNext(item),
                            error -> ongoingCategoriesRequest.onError(error),
                            () -> ongoingCategoriesRequest.onComplete());
        }

        return ongoingCategoriesRequest;
    }

    public Single<GfycatRecentCategory> getRecentCategory() {
        return Single.create((SingleOnSubscribe<GfycatRecentCategory>) subscriber -> {
            subscriber.onSuccess(new GfycatRecentCategory(
                    RecentFeedIdentifier.RECENT_FEED_TYPE.getName(),
                    getFeedDataFromDB(gfycatFeedCache, RecentFeedIdentifier.recent()).getGfycats()));
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    private Observable<GfycatCategoriesList> doLoadCategories() {
        return Observable.create(subscriber -> {
            Pair<GfycatCategoriesList, Boolean> cacheEntry = categoriesCache.get();
            if (cacheEntry != null) {
                subscriber.onNext(cacheEntry.first);
            }

            if (cacheEntry == null || cacheEntry.second) {
                Disposable networkDisposable = gfycatApi.getCategories(Locale.getDefault().getLanguage())
                        .map(this::filterInvalidCategories)
                        .subscribe(
                                categoriesList -> {
                                    if (categoriesCache.update(categoriesList)) {
                                        subscriber.onNext(categoriesList);
                                    }
                                    subscriber.onComplete();
                                },
                                subscriber::onError);

                subscriber.setCancellable(networkDisposable::dispose);
            } else {
                subscriber.onComplete();
            }
        });
    }

    private GfycatCategoriesList filterInvalidCategories(GfycatCategoriesList gfycatCategoriesList) {
        Iterator<GfycatCategory> listIterator = gfycatCategoriesList.getTags().iterator();

        while (listIterator.hasNext()) {
            GfycatCategory category = listIterator.next();
            if (!category.isValid()) {
                Logging.c(LOG_TAG, "Server returned invalid category in categories response category = ", category);
                listIterator.remove();
            }
        }

        return gfycatCategoriesList;
    }

    @Override
    public Flowable<FeedData> observeGfycats(Context context, FeedIdentifier identifier) {
        return observeGfycats(identifier);
    }

    public Flowable<FeedData> observeGfycats(FeedIdentifier identifier) {
        return Flowable.merge(Flowable.just(identifier), FeedCacheUriContract.observeChanges(identifier))
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.computation())
                .doOnNext(dbIdentifier -> Assertions.assertEquals(dbIdentifier, identifier, IllegalAccessException::new))
                .filter(identifier::equals)
                .map(feedIdentifier -> getFeedDataFromDB(gfycatFeedCache, feedIdentifier));
    }

    @Override
    public Completable getGfycats(FeedIdentifier feedIdentifier) {
        return getGfycatsObservableFactory
                .create(gfycatApi, feedIdentifier, DEFAULT_GET_ITEMS_VALUE)
                .flatMap(new GenericErrorCheck())
                .doOnNext(new InsertOrReplaceAction(gfycatFeedCache, feedIdentifier, CloseMode.Auto))
                .ignoreElements()
                .subscribeOn(Schedulers.io());
    }

    @Override
    public Completable getNewGfycats(FeedDescription feedDescription) {
        return getMoreGfycats(feedDescription, DEFAULT_GET_NEW_ITEMS_VALUE);
    }

    @Override
    public Completable getMoreGfycats(FeedDescription feedDescription) {
        return getMoreGfycats(feedDescription, DEFAULT_GET_MORE_ITEMS_VALUE);
    }

    public void prefetchCategories() {
        if (categoriesCache.get() == null) {
            Logging.d(LOG_TAG, "prefetchCategories() start");
            Disposable[] subscription = new Disposable[1];
            subscription[0] = getCategories().subscribe(gfycatCategoriesList -> Sugar.doIfNotNull(subscription[0], Disposable::dispose), Function::ignore);
        } else {
            Logging.d(LOG_TAG, "prefetchCategories() not needed");
        }
    }

    public Observable<Void> createFeedIfNotExist(FeedIdentifier feedIdentifier, Gfycat gfycat, String digest, CloseMode closeMode) {
        return Observable.fromCallable(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                InsertOrReplaceAction action = new InsertOrReplaceAction(gfycatFeedCache, feedIdentifier, closeMode);
                action.accept(new GfycatList(gfycat, digest));
                return null;
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    private Completable getMoreGfycats(FeedDescription feedDescription, int count) {
        return getMoreGfycatsObservableFactory
                .create(gfycatApi, feedDescription.getIdentifier(), feedDescription.getDigest(), count)
                .flatMap(new GenericErrorCheck())
                .doOnNext(new UpdateFeedAction(gfycatFeedCache, feedDescription.getIdentifier(), feedDescription.getDigest()))
                .ignoreElements()
                .subscribeOn(Schedulers.io());
    }

    public Completable addRecentGfycat(@NonNull Gfycat gfycat) {
        return Completable.create(completableSubscriber -> {
            gfycatFeedCache.insertFeed(
                    RecentFeedIdentifier.recent(),
                    new GfycatList(Collections.singletonList(gfycat)),
                    CloseMode.Close,
                    true /* append */);
            completableSubscriber.onComplete();
        })
                .doOnComplete(() -> Logging.d(LOG_TAG, gfycat.getGfyId() + " added to Recent category"))
                .doAfterTerminate(this::ensureRecentFitLimit)
                .ambWith(ensureRecentFitLimit())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    private Completable ensureRecentFitLimit() {
        return getRecentCategory()
                .observeOn(Schedulers.io())
                .map(GfycatCategory::getGfycats)
                .subscribeOn(Schedulers.io())
                .doOnSuccess(recentGfycats -> {
                    int deletedCount = 0;
                    while (recentGfycats.size() - deletedCount > RECENT_GFYCATS_LIMIT) {
                        Gfycat itemToDelete = recentGfycats.get(recentGfycats.size() - 1 - deletedCount);
                        removeFromRecentAsync(itemToDelete);
                        deletedCount++;
                    }
                    if (deletedCount > 0) {
                        Logging.d(LOG_TAG, "deleted " + deletedCount + " outdated recent gfycats");
                    }
                }).ignoreElement();
    }

    private void removeFromRecentAsync(Gfycat gfycat) {
        Completable.fromAction(() -> gfycatFeedCache.removeFromRecent(gfycat))
                .subscribeOn(Schedulers.io())
                .subscribe();
    }

    private static FeedData getFeedDataFromDB(GfycatFeedCache gfycatFeedCache, FeedIdentifier feedIdentifier) {
        return gfycatFeedCache.getFeedData(feedIdentifier);
    }

    public void dropFeed(FeedIdentifier feedIdentifier) {
        gfycatFeedCache.delete(feedIdentifier);
    }

    private static class UpdateFeedAction implements Consumer<GfycatList> {

        private final GfycatFeedCache gfycatFeedCache;
        private final FeedIdentifier identifier;
        private final String previousDigest;

        private UpdateFeedAction(GfycatFeedCache gfycatFeedCache, FeedIdentifier identifier, String previousDigest) {
            this.gfycatFeedCache = gfycatFeedCache;
            this.identifier = identifier;
            this.previousDigest = previousDigest;
        }

        @Override
        public void accept(GfycatList gfycatList) {
            Assertions.assertNotUIThread(IllegalAccessException::new);
            if (Utils.isEmpty(gfycatList.getGfycats()) && Utils.isEmpty(gfycatList.getNewGfycats())) {
                gfycatFeedCache.closeFeed(identifier, previousDigest);
            } else {
                gfycatFeedCache
                        .updateFeed(
                                identifier,
                                previousDigest,
                                gfycatList);
            }
        }
    }

    private static class InsertOrReplaceAction implements Consumer<GfycatList> {

        private final GfycatFeedCache gfycatFeedCache;
        private final FeedIdentifier identifier;
        private final CloseMode closeMode;

        private InsertOrReplaceAction(GfycatFeedCache gfycatFeedCache, FeedIdentifier identifier, CloseMode closeMode) {
            this.identifier = identifier;
            this.gfycatFeedCache = gfycatFeedCache;
            this.closeMode = closeMode;
        }

        @Override
        public void accept(GfycatList gfycatList) {
            gfycatFeedCache
                    .insertFeed(
                            identifier,
                            gfycatList,
                            closeMode);
        }
    }

    private static class GenericErrorCheck implements io.reactivex.functions.Function<GfycatList, Observable<GfycatList>> {
        @Override
        public Observable<GfycatList> apply(GfycatList searchResult) {
            if (!TextUtils.isEmpty(searchResult.getErrorMessage())) {
                return Observable.error(new FeedManager.InternalGfycatException(searchResult.getErrorMessage()));
            }
            return Observable.just(searchResult);
        }
    }

    class NetworkErrors {
        static final String NO_SEARCH_RESULT = "No search results";
    }
}