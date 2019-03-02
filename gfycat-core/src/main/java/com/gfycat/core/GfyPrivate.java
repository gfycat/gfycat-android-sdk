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

import com.gfycat.core.authentication.UserAccountManagerImpl;
import com.gfycat.core.creation.CreationAPI;
import com.gfycat.core.downloading.FeedManagerImpl;

import okhttp3.OkHttpClient;

/**
 * Set of private API that should be used by core internally.
 */
public class GfyPrivate {
    private static long DEFAULT_INITIALIZATION_TIMEOUT = 5000;

    private static GfyPrivate INSTANCE = null;

    private final String domainName;
    private final OkHttpClient videoDownloadingClient;
    private final CreationAPI creationAPI;
    private final UserAccountManagerImpl userAccountManager;
    private final FeedManagerImpl feedManager;

    private GfyPrivate(String domainName, OkHttpClient videoDownloadingClient, CreationAPI getCreationApi, UserAccountManagerImpl userAccountManager, FeedManagerImpl feedManager) {
        this.domainName = domainName;
        this.videoDownloadingClient = videoDownloadingClient;
        this.creationAPI = getCreationApi;
        this.userAccountManager = userAccountManager;
        this.feedManager = feedManager;
    }

    static synchronized void initialize(String domainName, OkHttpClient videoDownloadingClient, CreationAPI getCreationApi, UserAccountManagerImpl userAccountManager, FeedManagerImpl feedManager) {
        INSTANCE = new GfyPrivate(domainName, videoDownloadingClient, getCreationApi, userAccountManager, feedManager);
    }

    public static GfyPrivate get() {
        if (INSTANCE == null) {
            if (GfyCoreInitializer.initializePerformed()) {
                long start = System.currentTimeMillis();
                do {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                while (INSTANCE == null && System.currentTimeMillis() - start < DEFAULT_INITIALIZATION_TIMEOUT);

                if (INSTANCE == null) {
                    throw new IllegalStateException("Timeout! Gfycat SDK have not been initialized for a long time!");
                }
            } else {
                throw new IllegalStateException("Gfycat SDK is not initialized!");
            }
        }
        return INSTANCE;
    }

    public String getDomainName() {
        return domainName;
    }

    public OkHttpClient getVideoDownloadingClient() {
        return videoDownloadingClient;
    }

    public CreationAPI getCreationAPI() {
        return creationAPI;
    }

    public UserAccountManagerImpl getUserAccountManager() {
        return userAccountManager;
    }

    public FeedManagerImpl getFeedManager() {
        return feedManager;
    }

    /**
     * For test purposes.
     */
    void deInit() {
        INSTANCE = null;
    }
}
