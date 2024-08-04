/*
 * Copyright (c) 2024 Linus Andera
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.linusdev.cvg4j.nat.vulkan;

import de.linusdev.cvg4j.nat.NativeFunctions;
import de.linusdev.cvg4j.nat.NativeUtils;
import de.linusdev.cvg4j.nat.glfw3.GLFW;
import de.linusdev.cvg4j.nat.vulkan.constants.APIConstants;
import de.linusdev.cvg4j.nat.vulkan.handles.VkInstance;
import de.linusdev.cvg4j.nat.vulkan.structs.VkAllocationCallbacks;
import de.linusdev.cvg4j.nat.vulkan.structs.VkInstanceCreateInfo;
import org.jetbrains.annotations.Nullable;

public class VulkanUtils {

    public static int makeVersion(int major, int minor, int patch) {
        return (major << 22) | (minor << 12) | patch;
    }

    public static int makeApiVersion(int variant, int major, int minor, int patch) {
        return (variant << 29) | (major << 22) | (minor << 12) | patch;
    }

    public static boolean vkBool32ToBoolean(int value) {
        return value == APIConstants.VK_TRUE;
    }

    public static int booleanToVkBool32(boolean value) {
        return value ? APIConstants.VK_TRUE : APIConstants.VK_FALSE;
    }

    public static int VK_API_VERSION_1_0 = makeApiVersion(0, 1, 0, 0);
    public static int VK_API_VERSION_1_1 = makeApiVersion(0, 1, 1, 0);
    public static int VK_API_VERSION_1_2 = makeApiVersion(0, 1, 2, 0);
    public static int VK_API_VERSION_1_3 = makeApiVersion(0, 1, 3, 0);

    public static ReturnedVkResult vkCreateInstance(
            VkInstanceCreateInfo vkInstanceCreateInfo,
            @Nullable VkAllocationCallbacks pAllocator,
            VkInstance pInstance
    ) {
        long pointer = GLFW.glfwGetInstanceProcAddress(NativeUtils.getNullPointer(), "vkCreateInstance");

        int res = NativeFunctions.callNativeIFunctionPPP(
                pointer,
                vkInstanceCreateInfo.getPointer(),
                pAllocator == null ? NativeUtils.getNullPointer() : pAllocator.getPointer(),
                pInstance.getPointer()
        );

        return new ReturnedVkResult(res);
    }

}
