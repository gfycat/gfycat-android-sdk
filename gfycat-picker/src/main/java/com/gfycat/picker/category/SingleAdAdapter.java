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

package com.gfycat.picker.category;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.gfycat.common.ChainedException;
import com.gfycat.common.Recyclable;
import com.gfycat.common.utils.Assertions;
import com.gfycat.common.utils.Logging;
import com.gfycat.core.AdsManager;
import com.gfycat.core.ads.AdsDisabledException;
import com.gfycat.core.ads.AdsLoadedListener;
import com.gfycat.core.ads.AdsLoader;
import com.gfycat.core.ads.AdsPlacement;
import com.gfycat.core.ads.AdsPlugin;
import com.gfycat.core.ads.NoSuitableAdsConfigException;

import java.util.Set;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Ads loading is done in asynchronous way with next steps:
 * * loadAdsFactory from {@link AdsManager}
 * * getAdsLoader from {@link AdsPlugin}
 * * loadAds from {@link AdsLoader}
 * Fail on any step will cause to not showing ad.
 */
public class SingleAdAdapter extends RecyclerView.Adapter<AdViewHolder> {

    private static final String LOG_TAG = "SingleAdAdapter";

    private final AdsPlacement adsPlacement;
    private final Set<Recyclable> weakRecyclableItemsForRelease;
    private boolean isStaggeredLayoutManager;

    // states of data
    // isAvailable for loading data, shouldBeVisible related to screen size
    private boolean isAvailable = false; // ads should be visible if no error and no other conditions
    private boolean failedToLoad = false; // ads should be visible but failed to load
    private boolean isLoading = false; // ads are loading
    private boolean shouldBeVisible = false; // ads should not be visible because of no room on the screen

    // represents current state of adapter, should be computed from isAvailable and shouldBeVisible.
    private boolean isShown = false;

    private AdsLoader adsLoader;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    private AdsLoadedListener adsLoadedListener = new AdsLoadedListener() {
        @Override
        public void onAdsLoadError() {
            isLoading = false;
            failedToLoad = true;
            triggerNotifications();
        }

        @Override
        public void onAdsLoaded() {
            isLoading = false;
            if (isShown) notifyItemChanged(0);
        }
    };

    public SingleAdAdapter(AdsPlacement adsPlacement, @NonNull Set<Recyclable> weakRecyclableItemsForRelease) {
        this(adsPlacement, weakRecyclableItemsForRelease, false);
    }

    public SingleAdAdapter(AdsPlacement adsPlacement, @NonNull Set<Recyclable> weakRecyclableItemsForRelease, boolean isStaggeredLayoutManager) {
        this.adsPlacement = adsPlacement;
        this.weakRecyclableItemsForRelease = weakRecyclableItemsForRelease;
        this.isStaggeredLayoutManager = isStaggeredLayoutManager;

        loadAdsFactory();
    }

    /**
     * Initial ads loading step.
     */
    private void loadAdsFactory() {
        compositeDisposable.add(
                AdsManager.get()
                        .subscribe(
                                this::getAdsLoader,
                                throwable -> Logging.d(LOG_TAG, throwable, "No ads available.")));
    }

    /**
     * AdsManager loaded. Show ad placeholder and proceed to loading add.
     */
    private void getAdsLoader(AdsPlugin adsPlugin) {
        compositeDisposable.add(
                adsPlugin.getAdsLoader(adsPlacement)
                        .subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                this::loadAds,
                                this::canNotCreateLoader));

    }

    /**
     * AdsLoader loading failed, hide ad report issue.
     */
    private void canNotCreateLoader(Throwable throwable) {
        isAvailable = false;
        triggerNotifications();
        if (throwable instanceof AdsDisabledException) {
            Log.e(LOG_TAG, "Ads disabled for your application.");
        } else if (throwable instanceof NoSuitableAdsConfigException) {
            Log.e(LOG_TAG, "No suitable ads configuration for your application.");
        } else {
            Assertions.fail(new ChainedException(LOG_TAG + "::canNotCreateLoader()", throwable));
        }
    }

    /**
     * Final step to load ads.
     */
    private void loadAds(AdsLoader adsLoader) {
        this.adsLoader = adsLoader;
        isAvailable = true;
        isLoading = true;
        this.adsLoader.addListener(adsLoadedListener);
        this.adsLoader.load();
        triggerNotifications();
    }

    @Override
    public AdViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int adViewHeight = adsLoader == null ? ViewGroup.LayoutParams.WRAP_CONTENT : adsLoader.getNativeAdViewHeight();
        AdViewHolder holder = AdViewHolder.create(parent.getContext(), isStaggeredLayoutManager, adViewHeight);
        weakRecyclableItemsForRelease.add(holder);
        return holder;
    }

    @Override
    public void onBindViewHolder(AdViewHolder holder, int position) {
        if (adsLoader != null && !isLoading) {
            View adView = adsLoader.createNativeAdView();
            if (adView != null) holder.bind(adView);
        }
    }

    public void setAdsVisible(boolean visible) {
        shouldBeVisible = visible;
        triggerNotifications();
    }

    private void triggerNotifications() {

        boolean shouldBeShown = shouldBeVisible && isAvailable && !failedToLoad && !adsLoader.hasFailedToLoadAds();

        if (shouldBeShown && !isShown) {
            isShown = true;
            notifyItemInserted(0);
        } else if (!shouldBeShown && isShown) {
            isShown = false;
            notifyItemRemoved(0);
        } else {
            // state not changed
        }
    }

    @Override
    public int getItemCount() {
        return isShown ? 1 : 0;
    }

    public void release() {
        compositeDisposable.dispose();
        if (adsLoader != null) {
            adsLoader.removeListener(adsLoadedListener);
        }
    }
}
