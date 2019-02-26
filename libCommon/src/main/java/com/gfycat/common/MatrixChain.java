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

import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;

/**
 * Utility class that provides short variant of matrix manipulation.
 * <p>
 * Created by dekalo on 24.12.15.
 */
public class MatrixChain {

    private Matrix matrix;

    public MatrixChain() {
        matrix = new Matrix();
    }

    public MatrixChain(Matrix matrix) {
        this.matrix = new Matrix(matrix);
    }

    public MatrixChain invert() {
        Matrix inverted = new Matrix(matrix);
        matrix.invert(inverted);
        matrix = inverted;
        return this;
    }

    public float getScaleY() {
        float values[] = new float[9];
        matrix.getValues(values);
        return values[Matrix.MSCALE_Y];
    }

    public float getScaleX() {
        float values[] = new float[9];
        matrix.getValues(values);
        return values[Matrix.MSCALE_X];
    }

    public MatrixChain postRotate(float degrees, float px, float py) {
        matrix.postRotate(degrees, px, py);
        return this;
    }

    public MatrixChain rectToRect(RectF src, RectF dst) {
        matrix.setRectToRect(src, dst, Matrix.ScaleToFit.FILL);
        return this;
    }

    public Matrix get() {
        return matrix;
    }

    public MatrixChain scale(float scale) {
        matrix.postScale(scale, scale);
        return this;
    }

    public MatrixChain scale(float sx, float sy) {
        matrix.postScale(sx, sy);
        return this;
    }

    public MatrixChain move(float dx, float dy) {
        matrix.postTranslate(dx, dy);
        return this;
    }

    public MatrixChain multiply(Matrix matrix) {
        this.matrix.postConcat(matrix);
        return this;
    }
}
