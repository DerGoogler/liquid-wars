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
