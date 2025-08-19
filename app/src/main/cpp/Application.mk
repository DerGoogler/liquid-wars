APP_PLATFORM := android-14
APP_ABI := armeabi-v7a
APP_ABI += arm64-v8a
APP_ABI += x86_64
APP_ABI += x86
NDK_TOOLCHAIN_VERSION = clang
APP_STL := c++_shared

# Global 16 KB page size compatibility flags
APP_LDFLAGS := -Wl,-z,max-page-size=16384 -Wl,-z,common-page-size=16384 -Wl,-z,separate-code
APP_CFLAGS := -ffunction-sections -fdata-sections
APP_CPPFLAGS := -ffunction-sections -fdata-sections
