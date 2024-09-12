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

package de.linusdev.cvg4j.engine.vk.memory.manager.objects.buffer;

import de.linusdev.cvg4j.engine.exception.EngineException;
import de.linusdev.cvg4j.engine.vk.device.Device;
import de.linusdev.cvg4j.engine.vk.memory.manager.VulkanMemoryBoundObject;
import de.linusdev.cvg4j.nat.vulkan.bitmasks.enums.VkBufferUsageFlagBits;
import de.linusdev.cvg4j.nat.vulkan.bitmasks.enums.VkMemoryPropertyFlagBits;
import de.linusdev.cvg4j.nat.vulkan.enums.VkSharingMode;
import de.linusdev.cvg4j.nat.vulkan.enums.VkStructureType;
import de.linusdev.cvg4j.nat.vulkan.handles.VkBuffer;
import de.linusdev.cvg4j.nat.vulkan.handles.VkDeviceMemory;
import de.linusdev.cvg4j.nat.vulkan.structs.VkBufferCreateInfo;
import de.linusdev.cvg4j.nat.vulkan.structs.VkMemoryRequirements;
import de.linusdev.lutils.bitfield.IntBitfield;
import de.linusdev.lutils.nat.memory.stack.Stack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import static de.linusdev.lutils.nat.pointer.TypedPointer64.ref;
import static de.linusdev.lutils.nat.struct.abstracts.Structure.allocate;

public class VulkanBuffer extends VulkanMemoryBoundObject {

    /*
     * Information stored in this class
     */
    protected final @NotNull IntBitfield<VkBufferUsageFlagBits> usage;

    /*
     * Managed by this class
     */
    final @NotNull VkBuffer vkBuffer;


    public VulkanBuffer(
            @NotNull Device device, @NotNull String debugName, int size,
            @NotNull IntBitfield<VkBufferUsageFlagBits> usage
    ) {
        super(device, debugName, size);
        this.usage = usage;

        this.vkBuffer = allocate(new VkBuffer());
    }

    @Contract("_ -> this")
    public @NotNull VulkanBuffer create(@NotNull Stack stack) {
        assert assertState(State.NOT_CREATED);
        recreate(stack);
        return this;
    }

    protected void recreate(@NotNull Stack stack) {
        close();
        state = State.RECREATED;

        VkBufferCreateInfo bufferCreateInfo = stack.push(new VkBufferCreateInfo());
        bufferCreateInfo.sType.set(VkStructureType.BUFFER_CREATE_INFO);
        bufferCreateInfo.size.set(size);
        bufferCreateInfo.usage.replaceWith(usage);
        bufferCreateInfo.sharingMode.set(VkSharingMode.EXCLUSIVE);

        vkInstance.vkCreateBuffer(device.getVkDevice(), ref(bufferCreateInfo), ref(null), ref(vkBuffer)).check();

        stack.pop(); // bufferCreateInfo

        if(memoryTypeManager != null)
            memoryTypeManager.onChanged(stack, this, null);
    }

    @Override
    protected void unbind(@NotNull Stack stack) {
        super.unbind(stack);
        close();
    }

    @Override
    protected void bind(@NotNull Stack stack, @NotNull VkDeviceMemory vkDeviceMemory) {
        super.bind(stack, vkDeviceMemory);
        vkInstance.vkBindBufferMemory(device.getVkDevice(), vkBuffer, vkDeviceMemory, offset);
    }

    @Override
    public int calculateMemoryTypeIndex(
            @NotNull Stack stack,
            @NotNull IntBitfield<VkMemoryPropertyFlagBits> memFlags
    ) throws EngineException {
        assert assertStatePast(State.RECREATED);

        VkMemoryRequirements memoryRequirements = stack.push(new VkMemoryRequirements());
        device.getVkInstance().vkGetBufferMemoryRequirements(device.getVkDevice(), vkBuffer, ref(memoryRequirements));
        int memoryTypeIndex = device.findMemoryType(stack, memoryRequirements.memoryTypeBits.get(), memFlags);
        memoryRequirements(memoryRequirements);

        stack.pop(); // memoryRequirements

        return memoryTypeIndex;
    }

    public @NotNull VkBuffer getVkBuffer() {
        return vkBuffer;
    }


    @Override
    public void close() {
        if(state.isPast(State.RECREATED))
            vkInstance.vkDestroyBuffer(device.getVkDevice(), vkBuffer, ref(null));
        super.close();
    }
}
