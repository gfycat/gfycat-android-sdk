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

/**
 * {@link FeedIdentifier} generator.
 */
public class FeedIdentifierFactory {

    /**
     * Create {@link FeedIdentifier} with unique feed identifier.
     *
     * @param uniqueIdentifier returned from {@link FeedIdentifier#toUniqueIdentifier()}
     * @return Returns {@link FeedIdentifier}
     * @throws NoSuchFeedException if there is no {@link FeedIdentifier} associated with provided uniqueIdentifier.
     */
    public static FeedIdentifier fromUniqueIdentifier(String uniqueIdentifier) throws NoSuchFeedException {

        if (uniqueIdentifier.startsWith(PublicFeedIdentifier.PUBLIC_IDENTIFIER_SCHEME)) {
            return PublicFeedIdentifier.create(uniqueIdentifier);
        }

        if (uniqueIdentifier.startsWith(RecentFeedIdentifier.RECENT_FEED_TYPE.getName())) {
            return RecentFeedIdentifier.recent();
        }

        if (uniqueIdentifier.startsWith(SingleFeedIdentifier.Type.SINGLE.getName())) {
            return SingleFeedIdentifier.create(uniqueIdentifier);
        }

        throw new NoSuchFeedException(uniqueIdentifier);
    }

    /**
     * Indicates that there is no {@link FeedIdentifier} associated with provided uniqueIdentifier.
     */
    public static class NoSuchFeedException extends RuntimeException {

        private NoSuchFeedException(String uniqueIdentifier) {
            super("Can not instantiate feed from uniqueIdentifier = [" + uniqueIdentifier + "]");
        }
    }
}
