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

/**
 * Created by dekalo on 09/02/18.
 */

public class CountDownRunnableRunner {

    private final Runnable target;
    private int count;

    public CountDownRunnableRunner(int count, Runnable target) {
        if (count <= 0) throw new IllegalArgumentException();
        this.count = count;
        this.target = target;
    }

    public void countDown() {
        count--;
        if (count == 0) target.run();
    }
}
