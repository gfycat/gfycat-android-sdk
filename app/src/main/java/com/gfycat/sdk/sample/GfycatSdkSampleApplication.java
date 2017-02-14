package com.gfycat.sdk.sample;

import android.app.Application;

import com.gfycat.common.utils.Logging;
import com.gfycat.core.GfyCoreInitializationBuilder;
import com.gfycat.core.GfyCoreInitializer;
import com.gfycat.core.GfycatApplicationInfo;

/**
 * Created by dekalo on 14.02.17.
 */

public class GfycatSdkSampleApplication extends Application {
    @Override
    public void onCreate() {
        Logging.setEnabled(true);
        GfyCoreInitializer.initialize(
                new GfyCoreInitializationBuilder(
                        this,
                        new GfycatApplicationInfo("********", "********************************")));
    }
}
