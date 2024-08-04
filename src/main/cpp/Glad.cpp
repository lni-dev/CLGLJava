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

#include "de_linusdev_cvg4j_nat_glad_Glad.h"
#include "glad/gl.h"
#include "GLFW/glfw3.h"
#include "JniUtils.h"

/*
 * Class:     de_linusdev_clgl_nat_glad_Glad
 * Method:    gladLoadGL
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_de_linusdev_cvg4j_nat_glad_Glad__1gladLoadGL
  (JNIEnv* env, jclass clazz) {
    return gladLoadGL((GLADloadfunc)glfwGetProcAddress);
}

/*
 * Class:     de_linusdev_clgl_nat_glad_Glad
 * Method:    glClear
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_de_linusdev_cvg4j_nat_glad_Glad_glClear
  (JNIEnv* env, jclass clazz, jint mask) {
    glClear(mask);
}

/*
 * Class:     de_linusdev_clgl_nat_glad_Glad
 * Method:    glClearColor
 * Signature: (FFFF)V
 */
JNIEXPORT void JNICALL Java_de_linusdev_cvg4j_nat_glad_Glad_glClearColor
        (JNIEnv* env, jclass clazz, jfloat r, jfloat g, jfloat b, jfloat a) {
    glClearColor(r, g, b, a);
}

/*
 * Class:     de_linusdev_clgl_nat_glad_Glad
 * Method:    glFinish
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_de_linusdev_cvg4j_nat_glad_Glad_glFinish
  (JNIEnv* env, jclass clazz) {
    glFinish();
}

/*
 * Class:     de_linusdev_clgl_nat_glad_Glad
 * Method:    glBindFramebuffer
 * Signature: (II)V
 */
JNIEXPORT void JNICALL Java_de_linusdev_cvg4j_nat_glad_Glad_glBindFramebuffer
        (JNIEnv* env, jclass clazz, jint target, jint framebuffer) {
    glBindFramebuffer(target, framebuffer);
}

/*
 * Class:     de_linusdev_clgl_nat_glad_Glad
 * Method:    _glGenFramebuffers
 * Signature: (ILjava/nio/ByteBuffer;)V
 */
JNIEXPORT void JNICALL Java_de_linusdev_cvg4j_nat_glad_Glad__1glGenFramebuffers
        (JNIEnv* env, jclass clazz, jint n, jobject p_ids) {
    void* ids = env->GetDirectBufferAddress(p_ids);
    glGenFramebuffers(n, reinterpret_cast<GLuint*>(ids));
}

/*
 * Class:     de_linusdev_clgl_nat_glad_Glad
 * Method:    glGenFramebuffer
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_de_linusdev_cvg4j_nat_glad_Glad_glGenFramebuffer
        (JNIEnv* env, jclass clazz) {
    jint f;
    glGenFramebuffers(1, reinterpret_cast<GLuint*>(&f));
    DEBUG_MSG("glGenFramebuffer: generated: %u", *((GLuint*)(&f)))
    return f;
}

/*
 * Class:     de_linusdev_clgl_nat_glad_Glad
 * Method:    glCreateFramebuffer
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_de_linusdev_cvg4j_nat_glad_Glad_glCreateFramebuffer
        (JNIEnv *, jclass) {
    jint f;
    glCreateFramebuffers(1, reinterpret_cast<GLuint*>(&f));
    DEBUG_MSG("glCreateFramebuffers: generated: %u", *((GLuint*)(&f)))
    return f;
}

/*
 * Class:     de_linusdev_clgl_nat_glad_Glad
 * Method:    _glDeleteFramebuffers
 * Signature: (ILjava/nio/ByteBuffer;)V
 */
JNIEXPORT void JNICALL Java_de_linusdev_cvg4j_nat_glad_Glad__1glDeleteFramebuffers
        (JNIEnv* env, jclass clazz, jint n, jobject p_framebuffers) {
    void* framebuffers = env->GetDirectBufferAddress(p_framebuffers);
    glDeleteFramebuffers(n, reinterpret_cast<const GLuint*>(framebuffers));
}

/*
 * Class:     de_linusdev_clgl_nat_glad_Glad
 * Method:    glDeleteFramebuffer
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_de_linusdev_cvg4j_nat_glad_Glad_glDeleteFramebuffer
        (JNIEnv* env, jclass clazz, jint framebuffer) {
    glDeleteFramebuffers(1, reinterpret_cast<GLuint*>(&framebuffer));
}

/*
 * Class:     de_linusdev_clgl_nat_glad_Glad
 * Method:    _glNamedFramebufferRenderbuffer
 * Signature: (IIII)V
 */
JNIEXPORT void JNICALL Java_de_linusdev_cvg4j_nat_glad_Glad_glNamedFramebufferRenderbuffer
        (JNIEnv* env, jclass clazz, jint framebuffer, jint attachment, jint renderbuffertarget, jint renderbuffer) {
    glNamedFramebufferRenderbuffer(framebuffer, attachment, renderbuffertarget, renderbuffer);
    DEBUG_MSG("glNamedFramebufferRenderbuffer: framebuffer: %u, renderbuffer: %u", *((GLuint*)(&framebuffer)), *((GLuint*)(&renderbuffer)))
}

/*
 * Class:     de_linusdev_clgl_nat_glad_Glad
 * Method:    _glGenRenderbuffers
 * Signature: (ILjava/nio/ByteBuffer;)V
 */
JNIEXPORT void JNICALL Java_de_linusdev_cvg4j_nat_glad_Glad__1glGenRenderbuffers
        (JNIEnv* env, jclass clazz, jint n, jobject p_renderbuffers) {
    void* renderbuffers = env->GetDirectBufferAddress(p_renderbuffers);
    glGenRenderbuffers(n, reinterpret_cast<GLuint*>(renderbuffers));
}

/*
 * Class:     de_linusdev_clgl_nat_glad_Glad
 * Method:    glGenRenderbuffer
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_de_linusdev_cvg4j_nat_glad_Glad_glGenRenderbuffer
        (JNIEnv *, jclass) {
    jint r;
    glGenRenderbuffers(1, reinterpret_cast<GLuint*>(&r));
    DEBUG_MSG("glGenRenderbuffer: generated: %u", *((GLuint*)(&r)))
    return r;
}

/*
 * Class:     de_linusdev_clgl_nat_glad_Glad
 * Method:    glCreateRenderbuffer
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_de_linusdev_cvg4j_nat_glad_Glad_glCreateRenderbuffer
        (JNIEnv *, jclass) {
    jint r;
    glCreateRenderbuffers(1, reinterpret_cast<GLuint*>(&r));
    DEBUG_MSG("glCreateRenderbuffers: generated: %u", *((GLuint*)(&r)))
    return r;
}

/*
 * Class:     de_linusdev_clgl_nat_glad_Glad
 * Method:    _glDeleteRenderbuffers
 * Signature: (ILjava/nio/ByteBuffer;)V
 */
JNIEXPORT void JNICALL Java_de_linusdev_cvg4j_nat_glad_Glad__1glDeleteRenderbuffers
        (JNIEnv* env, jclass clazz, jint n, jobject p_renderbuffers) {
    void* renderbuffers = env->GetDirectBufferAddress(p_renderbuffers);
    glDeleteRenderbuffers(n, reinterpret_cast<GLuint*>(renderbuffers));
}

/*
 * Class:     de_linusdev_clgl_nat_glad_Glad
 * Method:    glDeleteRenderbuffer
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_de_linusdev_cvg4j_nat_glad_Glad_glDeleteRenderbuffer
        (JNIEnv* env, jclass clazz, jint renderbuffer) {
    glDeleteRenderbuffers(1, reinterpret_cast<GLuint*>(&renderbuffer));
}

/*
 * Class:     de_linusdev_clgl_nat_glad_Glad
 * Method:    _glNamedRenderbufferStorage
 * Signature: (IIII)V
 */
JNIEXPORT void JNICALL Java_de_linusdev_cvg4j_nat_glad_Glad_glNamedRenderbufferStorage
        (JNIEnv* env, jclass clazz, jint renderbuffer, jint internalformat, jint width, jint height) {
    DEBUG_MSG("glNamedRenderbufferStorage: renderbuffer: %u", *((GLuint*)(&renderbuffer)))
    glNamedRenderbufferStorage(renderbuffer, internalformat, width, height);
}

/*
 * Class:     de_linusdev_clgl_nat_glad_Glad
 * Method:    glBlitNamedFramebuffer
 * Signature: (IIIIIIIIIIII)V
 */
JNIEXPORT void JNICALL Java_de_linusdev_cvg4j_nat_glad_Glad_glBlitNamedFramebuffer(JNIEnv* env, jclass clazz,
         jint readFramebuffer,
         jint drawFramebuffer,
         jint srcX0, jint srcY0,
         jint srcX1, jint srcY1,
         jint dstX0, jint dstY0,
         jint dstX1, jint dstY1,
         jint mask, jint filter
) {
    glBlitNamedFramebuffer(
            readFramebuffer,
            drawFramebuffer,
            srcX0, srcY0,
            srcX1, srcY1,
            dstX0, dstY0,
            dstX1, dstY1,
            mask, filter
    );
}

/*
 * Class:     de_linusdev_clgl_nat_glad_Glad
 * Method:    glNamedFramebufferReadBuffer
 * Signature: (II)V
 */
JNIEXPORT void JNICALL Java_de_linusdev_cvg4j_nat_glad_Glad_glNamedFramebufferReadBuffer
        (JNIEnv* env, jclass clazz, jint framebuffer, jint mode) {
    glNamedFramebufferReadBuffer(framebuffer, mode);
}

/*
 * Class:     de_linusdev_clgl_nat_glad_Glad
 * Method:    glNamedFramebufferDrawBuffer
 * Signature: (II)V
 */
JNIEXPORT void JNICALL Java_de_linusdev_cvg4j_nat_glad_Glad_glNamedFramebufferDrawBuffer
        (JNIEnv* env, jclass clazz, jint framebuffer, jint buf) {
    glNamedFramebufferDrawBuffer(framebuffer, buf);
}

/*
 * Class:     de_linusdev_clgl_nat_glad_Glad
 * Method:    glGetString
 * Signature: (I)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_de_linusdev_cvg4j_nat_glad_Glad_glGetString
        (JNIEnv* env, jclass clazz, jint name) {
    const GLubyte* str = glGetString(name);

    jstring jStr = env->NewStringUTF(reinterpret_cast<const char*>(str));
    return jStr;
}

/*
 * Class:     de_linusdev_clgl_nat_glad_Glad
 * Method:    glEnable
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_de_linusdev_cvg4j_nat_glad_Glad_glEnable
        (JNIEnv* env, jclass clazz, jint cap) {
    glEnable(cap);
}

/*
 * Class:     de_linusdev_clgl_nat_glad_Glad
 * Method:    glDisable
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_de_linusdev_cvg4j_nat_glad_Glad_glDisable
        (JNIEnv* env, jclass clazz, jint cap) {
    glDisable(cap);
}


static jobject globalRefDebugMessageCallback = nullptr;
static jmethodID messageMethodId = nullptr;

/*
 * Class:     de_linusdev_clgl_nat_glad_Glad
 * Method:    glDebugMessageCallback
 * Signature: (Lde/linusdev/clgl/nat/glad/custom/DebugMessageCallback;J)V
 */
JNIEXPORT void JNICALL Java_de_linusdev_cvg4j_nat_glad_Glad_glDebugMessageCallback
        (JNIEnv* env, jclass clazz, jobject callback, jlong userParam) {
    if(globalRefDebugMessageCallback)
        env->DeleteGlobalRef(globalRefDebugMessageCallback);

    globalRefDebugMessageCallback = env->NewGlobalRef(callback);
    messageMethodId = env->GetMethodID(env->GetObjectClass(callback), "message", "(IIIILjava/nio/ByteBuffer;J)V");

    glDebugMessageControl(GL_DONT_CARE, GL_DONT_CARE, GL_DONT_CARE, 0, nullptr, GL_TRUE);
    glDebugMessageCallback(
            [](GLenum source, GLenum type, GLuint id, GLenum severity, GLsizei length, const GLchar* message, const void* userParam) {
                JNIEnv* env;
                JNI_UTILS->getEnv(&env);
                DEBUG_MSG("glDebugMessageCallback - 1: JNIEnv* env: %p", env);
                jobject msgBuffer = env->NewDirectByteBuffer(reinterpret_cast<void*>(const_cast<GLchar*>(message)), length);
                DEBUG_MSG("glDebugMessageCallback - 2");
                env->CallVoidMethod(
                        globalRefDebugMessageCallback,
                        messageMethodId,
                        source,
                        type,
                        id,
                        severity,
                        msgBuffer,
                        userParam
                );

                env->DeleteLocalRef(msgBuffer);
            },
            reinterpret_cast<const void*>(userParam)
    );
}


JNIEXPORT void JNICALL Java_de_linusdev_cvg4j_nat_glad_Glad__1glGenVertexArrays(
        JNIEnv* env, jclass clazz, jint n, jlong p_arrays
) {
    glGenVertexArrays(n, reinterpret_cast<GLuint*>(p_arrays));
}


JNIEXPORT void JNICALL Java_de_linusdev_cvg4j_nat_glad_Glad__1glDeleteVertexArrays(
        JNIEnv* env, jclass clazz, jint n, jlong p_arrays
) {
    glDeleteVertexArrays(n, reinterpret_cast<const GLuint*>(p_arrays));
}

JNIEXPORT void JNICALL Java_de_linusdev_cvg4j_nat_glad_Glad_glBindVertexArray(
        JNIEnv* env, jclass clazz, jint id
) {
    glBindVertexArray(id);
}



/*
 * Class:     de_linusdev_clgl_nat_glad_Glad
 * Method:    _glGenBuffers
 * Signature: (IJ)V
 */
JNIEXPORT void JNICALL Java_de_linusdev_cvg4j_nat_glad_Glad__1glGenBuffers(
        JNIEnv* env, jclass clazz, jint n, jlong p_buffers
) {
    glGenBuffers(n, reinterpret_cast<GLuint*>(p_buffers));
}

/*
 * Class:     de_linusdev_clgl_nat_glad_Glad
 * Method:    _glDeleteBuffers
 * Signature: (IJ)V
 */
JNIEXPORT void JNICALL Java_de_linusdev_cvg4j_nat_glad_Glad__1glDeleteBuffers(
        JNIEnv* env, jclass clazz, jint n, jlong p_buffers
) {
    glDeleteBuffers(n, reinterpret_cast<const GLuint*>(p_buffers));
}


