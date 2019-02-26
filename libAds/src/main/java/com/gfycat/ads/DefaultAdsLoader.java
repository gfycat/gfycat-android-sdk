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

package com.gfycat.ads;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.ads.AdChoicesView;
import com.facebook.ads.AdError;
import com.facebook.ads.MediaView;
import com.facebook.ads.NativeAd;
import com.facebook.ads.NativeAdsManager;
import com.gfycat.common.utils.Logging;
import com.gfycat.core.ads.AdsLoadedListener;
import com.gfycat.core.ads.AdsLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dgoliy on 9/22/17.
 */

public class DefaultAdsLoader implements AdsLoader {
    private static String TAG = "DefaultAdsLoader";

    private final int MAX_RETRIES = 3;
    private volatile int retryCount = 0;

    private boolean isLastLoadingFailed = false;

    private NativeAdsManager nativeAdsManager;
    private Context context;
    private List<AdsLoadedListener> adsLoadedListenerList;

    public DefaultAdsLoader(@NonNull Context context, @NonNull NativeAdsManager adsManager) {
        this.nativeAdsManager = adsManager;
        this.context = context;
        this.adsLoadedListenerList = new ArrayList<>();
    }

    @Override
    public void load() {
        nativeAdsManager.setListener(new NativeAdsManager.Listener() {
            @Override
            public void onAdsLoaded() {
                Logging.d(TAG, "onAdsLoaded");
                for (AdsLoadedListener l : adsLoadedListenerList) l.onAdsLoaded();
                retryCount = 0;
                isLastLoadingFailed = false;
            }

            @Override
            public void onAdError(AdError adError) {
                Logging.d(TAG, "onAdError : " + adError.getErrorCode() + " - " + adError.getErrorMessage());
                if (retryCount < MAX_RETRIES) {
                    retryCount++;
                    nativeAdsManager.loadAds();
                } else {
                    for (AdsLoadedListener l : adsLoadedListenerList) l.onAdsLoadError();
                    retryCount = 0;
                    isLastLoadingFailed = true;
                }
            }
        });
        nativeAdsManager.loadAds();
    }

    @Override
    public View createNativeAdView() {
        NativeAd ad = nativeAdsManager.nextNativeAd();
        if (ad == null) return null;

        ad.unregisterView();

        View adView = LayoutInflater.from(context).inflate(R.layout.ad_native, null, false);

        MediaView adMedia = adView.findViewById(R.id.native_ad_media);
        TextView adBody = adView.findViewById(R.id.native_ad_body);
        Button adCallToAction = adView.findViewById(R.id.native_ad_call_to_action);
        TextView adTitle = adView.findViewById(R.id.native_ad_title);
        ImageView adIcon = adView.findViewById(R.id.native_ad_icon);

        adMedia.setNativeAd(ad);
        adTitle.setText(ad.getAdTitle());
        adBody.setText(ad.getAdBody());
        adCallToAction.setText(ad.getAdCallToAction());
        NativeAd.downloadAndDisplayImage(ad.getAdIcon(), adIcon);

        LinearLayout adChoicesContainer = adView.findViewById(R.id.native_ad_choices_container);
        AdChoicesView adChoicesView = new AdChoicesView(context, ad, true);
        adChoicesContainer.addView(adChoicesView);

        List<View> clickableViews = new ArrayList<>();
        clickableViews.add(adCallToAction);
        clickableViews.add(adMedia);

        ad.registerViewForInteraction(adView, clickableViews);

        return adView;
    }

    @Override
    public int getNativeAdViewHeight() {
        return context == null ? 0 : context.getResources().getDimensionPixelSize(R.dimen.native_ad_height);
    }

    @Override
    public boolean hasFailedToLoadAds() {
        return isLastLoadingFailed;
    }

    @Override
    public void addListener(AdsLoadedListener listener) {
        adsLoadedListenerList.add(listener);
    }

    @Override
    public void removeListener(AdsLoadedListener listener) {
        adsLoadedListenerList.remove(listener);
    }
}
