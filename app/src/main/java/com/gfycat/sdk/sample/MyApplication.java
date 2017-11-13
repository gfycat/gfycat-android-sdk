package com.gfycat.sdk.sample;

import android.app.Application;

import com.gfycat.common.utils.Logging;
import com.gfycat.core.GfyCoreInitializationBuilder;
import com.gfycat.core.GfyCoreInitializer;
import com.gfycat.core.GfycatApplicationInfo;

public class MyApplication extends Application {
    private final int MIN_CACHE_SIZE_IN_MEGABYTES = 75;
    private final int MAX_CACHE_SIZE_IN_MEGABYTES = 500;

    @Override
    public void onCreate() {
        super.onCreate();

        // Enable SDK logcat output to see the under-the-hood logs, if needed
        Logging.setEnabled(true);

        // Create a builder with your application clientId and clientSecret
        GfyCoreInitializationBuilder builder = new GfyCoreInitializationBuilder(
                this,
                new GfycatApplicationInfo("CLIENT_ID", "CLIENT_SECRET"));

        // (Optional) Customize cache folder for Gfycat files
        try {
            builder.setCacheFolder(getCacheDir());
        } catch (GfyCoreInitializationBuilder.CacheFolderDoesNotExist |
                 GfyCoreInitializationBuilder.CacheFolderIsNotDirectory cacheFolderException) {
            cacheFolderException.printStackTrace();
        }

        // (Optional) Customize minimum and maximum cache storage space dedicated for Gfycat files.
        // Note : Gfycat SDK will never leave less then 50mb available on device's storage.
        // MinCacheSpace cannot be lower then 50mb. In case of insufficient space - SDK will work with no cache.
        // Gfycat SDK will take only 50% of available storage space until reaches MaxCacheSpace limit.
        builder.setMinCacheSpace(MIN_CACHE_SIZE_IN_MEGABYTES).setMaxCacheSpace(MAX_CACHE_SIZE_IN_MEGABYTES);

        GfyCoreInitializer.initialize(builder);
    }
}