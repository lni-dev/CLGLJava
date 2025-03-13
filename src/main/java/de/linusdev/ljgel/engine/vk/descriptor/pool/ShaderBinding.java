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

package de.linusdev.ljgel.engine.vk.descriptor.pool;

import de.linusdev.ljgel.nat.vulkan.enums.VkDescriptorType;
import de.linusdev.ljgel.nat.vulkan.handles.VkDescriptorSet;
import de.linusdev.ljgel.nat.vulkan.structs.VkDescriptorSetLayoutBinding;
import de.linusdev.ljgel.nat.vulkan.structs.VkWriteDescriptorSet;
import de.linusdev.lutils.nat.array.NativeArray;
import de.linusdev.lutils.nat.memory.stack.Stack;
import org.jetbrains.annotations.NotNull;

public interface ShaderBinding {

    @NotNull VkDescriptorType descriptorType();

    int descriptorCount();

    void createDescriptorSetBinding(@NotNull VkDescriptorSetLayoutBinding binding);

    void updateDescriptorSets(
            @NotNull Stack stack,
            @NotNull NativeArray<VkDescriptorSet> vkDescriptorSets,
            @NotNull NativeArray<VkWriteDescriptorSet> writes
    );

    void popUpdateDescriptorSets(@NotNull Stack stack, int count);

}
