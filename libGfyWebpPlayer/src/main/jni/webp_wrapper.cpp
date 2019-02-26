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

#include "webp_wrapper.h"

////////////////////////////////////////////////////////////////
/// Related to WebPImage
////////////////////////////////////////////////////////////////

/**
 * Creates a new WebPImage from the specified buffer.
 *
 * @param vBuffer the vector containing the bytes
 * @return a newly allocated WebPImage
 */
std::unique_ptr<WebPImageNativeContext> WebPImageNativeCreateFromByteVector(std::vector<uint8_t>& vBuffer) {

  std::unique_ptr<WebPImageNativeContext> spNativeContext(new WebPImageNativeContext());
  if (!spNativeContext) {
    throw UnableToAllocateNativeContextException();
  }

  // WebPData is on the stack as its only used during the call to WebPDemux.
  WebPData webPData;
  webPData.bytes = vBuffer.data();
  webPData.size = vBuffer.size();

  // Create the WebPDemuxer
  auto spDemuxer = std::unique_ptr<WebPDemuxer, decltype(&WebPDemuxDelete)> {
    WebPDemux(&webPData),
    WebPDemuxDelete
  };
  if (!spDemuxer) {
    // We may want to consider first using functions that will return a useful error code
    // if it fails to parse.
    throw CanNotCreateDemuxerException();
  }

  spNativeContext->pixelWidth = WebPDemuxGetI(spDemuxer.get(), WEBP_FF_CANVAS_WIDTH);
  spNativeContext->pixelHeight = WebPDemuxGetI(spDemuxer.get(), WEBP_FF_CANVAS_HEIGHT);
  spNativeContext->numFrames = WebPDemuxGetI(spDemuxer.get(), WEBP_FF_FRAME_COUNT);
  spNativeContext->loopCount = WebPDemuxGetI(spDemuxer.get(), WEBP_FF_LOOP_COUNT);
  spNativeContext->backgroundColor = WebPDemuxGetI(spDemuxer.get(), WEBP_FF_BACKGROUND_COLOR);

  // Compute cached fields that require iterating the frames.
  int durationMs = 0;
  std::vector<int> frameDurationsMs;
  WebPIterator iter;
  if (WebPDemuxGetFrame(spDemuxer.get(), 1, &iter)) {
    do {
      durationMs += iter.duration;
      frameDurationsMs.push_back(iter.duration);
    } while (WebPDemuxNextFrame(&iter));
    WebPDemuxReleaseIterator(&iter);
  }
  spNativeContext->durationMs = durationMs;
  spNativeContext->frameDurationsMs = frameDurationsMs;

  // Ownership of pDemuxer and vBuffer is transferred to WebPDemuxerWrapper here.
  // Note, according to Rob Arnold, createNew assumes we throw exceptions but we don't. Though
  // he claims this won't happen in practice cause "Linux will overcommit pages, we should only
  // get this error if we run out of virtual address space." Also, Daniel C may be working
  // on converting to exceptions.
  spNativeContext->spDemuxer = std::shared_ptr<WebPDemuxerWrapper>(new WebPDemuxerWrapper(std::move(spDemuxer), std::move(vBuffer)));

  return spNativeContext;
}

/**
 * Search for last key frame in range [start, end].
 */
int WebPImageNativeLastKeyFrameInRange(WebPImageNativeContext *pNativeContext, int start, int end) {

  for(int frame = end; frame >= start; frame --) {
    if(frame == 0) return 0;
      std::unique_ptr<WebPFrameNativeContext> spFrameNativeContext = WebPImageNativeGetFrame(pNativeContext, frame);
      if (!spFrameNativeContext) {
          return -1;
      }

    if(spFrameNativeContext->isKeyFrame()) return frame;
  }

  return -1;
}

/**
 * Gets the Frame at the specified index.
 *
 * @param index the index of the frame
 * @return a newly created WebPFrame for the specified frame
 */
std::unique_ptr<WebPFrameNativeContext> WebPImageNativeGetFrame(WebPImageNativeContext *pNativeContext, int index) {

  //  auto spIter = std::unique_ptr<WebPIterator, decltype(&WebPDemuxReleaseIterator)> {
  //    new WebPIterator(),
  //    WebPDemuxReleaseIterator
  //  };

  auto spIter = std::unique_ptr<WebPIterator>(new WebPIterator());

  std::unique_ptr<WebPFrameNativeContext> spFrameNativeContext(new WebPFrameNativeContext());
  if (!spFrameNativeContext) {
    return nullptr;
  }

  // Note, in WebP, frame numbers are one-based.
  if (index > 0 && WebPDemuxGetFrame(pNativeContext->spDemuxer->get(), index, spIter.get())) {
    spFrameNativeContext->disposeXOffset = spIter->x_offset;
    spFrameNativeContext->disposeYOffset = spIter->y_offset;
    spFrameNativeContext->disposeWidth = spIter->width;
    spFrameNativeContext->disposeHeight = spIter->height;
    spFrameNativeContext->disposeToBackgroundColor = spIter->dispose_method == WEBP_MUX_DISPOSE_BACKGROUND;
  } else {
    spFrameNativeContext->disposeXOffset = 0;
    spFrameNativeContext->disposeYOffset = 0;
    spFrameNativeContext->disposeWidth = pNativeContext->pixelWidth;
    spFrameNativeContext->disposeHeight = pNativeContext->pixelHeight;
    spFrameNativeContext->disposeToBackgroundColor = true;
  }

  // Note, in WebP, frame numbers are one-based.
  if (!WebPDemuxGetFrame(pNativeContext->spDemuxer->get(), index + 1, spIter.get())) {
    return nullptr;
  }

  spFrameNativeContext->spDemuxer = pNativeContext->spDemuxer;
  spFrameNativeContext->frameNum = spIter->frame_num;
  spFrameNativeContext->xOffset = spIter->x_offset;
  spFrameNativeContext->yOffset = spIter->y_offset;
  spFrameNativeContext->durationMs = spIter->duration;
  spFrameNativeContext->width = spIter->width;
  spFrameNativeContext->height = spIter->height;
  spFrameNativeContext->imageWidth = pNativeContext->pixelWidth;
  spFrameNativeContext->imageHeight = pNativeContext->pixelHeight;
//  spFrameNativeContext->backgroundColor = pNativeContext->backgroundColor;
  spFrameNativeContext->backgroundColor = 0;
  spFrameNativeContext->blendWithPreviousFrame = spIter->blend_method == WEBP_MUX_BLEND;
  spFrameNativeContext->hasAlpha = spIter->has_alpha == 1;
  spFrameNativeContext->pPayload = spIter->fragment.bytes;
  spFrameNativeContext->payloadSize = spIter->fragment.size;

  return spFrameNativeContext;
}

////////////////////////////////////////////////////////////////
/// Related to WebPFrame
////////////////////////////////////////////////////////////////

bool WebPFrameNativeRenderFrame(WebPFrameNativeContext *pNativeContext, int width, int height, int stride, uint8_t *pixels) {
  return WebPFrameNativeRenderFrame(pNativeContext, width, height, false, stride, pixels);
}

/**
 * Renders the frame to the specified pixel array. The array is expected to have a size that
 * is at least the the width and height of the frame. The frame is rendered where each pixel is
 * represented as a 32-bit BGRA pixel. The rendered stride is the same as the frame width. Note,
 * the number of pixels written to the array may be smaller than the canvas if the frame's
 * width/height is smaller than the canvas.
 */
bool WebPFrameNativeRenderFrame(WebPFrameNativeContext *pNativeContext, int width, int height, bool forceClearAll, int stride, uint8_t *pixels) {

  if (width < 0 || height < 0) {
    return false;
  }

  WebPDecoderConfig config;
  int ret = WebPInitDecoderConfig(&config);
  if (!ret) {
    return false;
  }

  const uint8_t* pPayload = pNativeContext->pPayload;
  size_t payloadSize = pNativeContext->payloadSize;

  ret = (WebPGetFeatures(pPayload , payloadSize, &config.input) == VP8_STATUS_OK);
  if (!ret) {
    return false;
  }

  int render_x = pNativeContext->xOffset;
  int render_y = pNativeContext->yOffset;
  int render_width = pNativeContext->width;
  int render_height = pNativeContext->height;

  int dispose_x = pNativeContext->disposeXOffset;
  int dispose_y = pNativeContext->disposeYOffset;
  int dispose_width = pNativeContext->disposeWidth;
  int dispose_height = pNativeContext->disposeHeight;

  config.options.no_fancy_upsampling = 1;
  if (width != pNativeContext->imageWidth || height != pNativeContext->imageHeight) {
    render_x = render_x * width / pNativeContext->imageWidth;
    render_y = render_y * height / pNativeContext->imageHeight;
    render_width = render_width * width / pNativeContext->imageWidth;
    render_height = render_height * height / pNativeContext->imageHeight;
    dispose_x = dispose_x * width / pNativeContext->imageWidth;
    dispose_y = dispose_y * height / pNativeContext->imageHeight;
    dispose_width = dispose_width * width / pNativeContext->imageWidth;
    dispose_height = dispose_height * height / pNativeContext->imageHeight;
    config.options.use_scaling = true;
    config.options.scaled_width = render_width;
    config.options.scaled_height = render_height;
  }

  if(forceClearAll) {
    memset(pixels, 0, stride * height);
  } else if (pNativeContext->disposeToBackgroundColor) {
    uint8_t bakcgroundAlpha = pNativeContext->backgroundColor & 0xff;
    if (bakcgroundAlpha) {
      uint8_t b = (pNativeContext->backgroundColor >> 24) & 0xff;
      uint8_t g = (pNativeContext->backgroundColor >> 16) & 0xff;
      uint8_t r = (pNativeContext->backgroundColor >> 8) & 0xff;
      uint8_t a = bakcgroundAlpha;
      uint8_t *buffer = pixels + dispose_y * stride + dispose_x * 4;
      uint8_t *p = buffer;
      for (int x = 0; x < dispose_width; ++x) {
        p[0] = r;
        p[1] = g;
        p[2] = b;
        p[3] = a;
        p += 4;
      }
      uint8_t *q = pixels + (dispose_y + 1) * stride + dispose_x * 4;
      for (int y = 1; y < dispose_height; ++y) {
        memmove(q, buffer, dispose_width * 4);
        q += stride;
      }
    } else if (dispose_x || dispose_y || dispose_width != width || dispose_height != height) {
      uint8_t *p = pixels + dispose_y * stride + dispose_x * 4;
      for (int y = 0; y < dispose_height; ++y) {
        memset(p, 0, dispose_width * 4);
        p += stride;
      }
    } else if (stride > 0) {
      memset(pixels, 0, height * stride);
    } else {
      memset(pixels + (height - 1) * stride, 0, -(height * stride));
    }
  }

  config.output.colorspace = MODE_rgbA;
  config.output.is_external_memory = 1;

  if (pNativeContext->blendWithPreviousFrame) {
    uint8_t *buffer = new uint8_t[render_height * render_width * 4];
    config.output.u.RGBA.rgba = buffer;
    config.output.u.RGBA.stride = render_width * 4;
    config.output.u.RGBA.size   = render_height * render_width * 4;
    ret = WebPDecode(pPayload, payloadSize, &config);
    if (ret == VP8_STATUS_OK) {
      uint8_t *p = pixels + render_y * stride + render_x * 4;
      uint8_t *q = buffer;
      for (int y = 0; y < render_height; ++y) {
        for (int x = 0; x < render_width; ++x) {
          p[0] = ((int)p[0] * (int)(255 - q[3]) + (int)q[0] * (int)q[3]) / 255;
          p[1] = ((int)p[1] * (int)(255 - q[3]) + (int)q[1] * (int)q[3]) / 255;
          p[2] = ((int)p[2] * (int)(255 - q[3]) + (int)q[2] * (int)q[3]) / 255;
          p[3] = ((int)p[3] * (int)(255 - q[3]) + (int)255  * (int)q[3]) / 255;
          p += 4;
          q += 4;
        }
        p -= render_width * 4;
        p += stride;
      }
    }
    delete [] buffer;
  } else {
    config.output.u.RGBA.rgba = pixels + render_y * stride + render_x * 4;
    config.output.u.RGBA.stride = stride;
    config.output.u.RGBA.size   = stride * height;
    ret = WebPDecode(pPayload, payloadSize, &config);
  }

  return ret == VP8_STATUS_OK;
}