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

package de.linusdev.cvg4j.engine.vk.memory.allocator.buffer;

import de.linusdev.cvg4j.engine.exception.EngineException;
import de.linusdev.cvg4j.engine.vk.device.Device;
import de.linusdev.cvg4j.engine.vk.memory.allocator.VulkanMemoryBoundObject;
import de.linusdev.cvg4j.nat.vulkan.bitmasks.enums.VkBufferUsageFlagBits;
import de.linusdev.cvg4j.nat.vulkan.bitmasks.enums.VkMemoryPropertyFlagBits;
import de.linusdev.cvg4j.nat.vulkan.enums.VkSharingMode;
import de.linusdev.cvg4j.nat.vulkan.enums.VkStructureType;
import de.linusdev.cvg4j.nat.vulkan.handles.VkBuffer;
import de.linusdev.cvg4j.nat.vulkan.handles.VkDeviceMemory;
import de.linusdev.cvg4j.nat.vulkan.handles.VkInstance;
import de.linusdev.cvg4j.nat.vulkan.structs.VkBufferCreateInfo;
import de.linusdev.cvg4j.nat.vulkan.structs.VkMemoryRequirements;
import de.linusdev.lutils.bitfield.IntBitfield;
import de.linusdev.lutils.nat.memory.Stack;
import org.jetbrains.annotations.NotNull;

import static de.linusdev.lutils.nat.pointer.TypedPointer64.ref;
import static de.linusdev.lutils.nat.struct.abstracts.Structure.allocate;

public class VulkanBuffer extends VulkanMemoryBoundObject {

    public static void create(
            @NotNull Stack stack,
            @NotNull VkInstance vkInstance,
            @NotNull Device device,
            @NotNull VulkanBuffer vulkanBuffer,
            @NotNull IntBitfield<VkBufferUsageFlagBits> usage,
            @NotNull IntBitfield<VkMemoryPropertyFlagBits> memFlags
    ) throws EngineException {
        VkBufferCreateInfo bufferCreateInfo = stack.push(new VkBufferCreateInfo());
        bufferCreateInfo.sType.set(VkStructureType.BUFFER_CREATE_INFO);
        bufferCreateInfo.size.set(vulkanBuffer.getSize());
        bufferCreateInfo.usage.replaceWith(usage);
        bufferCreateInfo.sharingMode.set(VkSharingMode.EXCLUSIVE);

        vkInstance.vkCreateBuffer(device.getVkDevice(), ref(bufferCreateInfo), ref(null), ref(vulkanBuffer.vkBuffer)).check();

        stack.pop(); // bufferCreateInfo

        VkMemoryRequirements memoryRequirements = stack.push(new VkMemoryRequirements());
        vkInstance.vkGetBufferMemoryRequirements(device.getVkDevice(), vulkanBuffer.vkBuffer, ref(memoryRequirements));
        int memoryTypeIndex = device.findMemoryType(stack, memoryRequirements.memoryTypeBits.get(), memFlags);
        vulkanBuffer.memoryRequirements(memoryRequirements, memoryTypeIndex, memFlags);

        stack.pop(); // memoryRequirements
    }


    /*
     * Managed by this class
     */
    final @NotNull VkBuffer vkBuffer;


    public VulkanBuffer(@NotNull VkInstance vkInstance, @NotNull Device device, @NotNull String debugName, int size) {
        super(vkInstance, device, debugName, size);

        this.vkBuffer = allocate(new VkBuffer());
    }

    @Override
    protected void bind(@NotNull Stack stack, @NotNull VkDeviceMemory vkDeviceMemory) {
        vkInstance.vkBindBufferMemory(device.getVkDevice(), vkBuffer, vkDeviceMemory, offset);
    }

    public @NotNull VkBuffer getVkBuffer() {
        return vkBuffer;
    }


    @Override
    public void close() {
        super.close();
        vkInstance.vkDestroyBuffer(device.getVkDevice(), vkBuffer, ref(null));
    }
}
