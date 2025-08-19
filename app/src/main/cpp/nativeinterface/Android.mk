include $(CLEAR_VARS)

MY_LOCAL_PATH := $(LOCAL_PATH)/nativeinterface

# APP_ALLOW_MISSING_DEPS = true

LOCAL_MODULE := nativeinterface

# 16 KB page size compatibility flags
LOCAL_LDFLAGS += "-Wl,-z,max-page-size=16384"
LOCAL_LDFLAGS += "-Wl,-z,common-page-size=16384"
LOCAL_LDFLAGS += "-Wl,-z,separate-code"

MY_LOCAL_SRC_FILES := $(wildcard $(MY_LOCAL_PATH)/*.cpp)
LOCAL_SRC_FILES := $(subst cpp/, , $(MY_LOCAL_SRC_FILES))

LOCAL_STATIC_LIBRARIES := application
LOCAL_LDLIBS := -llog -landroid -lGLESv1_CM

include $(BUILD_SHARED_LIBRARY)
