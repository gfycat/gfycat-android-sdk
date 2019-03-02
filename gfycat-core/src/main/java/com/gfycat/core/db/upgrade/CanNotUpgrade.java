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

package com.gfycat.core.db.upgrade;

import android.database.sqlite.SQLiteException;

/**
 * Created by dekalo on 28.04.16.
 */
public class CanNotUpgrade extends SQLiteException {

    public CanNotUpgrade(String details, int from, int to) {
        super("Can not upgrade from " + from + " to " + to + ", because of: " + details);
    }

    public CanNotUpgrade(String details, int from, int to, Throwable throwable) {
        super("Can not upgrade from " + from + " to " + to + ", because of: " + details, throwable);
    }
}
