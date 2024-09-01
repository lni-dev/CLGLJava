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

#include "de_linusdev_cvg4j_nat_glfw3_GLFW.h"
#include "JniUtils.h"

#define GLFW_INCLUDE_VULKAN
#include "GLFW/glfw3.h"

/*
 * Class:     de_linusdev_clgl_nat_glfw3_GLFWWindow
 * Method:    _glfwInit
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_de_linusdev_cvg4j_nat_glfw3_GLFW__1glfwInit
  (JNIEnv* env, jclass clazz) {
    return glfwInit();
}

/*
 * Class:     de_linusdev_clgl_nat_glfw3_GLFW
 * Method:    glfwSwapInterval
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_de_linusdev_cvg4j_nat_glfw3_GLFW_glfwSwapInterval(
        JNIEnv* env, jclass clazz,
        jint interval
) {
    glfwSwapInterval(interval);
}

/*
 * Class:     de_linusdev_clgl_nat_glfw3_GLFWWindow
 * Method:    _glfwWindowHint
 * Signature: (II)V
 */
JNIEXPORT void JNICALL Java_de_linusdev_cvg4j_nat_glfw3_GLFW__1glfwWindowHint
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
JNIEXPORT void JNICALL Java_de_linusdev_cvg4j_nat_glfw3_GLFW__1glfwSetErrorCallback
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
JNIEXPORT jlong JNICALL Java_de_linusdev_cvg4j_nat_glfw3_GLFW__1glfwCreateWindow
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
JNIEXPORT void JNICALL Java_de_linusdev_cvg4j_nat_glfw3_GLFW__1glfwTerminate
  (JNIEnv* env, jclass clazz) {
    glfwTerminate();
}

/*
 * Class:     de_linusdev_clgl_nat_glfw3_GLFWWindow
 * Method:    _glfwPollEvents
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_de_linusdev_cvg4j_nat_glfw3_GLFW__1glfwPollEvents
        (JNIEnv* env, jclass clazz) {
    glfwPollEvents();
}

/*
 * Class:     de_linusdev_clgl_nat_glfw3_GLFWWindow
 * Method:    _glfwMakeContextCurrent
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_de_linusdev_cvg4j_nat_glfw3_GLFW__1glfwMakeContextCurrent
  (JNIEnv* env, jclass clazz, jlong pointer) {
    auto* win = reinterpret_cast<GLFWwindow*>(pointer);
    glfwMakeContextCurrent(win);
}

/*
 * Class:     de_linusdev_clgl_nat_glfw3_GLFWWindow
 * Method:    _glfwDestroyWindow
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_de_linusdev_cvg4j_nat_glfw3_GLFW__1glfwDestroyWindow
  (JNIEnv* env, jclass clazz, jlong pointer) {
    auto* win = reinterpret_cast<GLFWwindow*>(pointer);
    glfwDestroyWindow(win);
}

/*
 * Class:     de_linusdev_clgl_nat_glfw3_GLFWWindow
 * Method:    _glfwShowWindow
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_de_linusdev_cvg4j_nat_glfw3_GLFW__1glfwShowWindow
  (JNIEnv* env, jclass clazz, jlong pointer) {
    auto* win = reinterpret_cast<GLFWwindow*>(pointer);
    glfwShowWindow(win);
}

/*
 * Class:     de_linusdev_clgl_nat_glfw3_GLFWWindow
 * Method:    _glfwSwapBuffers
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_de_linusdev_cvg4j_nat_glfw3_GLFW__1glfwSwapBuffers
  (JNIEnv* env, jclass clazz, jlong pointer) {
    auto* win = reinterpret_cast<GLFWwindow*>(pointer);
    glfwSwapBuffers(win);
}

/*
 * Class:     de_linusdev_clgl_nat_glfw3_GLFWWindow
 * Method:    _glfwSetWindowUserPointer
 * Signature: (JJ)V
 */
JNIEXPORT void JNICALL Java_de_linusdev_cvg4j_nat_glfw3_GLFW__1glfwSetWindowUserPointer(
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
JNIEXPORT void JNICALL Java_de_linusdev_cvg4j_nat_glfw3_GLFW__1glfwSetInputMode
        (JNIEnv* env, jclass clazz, jlong pointer, jint mode, jint value) {
    auto* win = reinterpret_cast<GLFWwindow*>(pointer);
    glfwSetInputMode(win, mode, value);
}

/*
 * Class:     de_linusdev_clgl_nat_glfw3_GLFWWindow
 * Method:    _glfwSetWindowSize
 * Signature: (JII)V
 */
JNIEXPORT void JNICALL Java_de_linusdev_cvg4j_nat_glfw3_GLFW__1glfwSetWindowSize
        (JNIEnv* env, jclass clazz, jlong pointer, jint width, jint height) {
    auto* win = reinterpret_cast<GLFWwindow*>(pointer);
    glfwSetWindowSize(win, width, height);
}

/*
 * Class:     de_linusdev_clgl_nat_glfw3_GLFWWindow
 * Method:    _glfwSetWindowTitle
 * Signature: (JLjava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_de_linusdev_cvg4j_nat_glfw3_GLFW__1glfwSetWindowTitle
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
JNIEXPORT void JNICALL Java_de_linusdev_cvg4j_nat_glfw3_GLFW__1glfwSetWindowAttrib
        (JNIEnv* env, jclass clazz, jlong pointer, jint attr, jint value) {
    auto* win = reinterpret_cast<GLFWwindow*>(pointer);
    glfwSetWindowAttrib(win, attr, value);
}

/*
 * Class:     de_linusdev_clgl_nat_glfw3_GLFWWindow
 * Method:    _glfwGetWindowUserPointer
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_de_linusdev_cvg4j_nat_glfw3_GLFW__1glfwGetWindowUserPointer
        (JNIEnv* env, jclass clazz, jlong pointer) {
    auto* win = reinterpret_cast<GLFWwindow*>(pointer);
    return reinterpret_cast<jlong>(glfwGetWindowUserPointer(win));
}

/*
 * Class:     de_linusdev_clgl_nat_glfw3_GLFWWindow
 * Method:    _glfwWindowShouldClose
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_de_linusdev_cvg4j_nat_glfw3_GLFW__1glfwWindowShouldClose
        (JNIEnv* env, jclass clazz, jlong pointer) {
    auto* win = reinterpret_cast<GLFWwindow*>(pointer);
    return glfwWindowShouldClose(win);
}

JNIEXPORT void JNICALL Java_de_linusdev_cvg4j_nat_glfw3_GLFW__1glfwGetFramebufferSize
        (JNIEnv* env, jclass clazz, jlong pointer, jobject p_width_height) {
    int* width_height = reinterpret_cast<int*>(env->GetDirectBufferAddress(p_width_height));
    auto* win = reinterpret_cast<GLFWwindow*>(pointer);

    glfwGetFramebufferSize(win, width_height, &(width_height[1]));
}

static jclass javaGLFWWindowClass = nullptr;

static jmethodID windowSizeCallbackMethodId = nullptr;
static jmethodID framebufferSizeCallbackMethodId = nullptr;
static jmethodID keyCallbackMethodId = nullptr;
static jmethodID charCallbackMethodId = nullptr;
static jmethodID cursorPosCallbackMethodId = nullptr;
static jmethodID cursorEnterCallbackMethodId = nullptr;
static jmethodID mouseButtonCallbackMethodId = nullptr;
static jmethodID scrollCallbackMethodId = nullptr;
static jmethodID joystickCallbackMethodId = nullptr;
static jmethodID dropCallbackMethodId = nullptr;
static jmethodID refreshCallbackMethodId = nullptr;
static jmethodID windowIconificationCallbackMethodId = nullptr;

/*
 * Class:     de_linusdev_clgl_nat_glfw3_GLFW
 * Method:    setJavaGLFWWindowClass
 * Signature: (Ljava/lang/Class;)V
 */
JNIEXPORT void JNICALL Java_de_linusdev_cvg4j_nat_glfw3_GLFW_setJavaGLFWWindowClass(
        JNIEnv* env, jclass clazz,
        jclass callbackClass
) {
    javaGLFWWindowClass = (jclass)(env->NewGlobalRef(callbackClass));
}

/*
 * Class:     de_linusdev_clgl_nat_glfw3_GLFW
 * Method:    glfwSetWindowSizeCallback
 * Signature: (JLjava/lang/Class;)V
 */
JNIEXPORT void JNICALL Java_de_linusdev_cvg4j_nat_glfw3_GLFW_glfwSetWindowSizeCallback(
        JNIEnv* env, jclass clazz,
        jlong p_window
) {
    auto* win = reinterpret_cast<GLFWwindow*>(p_window);
    windowSizeCallbackMethodId = env->GetStaticMethodID(javaGLFWWindowClass, "window_size_callback", "(JII)V");

    glfwSetWindowSizeCallback(win, [](GLFWwindow* pointer, int width, int height) {
        JNIEnv* env;
        JNI_UTILS->getEnv(&env);

        env->CallStaticVoidMethod(javaGLFWWindowClass, windowSizeCallbackMethodId,
                  reinterpret_cast<jlong>(pointer),
                  width, height
        );
    });
}

/*
 * Class:     de_linusdev_clgl_nat_glfw3_GLFW
 * Method:    glfwSetFramebufferSizeCallback
 * Signature: (JLjava/lang/Class;)V
 */
JNIEXPORT void JNICALL Java_de_linusdev_cvg4j_nat_glfw3_GLFW_glfwSetFramebufferSizeCallback(
        JNIEnv* env, jclass clazz,
        jlong p_window
) {
    auto* win = reinterpret_cast<GLFWwindow*>(p_window);
    framebufferSizeCallbackMethodId = env->GetStaticMethodID(javaGLFWWindowClass, "framebuffer_size_callback", "(JII)V");

    glfwSetFramebufferSizeCallback(win, [](GLFWwindow* pointer, int width, int height) {
        JNIEnv* env;
        JNI_UTILS->getEnv(&env);

        env->CallStaticVoidMethod(javaGLFWWindowClass, framebufferSizeCallbackMethodId,
                                  reinterpret_cast<jlong>(pointer),
                                  width, height
        );
    });
}

/*
 * Class:     de_linusdev_clgl_nat_glfw3_GLFW
 * Method:    glfwSetKeyCallback
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_de_linusdev_cvg4j_nat_glfw3_GLFW_glfwSetKeyCallback(
        JNIEnv* env, jclass clazz,
        jlong p_window
){
    auto* win = reinterpret_cast<GLFWwindow*>(p_window);
    keyCallbackMethodId = env->GetStaticMethodID(javaGLFWWindowClass, "key_callback", "(JIIII)V");

    glfwSetKeyCallback(win, [](GLFWwindow* pointer, int key, int scancode, int action, int mods) {
        JNIEnv* env;
        JNI_UTILS->getEnv(&env);

        env->CallStaticVoidMethod(javaGLFWWindowClass, keyCallbackMethodId,
                                  reinterpret_cast<jlong>(pointer),
                                  key, scancode, action, mods
        );
    });
}

/*
 * Class:     de_linusdev_clgl_nat_glfw3_GLFW
 * Method:    glfwSetCharCallback
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_de_linusdev_cvg4j_nat_glfw3_GLFW_glfwSetCharCallback(
        JNIEnv* env, jclass clazz,
        jlong p_window
){
    auto* win = reinterpret_cast<GLFWwindow*>(p_window);
    charCallbackMethodId = env->GetStaticMethodID(javaGLFWWindowClass, "character_callback", "(JI)V");

    glfwSetCharCallback(win, [](GLFWwindow* pointer, unsigned int codepoint) {
        JNIEnv* env;
        JNI_UTILS->getEnv(&env);

        env->CallStaticVoidMethod(javaGLFWWindowClass, charCallbackMethodId,
                                  reinterpret_cast<jlong>(pointer),
                                  codepoint
        );
    });
}

/*
 * Class:     de_linusdev_clgl_nat_glfw3_GLFW
 * Method:    glfwSetCursorPosCallback
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_de_linusdev_cvg4j_nat_glfw3_GLFW_glfwSetCursorPosCallback(
        JNIEnv* env, jclass clazz,
        jlong p_window
){
    auto* win = reinterpret_cast<GLFWwindow*>(p_window);
    cursorPosCallbackMethodId = env->GetStaticMethodID(javaGLFWWindowClass, "cursor_position_callback", "(JDD)V");

    glfwSetCursorPosCallback(win, [](GLFWwindow* pointer, double xpos, double ypos) {
        JNIEnv* env;
        JNI_UTILS->getEnv(&env);

        env->CallStaticVoidMethod(javaGLFWWindowClass, cursorPosCallbackMethodId,
                                  reinterpret_cast<jlong>(pointer),
                                  xpos, ypos
        );
    });
}

/*
 * Class:     de_linusdev_clgl_nat_glfw3_GLFW
 * Method:    glfwSetCursorEnterCallback
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_de_linusdev_cvg4j_nat_glfw3_GLFW_glfwSetCursorEnterCallback(
        JNIEnv* env, jclass clazz,
        jlong p_window
){
    auto* win = reinterpret_cast<GLFWwindow*>(p_window);
    cursorEnterCallbackMethodId = env->GetStaticMethodID(javaGLFWWindowClass, "cursor_enter_callback", "(JZ)V");

    glfwSetCursorEnterCallback(win, [](GLFWwindow* pointer, int entered) {
        JNIEnv* env;
        JNI_UTILS->getEnv(&env);

        env->CallStaticVoidMethod(javaGLFWWindowClass, cursorEnterCallbackMethodId,
                                  reinterpret_cast<jlong>(pointer),
                                  entered ? JNI_TRUE : JNI_FALSE
        );
    });
}

/*
 * Class:     de_linusdev_clgl_nat_glfw3_GLFW
 * Method:    glfwSetMouseButtonCallback
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_de_linusdev_cvg4j_nat_glfw3_GLFW_glfwSetMouseButtonCallback(
        JNIEnv* env, jclass clazz,
        jlong p_window
){
    auto* win = reinterpret_cast<GLFWwindow*>(p_window);
    mouseButtonCallbackMethodId = env->GetStaticMethodID(javaGLFWWindowClass, "mouse_button_callback", "(JIII)V");

    glfwSetMouseButtonCallback(win, [](GLFWwindow* pointer, int button, int action, int mods) {
        JNIEnv* env;
        JNI_UTILS->getEnv(&env);

        env->CallStaticVoidMethod(javaGLFWWindowClass, mouseButtonCallbackMethodId,
                                  reinterpret_cast<jlong>(pointer),
                                  button, action, mods
        );
    });
}

/*
 * Class:     de_linusdev_clgl_nat_glfw3_GLFW
 * Method:    glfwSetScrollCallback
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_de_linusdev_cvg4j_nat_glfw3_GLFW_glfwSetScrollCallback(
        JNIEnv* env, jclass clazz,
        jlong p_window
){
    auto* win = reinterpret_cast<GLFWwindow*>(p_window);
    scrollCallbackMethodId = env->GetStaticMethodID(javaGLFWWindowClass, "scroll_callback", "(JDD)V");

    glfwSetScrollCallback(win, [](GLFWwindow* pointer, double xoffset, double yoffset) {
        JNIEnv* env;
        JNI_UTILS->getEnv(&env);

        env->CallStaticVoidMethod(javaGLFWWindowClass, scrollCallbackMethodId,
                                  reinterpret_cast<jlong>(pointer),
                                  xoffset, yoffset
        );
    });
}

/*
 * Class:     de_linusdev_clgl_nat_glfw3_GLFW
 * Method:    glfwSetJoystickCallback
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_de_linusdev_cvg4j_nat_glfw3_GLFW_glfwSetJoystickCallback(
        JNIEnv* env, jclass clazz
){
    joystickCallbackMethodId = env->GetStaticMethodID(javaGLFWWindowClass, "joystick_callback", "(II)V");

    glfwSetJoystickCallback([](int jid, int event) {
        JNIEnv* env;
        JNI_UTILS->getEnv(&env);

        env->CallStaticVoidMethod(javaGLFWWindowClass, joystickCallbackMethodId,
                                  jid, event
        );
    });
}

/*
 * Class:     de_linusdev_clgl_nat_glfw3_GLFW
 * Method:    glfwSetDropCallback
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_de_linusdev_cvg4j_nat_glfw3_GLFW_glfwSetDropCallback(
        JNIEnv* env, jclass clazz,
        jlong p_window
){
    auto* win = reinterpret_cast<GLFWwindow*>(p_window);
    dropCallbackMethodId = env->GetStaticMethodID(javaGLFWWindowClass, "drop_callback", "(JILjava/nio/ByteBuffer;)V");

    glfwSetDropCallback(win, [](GLFWwindow* pointer, int count, const char** paths) {
        JNIEnv* env;
        JNI_UTILS->getEnv(&env);

        jobject pathsBuffer = env->NewDirectByteBuffer(paths, sizeof(char*) * count);

        env->CallStaticVoidMethod(javaGLFWWindowClass, dropCallbackMethodId,
                                  reinterpret_cast<jlong>(pointer),
                                  count,
                                  pathsBuffer
        );

        env->DeleteLocalRef(pathsBuffer);
    });
}

/*
 * Class:     de_linusdev_cvg4j_nat_glfw3_GLFW
 * Method:    glfwSetWindowRefreshCallback
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_de_linusdev_cvg4j_nat_glfw3_GLFW_glfwSetWindowRefreshCallback(
        JNIEnv* env, jclass clazz,
        jlong p_window
) {
    auto* win = reinterpret_cast<GLFWwindow*>(p_window);
    refreshCallbackMethodId = env->GetStaticMethodID(javaGLFWWindowClass, "window_refresh_callback", "(J)V");

    glfwSetWindowRefreshCallback(win, [](GLFWwindow* pointer) {
        JNIEnv* env;
        JNI_UTILS->getEnv(&env);

        env->CallStaticVoidMethod(javaGLFWWindowClass, refreshCallbackMethodId,
                                  reinterpret_cast<jlong>(pointer)
        );
    });
}

/*
 * Class:     de_linusdev_cvg4j_nat_glfw3_GLFW
 * Method:    glfwSetWindowIconifyCallback
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_de_linusdev_cvg4j_nat_glfw3_GLFW_glfwSetWindowIconifyCallback(
        JNIEnv* env, jclass clazz,
        jlong p_window
) {
    auto* win = reinterpret_cast<GLFWwindow*>(p_window);
    windowIconificationCallbackMethodId = env->GetStaticMethodID(javaGLFWWindowClass, "window_iconified", "(JZ)V");

    glfwSetWindowIconifyCallback(win, [](GLFWwindow* pointer, int iconified) {
        JNIEnv* env;
        JNI_UTILS->getEnv(&env);

        env->CallStaticVoidMethod(javaGLFWWindowClass, windowIconificationCallbackMethodId,
                                  reinterpret_cast<jlong>(pointer), iconified ? JNI_TRUE : JNI_FALSE
        );
    });
}

/*
 * Class:     de_linusdev_clgl_nat_glfw3_GLFW
 * Method:    _glfwGetKeyName
 * Signature: (II)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_de_linusdev_cvg4j_nat_glfw3_GLFW__1glfwGetKeyName(
        JNIEnv* env, jclass clazz,
        jint key,
        jint scancode
) {
    return env->NewStringUTF(glfwGetKeyName(key, scancode));
}

/*
 * Class:     de_linusdev_clgl_nat_glfw3_GLFW
 * Method:    glfwGetKeyScancode
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_de_linusdev_cvg4j_nat_glfw3_GLFW_glfwGetKeyScancode(
        JNIEnv* env, jclass clazz,
        jint key
) {
    return glfwGetKeyScancode(key);
}

JNIEXPORT jint JNICALL Java_de_linusdev_cvg4j_nat_glfw3_GLFW__1glfwGetError(
        JNIEnv* env, jclass clazz, jlong pointer
) {
    return glfwGetError(reinterpret_cast<const char**>(pointer));
}

JNIEXPORT void JNICALL Java_de_linusdev_cvg4j_nat_glfw3_GLFW_glfwDefaultWindowHints
        (JNIEnv *, jclass) {
    glfwDefaultWindowHints();
}

JNIEXPORT jint JNICALL Java_de_linusdev_cvg4j_nat_glfw3_GLFW_glfwVulkanSupported
        (JNIEnv *, jclass) {
    return glfwVulkanSupported();
}

JNIEXPORT jlong JNICALL Java_de_linusdev_cvg4j_nat_glfw3_GLFW_glfwGetInstanceProcAddress(
        JNIEnv* env, jclass clazz, jlong p_instance, jstring jprocname
) {
    const char* procname = env->GetStringUTFChars(jprocname, nullptr);

    GLFWvkproc ret = glfwGetInstanceProcAddress(
                    reinterpret_cast<VkInstance>(p_instance),
                    reinterpret_cast<const char*>(procname)
            );
    env->ReleaseStringUTFChars(jprocname, procname);

    return reinterpret_cast<jlong>(ret);
}

/*
 * Class:     de_linusdev_cvg4j_nat_glfw3_GLFW
 * Method:    glfwGetRequiredInstanceExtensions
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_de_linusdev_cvg4j_nat_glfw3_GLFW_glfwGetRequiredInstanceExtensions(
        JNIEnv * env, jclass clazz,
        jlong pCount
) {
    return (jlong) glfwGetRequiredInstanceExtensions(reinterpret_cast<uint32_t*>(pCount));
}

JNIEXPORT jint JNICALL Java_de_linusdev_cvg4j_nat_glfw3_GLFW_glfwCreateWindowSurface(
        JNIEnv *, jclass, jlong instance, jlong pWindow, jlong pAllocator, jlong pSurface
) {
    return glfwCreateWindowSurface(
            reinterpret_cast<VkInstance>(instance),
            reinterpret_cast<GLFWwindow*>(pWindow),
            reinterpret_cast<const VkAllocationCallbacks*>(pAllocator),
            reinterpret_cast<VkSurfaceKHR*>(pSurface)
    );
}

/*
 * Class:     de_linusdev_cvg4j_nat_glfw3_GLFW
 * Method:    glfwWaitEvents
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_de_linusdev_cvg4j_nat_glfw3_GLFW_glfwWaitEvents(JNIEnv *, jclass) {
    glfwWaitEvents();
}

/*
 * Class:     de_linusdev_cvg4j_nat_glfw3_GLFW
 * Method:    glfwSetWindowSizeLimits
 * Signature: (JIIII)V
 */
JNIEXPORT void JNICALL Java_de_linusdev_cvg4j_nat_glfw3_GLFW_glfwSetWindowSizeLimits(
        JNIEnv *, jclass,
        jlong pWindow, jint minWidth, jint minHeight, jint maxWidth, jint maxHeight
) {
    glfwSetWindowSizeLimits(reinterpret_cast<GLFWwindow*>(pWindow), minWidth, minHeight, maxWidth, maxHeight);
}

/*
 * Class:     de_linusdev_cvg4j_nat_glfw3_GLFW
 * Method:    glfwSetWindowAspectRatio
 * Signature: (JII)V
 */
JNIEXPORT void JNICALL Java_de_linusdev_cvg4j_nat_glfw3_GLFW_glfwSetWindowAspectRatio(
        JNIEnv *, jclass,
        jlong pWindow, jint numerator, jint denominator
) {
    glfwSetWindowAspectRatio(reinterpret_cast<GLFWwindow*>(pWindow), numerator, denominator);
}

