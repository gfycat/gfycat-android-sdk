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

package com.gfycat.picker;

import android.content.Context;
import android.support.annotation.NonNull;

import com.gfycat.common.utils.Logging;
import com.gfycat.core.GfyPrivateHelper;
import com.gfycat.core.GfycatPlugin;
import com.gfycat.core.downloading.FeedManagerImpl;

import io.reactivex.schedulers.Schedulers;

/**
 * Will start categories prefetch right after core initialization
 */
public class PickerCategoriesPrefetchPlugin implements GfycatPlugin {

    private static final String LOG_TAG = "PickerCategoriesPrefetchPlugin";

    public void initialize(@NonNull Context context) {
        Logging.d(LOG_TAG, "initialize()");
        GfyPrivateHelper.getFeedManagerImpl().observeOn(Schedulers.io()).subscribe(FeedManagerImpl::prefetchCategories);
    }
}
