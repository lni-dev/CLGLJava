
#include "clgl_WGL.h"
#include <windows.h>

/*
 * Class:     de_linusdev_clgl_nat_wgl_WGL
 * Method:    _wglGetCurrentContext
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_de_linusdev_cvg4j_nat_wgl_WGL__1wglGetCurrentContext
        (JNIEnv *, jclass) {
    return reinterpret_cast<jlong>(wglGetCurrentContext());
}

/*
 * Class:     de_linusdev_clgl_nat_wgl_WGL
 * Method:    _wglGetCurrentDC
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_de_linusdev_cvg4j_nat_wgl_WGL__1wglGetCurrentDC
        (JNIEnv *, jclass) {
    return reinterpret_cast<jlong>(wglGetCurrentDC());
}