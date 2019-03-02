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

import com.gfycat.core.FeedIdentifier;

/**
 * Contains basic feed data.
 */
public class FeedDescription {

    private final boolean isClosed;
    private final FeedIdentifier identifier;
    private final String digest;
    private final long creationTime;

    /**
     * Creates empty closed {@link FeedDescription}.
     *
     * @param feedIdentifier that {@link FeedDescription} should point to.
     */
    public FeedDescription(FeedIdentifier feedIdentifier) {
        this(false, feedIdentifier, null);
    }

    /**
     * For tests
     */
    public FeedDescription(boolean isClosed, FeedIdentifier identifier, String digest) {
        this(isClosed, identifier, digest, 0);
    }

    /**
     * Used by SDK internally.
     */
    public FeedDescription(boolean isClosed, FeedIdentifier identifier, String digest, long creationTime) {
        this.isClosed = isClosed;
        this.identifier = identifier;
        this.creationTime = creationTime;
        this.digest = digest;
    }

    /**
     * @return Returns true if feed is closed, false otherwise.
     */
    public boolean isClosed() {
        return isClosed;
    }

    /**
     * @return Returns {@link FeedIdentifier} that {@link FeedDescription} is pointing too.
     */
    public FeedIdentifier getIdentifier() {
        return identifier;
    }


    public long getCreationTime() {
        return creationTime;
    }

    /**
     * @return Returns digest (next page identifier)
     */
    public String getDigest() {
        return digest;
    }

    @Override
    public String toString() {
        return "{identifier: " + (identifier == null ? "null" : identifier.toString()) + " | isClosed: " + isClosed + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FeedDescription that = (FeedDescription) o;

        if (isClosed != that.isClosed) return false;
        if (!identifier.equals(that.identifier)) return false;
        return digest != null ? digest.equals(that.digest) : that.digest == null;

    }

    @Override
    public int hashCode() {
        int result = (isClosed ? 1 : 0);
        result = 31 * result + identifier.hashCode();
        result = 31 * result + (digest != null ? digest.hashCode() : 0);
        return result;
    }
}