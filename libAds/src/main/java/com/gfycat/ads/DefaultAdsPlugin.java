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

import com.facebook.ads.NativeAdsManager;
import com.gfycat.ads.remote.FbAdsConfig;
import com.gfycat.ads.remote.FbAdsConfigAPI;
import com.gfycat.core.ads.AdsDisabledException;
import com.gfycat.core.ads.AdsLoader;
import com.gfycat.core.ads.AdsPlacement;
import com.gfycat.core.ads.AdsPlugin;
import com.gfycat.core.ads.NoSuitableAdsConfigException;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.ReplaySubject;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by dgoliy on 9/12/17.
 */

public class DefaultAdsPlugin implements AdsPlugin {
    private final String BASE_ADS_CONFIG_URL = "https://mobileconfiguration.blob.core.windows.net/";

    private final int ADS_PRELOAD_COUNT = 1;

    private Context context;
    private FbAdsConfigAPI adsConfigAPI;
    private ReplaySubject<FbAdPlacementIdStorage> subject = ReplaySubject.create();

    private Map<String, DefaultAdsLoader> adsLoaders = new HashMap<>();

    public void initialize(@NonNull Context context) {

        this.context = context;

        adsConfigAPI = new Retrofit.Builder()
                .client(new OkHttpClient.Builder().build())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl(BASE_ADS_CONFIG_URL)
                .build()
                .create(FbAdsConfigAPI.class);

        adsConfigAPI.getConfig()
                .subscribeOn(Schedulers.io())
                .subscribe(this::processConfig);
    }

    private void processConfig(@NonNull FbAdsConfig config) {
        if (subject.hasComplete()) {
            return;
        }
        FbAdPlacementIdStorage storage = new FbAdPlacementIdStorage();
        try {
            storage.init(context, config);
            subject.onNext(storage);
            subject.onComplete();
        } catch (AdsDisabledException | NoSuitableAdsConfigException e) {
            subject.onError(e);
        }

        if (!subject.hasComplete() && !subject.hasThrowable()) {
            subject.onError(new IllegalStateException("Ads are disabled for this application"));
        }
    }

    @Override
    public Single<AdsLoader> getAdsLoader(AdsPlacement placement) {
        return subject.singleOrError().map(storage -> {
            DefaultAdsLoader defaultAdsLoader = adsLoaders.get(placement.getPlacementName());
            if (defaultAdsLoader == null) {
                NativeAdsManager nativeAdsManager = new NativeAdsManager(context, storage.getAdId(placement), ADS_PRELOAD_COUNT);
                defaultAdsLoader = new DefaultAdsLoader(context, nativeAdsManager);
                adsLoaders.put(placement.getPlacementName(), defaultAdsLoader);
            }
            return defaultAdsLoader;
        });
    }
}
