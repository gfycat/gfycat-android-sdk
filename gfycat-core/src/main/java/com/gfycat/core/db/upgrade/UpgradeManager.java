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

import android.database.sqlite.SQLiteDatabase;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by dekalo on 28.04.16.
 */
public class UpgradeManager {

    private SQLiteDatabase db;
    private int currentVersion;
    private final Map<Integer, UpgradeScript> upgradeScripts = new HashMap<>();

    public UpgradeManager(SQLiteDatabase db, int oldVersion) {
        this.db = db;
        this.currentVersion = oldVersion;
    }

    public void upgradeTo(int newVersion) throws CanNotUpgrade {
        while (currentVersion != newVersion) {
            UpgradeScript currentScript = upgradeScripts.get(currentVersion);
            if (currentScript == null)
                throw new CanNotUpgrade("No upgrade script.", currentVersion, newVersion);
            currentVersion = upgradeScripts.get(currentVersion).upgrade(db);
        }
    }
}
