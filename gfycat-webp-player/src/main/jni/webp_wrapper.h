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

//#include <array>
#include <memory>
#include <utility>
#include <vector>

#include "webp/decode.h"
#include "webp/demux.h"

/**
 * A holder for WebPDemuxer and its buffer. WebPDemuxer is needed by both WebPImage and
 * instances of WebPFrameIterator and it can't be released until all of them are done with it. This
 * wrapper is meant to be used inside of a std::shared_ptr to manage the resource.
 */
class WebPDemuxerWrapper {

public:
  WebPDemuxerWrapper(
    std::unique_ptr<WebPDemuxer, decltype(&WebPDemuxDelete)>&& pDemuxer,
    std::vector<uint8_t>&& pBuffer) :
    m_pDemuxer(std::move(pDemuxer)),
    m_pBuffer(std::move(pBuffer)) {
  }

  virtual ~WebPDemuxerWrapper() {
    //FBLOGD("Deleting Demuxer");
  }

  WebPDemuxer* get() {
    return m_pDemuxer.get();
  }

  size_t getBufferSize() {
    return m_pBuffer.size();
  }

private:
    std::unique_ptr<WebPDemuxer, decltype(&WebPDemuxDelete)> m_pDemuxer;
    std::vector<uint8_t> m_pBuffer;
};

/**
 * Native context for WebPImage.
 */
struct WebPImageNativeContext {

  /* Reference to the Demuxer */
  std::shared_ptr<WebPDemuxerWrapper> spDemuxer;

  /* Cached width of the image */
  int pixelWidth;

  /* Cached height of the image */
  int pixelHeight;

  /* Cached background color of the image */
  uint32_t backgroundColor;

  /* Cached number of the frames in the image */
  int numFrames;

  /** Cached loop count for the image. 0 means infinite. */
  int loopCount;

  /** Duration of all the animation (the sum of all the frames duration) */
  int durationMs;

  /** Array of each frame's duration (size of array is numFrames) */
  std::vector<int> frameDurationsMs;

  /** Reference counter. Instance is deleted when it goes from 1 to 0 */
  size_t refCount;
};

/**
 * Native context for WebPFrame.
 */
struct WebPFrameNativeContext {

  /* Reference to the Demuxer */
  std::shared_ptr<WebPDemuxerWrapper> spDemuxer;

  /** Frame number for the image. Starts at 1. */
  int frameNum;

  /** X offset for the frame relative to the image canvas */
  int xOffset;

  /** Y offset for the frame relative to the image canvas */
  int yOffset;

  /** Display duration for the frame in ms*/
  int durationMs;

  /** Width of this frame */
  int width;

  /** Height of this frame */
  int height;

  /** X offset for the previous frame relative to the image canvas */
  int disposeXOffset;

  /** Y offset for the previous frame relative to the image canvas */
  int disposeYOffset;

  /** Width of previous frame */
  int disposeWidth;

  /** Height of previous frame */
  int disposeHeight;

  /* Cached width of the image */
  int imageWidth;

  /* Cached height of the image */
  int imageHeight;

  /* Cached background color of the image */
  uint32_t backgroundColor;

  /** Whether the next frame might need to be blended with this frame */
  bool disposeToBackgroundColor;

  /** Whether this frame needs to be blended with the previous frame */
  bool blendWithPreviousFrame;

  /** Whether this frame needs to be blended with the previous frame */
  bool hasAlpha;

  /** Raw encoded bytes for the frame. Points to existing memory managed by WebPDemuxerWrapper */
  const uint8_t* pPayload;

  /** Size of payload in bytes */
  size_t payloadSize;

  /** Reference counter. Instance is deleted when it goes from 1 to 0 */
  size_t refCount;

  /** Some gfycat webps has content with flag blendWithPreviousFrame == true but hasAlpha == false,
   * so this mean we can skip flag blendWithPreviousFrame as it is false.
   */
  bool isKeyFrame() {
    return (!hasAlpha || !blendWithPreviousFrame) && !hasOffsets();
  }

  bool hasOffsets() {
    return imageWidth != width || imageHeight != height || xOffset != 0 || yOffset != 0;
  }
};

struct UnableToAllocateNativeContextException {};
struct CanNotCreateDemuxerException {};

std::unique_ptr<WebPImageNativeContext> WebPImageNativeCreateFromByteVector(std::vector<uint8_t>& vBuffer);
std::unique_ptr<WebPFrameNativeContext> WebPImageNativeGetFrame(WebPImageNativeContext *pNativeContext, int index);
int WebPImageNativeLastKeyFrameInRange(WebPImageNativeContext *pNativeContext, int start, int end);
bool WebPFrameNativeRenderFrame(WebPFrameNativeContext *pNativeContext, int width, int height, int stride, uint8_t *pixels);
bool WebPFrameNativeRenderFrame(WebPFrameNativeContext *pNativeContext, int width, int height, bool forceClearAll, int stride, uint8_t *pixels);
