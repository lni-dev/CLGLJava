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

#include "clgl_Glad.h"
#include "glad/gl.h"
#include "GLFW/glfw3.h"

/*
 * Class:     de_linusdev_clgl_nat_glad_Glad
 * Method:    gladLoadGL
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_de_linusdev_clgl_nat_glad_Glad_gladLoadGL
  (JNIEnv* env, jclass clazz) {
    return gladLoadGL((GLADloadfunc)glfwGetProcAddress);
}

/*
 * Class:     de_linusdev_clgl_nat_glad_Glad
 * Method:    glClear
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_de_linusdev_clgl_nat_glad_Glad_glClear
  (JNIEnv* env, jclass clazz, jint mask) {
    glClear(mask);
}

/*
 * Class:     de_linusdev_clgl_nat_glad_Glad
 * Method:    glFinish
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_de_linusdev_clgl_nat_glad_Glad_glFinish
  (JNIEnv* env, jclass clazz) {
    glFinish();
}

