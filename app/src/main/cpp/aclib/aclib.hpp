#ifndef ACLIB_HPP
#define ACLIB_HPP

#include <jni.h>
#include <GLES/gl.h>
#include "assets.hpp"
#include "log.hpp"
#include "lodepng.hpp"

#define ACTIVITY_NAME "com/dergoogler/liquidwars/GameActivity"

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
