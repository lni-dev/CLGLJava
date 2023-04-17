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

#include "JniUtils.h"

JniUtils::JniUtils(JNIEnv* env) {
    env->GetJavaVM(&jvm);
}

JavaVM* JniUtils::getVM() {
    return jvm;
}

void JniUtils::getEnv(JNIEnv** pToEnvP) {
    if(jvm->GetEnv(reinterpret_cast<void**>(pToEnvP), JNI_VERSION_10) == JNI_EDETACHED) {
        JavaVMAttachArgs args;
        args.version = JNI_VERSION_10;
        args.name = nullptr;
        args.group = nullptr;
        jvm->AttachCurrentThread(reinterpret_cast<void**>(pToEnvP), &args);
    }
}
