/*
 * Copyright (c) 2024-2025 Linus Andera
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

package de.linusdev.ljgel.nat.vulkan.utils;

import de.linusdev.ljgel.nat.vulkan.constants.APIConstants;

public class VulkanUtils {

    public static long VK_NULL_HANDLE = 0L;

    public static boolean vkBool32ToBoolean(int value) {
        return value == APIConstants.VK_TRUE;
    }

    public static int booleanToVkBool32(boolean value) {
        return value ? APIConstants.VK_TRUE : APIConstants.VK_FALSE;
    }





}
