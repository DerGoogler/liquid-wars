include $(CLEAR_VARS)

MY_LOCAL_PATH := $(LOCAL_PATH)/application

LOCAL_MODULE := application

MY_LOCAL_SRC_FILES := $(wildcard $(MY_LOCAL_PATH)/*.cpp)
LOCAL_SRC_FILES := $(subst jni/, , $(MY_LOCAL_SRC_FILES))

LOCAL_STATIC_LIBRARIES := aclib

LOCAL_CFLAGS := -Wall -Werror

include $(BUILD_STATIC_LIBRARY)
