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

package com.gfycat.core.db;

import com.gfycat.core.FeedIdentifier;
import com.gfycat.core.downloading.FeedData;
import com.gfycat.core.gfycatapi.pojo.Gfycat;
import com.gfycat.core.gfycatapi.pojo.GfycatList;

/**
 * Data saving interface for Database.
 * <p>
 * Created by dekalo on 10.09.15.
 */
public interface GfycatFeedCache {

    /**
     * @return gfycat with gfyId from DB if it exists, null otherwise.
     */
    Gfycat getGfycat(String gfyId);

    /**
     * @param feedIdentifier - delete cache for feedIdentifier.
     */
    boolean delete(FeedIdentifier feedIdentifier);

    /**
     * Get feed data for feedIdentifier
     */
    FeedData getFeedData(FeedIdentifier feedIdentifier);

    /**
     * Same as {@link GfycatFeedCache#insertFeed(FeedIdentifier, GfycatList, CloseMode, boolean)} with append = false.
     */
    void insertFeed(FeedIdentifier identifier, GfycatList data, CloseMode closeMode);

    /**
     * Insert Feed to database.
     *
     * @param identifier of feed to insert.
     * @param data       to insert.
     * @param closeMode  closing mode policy.
     * @param append     if true insert or add policy will be applied, if false feed would be replaced.
     */
    void insertFeed(FeedIdentifier identifier, GfycatList data, CloseMode closeMode, boolean append);

    /**
     * @param identifier     - see #FeedIdentifier class.
     * @param previousDigest - previous digest value for checking if we not duplicate the same request on update database.
     * @param data           - content that should be added to previously inserted Feed.
     */
    void updateFeed(FeedIdentifier identifier, String previousDigest, GfycatList data);

    /**
     * @param identifier - see #FeedIdentifier class.
     */
    void closeFeed(FeedIdentifier identifier, String previousDigest);

    /**
     * Mark item as disabled.
     * Mentioned item will not be shown in trending / tag feeds.
     */
    boolean markDeleted(Gfycat item, boolean deleted);

    /**
     * Removes gfycat from recent
     */
    void removeFromRecent(Gfycat item);

    /**
     * Mark published..
     */
    boolean markPublished(Gfycat item, boolean published);

    /**
     * Mark nsfw
     */
    boolean markNsfw(Gfycat item, boolean nsfw);

    /**
     * Do not show any gfycats from specified user.
     */
    boolean blockUser(String userName, boolean block);

    /**
     * Block gfycat, this gfycat item would not be visible anymore.
     */
    boolean blockItem(Gfycat gfycat, boolean block);
}
