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

#include "lifecycle.hpp"

void Java_com_xenris_liquidwarsos_NativeInterface_init(JNIEnv* env, jobject jobj, jobject am) {
    acLib = new ACLib(env, jobj, am);
}

void Java_com_xenris_liquidwarsos_NativeInterface_uninit(JNIEnv* env, jobject jobj) {
    acLib->destroy(env, jobj);
    delete(acLib);
    acLib = NULL;
}

void Java_com_xenris_liquidwarsos_NativeInterface_createGame(JNIEnv* env, jobject jobj, jint team, jint map, jint seed, jint dotsPerTeam) {
    createGame(team, map, seed, dotsPerTeam);
}

void Java_com_xenris_liquidwarsos_NativeInterface_destroyGame(JNIEnv* env, jobject jobj) {
    destroyGame();
}

void Java_com_xenris_liquidwarsos_NativeInterface_stepDots(JNIEnv* env, jobject jobj) {
    stepDots();
}

void Java_com_xenris_liquidwarsos_NativeInterface_setPlayerPosition(JNIEnv* env, jobject jobj, jint team, jshortArray jxa, jshortArray jya) {
    short* x = env->GetShortArrayElements(jxa, NULL);
    short* y = env->GetShortArrayElements(jya, NULL);

    setPlayerPosition(team, x, y);

    env->ReleaseShortArrayElements(jxa, x, 0);
    env->ReleaseShortArrayElements(jya, y, 0);
}

int Java_com_xenris_liquidwarsos_NativeInterface_getNearestDot(JNIEnv* env, jobject jobj, jint p, jshort px, jshort py) {
    return getNearestDot(p, px, py);
}

int Java_com_xenris_liquidwarsos_NativeInterface_teamScore(JNIEnv* env, jobject jobj, jint p) {
    return teamScore(p);
}

void Java_com_xenris_liquidwarsos_NativeInterface_setTimeSidebar(JNIEnv* env, jobject jobj, jfloat t) {
    setTimeSidebar(t);
}
