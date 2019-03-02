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
import android.text.TextUtils;

import com.gfycat.core.storage.DefaultDiskCache;

import java.io.File;

import okhttp3.Interceptor;

/**
 * Gfycat SDK initialize preferences class
 */
public class GfyCoreInitializationBuilder {

    private final Context context;
    private final GfycatApplicationInfo gfycatApplicationInfo;

    private File cacheFolder;
    private Interceptor jsonInterceptor;
    private Interceptor mediaInterceptor;
    private Runnable dropUserRelatedContent;

    private DefaultDiskCache.CacheSizeOptions cacheSizeOptions = new DefaultDiskCache.CacheSizeOptions();

    /**
     * @param context               of application.
     * @param gfycatApplicationInfo with correct CLIENT_ID and SECRET_ID.
     */
    public GfyCoreInitializationBuilder(Context context, GfycatApplicationInfo gfycatApplicationInfo) {
        if (context == null) throw new NullPointerException("Context should not be null");
        if (gfycatApplicationInfo == null)
            throw new NullPointerException("AppInfo should not be null");
        if (TextUtils.isEmpty(gfycatApplicationInfo.clientId))
            throw new IllegalArgumentException("gfycatApplicationInfo.clientId should be not empty.");
        if (TextUtils.isEmpty(gfycatApplicationInfo.clientSecret))
            throw new IllegalArgumentException("gfycatApplicationInfo.clientSecret should be not empty.");

        this.context = context.getApplicationContext();
        this.gfycatApplicationInfo = gfycatApplicationInfo;
    }

    /**
     * Set minimum storage space for Gfycat cache folder
     *
     * @param megabytes Amount of megabytes minimum.
     */
    public GfyCoreInitializationBuilder setMinCacheSpace(long megabytes) {
        cacheSizeOptions.setMinSpace(megabytes);
        return this;
    }

    /**
     * Set maximum storage space for Gfycat cache folder
     *
     * @param megabytes Amount of megabytes maximum.
     */
    public GfyCoreInitializationBuilder setMaxCacheSpace(long megabytes) {
        cacheSizeOptions.setMaxSpace(megabytes);
        return this;
    }

    /**
     * Force gfycat to use cache folder provided by application.
     *
     * @param cacheFolder application defined cache folder.
     *
     * @throws CacheFolderDoesNotExist   if provided path does not exist.
     * @throws CacheFolderIsNotDirectory if provided path is not directory.
     */
    public GfyCoreInitializationBuilder setCacheFolder(File cacheFolder) throws CacheFolderDoesNotExist, CacheFolderIsNotDirectory {
        if (!cacheFolder.exists())
            throw new CacheFolderDoesNotExist(cacheFolder);
        if (!cacheFolder.isDirectory())
            throw new CacheFolderIsNotDirectory(cacheFolder);
        this.cacheFolder = cacheFolder;
        return this;
    }

    GfyCoreInitializationBuilder setJsonInterceptor(Interceptor interceptor) {
        this.jsonInterceptor = interceptor;
        return this;
    }

    GfyCoreInitializationBuilder setVideoInterceptor(Interceptor interceptor) {
        this.mediaInterceptor = interceptor;
        return this;
    }

    /**
     * Subscribe for user sign out if any extra actions needed for proper user data removal.
     *
     * @param dropUserRelatedContent callback called on user sign out.
     */
    public GfyCoreInitializationBuilder setDropUserRelatedContentCallback(Runnable dropUserRelatedContent) {
        this.dropUserRelatedContent = dropUserRelatedContent;
        return this;
    }

    Context getContext() {
        return context;
    }

    GfycatApplicationInfo getGfycatApplicationInfo() {
        return gfycatApplicationInfo;
    }

    File getCacheFolder() {
        return cacheFolder;
    }

    DefaultDiskCache.CacheSizeOptions getCacheSizeOptions() {
        return cacheSizeOptions;
    }

    Interceptor getMediaInterceptor() {
        return mediaInterceptor == null ? NO_INTERCEPTOR : mediaInterceptor;
    }

    Interceptor getJsonInterceptor() {
        return jsonInterceptor == null ? NO_INTERCEPTOR : jsonInterceptor;
    }

    Runnable getDropUserRelatedContent() {
        return dropUserRelatedContent == null ? NO_DROP_USER_CONTENT : dropUserRelatedContent;
    }

    private static Interceptor NO_INTERCEPTOR = chain -> chain.proceed(chain.request());

    private static Runnable NO_DROP_USER_CONTENT = () -> {
    };

    /**
     * Used to indicate that provided path does not exist.
     */
    public class CacheFolderDoesNotExist extends Throwable {
        CacheFolderDoesNotExist(File file) {
            super("Provided file = " + file + " path not exists.");
        }
    }

    /**
     * Used to indicate that provided path is not directory.
     */
    public class CacheFolderIsNotDirectory extends Throwable {
        CacheFolderIsNotDirectory(File file) {
            super("Provided file = " + file + " path is not directory.");
        }
    }
}
