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

import android.content.pm.PackageManager;

import java.security.Permission;
import java.util.Arrays;

/**
 * Created by dekalo on 04.11.16.
 */

public class PermissionRequest {

    private final String[] permissionsNeeded;
    private final int[] permissionsStates;
    private final Runnable granted;
    private final Runnable denied;

    public PermissionRequest(String[] permissionsNeeded, Runnable granted, Runnable denied) {
        this.permissionsNeeded = permissionsNeeded;
        this.permissionsStates = new int[permissionsNeeded.length];
        Arrays.fill(this.permissionsStates, PackageManager.PERMISSION_DENIED);
        this.granted = granted;
        this.denied = denied;
    }

    public boolean hasPermission(String permission) {
        for (String permissionNeeded : permissionsNeeded) {
            return permissionNeeded.equals(permission);
        }
        return false;
    }

    public void updatePermissionStateIfExists(String permission, int state) {
        for (int i = 0; i < permissionsNeeded.length; i++) {
            if (permissionsNeeded[i].equals(permission)) {
                permissionsStates[i] = state;
            }
        }
    }

    public boolean isAllPermissionsGranted() {
        for (int state : permissionsStates)
            if (state != PackageManager.PERMISSION_GRANTED)
                return false;
        return true;
    }

    public String[] permissionsNeeded() {
        return permissionsNeeded;
    }

    public void permissionsGranted() {
        granted.run();
    }

    public void permissionsDenied() {
        denied.run();
    }
}
