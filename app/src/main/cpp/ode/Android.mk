include $(CLEAR_VARS)

MY_LOCAL_PATH := $(LOCAL_PATH)/ode

LOCAL_MODULE := ode

MY_LOCAL_SRC_FILES := $(wildcard $(MY_LOCAL_PATH)/*.cpp)
LOCAL_SRC_FILES := $(subst cpp/, , $(MY_LOCAL_SRC_FILES))

include $(BUILD_SHARED_LIBRARY)
