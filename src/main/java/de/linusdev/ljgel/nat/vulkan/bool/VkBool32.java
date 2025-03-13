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

package de.linusdev.ljgel.nat.vulkan.bool;

import de.linusdev.ljgel.nat.vulkan.utils.VulkanUtils;
import de.linusdev.lutils.math.vector.buffer.intn.BBUInt1;

public class VkBool32 extends BBUInt1 {


    public VkBool32() {
        super(false, null);
    }

    public void set(boolean bool) {
        set(VulkanUtils.booleanToVkBool32(bool));
    }

    public boolean getAsBool() {
        return VulkanUtils.vkBool32ToBoolean(get());
    }

}