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

package com.gfycat.common;

import android.annotation.SuppressLint;
import android.graphics.SurfaceTexture;
import android.view.Surface;

import java.io.Closeable;
import java.io.IOException;

/**
 * Surface that implements Closeable interface.
 * <p/>
 * Created by dekalo on 04.01.16.
 */
@SuppressLint("ParcelCreator")
public class ClosableSurface extends Surface implements Closeable {

    /**
     * Create Surface from a {@link SurfaceTexture}.
     * <p/>
     * Images drawn to the Surface will be made available to the {@link
     * SurfaceTexture}, which can attach them to an OpenGL ES texture via {@link
     * SurfaceTexture#updateTexImage}.
     *
     * @param surfaceTexture The {@link SurfaceTexture} that is updated by this
     *                       Surface.
     * @throws OutOfResourcesException if the surface could not be created.
     */
    public ClosableSurface(SurfaceTexture surfaceTexture) {
        super(surfaceTexture);
    }

    @Override
    public void close() throws IOException {
        release();
    }
}
