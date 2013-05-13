LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := swipe
LOCAL_SRC_FILES := android.c libswipe/square.c libswipe/unimag.c libswipe/pcm_helpers.c libswipe/list.c

include $(BUILD_SHARED_LIBRARY)