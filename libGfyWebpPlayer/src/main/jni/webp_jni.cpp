/*
 * MIT License
 *
 * Copyright (c) Facebook, Inc. and its affiliates.
 * Contains changes from (c) Gfycat, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

#define LOG_TAG "WebPImage"

#include "webp_wrapper.h"

#include <jni.h>
//#include <array>
#include <memory>
#include <utility>
#include <vector>
#include <android/bitmap.h>

#include "webp/decode.h"
#include "webp/demux.h"

#include "jni_helpers.h"

using namespace facebook;

// Class Names.
static const char* const kWebPImageClassPathName =
    "com/gfycat/webp/WebPImage";
static const char* const kWebPFrameClassPathName =
    "com/gfycat/webp/WebPFrame";

// Cached fields related to WebPImage
static jclass sClazzWebPImage;
static jmethodID sWebPImageConstructor;
static jfieldID sWebPImageFieldNativeContext;

// Cached fields related to WebPFrame
static jclass sClazzWebPFrame;
static jmethodID sWebPFrameConstructor;
static jfieldID sWebPFrameFieldNativeContext;


////////////////////////////////////////////////////////////////
/// Related to WebPImage
////////////////////////////////////////////////////////////////

/**
 * Creates a new WebPImage from the specified buffer.
 *
 * @param vBuffer the vector containing the bytes
 * @return a newly allocated WebPImage
 */
jobject WebPImage_nativeCreateFromByteVector(JNIEnv* pEnv, std::vector<uint8_t>& vBuffer) {
  try {
    std::unique_ptr<WebPImageNativeContext> spNativeContext = WebPImageNativeCreateFromByteVector(vBuffer);
    if (!spNativeContext) {
      throwOutOfMemoryError(pEnv, "Unable to allocate native context");
      return 0;
    }

    // Create the WebPImage with the native context.
    jobject ret = pEnv->NewObject(
            sClazzWebPImage,
            sWebPImageConstructor,
            (jlong) spNativeContext.get());
    if (ret != nullptr) {
      // Ownership was transferred.
      spNativeContext->refCount = 1;
      spNativeContext.release();
    }
    return ret;
  } catch (UnableToAllocateNativeContextException &) {
    throwOutOfMemoryError(pEnv, "UnableToAllocateNativeContextException happens");
    return 0;
  } catch (CanNotCreateDemuxerException &) {
    throwIllegalArgumentException(pEnv, "CanNotCreateDemuxerException happens");
    return 0;
  }
}

/**
 * Releases a reference to the WebPImageNativeContext and deletes it when the reference count
 * reaches 0
 */
void WebPImageNativeContext_releaseRef(JNIEnv* pEnv, jobject thiz, WebPImageNativeContext* p) {
  pEnv->MonitorEnter(thiz);
  p->refCount--;
  if (p->refCount == 0) {
    delete p;
  }
  pEnv->MonitorExit(thiz);
}

/**
 * Functor for getWebPImageNativeContext that releases the reference.
 */
struct WebPImageNativeContextReleaser {
  JNIEnv* pEnv;
  jobject webpImage;

  WebPImageNativeContextReleaser(JNIEnv* pEnv, jobject webpImage) :
      pEnv(pEnv), webpImage(webpImage) {}

  void operator()(WebPImageNativeContext* pNativeContext) {
    WebPImageNativeContext_releaseRef(pEnv, webpImage, pNativeContext);
  }
};

/**
 * Gets the WebPImageNativeContext from the mNativeContext of the WebPImage object. This returns
 * a reference counted shared_ptr.
 *
 * @return the shared_ptr which will be a nullptr in the case where the object has already been
 *    disposed
 */
std::unique_ptr<WebPImageNativeContext, WebPImageNativeContextReleaser>
    getWebPImageNativeContext(JNIEnv* pEnv, jobject thiz) {

  // A deleter that decrements the reference and possibly deletes the instance.
  WebPImageNativeContextReleaser releaser(pEnv, thiz);
  std::unique_ptr<WebPImageNativeContext, WebPImageNativeContextReleaser> ret(nullptr, releaser);
  pEnv->MonitorEnter(thiz);
  WebPImageNativeContext* pNativeContext =
      (WebPImageNativeContext*) pEnv->GetLongField(thiz, sWebPImageFieldNativeContext);
  if (pNativeContext != nullptr) {
    pNativeContext->refCount++;
    ret.reset(pNativeContext);
  }
  pEnv->MonitorExit(thiz);
  return ret;
}

/**
 * Creates a new WebPImage from the specified byte buffer. The data from the byte buffer is copied
 * into native memory managed by WebPImage.
 *
 * @param byteBuffer A java.nio.ByteBuffer. Must be direct. Assumes data is the entire capacity
 *      of the buffer
 * @return a newly allocated WebPImage
 */
jobject WebPImage_nativeCreateFromDirectByteBuffer(JNIEnv* pEnv, jclass clazz, jobject byteBuffer) {
  jbyte* bbufInput = (jbyte*) pEnv->GetDirectBufferAddress(byteBuffer);
  if (!bbufInput) {
    throwIllegalArgumentException(pEnv, "ByteBuffer must be direct");
    return 0;
  }

  jlong capacity = pEnv->GetDirectBufferCapacity(byteBuffer);
  if (pEnv->ExceptionCheck()) {
    return 0;
  }

  std::vector<uint8_t> vBuffer(bbufInput, bbufInput + capacity);
  return WebPImage_nativeCreateFromByteVector(pEnv, vBuffer);
}

/**
 * Creates a new WebPImage from the specified native pointer. The data is copied into memory
 managed by WebPImage.
 *
 * @param nativePtr the native memory pointer
 * @param sizeInBytes size in bytes of the buffer
 * @return a newly allocated WebPImage
 */
jobject WebPImage_nativeCreateFromNativeMemory(
    JNIEnv* pEnv,
    jclass clazz,
    jlong nativePtr,
    jint sizeInBytes) {

  jbyte* const pointer = (jbyte*) nativePtr;
  std::vector<uint8_t> vBuffer(pointer, pointer + sizeInBytes);
  return WebPImage_nativeCreateFromByteVector(pEnv, vBuffer);
}

/**
 * Search last key frame in provided range.
 *
 * @return keyFrame index or -1 if there are no keyframes.
 */
jint WebPImage_nativeLastKeyFrameInRange (JNIEnv* pEnv, jobject thiz, jint start, jint end) {

    auto spNativeContext = getWebPImageNativeContext(pEnv, thiz);
    if (!spNativeContext) {
        throwIllegalStateException(pEnv, "Already disposed");
        return -1;
    }

    return WebPImageNativeLastKeyFrameInRange(spNativeContext.get(), start, end);
}

/**
 * Gets the width of the image.
 *
 * @return the width of the image
 */
jint WebPImage_nativeGetWidth(JNIEnv* pEnv, jobject thiz) {
  auto spNativeContext = getWebPImageNativeContext(pEnv, thiz);
  if (!spNativeContext) {
    throwIllegalStateException(pEnv, "Already disposed");
    return 0;
  }

  return spNativeContext->pixelWidth;
}

/**
 * Gets the height of the image.
 *
 * @return the height of the image
 */
jint WebPImage_nativeGetHeight(JNIEnv* pEnv, jobject thiz) {
  auto spNativeContext = getWebPImageNativeContext(pEnv, thiz);
  if (!spNativeContext) {
    throwIllegalStateException(pEnv, "Already disposed");
    return 0;
  }
  return spNativeContext->pixelHeight;
}

/**
 * Gets the number of frames in the image.
 *
 * @return the number of frames in the image
 */
jint WebPImage_nativeGetFrameCount(JNIEnv* pEnv, jobject thiz) {
  auto spNativeContext = getWebPImageNativeContext(pEnv, thiz);
  if (!spNativeContext) {
    throwIllegalStateException(pEnv, "Already disposed");
    return 0;
  }
  return spNativeContext->numFrames;
}

/**
 * Gets the duration of the animated image.
 *
 * @return the duration of the animated image in milliseconds
 */
jint WebPImage_nativeGetDuration(JNIEnv* pEnv, jobject thiz) {
  auto spNativeContext = getWebPImageNativeContext(pEnv, thiz);
  if (!spNativeContext) {
    throwIllegalStateException(pEnv, "Already disposed");
    return 0;
  }
  return spNativeContext->durationMs;
}

/**
 * Gets the number of loops to run the animation for.
 *
 * @return the number of loops, or 0 to indicate infinite
 */
jint WebPImage_nativeGetLoopCount(JNIEnv* pEnv, jobject thiz) {
  auto spNativeContext = getWebPImageNativeContext(pEnv, thiz);
  if (!spNativeContext) {
    throwIllegalStateException(pEnv, "Already disposed");
    return 0;
  }
  return spNativeContext->loopCount;
}

/**
 * Gets the duration of each frame of the animated image.
 *
 * @return an array that is the size of the number of frames containing the duration of each frame
 *     in milliseconds
 */
jintArray WebPImage_nativeGetFrameDurations(JNIEnv* pEnv, jobject thiz) {
  auto spNativeContext = getWebPImageNativeContext(pEnv, thiz);
  if (!spNativeContext) {
    throwIllegalStateException(pEnv, "Already disposed");
    return NULL;
  }
  jintArray result = pEnv->NewIntArray(spNativeContext->numFrames);
  if (result == nullptr) {
    // pEnv->NewIntArray will have already instructed the environment to throw an exception.
    return nullptr;
  }

  pEnv->SetIntArrayRegion(
      result,
      0,
      spNativeContext->numFrames,
      spNativeContext->frameDurationsMs.data());
  return result;
}

/**
 * Gets the Frame at the specified index.
 *
 * @param index the index of the frame
 * @return a newly created WebPFrame for the specified frame
 */
jobject WebPImage_nativeGetFrame(JNIEnv* pEnv, jobject thiz, jint index) {
  auto spNativeContext = getWebPImageNativeContext(pEnv, thiz);
  if (!spNativeContext) {
    throwIllegalStateException(pEnv, "Already disposed");
    return nullptr;
  }

  std::unique_ptr<WebPFrameNativeContext> spFrameNativeContext = WebPImageNativeGetFrame(spNativeContext.get(), index);
  if (!spFrameNativeContext) {
    throwOutOfMemoryError(pEnv, "Unable to allocate WebPFrameNativeContext");
    return nullptr;
  }

  jobject ret = pEnv->NewObject(
      sClazzWebPFrame,
      sWebPFrameConstructor,
      (jlong) spFrameNativeContext.get());
  if (ret != nullptr) {
    // Ownership was transferred.
    spFrameNativeContext->refCount = 1;
    spFrameNativeContext.release();
  }
  return ret;
}

/**
 * Releases a reference to the WebPFrameNativeContext and deletes it when the reference count
 * reaches 0
 */
void WebPFrameNativeContext_releaseRef(JNIEnv* pEnv, jobject thiz, WebPFrameNativeContext* p) {
  pEnv->MonitorEnter(thiz);
  p->refCount--;
  if (p->refCount == 0) {
    delete p;
  }
  pEnv->MonitorExit(thiz);
}

/**
 * Functor for getWebPFrameNativeContext.
 */
struct WebPFrameNativeContextReleaser {
  JNIEnv* pEnv;
  jobject webpFrame;

  WebPFrameNativeContextReleaser(JNIEnv* pEnv, jobject webpFrame) :
      pEnv(pEnv), webpFrame(webpFrame) {}

  void operator()(WebPFrameNativeContext* pNativeContext) {
    WebPFrameNativeContext_releaseRef(pEnv, webpFrame, pNativeContext);
  }
};

/**
 * Gets the WebPFrameNativeContext from the mNativeContext of the WebPFrame object. This returns
 * a reference counted pointer.
 *
 * @return the reference counted pointer which will be a nullptr in the case where the object has
 *    already been disposed
 */
std::unique_ptr<WebPFrameNativeContext, WebPFrameNativeContextReleaser>
    getWebPFrameNativeContext(JNIEnv* pEnv, jobject thiz) {

  WebPFrameNativeContextReleaser releaser(pEnv, thiz);
  std::unique_ptr<WebPFrameNativeContext, WebPFrameNativeContextReleaser> ret(nullptr, releaser);
  pEnv->MonitorEnter(thiz);
  WebPFrameNativeContext* pNativeContext =
      (WebPFrameNativeContext*) pEnv->GetLongField(thiz, sWebPFrameFieldNativeContext);
  if (pNativeContext != nullptr) {
    pNativeContext->refCount++;
    ret.reset(pNativeContext);
  }
  pEnv->MonitorExit(thiz);
  return ret;
}

/**
 * Gets the size in bytes used by the {@link WebPImage}. The implementation only takes into
 * account the encoded data buffer as the other data structures are relatively tiny.
 *
 * @return approximate size in bytes used by the {@link WebPImage}
 */
jint WebPImage_nativeGetSizeInBytes(JNIEnv* pEnv, jobject thiz) {
  auto spNativeContext = getWebPImageNativeContext(pEnv, thiz);
  if (!spNativeContext) {
    throwIllegalStateException(pEnv, "Already disposed");
    return 0;
  }
  return spNativeContext->spDemuxer->getBufferSize();
}

/**
 * Disposes the WebImage, freeing native resources.
 */
void WebImage_nativeDispose(JNIEnv* pEnv, jobject thiz) {
  pEnv->MonitorEnter(thiz);
  WebPImageNativeContext* pNativeContext =
      (WebPImageNativeContext*) pEnv->GetLongField(thiz, sWebPImageFieldNativeContext);
  if (pNativeContext != nullptr) {
    pEnv->SetLongField(thiz, sWebPImageFieldNativeContext, 0);
    WebPImageNativeContext_releaseRef(pEnv, thiz, pNativeContext);
  }

  pEnv->MonitorExit(thiz);
}

/**
 * Finalizer for WebImage that frees native resources.
 */
void WebImage_nativeFinalize(JNIEnv* pEnv, jobject thiz) {
  WebImage_nativeDispose(pEnv, thiz);
}


////////////////////////////////////////////////////////////////
/// Related to WebPFrame
////////////////////////////////////////////////////////////////

/**
 * Renders the frame to the specified pixel array. The array is expected to have a size that
 * is at least the the width and height of the frame. The frame is rendered where each pixel is
 * represented as a 32-bit BGRA pixel. The rendered stride is the same as the frame width. Note,
 * the number of pixels written to the array may be smaller than the canvas if the frame's
 * width/height is smaller than the canvas.
 *
 * @param jPixels the array to render into
 */
void WebPFrame_nativeRenderFrame(
    JNIEnv* pEnv,
    jobject thiz,
    jobject bitmap,
    jboolean forceClearAll) {
  auto spNativeContext = getWebPFrameNativeContext(pEnv, thiz);
  if (!spNativeContext) {
    throwIllegalStateException(pEnv, "Already disposed");
    return;
  }

  AndroidBitmapInfo bitmapInfo;
  if (AndroidBitmap_getInfo(pEnv, bitmap, &bitmapInfo) != ANDROID_BITMAP_RESULT_SUCCESS) {
    throwIllegalStateException(pEnv, "Bad bitmap");
    return;
  }

  if (bitmapInfo.width <= 0 || bitmapInfo.height <= 0) {
    throwIllegalStateException(pEnv, "Width or height is too small");
    return;
  }

  if (bitmapInfo.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
    throwIllegalStateException(pEnv, "Wrong color format");
    return;
  }

  uint8_t* pixels;
  if (AndroidBitmap_lockPixels(pEnv, bitmap, (void**) &pixels) != ANDROID_BITMAP_RESULT_SUCCESS) {
    throwIllegalStateException(pEnv, "Bad bitmap");
    return;
  }

  bool ret = WebPFrameNativeRenderFrame(spNativeContext.get(), bitmapInfo.width, bitmapInfo.height, forceClearAll, bitmapInfo.stride, pixels);

  AndroidBitmap_unlockPixels(pEnv, bitmap);

  if (!ret) {
    throwIllegalStateException(pEnv, "Failed to decode frame");
  }
}

void WebPFrame_nativeRenderFrameNew(JNIEnv* pEnv, jobject thiz, jobject bitmap) {
  WebPFrame_nativeRenderFrame(pEnv, thiz, bitmap, false);
}

/**
 * Gets the duration of the frame.
 *
 * @return the duration of the frame in milliseconds
 */
jint WebPFrame_nativeGetDurationMs(JNIEnv* pEnv, jobject thiz) {
  auto spNativeContext = getWebPFrameNativeContext(pEnv, thiz);
  if (!spNativeContext) {
    throwIllegalStateException(pEnv, "Already disposed");
    return -1;
  }
  return spNativeContext->durationMs;
}

/**
 * Gets the width of the frame.
 *
 * @return the width of the frame
 */
jint WebPFrame_nativeGetWidth(JNIEnv* pEnv, jobject thiz) {
  auto spNativeContext = getWebPFrameNativeContext(pEnv, thiz);
  if (!spNativeContext) {
    throwIllegalStateException(pEnv, "Already disposed");
    return -1;
  }
  return spNativeContext->width;
}

/**
 * Gets the height of the frame.
 *
 * @return the height of the frame
 */
jint WebPFrame_nativeGetHeight(JNIEnv* pEnv, jobject thiz) {
  auto spNativeContext = getWebPFrameNativeContext(pEnv, thiz);
  if (!spNativeContext) {
    throwIllegalStateException(pEnv, "Already disposed");
    return -1;
  }
  return spNativeContext->height;
}

/**
 * Gets the x-offset of the frame relative to the image canvas.
 *
 * @return the x-offset of the frame
 */
jint WebPFrame_nativeGetXOffset(JNIEnv* pEnv, jobject thiz) {
  auto spNativeContext = getWebPFrameNativeContext(pEnv, thiz);
  if (!spNativeContext) {
    throwIllegalStateException(pEnv, "Already disposed");
    return -1;
  }
  return spNativeContext->xOffset;
}

/**
 * Gets the y-offset of the frame relative to the image canvas.
 *
 * @return the y-offset of the frame
 */
jint WebPFrame_nativeGetYOffset(JNIEnv* pEnv, jobject thiz) {
  auto spNativeContext = getWebPFrameNativeContext(pEnv, thiz);
  if (!spNativeContext) {
    throwIllegalStateException(pEnv, "Already disposed");
    return -1;
  }
  return spNativeContext->yOffset;
}

/**
 * Gets whether the current frame should be disposed to the background color (or may be needed
 * as the background of the next frame).
 *
 * @return whether the current frame should be disposed to the background color
 */
jboolean WebPFrame_nativeShouldDisposeToBackgroundColor(JNIEnv* pEnv, jobject thiz) {
  auto spNativeContext = getWebPFrameNativeContext(pEnv, thiz);
  if (!spNativeContext) {
    throwIllegalStateException(pEnv, "Already disposed");
    return -1;
  }
  return spNativeContext->disposeToBackgroundColor;
}

/**
 * Gets whether the current frame should be alpha blended over the previous frame.
 *
 * @return whether the current frame should be alpha blended over the previous frame
 */
jboolean WebPFrame_nativeShouldBlendWithPreviousFrame(JNIEnv* pEnv, jobject thiz) {
  auto spNativeContext = getWebPFrameNativeContext(pEnv, thiz);
  if (!spNativeContext) {
    throwIllegalStateException(pEnv, "Already disposed");
    return -1;
  }
  return spNativeContext->blendWithPreviousFrame;
}

/**
 * Gets whether the current frame should be alpha blended over the previous frame.
 *
 * @return whether the current frame should be alpha blended over the previous frame
 */
jboolean WebPFrame_nativeHasAlpha(JNIEnv* pEnv, jobject thiz) {
  auto spNativeContext = getWebPFrameNativeContext(pEnv, thiz);
  if (!spNativeContext) {
    throwIllegalStateException(pEnv, "Already disposed");
    return -1;
  }
  return spNativeContext->hasAlpha;
}

/**
 * Gets whether the current frame should be alpha blended over the previous frame.
 *
 * @return whether the current frame should be alpha blended over the previous frame
 */
jboolean WebPFrame_nativeHasOffsets(JNIEnv* pEnv, jobject thiz) {
  auto spNativeContext = getWebPFrameNativeContext(pEnv, thiz);
  if (!spNativeContext) {
    throwIllegalStateException(pEnv, "Already disposed");
    return -1;
  }
  return spNativeContext->hasOffsets();
}

jboolean WebPFrame_nativeIsKeyFrame(JNIEnv *pEnv, jobject thiz) {

  auto spNativeContext = getWebPFrameNativeContext(pEnv, thiz);
  if (!spNativeContext) {
    throwIllegalStateException(pEnv, "Already disposed");
    return -1;
  }

  return spNativeContext->isKeyFrame();
}

/**
 * Disposes the WebPFrameIterator, freeing native resources.
 */
void WebPFrame_nativeDispose(JNIEnv* pEnv, jobject thiz) {
  pEnv->MonitorEnter(thiz);
  WebPFrameNativeContext* pNativeContext =
      (WebPFrameNativeContext*) pEnv->GetLongField(thiz, sWebPFrameFieldNativeContext);
  if (pNativeContext) {
    pEnv->SetLongField(thiz, sWebPFrameFieldNativeContext, 0);
    WebPFrameNativeContext_releaseRef(pEnv, thiz, pNativeContext);
  }
  pEnv->MonitorExit(thiz);
}

/**
 * Finalizer for WebPFrame that frees native resources.
 */
void WebPFrame_nativeFinalize(JNIEnv* pEnv, jobject thiz) {
  WebPFrame_nativeDispose(pEnv, thiz);
}

static JNINativeMethod sWebPImageMethods[] = {
  { "nativeCreateFromDirectByteBuffer",
    "(Ljava/nio/ByteBuffer;)Lcom/gfycat/webp/WebPImage;",
    (void*)WebPImage_nativeCreateFromDirectByteBuffer },
  { "nativeCreateFromNativeMemory",
    "(JI)Lcom/gfycat/webp/WebPImage;",
    (void*)WebPImage_nativeCreateFromNativeMemory },
  { "nativeLastKeyFrameInRange",
    "(II)I",
    (void*)WebPImage_nativeLastKeyFrameInRange },
  { "nativeGetWidth",
    "()I",
    (void*)WebPImage_nativeGetWidth },
  { "nativeGetHeight",
    "()I",
    (void*)WebPImage_nativeGetHeight },
  { "nativeGetDuration",
    "()I",
    (void*)WebPImage_nativeGetDuration },
  { "nativeGetFrameCount",
    "()I",
    (void*)WebPImage_nativeGetFrameCount },
  { "nativeGetFrameDurations",
    "()[I",
    (void*)WebPImage_nativeGetFrameDurations },
  { "nativeGetDuration",
    "()I",
    (void*)WebPImage_nativeGetDuration },
  { "nativeGetLoopCount",
    "()I",
    (void*)WebPImage_nativeGetLoopCount },
  { "nativeGetFrame",
    "(I)Lcom/gfycat/webp/WebPFrame;",
    (void*)WebPImage_nativeGetFrame },
  { "nativeGetSizeInBytes",
    "()I",
    (void*)WebPImage_nativeGetSizeInBytes },
  { "nativeDispose",
    "()V",
    (void*)WebImage_nativeDispose },
  { "nativeFinalize",
    "()V",
    (void*)WebImage_nativeFinalize },
};

static JNINativeMethod sWebPFrameMethods[] = {
  { "nativeRenderFrame",
    "(Landroid/graphics/Bitmap;)V",
    (void*)WebPFrame_nativeRenderFrameNew },
  { "nativeRenderFrame",
    "(Landroid/graphics/Bitmap;Z)V",
    (void*)WebPFrame_nativeRenderFrame },
  { "nativeGetDurationMs",
    "()I",
    (void*)WebPFrame_nativeGetDurationMs },
  { "nativeGetWidth",
    "()I",
    (void*)WebPFrame_nativeGetWidth },
  { "nativeGetHeight",
    "()I",
    (void*)WebPFrame_nativeGetHeight },
  { "nativeGetXOffset",
    "()I",
    (void*)WebPFrame_nativeGetXOffset },
  { "nativeGetYOffset",
    "()I",
    (void*)WebPFrame_nativeGetYOffset },
  { "nativeGetDurationMs",
    "()I",
    (void*)WebPFrame_nativeGetDurationMs },
  { "nativeShouldDisposeToBackgroundColor",
    "()Z",
    (void*)WebPFrame_nativeShouldDisposeToBackgroundColor },
  { "nativeShouldBlendWithPreviousFrame",
    "()Z",
    (void*)WebPFrame_nativeShouldBlendWithPreviousFrame },
  { "nativeHasAlpha",
    "()Z",
    (void*)WebPFrame_nativeHasAlpha },
  { "nativeHasOffsets",
    "()Z",
    (void*)WebPFrame_nativeHasOffsets},
  { "nativeIsKeyFrame",
     "()Z",
    (void*)WebPFrame_nativeIsKeyFrame},
  { "nativeDispose",
    "()V",
    (void*)WebPFrame_nativeDispose },
  { "nativeFinalize",
    "()V",
    (void*)WebPFrame_nativeFinalize },
};

/**
 * Called by JNI_OnLoad to initialize the classes.
 */
int initWebPImage(JNIEnv* pEnv) {
  // WebPImage
  sClazzWebPImage = findClassOrThrow(pEnv, kWebPImageClassPathName);
  if (sClazzWebPImage == NULL) {
    return JNI_ERR;
  }

  // WebPImage.mNativeContext
  sWebPImageFieldNativeContext = getFieldIdOrThrow(pEnv, sClazzWebPImage, "mNativeContext", "J");
  if (!sWebPImageFieldNativeContext) {
    return JNI_ERR;
  }

  // WebPImage.<init>
  sWebPImageConstructor = getMethodIdOrThrow(pEnv, sClazzWebPImage, "<init>", "(J)V");
  if (!sWebPImageConstructor) {
    return JNI_ERR;
  }

  int result = pEnv->RegisterNatives(
      sClazzWebPImage,
      sWebPImageMethods,
      std::extent<decltype(sWebPImageMethods)>::value);
  if (result != JNI_OK) {
    return result;
  }

  // WebPFrame
  sClazzWebPFrame = findClassOrThrow(pEnv, kWebPFrameClassPathName);
  if (sClazzWebPFrame == NULL) {
    return JNI_ERR;
  }

  // WebPFrame.mNativeContext
  sWebPFrameFieldNativeContext = getFieldIdOrThrow(pEnv, sClazzWebPFrame, "mNativeContext", "J");
  if (!sWebPFrameFieldNativeContext) {
    return JNI_ERR;
  }

  // WebPFrame.<init>
  sWebPFrameConstructor = getMethodIdOrThrow(pEnv, sClazzWebPFrame, "<init>", "(J)V");
  if (!sWebPFrameConstructor) {
    return JNI_ERR;
  }

  result = pEnv->RegisterNatives(
      sClazzWebPFrame,
      sWebPFrameMethods,
      std::extent<decltype(sWebPFrameMethods)>::value);
  if (result != JNI_OK) {
    return result;
  }

  return JNI_OK;
}
