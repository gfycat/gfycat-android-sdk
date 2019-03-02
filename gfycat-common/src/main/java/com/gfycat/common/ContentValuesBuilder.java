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

import android.content.ContentValues;

/**
 * Created by dekalo on 21.09.16.
 */
public class ContentValuesBuilder {

    private ContentValues contentValues = new ContentValues();
    private boolean used = false;

    private void guard() {
        if (used) {
            contentValues = new ContentValues(contentValues);
            used = false;
        }
    }

    public ContentValuesBuilder put(String key, String value) {
        guard();
        contentValues.put(key, value);
        return this;
    }


    public ContentValuesBuilder put(String key, long value) {
        guard();
        contentValues.put(key, value);
        return this;
    }

    public ContentValuesBuilder put(String key, int value) {
        guard();
        contentValues.put(key, value);
        return this;
    }

    public ContentValues build() {
        guard();
        used = true;
        return contentValues;
    }
}
