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

import com.gfycat.core.authentication.UserAccountManager;
import com.gfycat.core.authentication.UserAccountManagerAsyncWrapper;
import com.gfycat.core.contentmanagement.NSFWContentManager;
import com.gfycat.core.contentmanagement.NSFWContentManagerAsyncWrapper;
import com.gfycat.core.contentmanagement.UserOwnedContentManager;
import com.gfycat.core.contentmanagement.UserOwnedContentManagerAsyncWrapper;
import com.gfycat.core.creation.UploadManager;
import com.gfycat.core.creation.UploadManagerAsyncWrapper;
import com.gfycat.core.downloading.FeedManager;
import com.gfycat.core.storage.MediaFilesManager;
import com.gfycat.core.storage.MediaFilesManagerAsyncWrapper;

import io.reactivex.Single;

/**
 * Root Gfycat class providing access to core elements
 */
public final class GfyCore {

    private static final GfyCore INSTANCE = new GfyCore();

    private FeedManagerAsyncWrapper feedManager = new FeedManagerAsyncWrapper();
    private MediaFilesManagerAsyncWrapper mediaFilesManager = new MediaFilesManagerAsyncWrapper();
    private UserAccountManagerAsyncWrapper userAccountManager = new UserAccountManagerAsyncWrapper();
    private UploadManagerAsyncWrapper uploadManager = new UploadManagerAsyncWrapper();
    private UserOwnedContentManagerAsyncWrapper userOwnedContentManager = new UserOwnedContentManagerAsyncWrapper();
    private NSFWContentManagerAsyncWrapper nsfwContentManager = new NSFWContentManagerAsyncWrapper();

    private GfyCore() {
    }

    // package visible init methods
    static Single<FeedManager> observeFeedManager() {
        return get().feedManager.observeFeedManager();
    }

    void initFeedManager(FeedManager feedManager) {
        this.feedManager.init(feedManager);
    }

    void initMediaFilesManager(MediaFilesManager mediaFilesManager) {
        this.mediaFilesManager.init(mediaFilesManager);
    }

    void initUserAccountManager(UserAccountManager userAccountManager) {
        this.userAccountManager.init(userAccountManager);
    }

    void initUploadManager(UploadManager uploadManager) {
        this.uploadManager.init(uploadManager);
    }

    void initUserOwnedContentManager(UserOwnedContentManager userOwnedContentManager) {
        this.userOwnedContentManager.init(userOwnedContentManager);
    }

    void initNsfwContentManager(NSFWContentManager nsfwContentManager) {
        this.nsfwContentManager.init(nsfwContentManager);
    }
    ///////////////////////////////

    /**
     * Assert that core was initialized.
     *
     * @throws IllegalStateException if core was not initialized.
     */
    public static void assertInitializeState() {
        if (!GfyCoreInitializer.initializePerformed())
            throw new IllegalStateException("GfyCore is not initialized!");
    }

    /**
     * @return Returns {@link FeedManager} instance.
     */
    public static FeedManager getFeedManager() {
        return get().feedManager;
    }

    static GfyCore get() {
        return INSTANCE;
    }

    /**
     * @return Returns {@link UserAccountManager} instance.
     */
    public static UserAccountManager getUserAccountManager() {
        return get().userAccountManager;
    }

    /**
     * @return Returns {@link MediaFilesManager} instance.
     */
    public static MediaFilesManager getMediaFilesManager() {
        return get().mediaFilesManager;
    }

    /**
     * @return Returns {@link UploadManager} instance.
     */
    public static UploadManager getUploadManager() {
        return get().uploadManager;
    }

    /**
     * @return Returns {@link NSFWContentManager} instance.
     */
    public static NSFWContentManager getNSFWContentManager() {
        return get().nsfwContentManager;
    }

    /**
     * @return Returns {@link UserOwnedContentManager} instance.
     */
    public static UserOwnedContentManager getUserOwnedContentManager() {
        return get().userOwnedContentManager;
    }

    /**
     * For test purposes.
     */
    void deInit() {
        feedManager = new FeedManagerAsyncWrapper();
        mediaFilesManager = new MediaFilesManagerAsyncWrapper();
        userAccountManager = new UserAccountManagerAsyncWrapper();
        uploadManager = new UploadManagerAsyncWrapper();
        userOwnedContentManager = new UserOwnedContentManagerAsyncWrapper();
        nsfwContentManager = new NSFWContentManagerAsyncWrapper();
    }
}
