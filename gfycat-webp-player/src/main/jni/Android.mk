LOCAL_PATH := $(call my-dir)
ORIG_LOCAL_PATH := $(LOCAL_PATH)
include $(CLEAR_VARS)

ifneq ($(findstring armeabi-v7a, $(TARGET_ARCH_ABI)),)
  # Setting LOCAL_ARM_NEON will enable -mfpu=neon which may cause illegal
  # instructions to be generated for armv7a code. Instead target the neon code
  # specifically.
  NEON := c.neon
  USE_CPUFEATURES := yes
else
  NEON := c
endif


WEBP_FILES := \
    src/dec/alpha_dec.c \
    src/dec/buffer_dec.c \
    src/dec/frame_dec.c \
    src/dec/idec_dec.c \
    src/dec/io_dec.c \
    src/dec/quant_dec.c \
    src/dec/tree_dec.c \
    src/dec/vp8_dec.c \
    src/dec/vp8l_dec.c \
    src/dec/webp_dec.c \
    src/demux/anim_decode.c \
    src/demux/demux.c \
    src/dsp/alpha_processing.c \
    src/dsp/alpha_processing_mips_dsp_r2.c \
    src/dsp/alpha_processing_neon.$(NEON) \
    src/dsp/alpha_processing_sse2.c \
    src/dsp/alpha_processing_sse41.c \
    src/dsp/argb.c \
    src/dsp/argb_mips_dsp_r2.c \
    src/dsp/argb_sse2.c \
    src/dsp/cpu.c \
    src/dsp/dec.c \
    src/dsp/dec_clip_tables.c \
    src/dsp/dec_mips32.c \
    src/dsp/dec_mips_dsp_r2.c \
    src/dsp/dec_msa.c \
    src/dsp/dec_neon.$(NEON) \
    src/dsp/dec_sse2.c \
    src/dsp/dec_sse41.c \
    src/dsp/filters.c \
    src/dsp/filters_mips_dsp_r2.c \
    src/dsp/filters_msa.c \
    src/dsp/filters_neon.$(NEON) \
    src/dsp/filters_sse2.c \
    src/dsp/lossless.c \
    src/dsp/lossless_mips_dsp_r2.c \
    src/dsp/lossless_msa.c \
    src/dsp/lossless_neon.$(NEON) \
    src/dsp/lossless_sse2.c \
    src/dsp/rescaler.c \
    src/dsp/rescaler_mips32.c \
    src/dsp/rescaler_mips_dsp_r2.c \
    src/dsp/rescaler_msa.c \
    src/dsp/rescaler_neon.$(NEON) \
    src/dsp/rescaler_sse2.c \
    src/dsp/upsampling.c \
    src/dsp/upsampling_mips_dsp_r2.c \
    src/dsp/upsampling_msa.c \
    src/dsp/upsampling_neon.$(NEON) \
    src/dsp/upsampling_sse2.c \
    src/dsp/yuv.c \
    src/dsp/yuv_mips32.c \
    src/dsp/yuv_mips_dsp_r2.c \
    src/dsp/yuv_sse2.c \
    src/utils/bit_reader_utils.c \
    src/utils/color_cache_utils.c \
    src/utils/filters_utils.c \
    src/utils/huffman_utils.c \
    src/utils/quant_levels_dec_utils.c \
    src/utils/random_utils.c \
    src/utils/rescaler_utils.c \
    src/utils/thread_utils.c \
    src/utils/utils.c \


LOCAL_MODULE := gfywebp
LOCAL_SRC_FILES := jni.cpp jni_helpers.cpp webp_wrapper.cpp webp_pinvoke.cpp webp_jni.cpp

LOCAL_CPPFLAGS += -std=c++11 -fexceptions
LOCAL_CFLAGS := -DANDROID -DHAVE_MALLOC_H -DHAVE_PTHREAD \
                -DWEBP_USE_THREAD \
                -finline-functions -frename-registers -ffast-math \
                -s -fomit-frame-pointer -Isrc/webp -Os -fno-exceptions -fno-rtti

LOCAL_C_INCLUDES += $(LOCAL_PATH)/src $(LOCAL_PATH)/libwebp/src

LOCAL_SRC_FILES += $(WEBP_FILES:%=libwebp/%)
LOCAL_LDLIBS := -llog -landroid -ljnigraphics

LOCAL_LDFLAGS := -latomic

ifeq ($(USE_CPUFEATURES),yes)
  LOCAL_STATIC_LIBRARIES := cpufeatures
endif


include $(BUILD_SHARED_LIBRARY)

ifeq ($(USE_CPUFEATURES),yes)
  $(call import-module,android/cpufeatures)
endif
