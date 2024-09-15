// Copyright (c) 2024 Linus Andera
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
// Created by Linus on 14.09.2024.
//

#include "de_linusdev_cvg4j_nat_vulkan_VulkanNatDebugUtilsMessageCallback.h"
#include "JniUtils.h"
#include <vulkan/vulkan.h>

static jclass javaCallbackClazz = nullptr;
static jmethodID javaCallbackMethodId = nullptr;

static VKAPI_ATTR VkBool32 VKAPI_CALL debugCallback(
        VkDebugUtilsMessageSeverityFlagBitsEXT messageSeverity,
        VkDebugUtilsMessageTypeFlagsEXT messageType,
        const VkDebugUtilsMessengerCallbackDataEXT* pCallbackData,
        void* pUserData
) {

    JNIEnv* env;
    JNI_UTILS->getEnv(&env);

    return env->CallStaticBooleanMethod(javaCallbackClazz, javaCallbackMethodId,
                                        (jint)(messageSeverity),
                                        (jint)(messageType),
                                        reinterpret_cast<jlong>(pCallbackData),
                                        reinterpret_cast<jlong>(pUserData)
    );

}

JNIEXPORT void JNICALL Java_de_linusdev_cvg4j_nat_vulkan_VulkanNatDebugUtilsMessageCallback_setCallbackClass(
        JNIEnv* env, jclass, jclass callbackClazz
) {
    javaCallbackClazz = (jclass)(env->NewGlobalRef(callbackClazz));
    javaCallbackMethodId = env->GetStaticMethodID(javaCallbackClazz, "callback", "(IIJJ)Z");
}

JNIEXPORT jlong JNICALL Java_de_linusdev_cvg4j_nat_vulkan_VulkanNatDebugUtilsMessageCallback_getVulkanDebugCallbackFunPointer(
        JNIEnv *, jclass
) {
    return reinterpret_cast<jlong>(debugCallback);
}