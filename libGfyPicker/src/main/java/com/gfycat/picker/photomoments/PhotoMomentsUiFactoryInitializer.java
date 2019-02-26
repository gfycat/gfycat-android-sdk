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

package com.gfycat.picker.photomoments;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.support.annotation.Nullable;

/**
 * Created by oleksandrbalandin on 8/15/17
 */

@Deprecated
public class PhotoMomentsUiFactoryInitializer {

    private static PhotoMomentsUiFactory photoMomentsUiFactory;

    public @Nullable
    PhotoMomentsUiFactory getPhotoMomentsUiFactory(Context context) {
        if (photoMomentsUiFactory == null) {
            initPhotoMomentsUiFactory(context);
        }
        return photoMomentsUiFactory;
    }

    private void initPhotoMomentsUiFactory(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            ApplicationInfo appInfo = packageManager.getApplicationInfo(
                    context.getPackageName(), PackageManager.GET_META_DATA);
            String className = appInfo.metaData.getString("photo_moments_ui_factory_class_name");
            if (className != null) {
                Class<?> clazz = Class.forName(className);
                photoMomentsUiFactory = (PhotoMomentsUiFactory) clazz.newInstance();
            }
        } catch (PackageManager.NameNotFoundException | IllegalAccessException | ClassNotFoundException | java.lang.InstantiationException e) {
            //nothing
        }
    }
}
