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

package com.gfycat.common;

import android.os.Bundle;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by dekalo on 18.10.16.
 */

public class BundleBuilder {

    private Bundle bundle = new Bundle();

    public BundleBuilder putString(String key, String val) {
        bundle.putString(key, val);
        return this;
    }

    public BundleBuilder putSerializable(String key, Serializable serializable) {
        bundle.putSerializable(key, serializable);
        return this;
    }

    public BundleBuilder putParcelable(String key, Parcelable parcelable) {
        bundle.putParcelable(key, parcelable);
        return this;
    }

    public BundleBuilder putLong(String key, long value) {
        bundle.putLong(key, value);
        return this;
    }

    public Bundle build() {
        return bundle;
    }
}
