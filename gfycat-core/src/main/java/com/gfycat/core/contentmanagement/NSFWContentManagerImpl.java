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

package com.gfycat.core.contentmanagement;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.gfycat.common.ChainedException;
import com.gfycat.common.utils.Assertions;
import com.gfycat.common.utils.Logging;
import com.gfycat.core.db.GfycatFeedCache;
import com.gfycat.core.gfycatapi.GfycatAPI;
import com.gfycat.core.gfycatapi.pojo.Gfycat;

import io.reactivex.schedulers.Schedulers;

/**
 * Created by dekalo on 15.06.16.
 */
public class NSFWContentManagerImpl implements NSFWContentManager {

    private static final String LOG_TAG = "NSFWContentManagerImpl";
    private final Handler handler;
    private final GfycatAPI api;
    private final GfycatFeedCache gfycatFeedCache;

    public NSFWContentManagerImpl(GfycatAPI gfycatAPI, GfycatFeedCache gfycatFeedCache) {
        api = gfycatAPI;
        this.gfycatFeedCache = gfycatFeedCache;
        handler = new Handler(Looper.getMainLooper());
    }

    @Override
    public Runnable reportItem(Gfycat gfcycat) {

        Logging.d(LOG_TAG, "reportItem(" + gfcycat.getGfyId() + ")");

        gfycatFeedCache.blockItem(gfcycat, true);
        api.blockContent(gfcycat.getGfyId()).subscribeOn(Schedulers.io())
                .subscribe(response -> {
                    Assertions.assertTrue(response.isSuccessful(), () -> new IllegalStateException("reportItem was not successful"));
                }, throwable -> Assertions.fail(new ChainedException(throwable)));

        return new UndoBlockItem(gfcycat);
    }

    @Override
    public Runnable reportUser(Gfycat gfycat, long undoPossibilityTimeMs) {

        String username = gfycat.getUserName();

        Logging.d(LOG_TAG, "reportUser(" + username + ")");
        if (isAnonymous(username)) {
            Logging.d(LOG_TAG, "reportUser(" + username + ") from anonymous user, let's just block content.");
            return reportItem(gfycat);
        }

        gfycatFeedCache.blockUser(gfycat.getUserName(), true);

        /*
         * Scheduling block user in undoPossibilityTime.
         * If user will press undo, we will get callback and removeCallback blockRemoteBlockUser from handler.
         */
        Runnable blockRemoteBlockUser = () -> api.blockUser(gfycat.getUserName()).subscribeOn(Schedulers.io())
                .subscribe(response -> {
                    Assertions.assertTrue(response.isSuccessful(), () -> new IllegalStateException("blocking user was not successful"));
                }, throwable -> Assertions.fail(new ChainedException(throwable)));

        handler.postDelayed(blockRemoteBlockUser, undoPossibilityTimeMs);

        return new UndoBlockUser(gfycat.getUserName(), () -> handler.removeCallbacks(blockRemoteBlockUser));
    }

    private boolean isAnonymous(String username) {
        return username == null || TextUtils.isEmpty(username) || "anonymous".equals(username);
    }


    private class UndoBlockUser implements Runnable {

        private final String username;
        private final Runnable callback;

        /**
         * @param callback will be called when undo will be invoked.
         */
        public UndoBlockUser(String username, Runnable callback) {
            this.username = username;
            this.callback = callback;
        }

        @Override
        public void run() {
            Logging.d(LOG_TAG, " UndoBlockUser::run(" + username + ")");
            gfycatFeedCache.blockUser(username, false);
            callback.run();
        }
    }

    private class UndoBlockItem implements Runnable {
        private final Gfycat gfycat;

        private UndoBlockItem(Gfycat gfycat) {
            this.gfycat = gfycat;
        }

        @Override
        public void run() {
            Logging.d(LOG_TAG, " UndoBlockItem::run(" + gfycat + ")");
            gfycatFeedCache.blockItem(gfycat, false);
        }
    }
}
