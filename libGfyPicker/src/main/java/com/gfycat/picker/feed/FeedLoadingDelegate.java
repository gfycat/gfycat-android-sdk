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

package com.gfycat.picker.feed;

import com.gfycat.common.ChainedException;
import com.gfycat.common.lifecycledelegates.ContextBaseDelegate;
import com.gfycat.common.lifecycledelegates.ContextResolver;
import com.gfycat.common.utils.Assertions;
import com.gfycat.common.utils.Logging;
import com.gfycat.common.utils.Sugar;
import com.gfycat.core.FeedIdentifier;
import com.gfycat.core.GfyCore;
import com.gfycat.core.GfyUtils;
import com.gfycat.core.downloading.FeedData;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;


/**
 * Created by dekalo on 02.11.16.
 */

public class FeedLoadingDelegate extends ContextBaseDelegate implements IFeedLoader {

    private static final String LOG_TAG = "FeedLoadingDelegate";

    private FeedIdentifier targetFeedIdentifier;
    private FeedData feedData;

    private boolean isFirstLoad = true;

    private long lastSuccessRequestMs = -1;
    private boolean hasError = false;

    private Disposable gfycatsSubscription;
    private Disposable initialLoadSubscription;
    private Disposable loadMoreSubscription;

    private List<FeedLoadingListener> onFeedLoadedListeners = new ArrayList<>();
    private boolean feedDataUpdated = false;

    private boolean feedForceReloadingNeeded;

    public FeedLoadingDelegate(ContextResolver contextResolver) {
        super(contextResolver);
    }

    @Override
    public boolean hasError() {
        return hasError;
    }

    @Override
    public void setFeedForceReloadingNeeded(boolean feedForceReloadingNeeded) {
        this.feedForceReloadingNeeded = feedForceReloadingNeeded;
    }

    @Override
    public long lastSuccessRequestMs() {
        return lastSuccessRequestMs;
    }

    @Override
    public void reLoad() {
        Logging.d(LOG_TAG, "reLoad()");
        loadInitialFeed();
    }

    @Override
    public void stopLoad() {
        if (gfycatsSubscription != null) gfycatsSubscription.dispose();
    }

    @Override
    public void loadMore() {
        Logging.d(LOG_TAG, "::loadMore::", "target = ", targetFeedIdentifier, " current = ", feedData == null ? "null" : feedData.getIdentifier(), " current ListenersCount = ", onFeedLoadedListeners.size());

        if (!isStarted()) {
            Assertions.fail(new IllegalStateException("startFeedLoader() called after fragment was stopped"));
            return;
        }

        if (feedData != null && !feedData.isClosed()) {
            onLoadingStarts();
            Sugar.doIfNotNull(loadMoreSubscription, Disposable::dispose);
            loadMoreSubscription = GfyCore.getFeedManager().getMoreGfycats(feedData.getFeedDescription())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            this::onLoadMoreSuccess,
                            this::onLoadMoreError);
        }
    }

    @Override
    public void initialize(FeedIdentifier feedIdentifier, FeedLoadingListener listener) {
        Logging.d(LOG_TAG, "initialize(", feedIdentifier, ")");
        Assertions.assertNotNull(feedIdentifier, NullPointerException::new);
        Assertions.assertNotNull(listener, NullPointerException::new);
        targetFeedIdentifier = feedIdentifier;
        onFeedLoadedListeners.add(listener);
        startFeedLoader();
    }

    @Override
    public void changeFeed(FeedIdentifier newIdentifier) {
        Logging.d(LOG_TAG, "changeFeed(", newIdentifier, ")");
        stopLoad();
        targetFeedIdentifier = newIdentifier;
        startFeedLoader();
    }

    private void subscribeFor(Flowable<FeedData> gfycatsObservable) {
        Sugar.doIfNotNull(gfycatsSubscription, Disposable::dispose);
        gfycatsSubscription = gfycatsObservable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::notifyFeedUpdate, throwable -> Assertions.fail(new ChainedException(throwable)));
    }

    @Override
    public void onStart() {
        super.onStart();
        startFeedLoader();
    }

    private void startFeedLoader() {
        Logging.d(LOG_TAG, "startFeedLoader()");

        if (isDestroyed()) {
            Assertions.fail(new IllegalStateException(LOG_TAG + "::startFeedLoader() called after it was destroyed."));
            return;
        }

        onLoadingStarts();
        subscribeFor(GfyCore.getFeedManager().observeGfycats(getContextResolver().getContext().getApplicationContext(), targetFeedIdentifier));
    }

    private void onLoadSuccess() {
        Logging.d(LOG_TAG, "onLoadSuccess(" + targetFeedIdentifier.toUniqueIdentifier() + ") " + feedData);
        lastSuccessRequestMs = System.currentTimeMillis();
        hasError = false;
    }

    private void onLoadError(Throwable throwable) {
        Logging.d(LOG_TAG, "onLoadError(", throwable, ") ", targetFeedIdentifier.toUniqueIdentifier());
        hasError = true;
        notifyOnError(throwable);
    }

    private void onLoadMoreSuccess() {
        Logging.d(LOG_TAG, "onLoadMoreSuccess() ", targetFeedIdentifier.toUniqueIdentifier());
        lastSuccessRequestMs = System.currentTimeMillis();
        hasError = false;
    }

    private void onLoadMoreError(Throwable throwable) {
        Logging.d(LOG_TAG, "onLoadMoreError(", throwable, ") ", targetFeedIdentifier.toUniqueIdentifier());
        hasError = true;
        notifyOnError(throwable);
    }

    private void onLoadingStarts() {
        Logging.d(LOG_TAG, "onLoadingStarts() ", targetFeedIdentifier.toUniqueIdentifier());
        feedDataUpdated = false;
        notifyOnFeedLoadingStarted();
    }

    private void notifyOnFeedLoadingStarted() {
        Logging.d(LOG_TAG, "::notifyOnFeedLoadingStarted()");

        Assertions.assertUIThread(IllegalStateException::new);
        if (ensureIsInCorrectStateForNotification()) {
            for (FeedLoadingListener onFeedLoadedListener : onFeedLoadedListeners)
                onFeedLoadedListener.onFeedLoadingStarted();
        }
    }

    private void notifyOnError(Throwable throwable) {
        Logging.d(LOG_TAG, throwable, "::notifyOnError()");
        Assertions.assertUIThread(IllegalStateException::new);
        if (ensureIsInCorrectStateForNotification()) {
            for (FeedLoadingListener onFeedLoadedListener : onFeedLoadedListeners)
                onFeedLoadedListener.onError(throwable);
        }
    }

    /**
     * This check handles issues described in for https://github.com/GfycatOrg/AndroidMessengerApp/pull/927
     */
    private boolean ensureIsInCorrectStateForNotification() {
        if (getContextResolver().getContext() == null) {
            Assertions.fail(new IllegalStateException(LOG_TAG + " is in incorrect state " +
                    getContextResolver().getStateForLogging() + " " +
                    "isStarted() = " + isStarted() + " " +
                    "isDestroyed() = " + isDestroyed() + " " +
                    "initialLoadSubscription = " + (initialLoadSubscription == null ? "null" : initialLoadSubscription.isDisposed()) + " " +
                    "gfycatsSubscription = " + (gfycatsSubscription == null ? "null" : gfycatsSubscription.isDisposed()) + " " +
                    "loadMoreSubscription = " + (loadMoreSubscription == null ? "null" : loadMoreSubscription.isDisposed()) + " "));
            return false;
        }
        return true;
    }

    private void notifyFeedUpdate(FeedData newFeedData) {
        Logging.d(LOG_TAG, "::notifyFeedUpdate(", newFeedData, ") ", " feedUpdated = ", feedDataUpdated);
        Assertions.assertUIThread(IllegalStateException::new);

        if (lastSuccessRequestMs < newFeedData.getFeedDescription().getCreationTime()) {
            lastSuccessRequestMs = newFeedData.getFeedDescription().getCreationTime();
        }

        feedDataUpdated = true;
        this.feedData = newFeedData;

        if (feedForceReloadingNeeded) {
            Logging.d(LOG_TAG, "feedForceReloadingNeeded = true, loadInitialFeed.");
            loadInitialFeed();
            feedForceReloadingNeeded = false;
        } else if (feedData.getCount() == 0 && !feedData.isClosed()) {
            // this is first load of content for this identifier
            Logging.d(LOG_TAG, "No content make initial load.");
            loadInitialFeed();
        } else if (isFirstLoad && feedData.getCount() > 0 && GfyUtils.isFeedOutdated(new Date(feedData.getFeedDescription().getCreationTime()))) {
            // this is first load of content on screen, it may be outdated, if so lets refresh initial content.
            Logging.d(LOG_TAG, "Content is outdated, do load content");
            loadInitialFeed();
        }

        if (ensureIsInCorrectStateForNotification()) {
            for (FeedLoadingListener onFeedLoadedListener : onFeedLoadedListeners)
                onFeedLoadedListener.onFeedLoaded(feedData);
        }

        isFirstLoad = false;
    }

    private void loadInitialFeed() {
        Sugar.doIfNotNull(initialLoadSubscription, Disposable::dispose);
        initialLoadSubscription = GfyCore.getFeedManager().getGfycats(targetFeedIdentifier).observeOn(AndroidSchedulers.mainThread()).subscribe(this::onLoadSuccess, this::onLoadError);
    }

    @Override
    public void onStop() {
        super.onStop();
        Sugar.doIfNotNull(loadMoreSubscription, Disposable::dispose);
        Sugar.doIfNotNull(gfycatsSubscription, Disposable::dispose);
        stopLoad();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Sugar.doIfNotNull(initialLoadSubscription, Disposable::dispose);
    }
}
