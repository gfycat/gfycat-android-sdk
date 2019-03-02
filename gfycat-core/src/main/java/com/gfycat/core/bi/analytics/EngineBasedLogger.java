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

package com.gfycat.core.bi.analytics;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by dekalo on 14.03.17.
 */

public class EngineBasedLogger implements BILogger {

    private BIEngine engine;

    @Override
    public void setupEngine(BIEngine engine) {
        this.engine = engine;
    }

    protected void track(String eventName) {
        track(eventName, new HashMap<>());
    }

    protected void track(String eventName, Map<String, String> params) {
        engine.track(eventName, params);
    }
}
