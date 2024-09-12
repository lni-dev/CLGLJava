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

import de.linusdev.cvg4j.engine.vk.memory.buffer.ArrayBuffer;
import de.linusdev.cvg4j.engine.vk.memory.buffer.BufferArrayInput;
import de.linusdev.cvg4j.engine.vk.memory.buffer.BufferOutput;
import de.linusdev.cvg4j.nat.vulkan.bitmasks.VkDependencyFlags;
import de.linusdev.cvg4j.nat.vulkan.bitmasks.VkPipelineStageFlags;
import de.linusdev.cvg4j.nat.vulkan.bitmasks.enums.VkAccessFlagBits;
import de.linusdev.cvg4j.nat.vulkan.bitmasks.enums.VkPipelineStageFlagBits;
import de.linusdev.cvg4j.nat.vulkan.constants.APIConstants;
import de.linusdev.cvg4j.nat.vulkan.enums.VkStructureType;
import de.linusdev.cvg4j.nat.vulkan.enums.VkVertexInputRate;
import de.linusdev.cvg4j.nat.vulkan.handles.VkCommandBuffer;
import de.linusdev.cvg4j.nat.vulkan.handles.VkInstance;
import de.linusdev.cvg4j.nat.vulkan.structs.VkBufferCopy;
import de.linusdev.cvg4j.nat.vulkan.structs.VkBufferMemoryBarrier;
import de.linusdev.cvg4j.nat.vulkan.structs.VkVertexInputAttributeDescription;
import de.linusdev.cvg4j.nat.vulkan.structs.VkVertexInputBindingDescription;
import de.linusdev.lutils.nat.memory.stack.Stack;
import de.linusdev.lutils.nat.struct.abstracts.Structure;
import de.linusdev.lutils.nat.struct.array.StructureArray;
import de.linusdev.lutils.nat.struct.array.StructureArraySupplier;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static de.linusdev.lutils.nat.pointer.TypedPointer64.ref;

public class VertexBuffer<V extends Structure> extends ArrayBuffer<V> {

    private final int binding;
    private final @NotNull VkVertexInputRate vertexInputRate;
    private final @NotNull List<VertexElement> attributeDescriptors;

    public VertexBuffer(
            @NotNull VkInstance vkInstance,
            @NotNull BufferArrayInput<V> input,
            @NotNull BufferOutput output,
            int binding,
            @NotNull VkVertexInputRate vertexInputRate,
            @NotNull List<VertexElement> attributeDescriptors
    ) {
        super(vkInstance, input, output);
        this.binding = binding;
        this.vertexInputRate = vertexInputRate;
        this.attributeDescriptors = attributeDescriptors;
    }

    @Override
    public void bufferCopyCommand(@NotNull Stack stack, @NotNull VkCommandBuffer vkCommandBuffer) {

        if(!input.getVulkanBuffer().isMapped()) return;

        VkPipelineStageFlags flags = stack.push(new VkPipelineStageFlags());
        flags.set(VkPipelineStageFlagBits.VERTEX_INPUT);
        VkPipelineStageFlags flags2 = stack.push(new VkPipelineStageFlags());
        flags2.set(VkPipelineStageFlagBits.TRANSFER);
        VkDependencyFlags flags3 = stack.push(new VkDependencyFlags());
        VkBufferMemoryBarrier vkBufferMemoryBarrier = stack.push(new VkBufferMemoryBarrier());
        vkBufferMemoryBarrier.sType.set(VkStructureType.BUFFER_MEMORY_BARRIER);
        vkBufferMemoryBarrier.srcAccessMask.set(VkAccessFlagBits.VERTEX_ATTRIBUTE_READ);
        vkBufferMemoryBarrier.dstAccessMask.set(VkAccessFlagBits.TRANSFER_WRITE);
        vkBufferMemoryBarrier.srcQueueFamilyIndex.set(APIConstants.VK_QUEUE_FAMILY_IGNORED);
        vkBufferMemoryBarrier.dstQueueFamilyIndex.set(APIConstants.VK_QUEUE_FAMILY_IGNORED);
        vkBufferMemoryBarrier.buffer.set(getVkBuffer());
        vkBufferMemoryBarrier.offset.set(0);
        vkBufferMemoryBarrier.size.set(APIConstants.VK_WHOLE_SIZE);
        vkInstance.vkCmdPipelineBarrier(vkCommandBuffer, flags, flags2, flags3, 0, ref(null), 1, ref(vkBufferMemoryBarrier), 0, ref(null));


        VkBufferCopy region = stack.push(new VkBufferCopy());
        region.size.set(input.getVulkanBuffer().getSize());

        vkInstance.vkCmdCopyBuffer(
                vkCommandBuffer,
                input.getVulkanBuffer().getVkBuffer(),
                output.getVulkanBuffer().getVkBuffer(),
                1,
                ref(region)
        );

        flags.set(VkPipelineStageFlagBits.TRANSFER);
        flags2.set(VkPipelineStageFlagBits.VERTEX_INPUT);
        vkBufferMemoryBarrier.sType.set(VkStructureType.BUFFER_MEMORY_BARRIER);
        vkBufferMemoryBarrier.srcAccessMask.set(VkAccessFlagBits.TRANSFER_WRITE);
        vkBufferMemoryBarrier.dstAccessMask.set(VkAccessFlagBits.VERTEX_ATTRIBUTE_READ);
        vkBufferMemoryBarrier.srcQueueFamilyIndex.set(APIConstants.VK_QUEUE_FAMILY_IGNORED);
        vkBufferMemoryBarrier.dstQueueFamilyIndex.set(APIConstants.VK_QUEUE_FAMILY_IGNORED);
        vkBufferMemoryBarrier.buffer.set(getVkBuffer());
        vkBufferMemoryBarrier.offset.set(0);
        vkBufferMemoryBarrier.size.set(APIConstants.VK_WHOLE_SIZE);
        vkInstance.vkCmdPipelineBarrier(vkCommandBuffer, flags, flags2, flags3, 0, ref(null), 1, ref(vkBufferMemoryBarrier), 0, ref(null));

        stack.pop();stack.pop();stack.pop();stack.pop(); // flags, flags2, flags3, vkBufferMemoryBarrier

        stack.pop();
    }

    public void createdDescriptor(@NotNull VkVertexInputBindingDescription description) {
        description.binding.set(binding);
        description.stride.set(input.getStride());
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
}
