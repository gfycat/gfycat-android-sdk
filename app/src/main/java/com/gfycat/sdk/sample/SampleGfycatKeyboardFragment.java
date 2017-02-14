package com.gfycat.sdk.sample;

import com.gfycat.core.FeedIdentifier;
import com.gfycat.core.gfycatapi.pojo.Gfycat;
import com.gfycat.keyboard.GfycatKeyboardFragment;

/**
 * Created by dekalo on 14.02.17.
 */
public class SampleGfycatKeyboardFragment extends GfycatKeyboardFragment {

    /**
     * Called when user clicked on gfycat in identifier.getName() category.
     *
     * @return true if GfycatPickerFragment should return to categories, false otherwise.
     */
    @Override
    public boolean onGfycatSelected(FeedIdentifier identifier, Gfycat gfycat, int position) {
        /**
         * Here you have Gfycat, and now you can get links to gfycat related files
         *
         * gfycat.getMobileMp4Url() - mobile optimized Mp4 file.
         * gfycat.getWebPUrl()      - webp file.
         * gfycat.getGif1mbUrl()    - GIF not bigger than 1 MB.
         * gfycat.getGif2mbUrl()    - GIF not bigger than 2 MB.
         * gfycat.getGif5mbUrl()    - GIF not bigger than 5 MB.
         */
        return false;
    }
}
