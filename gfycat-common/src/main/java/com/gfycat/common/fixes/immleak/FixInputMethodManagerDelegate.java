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

package com.gfycat.common.fixes.immleak;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;

import com.gfycat.common.lifecycledelegates.BaseActivityDelegate;

/**
 * Fix for https://code.google.com/p/android/issues/detail?id=34731.
 * <p>
 * Created by dekalo on 03.03.16.
 */
public class FixInputMethodManagerDelegate extends BaseActivityDelegate {

    public FixInputMethodManagerDelegate(Activity activity) {
        super(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        IMMLeaks.fixFocusedViewLeak(getActivity().getApplication());
    }

    @Override
    public void onDestroy() {
        fixInputMethodManager();
    }

    private void fixInputMethodManager() {
        final Object imm = getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        final Reflector.TypedObject windowToken = new Reflector.TypedObject(getActivity().getWindow().getDecorView().getWindowToken(), IBinder.class);
        Reflector.invokeMethodExceptionSafe(imm, "windowDismissed", windowToken);
        final Reflector.TypedObject view = new Reflector.TypedObject(null, View.class);
        Reflector.invokeMethodExceptionSafe(imm, "startGettingWindowFocus", view);
    }
}
