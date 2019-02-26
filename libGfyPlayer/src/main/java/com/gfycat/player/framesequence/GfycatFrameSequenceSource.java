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

package com.gfycat.player.framesequence;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.util.Pair;

import com.gfycat.common.ContextDetails;
import com.gfycat.common.utils.Assertions;
import com.gfycat.common.utils.Logging;
import com.gfycat.core.GfyCore;
import com.gfycat.core.NetworkUtils;
import com.gfycat.core.bi.analytics.GfycatAnalytics;
import com.gfycat.core.bi.corelogger.CoreLogger;
import com.gfycat.core.gfycatapi.pojo.Gfycat;
import com.gfycat.core.storage.CachedMediaFilesManager;
import com.gfycat.core.storage.MediaType;
import com.gfycat.framesequence.view.FrameSequenceSource;

import java.io.InterruptedIOException;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLProtocolException;

import io.reactivex.Single;

/**
 * Base {@link FrameSequenceSource} to display {@link Gfycat}'s.
 */
public abstract class GfycatFrameSequenceSource implements FrameSequenceSource {

    private static final String LOG_TAG = "GfycatFrameSequenceSource";

    private static final Integer MAX_INTERRUPTION_RETRY = 2;

    private final Gfycat gfycat;
    private final ContextDetails contextDetails;
    private final Context context;

    protected abstract MediaType getPlayerType();

    public GfycatFrameSequenceSource(Context context, Gfycat gfycat, ContextDetails details) {
        this.context = context;
        this.gfycat = gfycat;
        contextDetails = details.copy().put("gfyid", gfycat.getGfyId()).put("player", getPlayerType().getName());
    }

    public GfycatFrameSequenceSource(Context context, Gfycat gfycat) {
        this.context = context;
        this.gfycat = gfycat;
        contextDetails = new ContextDetails(Pair.create("gfyid", gfycat.getGfyId()), Pair.create("player", getPlayerType().getName()));
    }

    protected Gfycat getGfycat() {
        return gfycat;
    }

    @Override
    public String getId() {
        return gfycat.getGfyId();
    }

    @Override
    public String getType() {
        return "gfycat";
    }

    @NonNull
    @Override
    public ContextDetails getContextDetails() {
        return contextDetails;
    }

    @Override
    public int getAverageColorInt() {
        return gfycat.hasTransparency() ? Color.TRANSPARENT : gfycat.getAvgColorInt();
    }

    @Override
    public int getWidth() {
        return gfycat.getWidth();
    }

    @Override
    public int getHeight() {
        return gfycat.getHeight();
    }

    @Override
    public Single<Drawable> loadPoster() {
        return GfyCore.getMediaFilesManager().loadAsFile(gfycat, MediaType.posterType(gfycat))
                .retry((integer, throwable) -> integer < MAX_INTERRUPTION_RETRY && throwable instanceof InterruptedIOException)
                .map(file -> BitmapFactory.decodeFile(file.getPath()))
                .map(bitmap -> new BitmapDrawable(context.getResources(), bitmap));

    }

    @Override
    public void failedToGetPoster(Throwable throwable) {
        failedToGetContent(MediaType.posterType(gfycat), throwable);
    }

    @Override
    public void failedToGetFrameSequence(Throwable throwable) {
        failedToGetContent(getPlayerType(), throwable);
    }

    private void failedToGetContent(MediaType mediaType, Throwable throwable) {
        if (throwable instanceof SSLHandshakeException || throwable.getCause() instanceof SSLHandshakeException) {
            Assertions.fail(new SSLException("SSLHandshakeException happnes on get gfycat: " + getId() + " " + getContextDetails(), throwable));
        } else if (throwable instanceof SSLProtocolException || throwable.getCause() instanceof SSLProtocolException) {
            Assertions.fail(new SSLException("SSLProtocolException happnes on get gfycat: " + getId() + " " + getContextDetails(), throwable));
        } else if (throwable instanceof CachedMediaFilesManager.ForbiddenGfycatException) {
            GfycatAnalytics.getLogger(CoreLogger.class).logForbidden(getGfycat(), mediaType);
        } else if (!NetworkUtils.isAcceptableNetworkException(throwable)) {
            Assertions.fail(new FailedToGetContentException(LOG_TAG, throwable));
        } else {
            Logging.d(LOG_TAG, throwable, "Possible exception happens for " + gfycat);
        }
    }

    private static class FailedToGetContentException extends Exception {

        FailedToGetContentException(String message, Throwable throwable) {
            super(message, throwable);
        }
    }
}
