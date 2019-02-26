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

package com.gfycat.core;

import android.annotation.SuppressLint;
import android.view.View;

import com.gfycat.common.utils.VersionUtils;

/**
 * Created by anton on 2/10/17.
 */

public class ApiWrapper {
    /**
     * Fix for dalvikvm warning dropped to logs
     * if we have(but do not call) non-existing virtual method in our class dalvik will drop
     * warning to logs about this. Wrapper method in another fix this.
     */
    @SuppressLint("NewApi")
    public static void setElevation(View instance, int elevation) {
        if (VersionUtils.isAtLeastLollipop())
            instance.setElevation(elevation);
    }
}
