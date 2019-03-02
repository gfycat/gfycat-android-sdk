package com.gfycat.picker;

import com.gfycat.core.FeedIdentifier;
import com.gfycat.core.PublicFeedIdentifier;
import com.gfycat.core.RecentFeedIdentifier;
import com.gfycat.core.gfycatapi.pojo.GfycatCategory;

/**
 * Default feed selection resolver for picker.
 */
public class DefaultFeedSelectionResolver implements GfycatFeedSelectionResolver {

    private static final String TRENDING_TAG = PublicFeedIdentifier.Type.TRENDING.getName();

    @Override
    public FeedIdentifier resolveCategoryFeed(GfycatCategory category) {
        if (TRENDING_TAG.equals(category.getTag())) {
            return PublicFeedIdentifier.fromReaction(TRENDING_TAG);
        } else if (RecentFeedIdentifier.RECENT_FEED_TYPE.getName().equals(category.getTag())) {
            return RecentFeedIdentifier.recent();
        } else {
            return PublicFeedIdentifier.fromSearch(category.getTag());
        }
    }

    @Override
    public FeedIdentifier resolveSearchFeed(String searchQuery) {
        return PublicFeedIdentifier.fromSearch(searchQuery);
    }
}
