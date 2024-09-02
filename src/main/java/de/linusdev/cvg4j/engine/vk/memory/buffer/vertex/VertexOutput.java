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

package de.linusdev.cvg4j.engine.vk.memory.buffer.vertex;

import de.linusdev.cvg4j.engine.vk.memory.allocator.VulkanBuffer;
import de.linusdev.cvg4j.nat.vulkan.enums.VkVertexInputRate;
import de.linusdev.cvg4j.nat.vulkan.structs.VkVertexInputAttributeDescription;
import de.linusdev.cvg4j.nat.vulkan.structs.VkVertexInputBindingDescription;
import de.linusdev.lutils.nat.struct.array.StructureArray;
import de.linusdev.lutils.nat.struct.array.StructureArraySupplier;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class VertexOutput {

    private final @NotNull VulkanBuffer vulkanBuffer;

    private final int binding;
    private final int stride;
    private final @NotNull VkVertexInputRate vertexInputRate;
    private final @NotNull List<VertexElement> attributeDescriptors;

    public VertexOutput(
            @NotNull VulkanBuffer vulkanBuffer,
            int binding,
            int stride,
            @NotNull VkVertexInputRate vertexInputRate, @NotNull List<VertexElement> attributeDescriptors
    ) {
        this.vulkanBuffer = vulkanBuffer;
        this.stride = stride;
        this.binding = binding;
        this.vertexInputRate = vertexInputRate;
        this.attributeDescriptors = attributeDescriptors;
    }

    public void createdDescriptor(@NotNull VkVertexInputBindingDescription description) {
        description.binding.set(binding);
        description.stride.set(stride);
        description.inputRate.set(vertexInputRate);
    }

    public StructureArray<VkVertexInputAttributeDescription> createAttributeDescriptors(
            @NotNull StructureArraySupplier<VkVertexInputAttributeDescription> arraySupplier
    ) {
        StructureArray<VkVertexInputAttributeDescription> attributeDescriptions = arraySupplier.supply(
                attributeDescriptors.size(), VkVertexInputAttributeDescription.class, VkVertexInputAttributeDescription::new
        );

        int i = 0;
        for (VkVertexInputAttributeDescription attributeDescription : attributeDescriptions) {
            VertexElement element = attributeDescriptors.get(i++);
            attributeDescription.binding.set(binding);
            attributeDescription.location.set(element.location());
            attributeDescription.format.set(element.format());
            attributeDescription.offset.set(element.offset());
        }

        return attributeDescriptions;
    }

    public @NotNull VulkanBuffer getVulkanBuffer() {
        return vulkanBuffer;
    }
}
