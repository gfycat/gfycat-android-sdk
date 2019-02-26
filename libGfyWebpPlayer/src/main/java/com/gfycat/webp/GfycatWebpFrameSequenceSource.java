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

package com.gfycat.webp;

import android.content.Context;

import com.gfycat.common.ContextDetails;
import com.gfycat.core.GfyCore;
import com.gfycat.core.bi.analytics.GfycatAnalytics;
import com.gfycat.core.bi.corelogger.CoreLogger;
import com.gfycat.core.gfycatapi.pojo.Gfycat;
import com.gfycat.core.storage.MediaType;
import com.gfycat.framesequence.DropFramesStrategy;
import com.gfycat.framesequence.FrameSequence;
import com.gfycat.player.framesequence.GfycatFrameSequenceSource;

import io.reactivex.Single;

/**
 * {@link GfycatFrameSequenceSource} for {@link com.gfycat.framesequence.view.FrameSequenceView} to display webp image.
 * <p>
 * Used in {@link com.gfycat.webp.view.GfycatWebpView}.
 */
public class GfycatWebpFrameSequenceSource extends GfycatFrameSequenceSource {

    public GfycatWebpFrameSequenceSource(Context context, Gfycat gfycat, ContextDetails details) {
        super(context, gfycat, details);
    }

    public GfycatWebpFrameSequenceSource(Context context, Gfycat gfycat) {
        super(context, gfycat);
    }

    @Override
    protected MediaType getPlayerType() {
        return MediaType.WEBP;
    }

    @Override
    public Single<FrameSequence> loadFrameSequence() {
        return GfyCore.getMediaFilesManager().loadAsByteArray(getGfycat(), getPlayerType(), getContextDetails())
                .flatMap(this::safeCreateFrameSequence);
    }

    private Single<FrameSequence> safeCreateFrameSequence(byte[] bytes) {
        try {
            return Single.just(getGfycat().hasTransparency() ? new WebPNewFrameSequence(bytes) : new WebPOldFrameSequence(bytes));
        } catch (IllegalArgumentException e) {
            String message = "gfyId = " + getId() + " webpSource = " + MediaType.WEBP.getUrl(getGfycat());
            return Single.error(new BrokenWebPFrameSequenceException(message, e));
        }
    }

    @Override
    public void failedToGetFrameSequence(Throwable throwable) {
        if (throwable instanceof BrokenWebPFrameSequenceException) {
            GfycatAnalytics.getLogger(CoreLogger.class).logBrokenContent(getGfycat(), getPlayerType());
        } else {
            super.failedToGetFrameSequence(throwable);
        }
    }

    @Override
    public DropFramesStrategy getDropFramesStrategy() {
        return getGfycat().hasTransparency() ? DropFramesStrategy.DropNotAllowed : DropFramesStrategy.KeyFrameOrDropAllowed;
    }
}
