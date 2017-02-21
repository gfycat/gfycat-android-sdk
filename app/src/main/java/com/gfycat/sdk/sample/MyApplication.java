package com.gfycat.sdk.sample;

import android.app.Application;

import com.gfycat.common.utils.Logging;
import com.gfycat.core.GfyCoreInitializationBuilder;
import com.gfycat.core.GfyCoreInitializer;
import com.gfycat.core.GfycatApplicationInfo;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        GfyCoreInitializer.initialize(
                new GfyCoreInitializationBuilder(
                        this,
                        new GfycatApplicationInfo("CLIENT_ID", "CLIENT_SECRET")));
    }
}