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

package com.gfycat.common.lifecycledelegates;

import com.gfycat.common.Func0;
import com.gfycat.common.utils.Logging;


/**
 * Created by dekalo on 17/11/17.
 */

public class LifecycleLoggingDelegate extends ContextBaseDelegate {

    private final Func0<String> additionalInfo;
    private final String logTag;

    public LifecycleLoggingDelegate(ContextResolver contextResolver, String logTag, Func0<String> additionalInfo) {
        super(contextResolver);
        this.logTag = logTag;
        this.additionalInfo = additionalInfo;
    }

    public LifecycleLoggingDelegate(ContextResolver contextResolver, String logTag) {
        this(contextResolver, logTag, () -> "");
    }

    @Override
    public void onResume() {
        Logging.c(logTag, "onResume() ", additionalInfo.call());
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        Logging.c(logTag, "onPause() ", additionalInfo.call());
    }
}
