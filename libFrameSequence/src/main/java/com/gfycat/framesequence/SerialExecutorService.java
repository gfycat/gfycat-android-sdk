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

package com.gfycat.framesequence;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by dekalo on 06.11.15.
 */
public class SerialExecutorService extends ConstrainedExecutorService {

    public SerialExecutorService(Executor executor) {
        // SerialExecutorService is just a ConstrainedExecutorService with a concurrency limit
        // of one and an unbounded work queue.
        super("SerialExecutor", 1, executor, new LinkedBlockingQueue<Runnable>());
    }

    /**
     * Synchronized override of {@link ConstrainedExecutorService#execute(Runnable)} to
     * ensure that view of memory is consistent between different threads executing tasks serially.
     *
     * @param runnable The task to be executed.
     */
    @Override
    public synchronized void execute(Runnable runnable) {
        super.execute(runnable);
    }
}
