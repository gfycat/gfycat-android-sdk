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

package com.gfycat.core.authentication;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by dekalo on 12.07.17.
 */

public class GhostUserPreference {

    public static final String GHOST_USER_KEY = "ghost_user";

    private final SharedPreferences preferences;

    public GhostUserPreference(Context context) {
        this.preferences = context.getSharedPreferences(GHOST_USER_KEY, Context.MODE_PRIVATE);
    }

    public void setUserGhost() {
        setGhostFlag(true);
    }

    public void setUserReal() {
        setGhostFlag(false);
    }

    private void setGhostFlag(boolean value) {
        preferences.edit().putBoolean(GHOST_USER_KEY, value).apply();
    }

    public boolean isUserGhost() {
        return preferences.getBoolean(GHOST_USER_KEY, true);
    }
}
