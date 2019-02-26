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

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.gfycat.common.ContextDetails;
import com.gfycat.common.utils.Assertions;
import com.gfycat.common.utils.Logging;
import com.gfycat.core.FeedIdentifier;
import com.gfycat.core.PublicFeedIdentifier;
import com.gfycat.core.RecentFeedIdentifier;
import com.gfycat.core.bi.impression.ImpressionInfo;
import com.gfycat.core.gfycatapi.pojo.Gfycat;
import com.gfycat.picker.ContextDetailsValues;

/**
 * Main controller for CellView in ColumnFeedFragment.
 * Responsible for coordinate all initialization and interaction will cell.
 * <p>
 * Created by dekalo on 01.09.15.
 */
public abstract class GfyViewHolder extends RecyclerView.ViewHolder implements GfyItemHolder {

    private static final String LOG_TAG = GfyViewHolder.class.getSimpleName();

    private Gfycat currentGfycatItem;
    private FeedIdentifier feedIdentifier;

    private ContextDetails details = null;

    private boolean isAutoPlay;


    protected FeedIdentifier getFeedIdentifier() {
        return feedIdentifier;
    }

    public Gfycat getItem() {
        return currentGfycatItem;
    }

    protected Context getContext() {
        return itemView.getContext();
    }

    public ContextDetails getContextDetails() {
        return details;
    }

    public GfyViewHolder(View view) {
        super(view);
    }

    public void bind(final Gfycat gfycat, FeedIdentifier identifier) {
        if (currentGfycatItem != null) recycle();
        currentGfycatItem = gfycat;
        feedIdentifier = identifier;

        details = new ContextDetails()
                .put(ContextDetailsValues.GFYNAME_KEY, gfycat.getGfyId())
                .put(ContextDetailsValues.SOURCE, "GfyViewHolder")
                .put(ContextDetailsValues.FEED_IDENTIFIER, identifier.toUniqueIdentifier());
    }

    @Override
    public void recycle() {
        Logging.d(LOG_TAG, "recycle() for ", details);

        isAutoPlay = false;
        currentGfycatItem = null;
        feedIdentifier = null;
    }

    @Override
    public void autoPause() {
        Logging.d(LOG_TAG, "autoPause() ", details);
        isAutoPlay = false;
    }

    @Override
    public void autoPlay() {
        Logging.d(LOG_TAG, "autoPlay() ", details);
        isAutoPlay = true;
    }

    @Override
    public boolean isAutoPlay() {
        return isAutoPlay;
    }

    protected static ImpressionInfo prepareImpressionInfo(Gfycat gfycat, FeedIdentifier identifier, String flow) {

        ImpressionInfo impressionInfo = new ImpressionInfo();
        impressionInfo.setFlow(flow);
        impressionInfo.setGfyId(gfycat.getGfyId());

        if (RecentFeedIdentifier.RECENT_FEED_TYPE.equals(identifier.getType())) {
            impressionInfo.setContext(ImpressionInfo.RECENT_CONTEXT);
            impressionInfo.setKeyword(ImpressionInfo.KEYWORD_NOT_AVAILABLE);
        } else if (PublicFeedIdentifier.Type.REACTIONS.equals(identifier.getType())) {
            impressionInfo.setContext(ImpressionInfo.CATEGORY_CONTEXT);
            impressionInfo.setKeyword(identifier.toName());
        } else if (PublicFeedIdentifier.Type.SEARCH.equals(identifier.getType())) {
            impressionInfo.setContext(ImpressionInfo.SEARCH_CONTEXT);
            impressionInfo.setKeyword(identifier.toName());
        } else if (PublicFeedIdentifier.Type.ME.equals(identifier.getType())) {
            impressionInfo.setContext(ImpressionInfo.PROFILE_CONTEXT);
            impressionInfo.setKeyword(ImpressionInfo.KEYWORD_NOT_AVAILABLE);
        } else if (PublicFeedIdentifier.Type.USER.equals(identifier.getType())) {
            impressionInfo.setContext(ImpressionInfo.USER_CONTEXT);
            impressionInfo.setKeyword(identifier.toName());
        } else if (PublicFeedIdentifier.Type.SOUND_SEARCH.equals(identifier.getType())) {
            impressionInfo.setContext(ImpressionInfo.SOUND_SEARCH_CONTEXT);
            impressionInfo.setKeyword(identifier.toName());
        } else if (PublicFeedIdentifier.Type.SOUND_TRENDING.equals(identifier.getType())) {
            impressionInfo.setContext(ImpressionInfo.SOUND_TRENDING_CONTEXT);
            impressionInfo.setKeyword(identifier.getType().getName());
        } else {
            Assertions.fail(new UnsupportedOperationException("Unreachable code identifier = " + identifier.toUniqueIdentifier() + " gfycat = " + gfycat.getGfyId()));
            impressionInfo.setContext(identifier.getType().getName());
            impressionInfo.setKeyword(identifier.toName());
        }
        return impressionInfo;
    }
}