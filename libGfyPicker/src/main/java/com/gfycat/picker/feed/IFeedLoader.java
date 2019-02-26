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

package com.gfycat.picker.feed;

import com.gfycat.core.FeedIdentifier;
import com.gfycat.core.downloading.FeedData;

/**
 * Created by dekalo on 02.11.16.
 */

public interface IFeedLoader {

    interface FeedLoadingListener {

        /**
         * Called when loading operation started.
         */
        void onFeedLoadingStarted();

        /**
         * Called when feedData obtained or changed.
         */
        void onFeedLoaded(FeedData feedData);

        /**
         * TODO: add posible exception declaration.
         *
         * @param throwable - when error happend during loading process.
         */
        void onError(Throwable throwable);
    }

    void initialize(FeedIdentifier identifier, FeedLoadingListener listener);

    void changeFeed(FeedIdentifier identifier);

    void reLoad();

    void loadMore();

    void stopLoad();

    long lastSuccessRequestMs();

    boolean hasError();

    void setFeedForceReloadingNeeded(boolean feedForceReloadingNeeded);
}
