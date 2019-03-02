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

package com.gfycat.gif;

import android.content.Context;

import com.gfycat.common.ContextDetails;
import com.gfycat.core.GfyCore;
import com.gfycat.core.gfycatapi.pojo.Gfycat;
import com.gfycat.core.storage.MediaType;
import com.gfycat.framesequence.DropFramesStrategy;
import com.gfycat.framesequence.FrameSequence;
import com.gfycat.gif.view.GfycatGifView;
import com.gfycat.player.framesequence.GfycatFrameSequenceSource;

import io.reactivex.Single;

/**
 * {@link GfycatGifFrameSequenceSource} for {@link com.gfycat.framesequence.view.FrameSequenceView} to display 1MB GIF image from gfycat.com.
 * <p>
 * Used in {@link GfycatGifView}.
 */
public class GfycatGifFrameSequenceSource extends GfycatFrameSequenceSource {

    public GfycatGifFrameSequenceSource(Context context, Gfycat gfycat, ContextDetails details) {
        super(context, gfycat, details);
    }

    public GfycatGifFrameSequenceSource(Context context, Gfycat gfycat) {
        super(context, gfycat);
    }

    @Override
    protected MediaType getPlayerType() {
        return MediaType.GIF1;
    }

    @Override
    public Single<FrameSequence> loadFrameSequence() {
        return GfyCore.getMediaFilesManager().loadAsByteArray(getGfycat(), getPlayerType())
                .flatMap(this::safeCreateFrameSequence);
    }

    private Single<FrameSequence> safeCreateFrameSequence(byte[] data) {
        try {
            return Single.just(new GifFrameSequence(data));
        } catch (Exception e) {
            String message = "gfyId = " + getId() + " gifSource(" + getPlayerType().getName() + ") = " + getPlayerType().getUrl(getGfycat());
            return Single.error(new BrokenGifException(message, e));
        }
    }

    @Override
    public DropFramesStrategy getDropFramesStrategy() {
        return DropFramesStrategy.DropAllowed;
    }
}
