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

package com.gfycat.common.recycler;

import android.support.v7.widget.RecyclerView;

/**
 * Created by dekalo on 19.01.17.
 */

public class PlaybackManager {

    private final AutoPlayController autoPlayController;
    private RecyclerView recyclerView;

    private boolean shouldPlay = true;
    private boolean started = false;

    public PlaybackManager(AutoPlayController autoPlayController) {
        this.autoPlayController = autoPlayController;
    }

    public void shouldPlay(boolean shouldPlay) {
        this.shouldPlay = shouldPlay;
        invalidate();
    }

    public void started() {
        started = true;
        invalidate();
    }

    public void stopped() {
        started = false;
        invalidate();
    }

    public void setRecyclerView(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
        invalidate();
    }

    private void invalidate() {
        if (recyclerView != null) {
            autoPlayController.setEnabled(recyclerView, started && shouldPlay);
        }
    }
}
