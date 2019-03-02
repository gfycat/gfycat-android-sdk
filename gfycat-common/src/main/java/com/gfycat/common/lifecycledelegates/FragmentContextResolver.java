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

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;

/**
 * Created by dekalo on 02.11.16.
 */

public class FragmentContextResolver implements ContextResolver {

    private final Fragment fragment;

    public FragmentContextResolver(Fragment fragment) {
        this.fragment = fragment;
    }

    @Override
    public Context getContext() {
        return fragment.getContext();
    }

    @Override
    public String getStateForLogging() {
        return "" +
                "context = " + fragment.getContext() + " " +
                "isAdded = " + fragment.isAdded() + " " +
                "isDetached = " + fragment.isDetached() + " " +
                "isRemoving = " + fragment.isRemoving() + " " +
                "isResumed = " + fragment.isResumed() + " " +
                "isHidden = " + fragment.isHidden() + " " +
                "isInLayout = " + fragment.isInLayout();
    }
}
