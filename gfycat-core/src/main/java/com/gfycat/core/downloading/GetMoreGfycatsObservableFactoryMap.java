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
import com.gfycat.core.FeedType;
import com.gfycat.core.GfyCore;
import com.gfycat.core.PublicFeedIdentifier;
import com.gfycat.core.FeedIdentifierParameters;
import com.gfycat.core.gfycatapi.GfycatAPI;
import com.gfycat.core.gfycatapi.pojo.GfycatList;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.Observable;

/**
 * * NetworkClient support class. Will map correct method endpoint(GfycatAPI) by FeedIdentifier.
 * <p>
 * Created by dekalo on 19.01.16.
 */
public class GetMoreGfycatsObservableFactoryMap implements GetMoreGfycatsObservableFactory {

    private Map<FeedType, GetMoreGfycatsObservableFactory> prefixObservableMap = new HashMap<>();

    public GetMoreGfycatsObservableFactoryMap() {
        prefixObservableMap.put(PublicFeedIdentifier.Type.TRENDING, new TrendingObservableFactory());
        prefixObservableMap.put(PublicFeedIdentifier.Type.SEARCH, new SearchObservableFactory());
        prefixObservableMap.put(PublicFeedIdentifier.Type.TAG, new TagObservableFactory());
        prefixObservableMap.put(PublicFeedIdentifier.Type.REACTIONS, new ReactionObservableFactory());
        prefixObservableMap.put(PublicFeedIdentifier.Type.USER, new UserObservableFactory());
        prefixObservableMap.put(PublicFeedIdentifier.Type.ME, new MeObservableFactory());
        prefixObservableMap.put(PublicFeedIdentifier.Type.SOUND_TRENDING, new SoundTrendingObservableFactory());
        prefixObservableMap.put(PublicFeedIdentifier.Type.SOUND_SEARCH, new SoundSearchObservableFactory());
        // No GetMoreGfycatsObservableFactory for Recent category since it is always closed
    }

    @Override
    public Observable<GfycatList> create(GfycatAPI api, FeedIdentifier feedIdentifier, String nextPartIdentifier, int count) {
        return prefixObservableMap.get(feedIdentifier.getType()).create(api, feedIdentifier, nextPartIdentifier, count);
    }

    private static class TrendingObservableFactory implements GetMoreGfycatsObservableFactory {
        @Override
        public Observable<GfycatList> create(GfycatAPI api, FeedIdentifier feedIdentifier, String nextPartIdentifier, int count) {
            return api.getTrendingGfycats(nextPartIdentifier, count);
        }
    }

    private static class SearchObservableFactory implements GetMoreGfycatsObservableFactory {
        @Override
        public Observable<GfycatList> create(GfycatAPI api, FeedIdentifier feedIdentifier, String nextPartIdentifier, int count) {

            PublicFeedIdentifier remoteFeedIdentifier = (PublicFeedIdentifier) feedIdentifier;

            return api.search(
                    remoteFeedIdentifier.getParameter(FeedIdentifierParameters.SEARCH_TEXT_PARAMETER),
                    nextPartIdentifier,
                    count,
                    remoteFeedIdentifier.getParameter(FeedIdentifierParameters.MIN_LENGTH),
                    remoteFeedIdentifier.getParameter(FeedIdentifierParameters.MAX_LENGTH),
                    remoteFeedIdentifier.getParameter(FeedIdentifierParameters.MIN_ASPECT_RATIO),
                    remoteFeedIdentifier.getParameter(FeedIdentifierParameters.MAX_ASPECT_RATIO),
                    remoteFeedIdentifier.getParameter(FeedIdentifierParameters.CONTENT_RATING))
                    .map(new MapMoreSearchResult());
        }
    }

    private static class TagObservableFactory implements GetMoreGfycatsObservableFactory {
        @Override
        public Observable<GfycatList> create(GfycatAPI api, FeedIdentifier feedIdentifier, String nextPartIdentifier, int count) {
            return api.getListForTag(feedIdentifier.toName(), nextPartIdentifier, count);
        }
    }

    private static class ReactionObservableFactory implements GetMoreGfycatsObservableFactory {
        @Override
        public Observable<GfycatList> create(GfycatAPI api, FeedIdentifier feedIdentifier, String nextPartIdentifier, int count) {
            return api.getReactions(feedIdentifier.toName(), nextPartIdentifier, count);
        }
    }

    private class UserObservableFactory implements GetMoreGfycatsObservableFactory {
        @Override
        public Observable<GfycatList> create(GfycatAPI api, FeedIdentifier feedIdentifier, String digestOrCursor, int count) {
            return api.getListForUser(feedIdentifier.toName(), digestOrCursor, count);
        }
    }

    private class MeObservableFactory implements GetMoreGfycatsObservableFactory {
        @Override
        public Observable<GfycatList> create(GfycatAPI api, FeedIdentifier feedIdentifier, String digestOrCursor, int count) {
            return GfyCore.getUserAccountManager().isSignedIn()
                    ? api.getMyGfycats(digestOrCursor, count)
                    : Observable.empty();
        }
    }

    private static class SoundTrendingObservableFactory implements GetMoreGfycatsObservableFactory {
        @Override
        public Observable<GfycatList> create(GfycatAPI api, FeedIdentifier feedIdentifier, String nextPartIdentifier, int count) {

            PublicFeedIdentifier remoteFeedIdentifier = (PublicFeedIdentifier) feedIdentifier;

            return api.getTrendingSoundGfycats(
                    nextPartIdentifier,
                    count,
                    remoteFeedIdentifier.getParameter(FeedIdentifierParameters.MIN_LENGTH),
                    remoteFeedIdentifier.getParameter(FeedIdentifierParameters.MAX_LENGTH),
                    remoteFeedIdentifier.getParameter(FeedIdentifierParameters.MIN_ASPECT_RATIO),
                    remoteFeedIdentifier.getParameter(FeedIdentifierParameters.MAX_ASPECT_RATIO),
                    remoteFeedIdentifier.getParameter(FeedIdentifierParameters.CONTENT_RATING));
        }
    }


    private static class SoundSearchObservableFactory implements GetMoreGfycatsObservableFactory {
        @Override
        public Observable<GfycatList> create(GfycatAPI api, FeedIdentifier feedIdentifier, String nextPartIdentifier, int count) {
            PublicFeedIdentifier remoteFeedIdentifier = (PublicFeedIdentifier) feedIdentifier;
            return api.soundSearch(
                    remoteFeedIdentifier.getParameter(FeedIdentifierParameters.SEARCH_TEXT_PARAMETER),
                    nextPartIdentifier,
                    count,
                    remoteFeedIdentifier.getParameter(FeedIdentifierParameters.MIN_LENGTH),
                    remoteFeedIdentifier.getParameter(FeedIdentifierParameters.MAX_LENGTH),
                    remoteFeedIdentifier.getParameter(FeedIdentifierParameters.MIN_ASPECT_RATIO),
                    remoteFeedIdentifier.getParameter(FeedIdentifierParameters.MAX_ASPECT_RATIO),
                    remoteFeedIdentifier.getParameter(FeedIdentifierParameters.CONTENT_RATING))
                    .map(new MapMoreSearchResult());
        }
    }
}