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

#include "aclib.hpp"

ACLib* acLib;

ACLib::ACLib(JNIEnv* env, jobject jobj, jobject am) {
    this->jobj = env->NewGlobalRef(jobj);
    env->GetJavaVM(&this->jvm);
    acAssets = new ACAssets(AAssetManager_fromJava(env, am));
}

ACLib::~ACLib() {
    delete(acAssets);
}

void ACLib::destroy(JNIEnv* env, jobject jobj) {
    env->DeleteGlobalRef(this->jobj);
}

void ACLib::glOrthogonal(float left, float right, float bottom, float top, float near, float far) {
    glMatrixMode(GL_PROJECTION);
    glLoadIdentity();
    glOrthof(left, right, bottom, top, near, far);
    glMatrixMode(GL_MODELVIEW);
}

void ACLib::finish() {
    JNIEnv* env;
    jvm->AttachCurrentThread(&env, NULL);
    jclass cls = env->FindClass(ACTIVITY_NAME);
    jmethodID mid = env->GetMethodID(cls, "finish", "()V");
    env->CallVoidMethod(jobj, mid);
}
