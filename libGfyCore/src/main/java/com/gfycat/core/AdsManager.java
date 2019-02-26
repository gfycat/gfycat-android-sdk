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
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.gfycat.common.Function;
import com.gfycat.common.utils.Logging;
import com.gfycat.core.ads.AdsPlugin;

import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.subjects.AsyncSubject;

/**
 * Created by dgoliy on 9/12/17.
 */

public class AdsManager {

    private static final String LOG_TAG = "AdsManager";

    private static final String META_ADS_FACTORY_CLASS_NAME = "ads_plugin_class_name";

    public static final float MIN_VIEW_TO_SCREEN_HEIGHT_RATIO = 0.6f;

    private static AsyncSubject<AdsPlugin> adsFactorySubject = AsyncSubject.create();

    /**
     * If SDK is configured without AdsPlugin - {@link AdsUnavailableException} will be triggered.
     * And as adsFactorySubject has no subscribers yet - {@link io.reactivex.exceptions.UndeliverableException} will be
     * generated and reported to global Rx Error Handler.
     * <p>
     * It is not intended behavior and to hide this only way see new is to add fake subscriber for adsFactorySubject,
     * so {@link io.reactivex.exceptions.UndeliverableException} will not be generated.
     */
    private static final Disposable HIDE_UNDELIVERABLE_EXCEPTION = adsFactorySubject.subscribe(Function::ignore, Function::ignore);

    static void initialize(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            ApplicationInfo appInfo = packageManager.getApplicationInfo(
                    context.getPackageName(), PackageManager.GET_META_DATA);
            if (appInfo != null && appInfo.metaData != null) {
                String className = appInfo.metaData.getString(META_ADS_FACTORY_CLASS_NAME);
                if (className != null) {
                    Class<?> clazz = Class.forName(className);
                    AdsPlugin adsPlugin = (AdsPlugin) clazz.newInstance();
                    adsPlugin.initialize(context.getApplicationContext());
                    Logging.d(LOG_TAG, "AdsPlugin was provided, class = ", adsPlugin.getClass().getSimpleName());
                    adsFactorySubject.onNext(adsPlugin);
                    adsFactorySubject.onComplete();
                }
            }
        } catch (PackageManager.NameNotFoundException | IllegalAccessException | ClassNotFoundException | java.lang.InstantiationException e) {
            // nothing
        }
        if (!adsFactorySubject.hasComplete()) {
            Logging.d(LOG_TAG, "AdsPlugin was not provided");
            adsFactorySubject.onError(new AdsUnavailableException());
        }
    }

    public static Single<AdsPlugin> get() {
        return adsFactorySubject.singleOrError();
    }

    private static class AdsUnavailableException extends Throwable {
    }

    static void deInitialize() {
        adsFactorySubject = AsyncSubject.create();
    }
}
