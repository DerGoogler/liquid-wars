include $(CLEAR_VARS)

MY_LOCAL_PATH := $(LOCAL_PATH)/ode

LOCAL_MODULE := ode

# 16 KB page size compatibility flags
LOCAL_CFLAGS += -ffunction-sections -fdata-sections
LOCAL_CPPFLAGS += -ffunction-sections -fdata-sections
LOCAL_LDFLAGS += "-Wl,-z,max-page-size=16384"
LOCAL_LDFLAGS += "-Wl,-z,common-page-size=16384"
LOCAL_LDFLAGS += "-Wl,-z,separate-code"
LOCAL_LDFLAGS += "-Wl,--gc-sections"
LOCAL_LDFLAGS += "-Wl,-T,$(MY_LOCAL_PATH)/ode_16kb.ld"

# Include all .c and .cpp files recursively
MY_LOCAL_CPP_FILES := $(shell find $(MY_LOCAL_PATH)/src -name "*.cpp")
MY_LOCAL_C_FILES := $(shell find $(MY_LOCAL_PATH)/src -name "*.c")
LOCAL_SRC_FILES := $(subst cpp/, , $(MY_LOCAL_CPP_FILES))
LOCAL_SRC_FILES += $(subst cpp/, , $(MY_LOCAL_C_FILES))

# Include headers path
LOCAL_C_INCLUDES := $(MY_LOCAL_PATH)/src $(MY_LOCAL_PATH)/src/joints

include $(BUILD_SHARED_LIBRARY)
