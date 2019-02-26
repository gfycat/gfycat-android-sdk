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

import com.gfycat.common.utils.Assertions;
import com.gfycat.core.FeedIdentifier;
import com.gfycat.core.FeedType;
import com.gfycat.core.GfyCore;
import com.gfycat.core.PublicFeedIdentifier;
import com.gfycat.core.RecentFeedIdentifier;
import com.gfycat.core.FeedIdentifierParameters;
import com.gfycat.core.gfycatapi.GfycatAPI;
import com.gfycat.core.gfycatapi.pojo.Gfycat;
import com.gfycat.core.gfycatapi.pojo.GfycatList;
import com.gfycat.core.gfycatapi.pojo.OneGfyItem;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.functions.Function;


/**
 * NetworkClient support class. Will map correct method endpoint(GfycatAPI) by FeedIdentifier.
 * <p/>
 * Created by dekalo on 19.01.16.
 */
public class GetGfycatsObservableFactoryMap implements GetGfycatsObservableFactory {

    private Map<FeedType, GetGfycatsObservableFactory> prefixObservableMap = new HashMap<>();

    public GetGfycatsObservableFactoryMap() {
        prefixObservableMap.put(PublicFeedIdentifier.Type.TRENDING, new TrendingObservableFactory());
        prefixObservableMap.put(PublicFeedIdentifier.Type.SEARCH, new SearchObservableFactory());
        prefixObservableMap.put(PublicFeedIdentifier.Type.TAG, new TagObservableFactory());
        prefixObservableMap.put(PublicFeedIdentifier.Type.REACTIONS, new ReactionObservableFactory());
        prefixObservableMap.put(PublicFeedIdentifier.Type.SINGLE, new SingleObservableFactory());
        prefixObservableMap.put(PublicFeedIdentifier.Type.USER, new UserObservableFactory());
        prefixObservableMap.put(PublicFeedIdentifier.Type.ME, new MeObservableFactory());
        prefixObservableMap.put(RecentFeedIdentifier.RECENT_FEED_TYPE, new RecentObservableFactory());
        prefixObservableMap.put(PublicFeedIdentifier.Type.SOUND_TRENDING, new SoundTrendingObservableFactory());
        prefixObservableMap.put(PublicFeedIdentifier.Type.SOUND_SEARCH, new SoundSearchObservableFactory());
    }

    @Override
    public Observable<GfycatList> create(GfycatAPI api, FeedIdentifier feedIdentifier, int count) {
        return prefixObservableMap.get(feedIdentifier.getType()).create(api, feedIdentifier, count);
    }

    private static class TrendingObservableFactory implements GetGfycatsObservableFactory {
        @Override
        public Observable<GfycatList> create(GfycatAPI api, FeedIdentifier feedIdentifier, int count) {
            return api.getTrendingGfycats(null, count);
        }
    }

    private static class SearchObservableFactory implements GetGfycatsObservableFactory {
        @Override
        public Observable<GfycatList> create(GfycatAPI api, FeedIdentifier feedIdentifier, int count) {

            PublicFeedIdentifier remoteFeedIdentifier = (PublicFeedIdentifier) feedIdentifier;

            return api.search(
                    remoteFeedIdentifier.getParameter(FeedIdentifierParameters.SEARCH_TEXT_PARAMETER),
                    null,
                    count,
                    remoteFeedIdentifier.getParameter(FeedIdentifierParameters.MIN_LENGTH),
                    remoteFeedIdentifier.getParameter(FeedIdentifierParameters.MAX_LENGTH),
                    remoteFeedIdentifier.getParameter(FeedIdentifierParameters.MIN_ASPECT_RATIO),
                    remoteFeedIdentifier.getParameter(FeedIdentifierParameters.MAX_ASPECT_RATIO),
                    remoteFeedIdentifier.getParameter(FeedIdentifierParameters.CONTENT_RATING))
                    .flatMap(new MapSearchResult());
        }
    }

    private static class TagObservableFactory implements GetGfycatsObservableFactory {
        @Override
        public Observable<GfycatList> create(GfycatAPI api, FeedIdentifier feedIdentifier, int count) {
            return api.getListForTag(feedIdentifier.toName(), null, count);
        }
    }

    private static class ReactionObservableFactory implements GetGfycatsObservableFactory {
        @Override
        public Observable<GfycatList> create(GfycatAPI api, FeedIdentifier feedIdentifier, int count) {
            return api.getReactions(feedIdentifier.toName(), null, count);
        }
    }

    private static class SingleObservableFactory implements GetGfycatsObservableFactory {
        @Override
        public Observable<GfycatList> create(final GfycatAPI api, final FeedIdentifier feedIdentifier, int count) {
            return api
                    .getOneGfycatItemObservable(feedIdentifier.toName())
                    .map(new MapOneItemToGfycatList(feedIdentifier));

        }

        private static class MapOneItemToGfycatList implements Function<OneGfyItem, GfycatList> {

            private final FeedIdentifier feedIdentifier;

            private MapOneItemToGfycatList(FeedIdentifier feedIdentifier) {
                this.feedIdentifier = feedIdentifier;
            }

            @Override
            public GfycatList apply(OneGfyItem oneGfyItem) {
                Assertions.assertNotNull(oneGfyItem, NullPointerException::new);
                if (oneGfyItem == null) return new GfycatList();
                Gfycat item = oneGfyItem.getGfyItem();

                if (oneGfyItem.getErrorMessage() != null)
                    throw new NotAvailableInTheAppException(feedIdentifier.toName(), oneGfyItem.getErrorMessage().getDescription());

                Assertions.assertNotNull(item, NullPointerException::new);
                if (item == null) return new GfycatList();

                return new GfycatList(item);
            }
        }
    }

    private class UserObservableFactory implements GetGfycatsObservableFactory {
        @Override
        public Observable<GfycatList> create(GfycatAPI api, FeedIdentifier feedIdentifier, int count) {
            return api.getListForUser(feedIdentifier.toName(), null, count);
        }
    }

    private class MeObservableFactory implements GetGfycatsObservableFactory {
        @Override
        public Observable<GfycatList> create(GfycatAPI api, FeedIdentifier feedIdentifier, int count) {
            return GfyCore.getUserAccountManager().isSignedIn()
                    ? api.getMyGfycats(null, count)
                    : Observable.empty();
        }
    }

    private class RecentObservableFactory implements GetGfycatsObservableFactory {
        @Override
        public Observable<GfycatList> create(GfycatAPI api, FeedIdentifier feedIdentifier, int count) {
            // there is no API call for Recent category since Gfycats are pull from local cache only
            return Observable.empty();
        }
    }

    private static class SoundTrendingObservableFactory implements GetGfycatsObservableFactory {
        @Override
        public Observable<GfycatList> create(GfycatAPI api, FeedIdentifier feedIdentifier, int count) {

            PublicFeedIdentifier remoteFeedIdentifier = (PublicFeedIdentifier) feedIdentifier;

            return api.getTrendingSoundGfycats(
                    null,
                    count,
                    remoteFeedIdentifier.getParameter(FeedIdentifierParameters.MIN_LENGTH),
                    remoteFeedIdentifier.getParameter(FeedIdentifierParameters.MAX_LENGTH),
                    remoteFeedIdentifier.getParameter(FeedIdentifierParameters.MIN_ASPECT_RATIO),
                    remoteFeedIdentifier.getParameter(FeedIdentifierParameters.MAX_ASPECT_RATIO),
                    remoteFeedIdentifier.getParameter(FeedIdentifierParameters.CONTENT_RATING));
        }
    }

    private static class SoundSearchObservableFactory implements GetGfycatsObservableFactory {
        @Override
        public Observable<GfycatList> create(GfycatAPI api, FeedIdentifier feedIdentifier, int count) {

            PublicFeedIdentifier remoteFeedIdentifier = (PublicFeedIdentifier) feedIdentifier;

            return api.soundSearch(
                    remoteFeedIdentifier.getParameter(FeedIdentifierParameters.SEARCH_TEXT_PARAMETER),
                    null,
                    count,
                    remoteFeedIdentifier.getParameter(FeedIdentifierParameters.MIN_LENGTH),
                    remoteFeedIdentifier.getParameter(FeedIdentifierParameters.MAX_LENGTH),
                    remoteFeedIdentifier.getParameter(FeedIdentifierParameters.MIN_ASPECT_RATIO),
                    remoteFeedIdentifier.getParameter(FeedIdentifierParameters.MAX_ASPECT_RATIO),
                    remoteFeedIdentifier.getParameter(FeedIdentifierParameters.CONTENT_RATING))
                    .flatMap(new MapSearchResult());
        }
    }
}
