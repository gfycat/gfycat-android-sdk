package com.gfycat.sdk.sample;

import com.gfycat.core.FeedIdentifier;
import com.gfycat.core.PublicFeedIdentifier;
import com.gfycat.core.RecentFeedIdentifier;
import com.gfycat.core.gfycatapi.pojo.GfycatCategory;
import com.gfycat.picker.GfycatFeedSelectionResolver;

public class SoundOnlyFeedResolver implements GfycatFeedSelectionResolver {
    @Override
    public FeedIdentifier resolveCategoryFeed(GfycatCategory gfycatCategory) {
        if (RecentFeedIdentifier.RECENT.equals(gfycatCategory.getTag())) {
            return RecentFeedIdentifier.recent();
        } else if (PublicFeedIdentifier.Type.TRENDING.getName().equals(gfycatCategory.getTag())) {
            return PublicFeedIdentifier.soundTrending();
        } else {
            return PublicFeedIdentifier.fromSoundSearch(gfycatCategory.getTag());
        }
    }

    @Override
    public FeedIdentifier resolveSearchFeed(String searchQuery) {
        return PublicFeedIdentifier.fromSearch(searchQuery);
    }
}
