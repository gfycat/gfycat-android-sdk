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

/**
 * Created by dekalo on 06.11.15.
 */
public class FPSDebugger {

    private int framesCount = 0;
    private final long frameDuration[];

    /**
     * @param framesToKeep - baed on this count of last frames max and avg would be generated.
     */
    public FPSDebugger(int framesToKeep) {
        frameDuration = new long[framesToKeep];
    }

    public void addFrame(long duration) {
        frameDuration[framesCount % frameDuration.length] = duration;
        framesCount++;
    }

    public long avg() {
        if(framesCount == 0) return -1;
        long sum = 0;
        int size = Math.min(frameDuration.length, framesCount);
        for (int i = 0; i < Math.min(frameDuration.length, framesCount); i++) {
            sum += frameDuration[i];
        }
        return sum / size;
    }

    public long max() {
        long max = Long.MIN_VALUE;
        int size = Math.min(frameDuration.length, framesCount);
        for (int i = 0; i < size; i++) {
            if (max < frameDuration[i]) {
                max = frameDuration[i];
            }
        }
        return max;
    }

    public long last() {
        return frameDuration[framesCount % frameDuration.length];
    }
}
