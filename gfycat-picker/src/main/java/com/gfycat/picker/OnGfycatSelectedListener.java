package com.gfycat.picker;

import com.gfycat.core.FeedIdentifier;
import com.gfycat.core.gfycatapi.pojo.Gfycat;

/**
 * Provides Gfycat click handling functionality for SDK users.
 */
public interface OnGfycatSelectedListener {
    /**
     * Called when user clicked on gfycat in identifier.getName() category.
     */
    void onGfycatSelected(FeedIdentifier identifier, Gfycat gfycat, int position);
}
