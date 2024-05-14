#ifndef LOG_HPP
#define LOG_HPP

#include <android/log.h>

#define logInfo(...) ((void)__android_log_print(ANDROID_LOG_INFO, "LiquidWarsLog", __VA_ARGS__))

#endif
