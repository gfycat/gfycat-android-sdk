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

package com.gfycat.common.permissions.delegates;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

/**
 * Created by anton on 11/2/16.
 */

public class FragmentPermissionDelegate extends PermissionDelegate<Fragment> {
    public FragmentPermissionDelegate(Fragment target) {
        super(target);
    }

    @Override
    public boolean shouldShowRequestPermissionRationale(@NonNull String[] permissions) {
        for (String permission : permissions)
            if (getTarget().shouldShowRequestPermissionRationale(permission))
                return true;

        return false;
    }

    @Override
    public void requestPermission(@NonNull String[] permissions, int requestCode) {
        getTarget().requestPermissions(permissions, requestCode);
    }

    @Override
    public Context getContext() {
        return getTarget().getActivity();
    }
}
