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

import android.app.Application;
import android.content.Context;
import android.support.v4.content.ContextCompat;

import com.gfycat.common.ChainedException;
import com.gfycat.common.utils.Assertions;
import com.gfycat.common.utils.Logging;
import com.gfycat.core.authentication.AuthenticationAPI;
import com.gfycat.core.authentication.SignUpAPI;
import com.gfycat.core.authentication.TokenAuthenticator;
import com.gfycat.core.authentication.UserAccountManagerImpl;
import com.gfycat.core.bi.analytics.GfycatAnalytics;
import com.gfycat.core.bi.analytics.MetricsEngine;
import com.gfycat.core.bi.corelogger.CoreLogger;
import com.gfycat.core.bi.corelogger.CoreLoggerImpl;
import com.gfycat.core.bi.impression.GfycatImpression;
import com.gfycat.core.contentmanagement.NSFWContentManagerImpl;
import com.gfycat.core.contentmanagement.UserOwnedContentManagerImpl;
import com.gfycat.core.creation.CreationAPI;
import com.gfycat.core.creation.DefaultUploadManager;
import com.gfycat.core.db.GfycatFeedDatabaseCache;
import com.gfycat.core.downloading.CategoriesCache;
import com.gfycat.core.downloading.FeedManagerImpl;
import com.gfycat.core.gfycatapi.GfycatAPI;
import com.gfycat.core.storage.CachedMediaFilesManager;
import com.gfycat.core.storage.DefaultDiskCache;
import com.gfycat.core.storage.DiskCache;
import com.gfycat.disklrucache.DiskLruCache;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import io.reactivex.Completable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Main GfyCore initialization class.
 */
public class GfyCoreInitializer {

    private static final String LOG_TAG = "GfyCoreInitializer";

    private static final int MAX_DOWNLOADING_VIDEOS_COUNT = 2;
    private static boolean initializationPerformed;
    static volatile boolean initializationCompleted;

    /**
     * GfyCore initialization method.
     * <p>
     * This should be called only once for each application session.
     * Add this to {@link Application#onCreate()}
     *
     * @param builder contains required and optional GfyCore initialization params.
     */
    public static synchronized void initialize(GfyCoreInitializationBuilder builder) {
        if (initializationPerformed) {
            Assertions.fail(new IllegalStateException("GfyCoreInitializer.initialize() called more than once."));
        } else {
            // start initialization
            initializationPerformed = true;
            coreInitSingleObservable(builder)
                    .subscribeOn(Schedulers.io())
                    .doOnError(throwable -> Assertions.fail(new ChainedException(throwable)))
                    .subscribe();
        }
    }

    static boolean initializePerformed() {
        return initializationPerformed;
    }

    private static Completable coreInitSingleObservable(GfyCoreInitializationBuilder builder) {
        return Completable.create(subscriber -> {

            Logging.d(LOG_TAG, "initialization start");

            DiskLruCache.setupAssertionsLogger(throwable -> Assertions.fail(new ChainedException(throwable)));

            Queue<File> cacheFolderOptions = builder.getCacheFolder() == null
                    ? collectCacheVariants(builder.getContext())
                    : new LinkedList<>(Collections.singletonList(builder.getCacheFolder()));

            DiskCache diskCache = DefaultDiskCache.initialize(cacheFolderOptions, builder.getCacheSizeOptions());

            String appDomainName = getApplicationDomain(builder.getGfycatApplicationInfo());

            OkHttpClient.Builder videoClientBuilder =
                    new OkHttpClient.Builder()
                            .cache(null)
                            .connectionPool(new ConnectionPool(0, 1, TimeUnit.SECONDS))
                            .protocols(Collections.singletonList(Protocol.HTTP_1_1))
                            .dispatcher(new Dispatcher(Executors.newFixedThreadPool(MAX_DOWNLOADING_VIDEOS_COUNT)));

            AuthenticationAPI authenticationApi = new Retrofit.Builder()
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .client(new OkHttpClient.Builder().addInterceptor(builder.getJsonInterceptor()).build())
                    .baseUrl(NetworkConfig.buildApiUrl(appDomainName))
                    .build()
                    .create(AuthenticationAPI.class);

            TokenAuthenticator authenticator = new TokenAuthenticator(builder.getContext(), builder.getGfycatApplicationInfo(), authenticationApi);

            OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder().authenticator(authenticator).addInterceptor(authenticator).addInterceptor(builder.getJsonInterceptor());
            OkHttpClient client = clientBuilder.build();

            OkHttpClient noAuthClient = new OkHttpClient.Builder().addInterceptor(builder.getMediaInterceptor()).build(); //https://gfycat.atlassian.net/browse/ANDMES-547
            OkHttpClient videoClient = videoClientBuilder.addInterceptor(builder.getMediaInterceptor()).retryOnConnectionFailure(false).build();

            String apiUrl = NetworkConfig.buildApiUrl(appDomainName);

            SignUpAPI signUpApi = buildGeneralApi(apiUrl, client, SignUpAPI.class);
            GfycatAPI gfycatApi = buildGeneralApi(apiUrl, client, GfycatAPI.class);
            CreationAPI creationApi = buildGeneralApi(apiUrl, client, CreationAPI.class);
            NoAuthAPI noAuthApi = buildGeneralApi(apiUrl, noAuthClient, NoAuthAPI.class);

            authenticator.setSignUpAPI(signUpApi);

            GfycatFeedDatabaseCache feedCache = new GfycatFeedDatabaseCache(builder.getContext());

            FeedManagerImpl feedManager = new FeedManagerImpl(new CategoriesCache(builder.getContext()), gfycatApi, feedCache);
            GfyCore.get().initFeedManager(feedManager);

            UserAccountManagerImpl userAccountManager = new UserAccountManagerImpl(builder.getContext(), authenticator, gfycatApi, noAuthApi, () -> {
                feedCache.delete(PublicFeedIdentifier.myGfycats());
                builder.getDropUserRelatedContent();
            });
            GfyCore.get().initUserAccountManager(userAccountManager);

            CachedMediaFilesManager videoDownloadingManager = new CachedMediaFilesManager(videoClient, diskCache);
            GfyCore.get().initMediaFilesManager(videoDownloadingManager);

            NSFWContentManagerImpl nsfwContentManager = new NSFWContentManagerImpl(gfycatApi, feedCache);
            GfyCore.get().initNsfwContentManager(nsfwContentManager);

            UserOwnedContentManagerImpl userOwnedContentManager = new UserOwnedContentManagerImpl(gfycatApi, creationApi, feedCache);
            GfyCore.get().initUserOwnedContentManager(userOwnedContentManager);

            DefaultUploadManager uploadManager = new DefaultUploadManager(creationApi, videoClient, NetworkConfig.buildUploadUrl(appDomainName), gfyName -> feedManager.getGfycat(gfyName).blockingGet());
            GfyCore.get().initUploadManager(uploadManager);

            GfyPrivate.initialize(appDomainName, videoClient, creationApi, userAccountManager, feedManager);
            GfycatImpression.initialize(builder.getContext(), builder.getGfycatApplicationInfo());

            GfycatAnalytics.addEngine(new MetricsEngine(builder.getContext(), builder.getGfycatApplicationInfo()));
            GfycatAnalytics.addLogger(CoreLogger.class, new CoreLoggerImpl());

            initializeAdsPlugin(builder.getContext());
            GfycatPluginInitializer.initialize(builder.getContext());

            Logging.d(LOG_TAG, "initialization end");
            initializationCompleted = true;

            subscriber.onComplete();
        });
    }

    private static void initializeAdsPlugin(Context context) {
        AdsManager.initialize(context);
    }

    private static boolean isCustomDomainAllowed(GfycatApplicationInfo applicationInfo) {
        return Pattern.matches("\\b3_.*", applicationInfo.clientId);
    }

    private static String getApplicationDomain(GfycatApplicationInfo applicationInfo) {
        if (applicationInfo instanceof GfycatCustomDomain && isCustomDomainAllowed(applicationInfo)) {
            return ((GfycatCustomDomain) applicationInfo).getDomain();
        }
        return NetworkConfig.DEFAULT_DOMAIN;
    }

    private static Queue<File> collectCacheVariants(Context context) {
        LinkedList<File> cacheVariants = new LinkedList<>();
        try {
            cacheVariants.addAll(Arrays.asList(ContextCompat.getExternalCacheDirs(context)));
        } catch (NullPointerException e) {
            // Fix cache folder creation crash on KitKat devices
            Assertions.fail(e);
        }
        cacheVariants.add(context.getCacheDir());
        return cacheVariants;
    }

    private static <T> T buildGeneralApi(String baseUrl, OkHttpClient client, Class<T> clazz) {
        return new Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(baseUrl)
                .build()
                .create(clazz);
    }

    /**
     * For test purposes.
     */
    static void deInitialize() {
        GfyCore.get().deInit();
        GfyPrivate.get().deInit();
        initializationPerformed = false;
        initializationCompleted = false;
    }
}