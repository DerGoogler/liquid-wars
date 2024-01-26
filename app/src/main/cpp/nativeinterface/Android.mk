include $(CLEAR_VARS)

MY_LOCAL_PATH := $(LOCAL_PATH)/nativeinterface

# APP_ALLOW_MISSING_DEPS = true

LOCAL_MODULE := nativeinterface

MY_LOCAL_SRC_FILES := $(wildcard $(MY_LOCAL_PATH)/*.cpp)
LOCAL_SRC_FILES := $(subst cpp/, , $(MY_LOCAL_SRC_FILES))

LOCAL_STATIC_LIBRARIES := application
LOCAL_LDLIBS := -llog -landroid -lGLESv1_CM

include $(BUILD_SHARED_LIBRARY)
