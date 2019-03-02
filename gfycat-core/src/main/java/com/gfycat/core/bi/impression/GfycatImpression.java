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

package com.gfycat.core.bi.impression;

import android.content.Context;

import com.gfycat.common.BILogcat;
import com.gfycat.common.utils.Utils;
import com.gfycat.core.GfyPrivate;
import com.gfycat.core.GfycatApplicationInfo;
import com.gfycat.core.bi.CommonKeys;

import java.util.Map;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * Created by Madden on 06.02.2017.
 */

public class GfycatImpression {
    private final String LOG_TAG = "GfycatImpression";

    private final String deviceID;
    private final String appId;
    private final String clientId;
    private final String versionName;
    private final PixelAPI pixelApi;

    private static GfycatImpression instance;

    public static synchronized void initialize(Context context, GfycatApplicationInfo gfycatApplicationInfo) {
        if (instance != null) throw new IllegalStateException("Initialize called twice.");
        instance = new GfycatImpression(context, gfycatApplicationInfo);
    }

    private static GfycatImpression get() {
        if (instance == null) throw new IllegalStateException("Initialize not called.");
        return instance;
    }

    private GfycatImpression(Context context, GfycatApplicationInfo gfycatApplicationInfo) {
        this.deviceID = Utils.getDeviceID(context);
        this.appId = Utils.getApplicationId(context);
        this.clientId = gfycatApplicationInfo.clientId;
        this.versionName = Utils.getVersionName(context);
        this.pixelApi = buildPixelApi();
    }

    private PixelAPI buildPixelApi() {
        return new Retrofit.Builder()
                .client(new OkHttpClient())
                .baseUrl("https://px." + GfyPrivate.get().getDomainName())
                .build()
                .create(PixelAPI.class);
    }

    public static void logImpression(ImpressionInfo impressionInfo) {
        impressionInfo.ensureInfoSetCorrectly();
        get().track(impressionInfo.asParams());
    }

    private void track(Map<String, String> params) {
        addPixelParams(params);
        BILogcat.log(LOG_TAG, "impression", params);
        pixelApi.pixelCall(params, PixelAPI.DUMMY_BODY).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
            }
        });
    }

    private void addPixelParams(Map<String, String> params) {
        params.put(CommonKeys.APP_ID_KEY, appId);
        params.put(CommonKeys.CLIENT_ID_KEY, clientId);
        params.put(CommonKeys.VER_KEY, versionName);
        params.put(CommonKeys.USER_ID_KEY, deviceID);
    }

    /**
     * For test purpose only.
     */
    static void deInitialize() {
        instance = null;
    }
}
