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

import android.os.Process;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;

import static java.util.concurrent.Executors.newFixedThreadPool;

/**
 * Class to configure FrameSequenceDrawable class.
 * <p/>
 * Created by dekalo on 09.11.15.
 */
public class FrameSequenceConfiguration {


    public enum LogLevel {
        NONE,
        VERBOSE
    }

    private static final long DEFAULT_MIN_TIME_TO_RENDER_NEXT_FRAME = 40; //ms
    private static FrameSequenceConfiguration INSTANCE;

    private final LogLevel logLevel;
    private final long minTimeToRenderNextFrame;
    private final ExecutorService decodingExecutorService;

    private FrameSequenceConfiguration(long minTimeToRenderNextFrame, ExecutorService decodingExecutor, LogLevel logLevel) {
        this.minTimeToRenderNextFrame = ((minTimeToRenderNextFrame == -1) ? DEFAULT_MIN_TIME_TO_RENDER_NEXT_FRAME : minTimeToRenderNextFrame);
        this.decodingExecutorService = ((decodingExecutor != null) ? decodingExecutor : getDefaultDecodingExecutor());
        this.logLevel = ((logLevel != null) ? logLevel : LogLevel.NONE);
    }

    public static synchronized void init(FrameSequenceConfiguration configuration) {
        if (INSTANCE != null)
            throw new IllegalStateException("FrameSequenceConfiguration::could be called only once. Be sure that this method was called before any drawable was rendered.");
        INSTANCE = configuration;
    }

    private static void ensureInitialized() {
        if (INSTANCE == null) {
            INSTANCE = new FrameSequenceConfiguration.Builder().build();
        }
    }

    public static FrameSequenceConfiguration get() {
        ensureInitialized();
        return INSTANCE;
    }

    public static boolean loggingEnabled() {
        return LogLevel.VERBOSE.equals(get().getLogLevel());
    }

    public LogLevel getLogLevel() {
        return logLevel;
    }

    public Executor getDecodingExecutor() {
        return decodingExecutorService;
    }

    public long getMinTimeToRenderNextFrame() {
        return minTimeToRenderNextFrame;
    }

    private static ExecutorService getDefaultDecodingExecutor() {
        int threadsCount = getDecodingExecutorThreadsCount();
        return newFixedThreadPool(threadsCount, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable runnable) {
                return new Thread(
                        new Runnables(
                                new ThreadPriorityRunnable(Process.THREAD_PRIORITY_BACKGROUND),
                                runnable));
            }
        });
    }

    private static int getDecodingExecutorThreadsCount() {
        switch (Runtime.getRuntime().availableProcessors()) {
            case 1:
                return 1;
            case 2:
            case 3:
            case 4:
                return 2;
            case 5:
            case 6:
                return 3;
            case 7:
            case 8:
                return 4;
            default: // for future
                return 5;
        }
    }

    public static class Builder {

        private LogLevel logLevel;
        private long minTimeToRenderNextFrame = -1;
        private ExecutorService decodingExecutor;

        public Builder setLogLevel(LogLevel logLevel) {
            this.logLevel = logLevel;
            return this;
        }

        public Builder setDecodingExecutor(ExecutorService decodingExecutor) {
            this.decodingExecutor = decodingExecutor;
            return this;
        }

        public Builder setMinTimeToRenderNextFrame(long minTimeToRenderNextFrame) {
            this.minTimeToRenderNextFrame = minTimeToRenderNextFrame;
            return this;
        }

        public FrameSequenceConfiguration build() {
            return new FrameSequenceConfiguration(minTimeToRenderNextFrame, decodingExecutor, logLevel);
        }

    }
}
