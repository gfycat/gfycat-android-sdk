package com.gfycat.picker;

import com.gfycat.core.FeedIdentifier;
import com.gfycat.core.gfycatapi.pojo.GfycatCategory;

public interface GfycatFeedSelectionResolver {
    /**
     * Called when user selected category {@link GfycatCategory}.
     */
    FeedIdentifier resolveCategoryFeed(GfycatCategory gfycatCategory);

    /**
     * Called when user selected category from search.
     */
    FeedIdentifier resolveSearchFeed(String searchQuery);
}
