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

package com.gfycat.picker.category;

import com.gfycat.common.Action0;
import com.gfycat.common.Action1;
import com.gfycat.core.GfyCore;
import com.gfycat.core.GfyPrivateHelper;
import com.gfycat.core.downloading.FeedManagerImpl;
import com.gfycat.core.gfycatapi.pojo.GfycatCategoriesList;
import com.gfycat.core.gfycatapi.pojo.GfycatRecentCategory;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.internal.disposables.ListCompositeDisposable;


/**
 * Created by dgoliy on 4/5/17.
 */

public class GfycatCategoriesLoader {

    public static Disposable subscribe(
            Action1<? super GfycatRecentCategory> onRecentCategoryLoaded,
            Action1<? super GfycatCategoriesList> onCategoriesLoaded,
            Action1<Throwable> onCategoriesLoadFailed,
            Action0 onLoadingComplete) {
        return new ListCompositeDisposable(
                subscribeForCategories(onCategoriesLoaded, onCategoriesLoadFailed, onLoadingComplete),
                subscribeForRecent(onRecentCategoryLoaded));
    }

    public static Disposable subscribeForRecent(
            Action1<? super GfycatRecentCategory> onRecentCategoryLoaded) {
        return GfyPrivateHelper.getFeedManagerImpl()
                .flatMap(FeedManagerImpl::getRecentCategory)
                .subscribe(onRecentCategoryLoaded::call);
    }

    public static Disposable subscribeForCategories(
            Action1<? super GfycatCategoriesList> onCategoriesLoaded,
            Action1<Throwable> onCategoriesLoadFailed,
            Action0 onLoadingComplete) {
        return GfyCore.getFeedManager()
                .getCategories()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        onCategoriesLoaded::call,
                        onCategoriesLoadFailed::call,
                        onLoadingComplete::call);
    }
}
