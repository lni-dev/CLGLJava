
#include "clgl_NativeUtils.h"
#include "CL/cl.h"

/*
 * Class:     de_linusdev_clgl_nat_NativeUtils
 * Method:    isNull
 * Signature: (J)Z
 */
JNIEXPORT jboolean JNICALL Java_de_linusdev_clgl_nat_NativeUtils_isNull
  (JNIEnv* env, jclass clazz, jlong pointer) {
    return reinterpret_cast<void*>(pointer) == nullptr ? JNI_TRUE : JNI_FALSE;
}

/*
 * Class:     de_linusdev_clgl_nat_NativeUtils
 * Method:    _getNullPointer
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_de_linusdev_clgl_nat_NativeUtils__1getNullPointer
        (JNIEnv *, jclass) {
    return reinterpret_cast<jlong>(nullptr);
}

JNIEXPORT jlong JNICALL Java_de_linusdev_clgl_nat_NativeUtils_sf_1cl_1mem
        (JNIEnv *, jclass) {
    return sizeof(cl_mem);
}


