
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

#include "clgl_Load.h"

#include "JniUtils.h"



/*
 * Class:     de_linusdev_clgl_nat_Load
 * Method:    _test
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_de_linusdev_clgl_nat_Load__1init
    (JNIEnv* env, jclass clazz)
{
    if(JNI_UTILS != nullptr) return;

    JNI_UTILS = new JniUtils(env);
    printMethodId = env->GetStaticMethodID(clazz, "printInJava", "(Ljava/lang/String;)V");
    loadClass = clazz;
}

JNIEXPORT void JNICALL Java_de_linusdev_clgl_nat_Load__1close
        (JNIEnv* env, jclass clazz)
{
    //This causes problems, when some threads are still working while another already called the close method
    //delete JNI_UTILS;
    //JNI_UTILS = nullptr;
}



