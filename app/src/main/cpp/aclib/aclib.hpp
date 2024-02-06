//    This file is part of Liquid Wars.
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

#ifndef ACLIB_HPP
#define ACLIB_HPP

#include <jni.h>
#include <GLES/gl.h>
#include "assets.hpp"
#include "log.hpp"
#include "lodepng.hpp"

#define ACTIVITY_NAME "com/xenris/liquidwarsos/GameActivity"

class ACLib {
    private:
        JavaVM* jvm;
        jobject jobj;

    public:
        ACLib(JNIEnv* env, jobject jobj, jobject am);
        ~ACLib();
        void destroy(JNIEnv* env, jobject jobj);
        void glOrthogonal(float left, float right, float bottom, float top, float near, float far);
        void finish();
};

extern ACLib* acLib;

#endif
