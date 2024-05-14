#ifndef LIFECYCLE_HPP
#define LIFECYCLE_HPP

#include <jni.h>
#include <aclib.hpp>

extern "C" {
void Java_com_xenris_liquidwarsos_NativeInterface_init(JNIEnv* env, jclass jobj, jobject am);
void Java_com_xenris_liquidwarsos_NativeInterface_uninit(JNIEnv* env, jclass jobj);
void Java_com_xenris_liquidwarsos_NativeInterface_createGame(JNIEnv* env, jclass jobj, jint team, jint map, jint seed, jint dotsPerTeam);
void Java_com_xenris_liquidwarsos_NativeInterface_destroyGame(JNIEnv* env, jclass jobj);
void Java_com_xenris_liquidwarsos_NativeInterface_stepDots(JNIEnv* env, jclass jobj);
void Java_com_xenris_liquidwarsos_NativeInterface_setPlayerPosition(JNIEnv* env, jclass jobj, jint team, jshortArray jxa, jshortArray jya);
int Java_com_xenris_liquidwarsos_NativeInterface_getNearestDot(JNIEnv* env, jclass jobj, jint p, jshort px, jshort py);
int Java_com_xenris_liquidwarsos_NativeInterface_teamScore(JNIEnv* env, jclass jobj, jint p);
void Java_com_xenris_liquidwarsos_NativeInterface_setTimeSidebar(JNIEnv* env, jclass jobj, jfloat t);
}

void createGame(int team, int map, int seed, int dotsPerTeam);
void destroyGame();
void stepDots();
void setPlayerPosition(int team, short* x, short* y);
int getNearestDot(int p, short px, short py);
int teamScore(int p);
void setTimeSidebar(float t);

#endif
