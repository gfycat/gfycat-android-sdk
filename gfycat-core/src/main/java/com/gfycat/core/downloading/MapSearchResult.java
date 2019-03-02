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

import com.gfycat.core.gfycatapi.pojo.GfycatList;
import com.gfycat.core.gfycatapi.pojo.SearchResult;

import io.reactivex.Observable;
import io.reactivex.functions.Function;


/**
 * Created by dekalo on 19.01.16.
 */
class MapSearchResult implements Function<SearchResult, Observable<GfycatList>> {

    @Override
    public Observable<GfycatList> apply(SearchResult searchResult) {
        if (FeedManagerImpl.NetworkErrors.NO_SEARCH_RESULT.equals(searchResult.getErrorMessage())) {
            return Observable.error(new FeedManager.NoSearchResultException());
        }
        return Observable.just(searchResult);
    }
}
