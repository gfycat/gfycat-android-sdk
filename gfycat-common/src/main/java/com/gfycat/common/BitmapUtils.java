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

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.media.ExifInterface;
import android.util.Base64;
import android.util.Pair;

import com.gfycat.common.utils.Utils;

import org.apache.commons.io.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by Andrew Khloponin
 */
public class BitmapUtils {

    public static Pair<Integer, Integer> getScaleSize(float width, float height, float maxWidth, float maxHeight) {
        float w = width, h = height;

        float ar = width / height;
        float maxAr = maxWidth / maxHeight;

        if (maxAr > ar) {
            if (height > maxHeight) {
                h = maxHeight;
                w = h * width / height;
            }
        } else {
            if (width > maxWidth) {
                w = maxWidth;
                h = w * height / width;
            }
        }
        return new Pair<>(Math.round(w), Math.round(h));
    }

    public static Bitmap centerCrop(Bitmap srcBmp) {
        Bitmap dstBmp = null;
        if (srcBmp.getWidth() >= srcBmp.getHeight()) {

            dstBmp = Bitmap.createBitmap(
                    srcBmp,
                    srcBmp.getWidth() / 2 - srcBmp.getHeight() / 2,
                    0,
                    srcBmp.getHeight(),
                    srcBmp.getHeight()
            );

        } else {

            dstBmp = Bitmap.createBitmap(
                    srcBmp,
                    0,
                    srcBmp.getHeight() / 2 - srcBmp.getWidth() / 2,
                    srcBmp.getWidth(),
                    srcBmp.getWidth()
            );
        }

        return dstBmp;
    }

    public static Bitmap asRoundedCorners(Bitmap source, final float radius) {
        return asRoundedCorners(source, radius, 0);
    }

    public static Bitmap asRoundedCorners(Bitmap source, final float radius, final float margin) {

        final Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setShader(new BitmapShader(source, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));

        Bitmap output = Bitmap.createBitmap(source.getWidth(), source.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        canvas.drawRoundRect(new RectF(margin, margin, source.getWidth() - margin, source.getHeight() - margin), radius, radius, paint);

        if (source != output) {
            source.recycle();
        }

        return output;
    }

    public static Bitmap asCircle(Bitmap source, Bitmap.Config config) {
        int size = Math.min(source.getWidth(), source.getHeight());

        int x = (source.getWidth() - size) / 2;
        int y = (source.getHeight() - size) / 2;

        Bitmap squaredBitmap = Bitmap.createBitmap(source, x, y, size, size);
        if (squaredBitmap != source) {
            source.recycle();
        }

        Bitmap bitmap = Bitmap.createBitmap(size, size, config);

        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        BitmapShader shader = new BitmapShader(squaredBitmap, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP);
        paint.setShader(shader);
        paint.setAntiAlias(true);

        float r = size / 2f;
        canvas.drawCircle(r, r, r, paint);

        squaredBitmap.recycle();
        return bitmap;
    }

    public static Bitmap createColorBitmap(int width, int height, int color) {
        Rect rect = new Rect(0, 0, width, height);

        Bitmap image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(image);

        Paint paint = new Paint();
        paint.setColor(color);

        canvas.drawRect(rect, paint);

        return image;
    }

    public static Bitmap asCircle(Bitmap source) {
        return asCircle(source, source.getConfig());
    }

    public static Bitmap fromBase64String(String s) {
        return fromByteArray(Base64.decode(s, 0));
    }

    public static String toBase64String(Bitmap bitmap) {
        return Base64.encodeToString(toByteArray(bitmap), 0);
    }

    public static Bitmap fromByteArray(byte[] data) {
        return BitmapFactory.decodeByteArray(data, 0, data.length);
    }

    public static byte[] toByteArray(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, outputStream);
        return outputStream.toByteArray();
    }

    public static void saveToFile(Bitmap bitmap, File bitmapFile, Bitmap.CompressFormat format) throws FileNotFoundException {
        OutputStream outputStream = null;
        try {
            bitmap.compress(format, 100, outputStream = new FileOutputStream(bitmapFile));
        } finally {
            IOUtils.closeQuietly(outputStream);
        }
    }

    public static int calcAverageColor(Bitmap bitmap) {
        int R = 0;
        int G = 0;
        int B = 0;
        int height = bitmap.getHeight();
        int width = bitmap.getWidth();
        int n = 0;
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        for (int i = 0; i < pixels.length; i++) {
            int color = pixels[i];
            R += Color.red(color);
            G += Color.green(color);
            B += Color.blue(color);
            n++;
        }
        return Color.rgb(R / n, G / n, B / n);
    }

    public static Bitmap decodeBitmapFromFile(String filePath, int requestedWidth) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);

        options.inSampleSize = calculateInSampleSize(options, requestedWidth);

        options.inJustDecodeBounds = false;
        return BitmapUtils.scale(
                fixBitmapOrientation(
                        BitmapFactory.decodeFile(filePath, options),
                        filePath),
                new Rect(0, 0, requestedWidth, -1));
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth) {
        int inSampleSize = 1;

        final int halfWidth = options.outWidth / 2;
        while ((halfWidth / inSampleSize) > reqWidth) {
            inSampleSize *= 2;
        }

        return inSampleSize;
    }

    public static Bitmap fixBitmapOrientation(Bitmap bitmap, String sourceImageFilePath) {
        if (bitmap == null) {
            return null;
        }

        int rotate = getImageOrientation(sourceImageFilePath);

        if (rotate == 0) {
            return bitmap;
        }

        Matrix matrix = new Matrix();
        matrix.postRotate(rotate);
        Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        bitmap.recycle();
        return rotatedBitmap;
    }

    public static int getImageOrientation(String imageFilePath) {
        try {
            File imageFile = Utils.createFileRuntimeSafe(imageFilePath);
            ExifInterface exif = new ExifInterface(imageFile.getAbsolutePath());
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    return 270;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    return 180;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    return 90;
                default:
                    return 0;
            }
        } catch (IOException e) {
            return 0;
        }
    }

    public static void saveToFileSafe(Bitmap bitmap, File file, Bitmap.CompressFormat format) {
        try {
            saveToFile(bitmap, file, format);
        } catch (FileNotFoundException ignored) {
        }
    }

    public static Bitmap scale(Bitmap source, Rect requiredSize) {

        float factor = 1;

        if (requiredSize.width() > 0) {
            factor = Math.min(factor, requiredSize.width() / (float) source.getWidth());
        }

        if (requiredSize.height() > 0) {
            factor = Math.min(factor, requiredSize.height() / (float) source.getHeight());
        }

        if (EPS.isSame(factor, 1f)) return source;

        return Bitmap.createScaledBitmap(source, (int) (source.getWidth() * factor), (int) (source.getHeight() * factor), false);
    }
}