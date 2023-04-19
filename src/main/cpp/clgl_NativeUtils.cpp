
#include "clgl_NativeUtils.h"

/*
 * Class:     de_linusdev_clgl_nat_NativeUtils
 * Method:    isNull
 * Signature: (J)Z
 */
JNIEXPORT jboolean JNICALL Java_de_linusdev_clgl_nat_NativeUtils_isNull
  (JNIEnv* env, jclass clazz, jlong pointer) {
    return reinterpret_cast<void*>(pointer) == nullptr ? JNI_TRUE : JNI_FALSE;
}


