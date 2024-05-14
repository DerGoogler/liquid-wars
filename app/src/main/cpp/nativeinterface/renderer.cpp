#include "renderer.hpp"

extern "C" void Java_com_xenris_liquidwarsos_NativeInterface_onSurfaceCreated(JNIEnv * env,
                                                                              jclass jobj) {
    onSurfaceCreated();
}

extern "C" void Java_com_xenris_liquidwarsos_NativeInterface_onDrawFrame(JNIEnv * env, jclass jobj) {
    onDrawFrame();
}

extern "C" void Java_com_xenris_liquidwarsos_NativeInterface_onSurfaceChanged(JNIEnv * env,
                                                                              jclass jobj, jint width, jint height) {
    onSurfaceChanged(width, height);
}
