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

package com.gfycat.webp;

import android.graphics.Bitmap;

/**
 * A single frame of a {@link WebPImage}.
 */
public class WebPFrame {

  // Accessed by native methods
  @SuppressWarnings("unused")
  private long mNativeContext;

  /**
   * Constructs the frame with the native pointer. This is called by native code.
   *
   * @param nativeContext the native pointer
   */
  WebPFrame(long nativeContext) {
    mNativeContext = nativeContext;
  }

  protected void finalize() {
    nativeFinalize();
  }

  public void dispose() {
    nativeDispose();
  }

  public void renderFrame(Bitmap bitmap) {
    nativeRenderFrame(bitmap);
  }

    public void renderFrame(Bitmap bitmap, boolean forceClear) {
        nativeRenderFrame(bitmap, forceClear);
    }

  public int getDurationMs() {
    return nativeGetDurationMs();
  }

  public int getWidth() {
    return nativeGetWidth();
  }

  public int getHeight() {
    return nativeGetHeight();
  }

  public int getXOffset() {
    return nativeGetXOffset();
  }

  public int getYOffset() {
    return nativeGetYOffset();
  }

  public boolean shouldDisposeToBackgroundColor() {
    return nativeShouldDisposeToBackgroundColor();
  }

  public boolean shouldBlendWithPreviousFrame() {
    return nativeShouldBlendWithPreviousFrame();
  }

  public boolean isKeyFrame() {
    return nativeIsKeyFrame();
  }

  public boolean hasAlpha() {
    return nativeHasAlpha();
  }

  public boolean hasOffsets() {
    return nativeHasOffsets();
  }

  private native void nativeRenderFrame(Bitmap bitmap);
  private native void nativeRenderFrame(Bitmap bitmap, boolean blend);
  private native int nativeGetDurationMs();
  private native int nativeGetWidth();
  private native int nativeGetHeight();
  private native int nativeGetXOffset();
  private native int nativeGetYOffset();
  private native boolean nativeShouldDisposeToBackgroundColor();
  private native boolean nativeShouldBlendWithPreviousFrame();
  private native boolean nativeHasAlpha();
  private native boolean nativeIsKeyFrame();
  private native boolean nativeHasOffsets();
  private native void nativeDispose();
  private native void nativeFinalize();
}
