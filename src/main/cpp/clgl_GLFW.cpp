// Copyright (c) 2023 Linus Andera
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

#include "clgl_GLFW.h"
#include "GLFW/glfw3.h"
#include "JniUtils.h"

/*
 * Class:     de_linusdev_clgl_nat_glfw3_GLFWWindow
 * Method:    _glfwInit
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_de_linusdev_clgl_nat_glfw3_GLFW__1glfwInit
  (JNIEnv* env, jclass clazz) {
    return glfwInit();
}

/*
 * Class:     de_linusdev_clgl_nat_glfw3_GLFWWindow
 * Method:    _glfwWindowHint
 * Signature: (II)V
 */
JNIEXPORT void JNICALL Java_de_linusdev_clgl_nat_glfw3_GLFW__1glfwWindowHint
  (JNIEnv* env, jclass clazz, jint hint, jint value) {
    glfwWindowHint(hint, value);
}

static jobject globalRefErrorCallback = nullptr;
static jmethodID onErrorMethodId = nullptr;

/*
 * Class:     de_linusdev_clgl_nat_glfw3_GLFWWindow
 * Method:    _glfwSetErrorCallback
 * Signature: (Lde/linusdev/clgl/nat/glfw3/ErrorCallback;)V
 */
JNIEXPORT void JNICALL Java_de_linusdev_clgl_nat_glfw3_GLFW__1glfwSetErrorCallback
  (JNIEnv* env, jclass clazz, jobject callback) {

    if(globalRefErrorCallback)
        env->DeleteGlobalRef(globalRefErrorCallback);

    globalRefErrorCallback = env->NewGlobalRef(callback);
    onErrorMethodId = env->GetMethodID(env->GetObjectClass(callback), "onError", "(ILjava/lang/String;)V");

    glfwSetErrorCallback([](int error, const char* description) {
        JNIEnv* env;
        JNI_UTILS->getEnv(&env);

        jstring jSDescription = env->NewStringUTF(description);

        env->CallVoidMethod(globalRefErrorCallback, onErrorMethodId, error, jSDescription);

        env->DeleteLocalRef(jSDescription);
    });
}

/*
 * Class:     de_linusdev_clgl_nat_glfw3_GLFWWindow
 * Method:    _glfwCreateWindow
 * Signature: (IILjava/lang/String;)J
 */
JNIEXPORT jlong JNICALL Java_de_linusdev_clgl_nat_glfw3_GLFW__1glfwCreateWindow
  (JNIEnv* env, jclass clazz, jint width, jint height, jstring title) {
    const char* cTitle = env->GetStringUTFChars(title, nullptr);
    GLFWwindow* pointer = glfwCreateWindow(width, height, cTitle, nullptr, nullptr);
    env->ReleaseStringUTFChars(title, cTitle);
    return reinterpret_cast<jlong>(pointer);
}

/*
 * Class:     de_linusdev_clgl_nat_glfw3_GLFWWindow
 * Method:    _glfwTerminate
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_de_linusdev_clgl_nat_glfw3_GLFW__1glfwTerminate
  (JNIEnv* env, jclass clazz) {
    glfwTerminate();
}

/*
 * Class:     de_linusdev_clgl_nat_glfw3_GLFWWindow
 * Method:    _glfwPollEvents
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_de_linusdev_clgl_nat_glfw3_GLFW__1glfwPollEvents
        (JNIEnv* env, jclass clazz) {
    glfwPollEvents();
}

/*
 * Class:     de_linusdev_clgl_nat_glfw3_GLFWWindow
 * Method:    _glfwMakeContextCurrent
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_de_linusdev_clgl_nat_glfw3_GLFW__1glfwMakeContextCurrent
  (JNIEnv* env, jclass clazz, jlong pointer) {
    auto* win = reinterpret_cast<GLFWwindow*>(pointer);
    glfwMakeContextCurrent(win);
}

/*
 * Class:     de_linusdev_clgl_nat_glfw3_GLFWWindow
 * Method:    _glfwDestroyWindow
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_de_linusdev_clgl_nat_glfw3_GLFW__1glfwDestroyWindow
  (JNIEnv* env, jclass clazz, jlong pointer) {
    auto* win = reinterpret_cast<GLFWwindow*>(pointer);
    glfwDestroyWindow(win);
}

/*
 * Class:     de_linusdev_clgl_nat_glfw3_GLFWWindow
 * Method:    _glfwShowWindow
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_de_linusdev_clgl_nat_glfw3_GLFW__1glfwShowWindow
  (JNIEnv* env, jclass clazz, jlong pointer) {
    auto* win = reinterpret_cast<GLFWwindow*>(pointer);
    glfwShowWindow(win);
}

/*
 * Class:     de_linusdev_clgl_nat_glfw3_GLFWWindow
 * Method:    _glfwSwapBuffers
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_de_linusdev_clgl_nat_glfw3_GLFW__1glfwSwapBuffers
  (JNIEnv* env, jclass clazz, jlong pointer) {
    auto* win = reinterpret_cast<GLFWwindow*>(pointer);
    glfwSwapBuffers(win);
}

/*
 * Class:     de_linusdev_clgl_nat_glfw3_GLFWWindow
 * Method:    _glfwSetWindowUserPointer
 * Signature: (JJ)V
 */
JNIEXPORT void JNICALL Java_de_linusdev_clgl_nat_glfw3_GLFW__1glfwSetWindowUserPointer(
        JNIEnv* env, jclass clazz,
        jlong pointer,
        jlong userPointer
) {
    auto* win = reinterpret_cast<GLFWwindow*>(pointer);
    glfwSetWindowUserPointer(
            win,
            reinterpret_cast<void*>(userPointer)
    );
}

/*
 * Class:     de_linusdev_clgl_nat_glfw3_GLFWWindow
 * Method:    _glfwSetInputMode
 * Signature: (JII)V
 */
JNIEXPORT void JNICALL Java_de_linusdev_clgl_nat_glfw3_GLFW__1glfwSetInputMode
        (JNIEnv* env, jclass clazz, jlong pointer, jint mode, jint value) {
    auto* win = reinterpret_cast<GLFWwindow*>(pointer);
    glfwSetInputMode(win, mode, value);
}

/*
 * Class:     de_linusdev_clgl_nat_glfw3_GLFWWindow
 * Method:    _glfwSetWindowSize
 * Signature: (JII)V
 */
JNIEXPORT void JNICALL Java_de_linusdev_clgl_nat_glfw3_GLFW__1glfwSetWindowSize
        (JNIEnv* env, jclass clazz, jlong pointer, jint width, jint height) {
    auto* win = reinterpret_cast<GLFWwindow*>(pointer);
    glfwSetWindowSize(win, width, height);
}

/*
 * Class:     de_linusdev_clgl_nat_glfw3_GLFWWindow
 * Method:    _glfwSetWindowTitle
 * Signature: (JLjava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_de_linusdev_clgl_nat_glfw3_GLFW__1glfwSetWindowTitle
        (JNIEnv* env, jclass clazz, jlong pointer, jstring title) {
    auto* win = reinterpret_cast<GLFWwindow*>(pointer);

    const char* cTitle = env->GetStringUTFChars(title, nullptr);

    glfwSetWindowTitle(win, cTitle);
}

/*
 * Class:     de_linusdev_clgl_nat_glfw3_GLFWWindow
 * Method:    _glfwSetWindowAttrib
 * Signature: (JII)V
 */
JNIEXPORT void JNICALL Java_de_linusdev_clgl_nat_glfw3_GLFW__1glfwSetWindowAttrib
        (JNIEnv* env, jclass clazz, jlong pointer, jint attr, jint value) {
    auto* win = reinterpret_cast<GLFWwindow*>(pointer);
    glfwSetWindowAttrib(win, attr, value);
}

/*
 * Class:     de_linusdev_clgl_nat_glfw3_GLFWWindow
 * Method:    _glfwGetWindowUserPointer
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_de_linusdev_clgl_nat_glfw3_GLFW__1glfwGetWindowUserPointer
        (JNIEnv* env, jclass clazz, jlong pointer) {
    auto* win = reinterpret_cast<GLFWwindow*>(pointer);
    return reinterpret_cast<jlong>(glfwGetWindowUserPointer(win));
}

/*
 * Class:     de_linusdev_clgl_nat_glfw3_GLFWWindow
 * Method:    _glfwWindowShouldClose
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_de_linusdev_clgl_nat_glfw3_GLFW__1glfwWindowShouldClose
        (JNIEnv* env, jclass clazz, jlong pointer) {
    auto* win = reinterpret_cast<GLFWwindow*>(pointer);
    return glfwWindowShouldClose(win);
}

JNIEXPORT void JNICALL Java_de_linusdev_clgl_nat_glfw3_GLFW__1glfwGetFramebufferSize
        (JNIEnv* env, jclass clazz, jlong pointer, jobject p_width_height) {
    int* width_height = reinterpret_cast<int*>(env->GetDirectBufferAddress(p_width_height));
    auto* win = reinterpret_cast<GLFWwindow*>(pointer);

    glfwGetFramebufferSize(win, width_height, &(width_height[1]));
}

