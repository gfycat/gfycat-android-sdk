//
//  webp_pinvoke.c
//  libwebp
//
//  Created by Victor Pavlychko on 2/9/18.
//

#include "webp_wrapper.h"

extern "C" WebPImageNativeContext *_WebPImage_CreateFromMemory(uint8_t *data, int32_t size) {
  std::vector<uint8_t> vBuffer(data, data + size);
  return WebPImageNativeCreateFromByteVector(vBuffer).release();
}

extern "C" void _WebPImage_Release(WebPImageNativeContext *context) {
  delete context;
}

extern "C" int32_t _WebPImage_GetWidth(WebPImageNativeContext *context) {
  return context->pixelWidth;
}

extern "C" int32_t _WebPImage_GetHeight(WebPImageNativeContext *context) {
  return context->pixelHeight;
}

extern "C" int32_t _WebPImage_GetFrameCount(WebPImageNativeContext *context) {
  return context->numFrames;
}

extern "C" int32_t _WebPImage_GetDuration(WebPImageNativeContext *context) {
  return context->durationMs;
}

extern "C" int32_t _WebPImage_GetLoopCount(WebPImageNativeContext *context) {
  return context->loopCount;
}

extern "C" int32_t _WebPImage_GetFrameDurations(WebPImageNativeContext *context) {
  return 0;
}

extern "C" WebPFrameNativeContext *_WebPImage_GetFrame(WebPImageNativeContext *context, int index) {
  std::unique_ptr<WebPFrameNativeContext> frameContext = WebPImageNativeGetFrame(context, index);
  return frameContext.release();
}

extern "C" void _WebPFrame_RenderFrame(WebPFrameNativeContext *context, int32_t width, int32_t height, int32_t stride, uint8_t *pixels) {
  WebPFrameNativeRenderFrame(context, width, height, stride, pixels);
}

extern "C" int32_t _WebPFrame_GetDurationMs(WebPFrameNativeContext *context) {
  return context->durationMs;
}

extern "C" int32_t _WebPFrame_GetWidth(WebPFrameNativeContext *context) {
  return context->width;
}

extern "C" int32_t _WebPFrame_GetHeight(WebPFrameNativeContext *context) {
  return context->height;
}

extern "C" int32_t _WebPFrame_GetXOffset(WebPFrameNativeContext *context) {
  return context->xOffset;
}

extern "C" int32_t _WebPFrame_GetYOffset(WebPFrameNativeContext *context) {
  return context->yOffset;
}

extern "C" int32_t _WebPFrame_GetShouldDisposeToBackgroundColor(WebPFrameNativeContext *context) {
  return context->disposeToBackgroundColor;
}

extern "C" int32_t _WebPFrame_GetShouldBlendWithPreviousFrame(WebPFrameNativeContext *context) {
  return context->blendWithPreviousFrame;
}

extern "C" void _WebPFrame_Release(WebPFrameNativeContext *context) {
  delete context;
}
