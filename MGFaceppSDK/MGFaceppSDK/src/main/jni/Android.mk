LOCAL_PATH := $(call my-dir)

MEGVII_FACEPP_VERSION := 0.4.1

include $(CLEAR_VARS)
LOCAL_MODULE := fppapi
LOCAL_SRC_FILES  := $(LOCAL_PATH)/libs/${TARGET_ARCH_ABI}/libMegviiFacepp-${MEGVII_FACEPP_VERSION}.so
include $(PREBUILT_SHARED_LIBRARY)


include $(CLEAR_VARS)
LOCAL_MODULE    := MegviiFacepp-jni-${MEGVII_FACEPP_VERSION}
LOCAL_SRC_FILES := megvii_facepp_jni.cpp
LOCAL_C_INCLUDES := include
LOCAL_C_INCLUDES += thirdparty security
LOCAL_SHARED_LIBRARIES := fppapi
LOCAL_LDLIBS += -L$(SYSROOT)/usr/lib -llog -Wl,-s
LOCAL_CPPFLAGS += -std=c++11 -ffunction-sections -fdata-sections -fvisibility=hidden \
        -Wall -Wextra -fweb
LOCAL_LDFLAGS += -Wl,--gc-sections

ifeq ($(TARGET_ARCH_ABI),armeabi-v7a)
    LOCAL_ARM_NEON := true
    LOCAL_CPPFLAGS += -mfpu=neon -mfloat-abi=softfp
endif

ifeq ($(TARGET_ARCH_ABI),x86)
    LOCAL_CPPFLAGS += -msse4.2
endif

LOCAL_ARM_MODE := arm
LOCAL_CPPFLAGS := $(LOCAL_CPPFLAGS) -std=c++11 -Wall -Wextra -fweb
include $(BUILD_SHARED_LIBRARY)
