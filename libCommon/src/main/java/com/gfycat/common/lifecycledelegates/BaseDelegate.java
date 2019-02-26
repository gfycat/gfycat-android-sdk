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

package com.gfycat.common.lifecycledelegates;

import android.content.Intent;
import android.os.Bundle;

/**
 * Created by dekalo on 02.11.16.
 */

public class BaseDelegate implements LifecycleDelegate {

    private boolean isResumed;
    private boolean isStarted;
    private boolean isDestroyed;

    public boolean isStarted() {
        return isStarted;
    }

    public boolean isDestroyed() {
        return isDestroyed;
    }

    public boolean isResumed() {
        return isResumed;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
    }

    @Override
    public void onStart() {
        isStarted = true;
    }

    @Override
    public void onResume() {
        isResumed = true;
    }

    @Override
    public void onPause() {
        isResumed = false;
    }

    @Override
    public void onStop() {
        isStarted = false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

    }

    @Override
    public void onDestroy() {
        isDestroyed = true;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

    }
}
