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

//
// Created by Linus on 16/04/2023.
//

#ifndef CLGLJAVA_JNIUTILS_H
#define CLGLJAVA_JNIUTILS_H

#include <jni.h>

#define GET_BUF_ADDRESS_NULLABLE(OBJ) (OBJ == nullptr ? nullptr : env->GetDirectBufferAddress(OBJ))
#define DEBUG_MSG(...)
//#define DEBUG_MSG(...)  {char buff_uhiihoiol[5000];snprintf(buff_uhiihoiol, sizeof(buff_uhiihoiol), __VA_ARGS__);JNI_UTILS->printInJava(buff_uhiihoiol);}

class JniUtils {
private:
    JavaVM* jvm = nullptr;

public:


    explicit JniUtils(JNIEnv* env);

    JavaVM* getVM();
    void getEnv(JNIEnv** pToEnvP);

    void printInJava(const char* str);


};

extern JniUtils* JNI_UTILS;
extern jclass loadClass;
extern jmethodID printMethodId;




#endif //CLGLJAVA_JNIUTILS_H
