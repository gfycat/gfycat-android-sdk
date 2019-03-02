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

package com.gfycat.core.downloading;

import com.gfycat.common.utils.Logging;
import com.gfycat.core.FeedIdentifier;
import com.gfycat.core.gfycatapi.pojo.Gfycat;

import java.util.Collections;
import java.util.List;

/**
 * Contains current {@link FeedDescription} and list of feed gfycats.
 * <p>
 * Received either from cache or from network.
 */
public class FeedData {

    private final FeedDescription feedDescription;
    private final List<Gfycat> gfycats;

    /**
     * Constructs {@link FeedData} with empty gfycat list.
     */
    public FeedData(FeedDescription feedDescription) {
        this(feedDescription, Collections.emptyList());
    }

    /**
     * Constructs {@link FeedData} with provided {@link FeedDescription} and {@link List<Gfycat>}.
     */
    public FeedData(FeedDescription feedDescription, List<Gfycat> gfycats) {
        this.feedDescription = feedDescription;
        this.gfycats = gfycats;
    }

    /**
     * @return Returns true if feed is empty, false otherwise.
     */
    public boolean isEmpty() {
        return feedDescription == null || gfycats == null || gfycats.isEmpty();
    }

    /**
     * @return Returns related {@link FeedIdentifier}.
     */
    public FeedIdentifier getIdentifier() {
        return feedDescription.getIdentifier();
    }

    /**
     * @return Returns true is feed has ended and calling loadMore will not load any new items.
     */
    public boolean isClosed() {
        return feedDescription.isClosed();
    }

    /**
     * @return Returns related {@link FeedDescription}.
     */
    public FeedDescription getFeedDescription() {
        return feedDescription;
    }

    /**
     * @return Returns Feed gfycats list.
     */
    public List<Gfycat> getGfycats() {
        return gfycats;
    }

    /**
     * @return Returns count of gfycats in the list, if there are any, or -1 if gfycats have not been loaded yet.
     */
    public int getCount() {
        return gfycats == null ? -1 : gfycats.size();
    }

    @Override
    public String toString() {
        return "FeedData{" +
                "feedDescription=" + feedDescription +
                ", gfycats=" + (gfycats == null ? "null" : gfycats.size()) +
                '}';
    }

    void dump(String logTag) {
        Logging.d(logTag, "feedDescription = ", feedDescription, " size = ", gfycats.size(), " ", hashCode());
        for (Gfycat gfycat : gfycats) {
            Logging.d(logTag, "gfyId = ", gfycat.getGfyId(), " ", hashCode());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FeedData feedData = (FeedData) o;

        if (!feedDescription.equals(feedData.feedDescription)) return false;
        return gfycats != null ? gfycats.equals(feedData.gfycats) : feedData.gfycats == null;

    }

    @Override
    public int hashCode() {
        int result = feedDescription.hashCode();
        result = 31 * result + (gfycats != null ? gfycats.hashCode() : 0);
        return result;
    }
}
