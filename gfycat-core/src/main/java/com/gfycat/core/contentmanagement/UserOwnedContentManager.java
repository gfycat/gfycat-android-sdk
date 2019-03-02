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

import com.gfycat.core.gfycatapi.pojo.Gfycat;
import com.gfycat.core.gfycatapi.pojo.UserInfo;

import java.util.List;

import io.reactivex.Single;

/**
 * Allows User owned Gfycats management.
 * <p>
 * User owned Gfycats are the ones with {@link Gfycat#getUserName()} equal to {@link UserInfo#getUserid()}.
 */
public interface UserOwnedContentManager {

    /**
     * Delete gfycat.
     *
     * @param gfycat to delete.
     */
    void delete(Gfycat gfycat);

    /**
     * Make gfycat private.
     *
     * @param undoDuration time in milliseconds while undo is possible.
     * @return undo runnable to cancel this action.
     */
    Runnable makePrivate(Gfycat gfycat, long undoDuration);

    /**
     * Make gfycat public.
     *
     * @param undoDuration time in milliseconds while undo is possible.
     * @return undo runnable to cancel this action.
     */
    Runnable makePublic(Gfycat gfycat, long undoDuration);

    /**
     * Mark content as being suitable for all ages.
     *
     * @param gfycat       to be updated.
     * @param undoDuration time in milliseconds while undo is possible.
     * @return undo runnable to cancel this action.
     */
    Runnable suitableForAllAges(Gfycat gfycat, long undoDuration);

    /**
     * Mark own content as being suitable for 18+ only.
     *
     * @param gfycat       to be updated.
     * @param undoDuration time in milliseconds while undo is possible.
     * @return undo runnable to cancel this action.
     */
    Runnable markAs18Only(Gfycat gfycat, long undoDuration);

    Single<Boolean> updatePublishState(Gfycat gfycat, boolean published);

    Single<Boolean> updateDescription(Gfycat gfycat, String description);

    Single<Boolean> updateTitle(Gfycat gfycat, String title);

    Single<Boolean> addTags(Gfycat gfycat, List<String> published);
}
