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
 * Created by dekalo on 22.02.16.
 */
public class EPS {

    public static final float EPS = 0.001f;

    public static boolean isAboutZero(float value) {
        return isAboutZero(value, EPS);
    }

    public static boolean isAboutZero(float value, float accuracy) {
        return Math.abs(value) < accuracy;
    }

    public static boolean isLessThan(float value, int greaterValue) {
        return value < greaterValue + EPS;
    }

    public static boolean isLessThan(float value, float greaterValue) {
        return value < greaterValue + EPS;
    }

    public static boolean isLessThan(float value, float greaterValue, float accuracy) {
        return value < greaterValue + accuracy;
    }

    public static boolean isSame(float a, int b) {
        return isAboutZero(a - b);
    }

    public static boolean isSame(float a, float b) {
        return isAboutZero(a - b);
    }

    public static boolean isSame(float a, float b, float accuracy) {
        return isAboutZero(a - b, accuracy);
    }
}
