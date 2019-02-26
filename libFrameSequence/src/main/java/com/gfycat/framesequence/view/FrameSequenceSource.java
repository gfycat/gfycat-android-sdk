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

package com.gfycat.framesequence.view;

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

import com.gfycat.common.ContextDetails;
import com.gfycat.framesequence.DropFramesStrategy;
import com.gfycat.framesequence.FrameSequence;

import java.io.File;

import io.reactivex.Single;

/**
 * Initialization source for {@link FrameSequenceView}
 */
public interface FrameSequenceSource {

    /**
     * @return Returns {@link ContextDetails} for logging purposes.
     */
    @NonNull
    ContextDetails getContextDetails();

    /**
     * @return Returns average color for placeholder.
     */
    int getAverageColorInt();

    /**
     * @return Returns width of {@link FrameSequence} item.
     */
    int getWidth();

    /**
     * @return Returns height of {@link FrameSequence} item.
     */
    int getHeight();

    /**
     * @return Returns observable to load poster as {@link File}.
     */
    Single<Drawable> loadPoster();

    /**
     * @return Returns {@link Single} to load {@link FrameSequence}
     */
    Single<FrameSequence> loadFrameSequence();

    /**
     * @return Returns content id.
     */
    String getId();

    /**
     * @return Returns content type.
     */
    String getType();

    /**
     * Called when content was failed to load.
     */
    void failedToGetPoster(Throwable throwable);

    /**
     * Called when content was failed to load.
     */
    void failedToGetFrameSequence(Throwable throwable);

    /**
     * Returns drop frames strategy for FrameSequence.
     */
    DropFramesStrategy getDropFramesStrategy();
}
