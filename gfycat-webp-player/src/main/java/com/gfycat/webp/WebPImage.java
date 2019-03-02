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

import com.gfycat.common.SoLibraryLoader;
import com.gfycat.common.utils.Assertions;

import java.nio.ByteBuffer;

/**
 * A representation of a WebP image. An instance of this class will hold a copy of the encoded
 * data in memory along with the parsed header data. Frames are decoded on demand via
 * {@link WebPFrame}.
 */
public class WebPImage {

  private volatile static boolean sInitialized;

  // Accessed by native methods
  @SuppressWarnings("unused")
  private long mNativeContext;

  private static synchronized void ensure() {
    if (!sInitialized) {
      sInitialized = true;
      SoLibraryLoader.loadLibrary("gfywebp");
    }
  }

  /**
   * Constructs the image with the native pointer. This is called by native code.
   *
   * @param nativeContext the native pointer
   */
  WebPImage(long nativeContext) {
    mNativeContext = nativeContext;
  }

  @Override
  protected void finalize() {
    nativeFinalize();
  }

  public void dispose() {
    nativeDispose();
  }

  /**
   * Creates a {@link WebPImage} from the specified encoded data. This will throw if it fails
   * to create. This is meant to be called on a worker thread.
   *
   * @param source the data to the image (a copy will be made)
   */
  public static WebPImage create(byte[] source) {
    ensure();
    Assertions.assertNotNull(source, IllegalStateException::new);

    ByteBuffer byteBuffer = ByteBuffer.allocateDirect(source.length);
    byteBuffer.put(source);
    byteBuffer.rewind();

    return nativeCreateFromDirectByteBuffer(byteBuffer);
  }

  public static WebPImage create(long nativePtr, int sizeInBytes) {
    ensure();
    Assertions.assertTrue(nativePtr != 0, IllegalStateException::new);
    return nativeCreateFromNativeMemory(nativePtr, sizeInBytes);
  }

  public int getWidth() {
    return nativeGetWidth();
  }

  public int getHeight() {
    return nativeGetHeight();
  }

  public int getFrameCount() {
    return nativeGetFrameCount();
  }

  public int getDuration() {
    return nativeGetDuration();
  }

  public int[] getFrameDurations() {
    return nativeGetFrameDurations();
  }

  public int getLoopCount() {
    return nativeGetLoopCount();
  }

  public WebPFrame getFrame(int frameNumber) {
    return nativeGetFrame(frameNumber);
  }

  public int getSizeInBytes() {
    return nativeGetSizeInBytes();
  }

  public boolean doesRenderSupportScaling() {
    return true;
  }

  public int lastKeyFrameInRange(int start, int end) {
    return nativeLastKeyFrameInRange(start, end);
  }

  private static native WebPImage nativeCreateFromDirectByteBuffer(ByteBuffer buffer);
  private static native WebPImage nativeCreateFromNativeMemory(long nativePtr, int sizeInBytes);
  private native int nativeLastKeyFrameInRange(int start, int end);
  private native int nativeGetWidth();
  private native int nativeGetHeight();
  private native int nativeGetDuration();
  private native int nativeGetFrameCount();
  private native int[] nativeGetFrameDurations();
  private native int nativeGetLoopCount();
  private native WebPFrame nativeGetFrame(int frameNumber);
  private native int nativeGetSizeInBytes();
  private native void nativeDispose();
  private native void nativeFinalize();
}
