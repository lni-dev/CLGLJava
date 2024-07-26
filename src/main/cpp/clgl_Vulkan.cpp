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
// Created by Linus on 26.04.2024.
//

#include "clgl_Vulkan.h"

#include <vulkan/vulkan.h>
#include "JniUtils.h"



typedef VkResult (VKAPI_PTR *PFN_callVulkanFunction1)(void* pCreateInfo, void* pAllocator, void* pInstance);

JNIEXPORT jint JNICALL Java_de_linusdev_clgl_vulkan_Vulkan_callVulkanFunction1(
        JNIEnv *, jclass, jlong p_function, jlong p_1, jlong p_2, jlong p_3
) {
    auto fun = reinterpret_cast<PFN_callVulkanFunction1>(p_function);
    VkResult ret = fun(
            reinterpret_cast<void*>(p_1),
            reinterpret_cast<void*>(p_2),
            reinterpret_cast<void*>(p_3)
    );

    return ret;
}

