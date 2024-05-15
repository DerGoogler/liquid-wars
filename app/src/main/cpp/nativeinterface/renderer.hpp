#ifndef RENDERER_HPP
#define RENDERER_HPP

#include <jni.h>

extern "C" {
void Java_com_dergoogler_liquidwars_NativeInterface_onSurfaceCreated(JNIEnv * env, jclass jobj);
void Java_com_dergoogler_liquidwars_NativeInterface_onDrawFrame(JNIEnv * env, jclass jobj);
void Java_com_dergoogler_liquidwars_NativeInterface_onSurfaceChanged(JNIEnv * env, jclass jobj, jint width, jint height);
}

void onSurfaceCreated();
void onDrawFrame();
void onSurfaceChanged(int width, int height);

#endif
