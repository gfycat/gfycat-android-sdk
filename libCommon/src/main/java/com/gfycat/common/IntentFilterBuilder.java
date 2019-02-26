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

import android.content.IntentFilter;

/**
 * Created by dekalo on 31.08.17.
 */

public class IntentFilterBuilder {
    private IntentFilter intentFilter;

    public IntentFilterBuilder() {
        this.intentFilter = new IntentFilter();
    }

    public IntentFilterBuilder addAction(String action) {
        intentFilter.addAction(action);
        return this;
    }

    public IntentFilter build() {
        IntentFilter result = intentFilter;
        intentFilter = new IntentFilter();
        return result;

    }
}
