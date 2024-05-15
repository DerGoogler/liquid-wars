#include "lifecycle.hpp"

extern "C" void Java_com_dergoogler_liquidwars_NativeInterface_init(JNIEnv* env, jclass jobj, jobject am) {
    acLib = new ACLib(env, jobj, am);
}

extern "C" void Java_com_dergoogler_liquidwars_NativeInterface_uninit(JNIEnv* env, jclass jobj) {
    acLib->destroy(env, jobj);
    delete(acLib);
    acLib = NULL;
}

extern "C" void Java_com_dergoogler_liquidwars_NativeInterface_createGame(JNIEnv* env, jclass jobj, jint team, jint map, jint seed, jint dotsPerTeam) {
    createGame(team, map, seed, dotsPerTeam);
}

extern "C" void Java_com_dergoogler_liquidwars_NativeInterface_destroyGame(JNIEnv* env, jclass jobj) {
    destroyGame();
}

extern "C" void Java_com_dergoogler_liquidwars_NativeInterface_stepDots(JNIEnv* env, jclass jobj) {
    stepDots();
}

extern "C" void Java_com_dergoogler_liquidwars_NativeInterface_setPlayerPosition(JNIEnv* env, jclass jobj, jint team, jshortArray jxa, jshortArray jya) {
    short* x = env->GetShortArrayElements(jxa, NULL);
    short* y = env->GetShortArrayElements(jya, NULL);

    setPlayerPosition(team, x, y);

    env->ReleaseShortArrayElements(jxa, x, 0);
    env->ReleaseShortArrayElements(jya, y, 0);
}

extern "C" int Java_com_dergoogler_liquidwars_NativeInterface_getNearestDot(JNIEnv* env, jclass jobj, jint p, jshort px, jshort py) {
    return getNearestDot(p, px, py);
}

extern "C" int Java_com_dergoogler_liquidwars_NativeInterface_teamScore(JNIEnv* env, jclass jobj, jint p) {
    return teamScore(p);
}

extern "C" void Java_com_dergoogler_liquidwars_NativeInterface_setTimeSidebar(JNIEnv* env, jclass jobj, jfloat t) {
    setTimeSidebar(t);
}
