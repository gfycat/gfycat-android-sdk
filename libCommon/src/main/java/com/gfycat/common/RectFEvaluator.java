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

import android.animation.TypeEvaluator;
import android.graphics.RectF;

/**
 * Created by dekalo on 25/10/17.
 */

public class RectFEvaluator implements TypeEvaluator<RectF> {
    @Override
    public RectF evaluate(float fraction, RectF s, RectF e) {
        return new RectF(
                (s.left + fraction * (e.left - s.left)),
                (s.top + fraction * (e.top - s.top)),
                (s.right + fraction * (e.right - s.right)),
                (s.bottom + fraction * (e.bottom - s.bottom)));
    }
}
