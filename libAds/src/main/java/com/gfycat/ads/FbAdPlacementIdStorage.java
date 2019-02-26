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

import com.gfycat.ads.remote.FbAdsConfig;
import com.gfycat.common.utils.Assertions;
import com.gfycat.common.utils.Logging;
import com.gfycat.core.ads.AdsDisabledException;
import com.gfycat.core.ads.AdsPlacement;
import com.gfycat.core.ads.NoSuitableAdsConfigException;

import java.util.HashMap;
import java.util.Locale;

/**
 * Ad Placement Id container initialized with {@link FbAdsConfig}
 */
public final class FbAdPlacementIdStorage {
    private static final String TAG = "FbAdPlacementIdStorage";

    private static final String DEFAULT_ADS_APPLICATION_PACKAGE = "default";

    /**
     * Map for {@link AdsPlacement#getPlacementName()} to adIds.
     */
    private HashMap<String, String> adsPlacements = new HashMap<>();

    /**
     * Return adId by @{@link AdsPlacement}.
     */
    public String getAdId(AdsPlacement placement) {
        if (adsPlacements.containsKey(placement.getPlacementName()))
            return adsPlacements.get(placement.getPlacementName());

        throw new IllegalStateException("No placementId assigned for " + placement.getPlacementName());
    }

    public void init(@NonNull Context context, @NonNull FbAdsConfig config) throws AdsDisabledException, NoSuitableAdsConfigException {
        String pkgName = context.getApplicationContext().getPackageName();
        Logging.d(TAG, "Reading config " + config.name);
        if (config.ids == null || config.ids.length == 0) {
            Assertions.fail(new IllegalStateException("Server returned no FbApplication ids"));
            throw new NoSuitableAdsConfigException();
        }

        FbAdsConfig.FbAdApplication defaultAdApplication = null;
        FbAdsConfig.FbAdApplication currentAdApplication = null;
        for (FbAdsConfig.FbAdApplication adApplication : config.ids) {
            if (DEFAULT_ADS_APPLICATION_PACKAGE.equals(adApplication.application)) {
                defaultAdApplication = adApplication;
            }
            if (pkgName.equals(adApplication.application)) {
                currentAdApplication = adApplication;
            }
        }

        if (defaultAdApplication != null) assignAdIds(defaultAdApplication);
        if (currentAdApplication != null) assignAdIds(currentAdApplication);


        if (currentAdApplication != null && !currentAdApplication.isEnabled) {
            throw new AdsDisabledException();
        } else if (defaultAdApplication == null && currentAdApplication == null) {
            throw new NoSuitableAdsConfigException();
        }
    }

    private void assignAdIds(FbAdsConfig.FbAdApplication application) {
        if (application != null && application.ads != null) {
            for (FbAdsConfig.FbAdPlacementId id : application.ads) {
                adsPlacements.put(id.type, id.adId);
                Logging.d(TAG, String.format(Locale.US, "%s : %s\t->\t%s", application.application, id.type, id.adId));
            }
        }
    }
}
