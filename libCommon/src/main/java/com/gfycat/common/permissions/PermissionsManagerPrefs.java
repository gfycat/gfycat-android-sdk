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

package com.gfycat.common.permissions;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by oleksandrbalandin on 10/20/17
 */

public class PermissionsManagerPrefs {
    private static final String SHARED_PREFS_FILE_NAME = "PermissionsManagerPrefs";
    private static final String PERMISSION_NEVER_ASK_AGAIN_PREFS_KEY = "permission_never_ask_again";

    public static boolean isPermissionNeverAskAgain(Context context, String permissionName) {
        return getPrefs(context).getBoolean(getPermissionPrefKey(permissionName), false);
    }

    public static void setPermissionNeverAskAgain(Context context, String permissionName, boolean isNeverAskAgain) {
        getPrefs(context).edit().putBoolean(getPermissionPrefKey(permissionName), isNeverAskAgain).apply();
    }

    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(SHARED_PREFS_FILE_NAME, Context.MODE_PRIVATE);
    }

    private static String getPermissionPrefKey(String permissionName) {
        return permissionName + "_" +PERMISSION_NEVER_ASK_AGAIN_PREFS_KEY;
    }
}
