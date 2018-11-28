LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE := dpnusb
LOCAL_SRC_FILES := ./lib$(LOCAL_MODULE).so
include $(PREBUILT_SHARED_LIBRARY)


include $(CLEAR_VARS)

LOCAL_CFLAGS := -O2 -fvisibility=hidden -fomit-frame-pointer -fstrict-aliasing -ffunction-sections -fdata-sections -ffast-math
LOCAL_CPPFLAGS := -O2 -fvisibility=hidden -fvisibility-inlines-hidden -fomit-frame-pointer -fstrict-aliasing -ffunction-sections -fdata-sections -ffast-math
LOCAL_LDFLAGS += -Wl,--gc-sections

LOCAL_CFLAGS += 
LOCAL_CPPFLAGS += 
LOCAL_LDFLAGS += 

LOCAL_LDLIBS += -lz -lm -llog -ljnigraphics
LOCAL_CFLAGS += -fPIE -DOPENCV
LOCAL_LDFLAGS += -fPIE
LOCAL_CPPFLAGS += -std=c++11 -o3
LOCAL_EXPORT_CPPFLAGS := -std=c++11

LOCAL_STATIC_LIBRARIES +=
LOCAL_SHARED_LIBRARIES += dpnusb

LOCAL_C_INCLUDES += $(LOCAL_PATH)/..  $(LOCAL_PATH)
LOCAL_EXPORT_C_INCLUDES += $(LOCAL_PATH)/..  $(LOCAL_PATH)

LOCAL_MODULE := native-lib
LOCAL_SRC_FILES := ../dpnJniEntry.cpp

include $(BUILD_SHARED_LIBRARY)

