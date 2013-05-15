//    This file is part of Liquid Wars.
//
//    Copyright (C) 2013 Henry Shepperd (hshepperd@gmail.com)
//
//    Liquid Wars is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    Liquid Wars is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with Liquid Wars.  If not, see <http://www.gnu.org/licenses/>.

#ifndef LIFECYCLE_HPP
#define LIFECYCLE_HPP

#include <jni.h>
#include <aclib.hpp>

extern "C" {
void Java_com_xenris_liquidwarsos_NativeInterface_init(JNIEnv* env, jobject jobj, jobject am);
void Java_com_xenris_liquidwarsos_NativeInterface_uninit(JNIEnv* env, jobject jobj);
void Java_com_xenris_liquidwarsos_NativeInterface_createGame(JNIEnv* env, jobject jobj, jint team, jint map, jint seed);
void Java_com_xenris_liquidwarsos_NativeInterface_destroyGame(JNIEnv* env, jobject jobj);
void Java_com_xenris_liquidwarsos_NativeInterface_stepDots(JNIEnv* env, jobject jobj);
void Java_com_xenris_liquidwarsos_NativeInterface_setPlayerPosition(JNIEnv* env, jobject jobj, jint team, jshortArray jxa, jshortArray jya);
int Java_com_xenris_liquidwarsos_NativeInterface_getNearestDot(JNIEnv* env, jobject jobj, jint p, jshort px, jshort py);
int Java_com_xenris_liquidwarsos_NativeInterface_teamScore(JNIEnv* env, jobject jobj, jint p);
void Java_com_xenris_liquidwarsos_NativeInterface_setTimeSidebar(JNIEnv* env, jobject jobj, jfloat t);
}

void createGame(int team, int map, int seed);
void destroyGame();
void stepDots();
void setPlayerPosition(int team, short* x, short* y);
int getNearestDot(int p, short px, short py);
int teamScore(int p);
void setTimeSidebar(float t);

#endif
