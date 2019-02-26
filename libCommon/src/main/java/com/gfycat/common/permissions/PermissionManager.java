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
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;

import com.gfycat.common.permissions.delegates.PermissionDelegate;
import com.gfycat.common.utils.Logging;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Created by dekalo on 11.10.16.
 */

public class PermissionManager {

    private final List<PermissionRequest> permissionRequests;
    private final int requestCode;
    private final PermissionDelegate delegate;

    public PermissionManager(PermissionDelegate delegate, int requestCode, PermissionRequest... permissionRequest) {
        this.delegate = delegate;
        this.permissionRequests = new ArrayList<>(Arrays.asList(permissionRequest));
        this.requestCode = requestCode;
    }

    public void requestPermissions() {
        /* Check permissions for Android 6.+ */
        List<String> permissionsForRequest = new ArrayList<>();
        Iterator<PermissionRequest> iterator = permissionRequests.iterator();
        while (iterator.hasNext()) {
            PermissionRequest request = iterator.next();
            for (String key : request.permissionsNeeded()) {
                boolean permissionValue = PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(delegate.getContext(), key);
                if (!permissionValue) {
                    if (!permissionsForRequest.contains(key)) {
                        permissionsForRequest.add(key);
                    }
                } else {
                    request.updatePermissionStateIfExists(key, PackageManager.PERMISSION_GRANTED);
                }
            }

            if (request.isAllPermissionsGranted()) {
                request.permissionsGranted();
                iterator.remove();
            }
        }

        if (!permissionsForRequest.isEmpty()) {
            delegate.requestPermission(permissionsForRequest.toArray(new String[permissionsForRequest.size()]), requestCode);
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        Logging.logPermissions(delegate.getContext());
        if (this.requestCode == requestCode) {
            for (PermissionRequest request : permissionRequests) {
                for (int i = 0; i < permissions.length; i++) {
                    request.updatePermissionStateIfExists(permissions[i], grantResults[i]);
                }

                for (int i = 0; i < permissions.length; i++) {
                    if (grantResults.length > 0 && grantResults[i] == PackageManager.PERMISSION_DENIED
                            && !delegate.shouldShowRequestPermissionRationale(new String[]{permissions[i]})) {
                        PermissionsManagerPrefs.setPermissionNeverAskAgain(delegate.getContext(), permissions[i], true);
                    }
                }

                if (request.isAllPermissionsGranted()) {
                    request.permissionsGranted();
                } else {
                    request.permissionsDenied();
                }
            }
        }
    }

    public boolean isPermissionNeverAskAgain(String permissionName) {
        return PermissionsManagerPrefs.isPermissionNeverAskAgain(delegate.getContext(), permissionName);
    }
}
