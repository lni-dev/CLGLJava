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

package de.linusdev.cvg4j.engine.vk;

import de.linusdev.cvg4j.engine.vk.device.Device;
import de.linusdev.cvg4j.nat.vulkan.bitmasks.enums.VkCommandPoolCreateFlagBits;
import de.linusdev.cvg4j.nat.vulkan.enums.VkCommandBufferLevel;
import de.linusdev.cvg4j.nat.vulkan.enums.VkStructureType;
import de.linusdev.cvg4j.nat.vulkan.handles.VkCommandBuffer;
import de.linusdev.cvg4j.nat.vulkan.handles.VkCommandPool;
import de.linusdev.cvg4j.nat.vulkan.handles.VkDevice;
import de.linusdev.cvg4j.nat.vulkan.handles.VkInstance;
import de.linusdev.cvg4j.nat.vulkan.structs.VkCommandBufferAllocateInfo;
import de.linusdev.cvg4j.nat.vulkan.structs.VkCommandPoolCreateInfo;
import de.linusdev.lutils.nat.memory.Stack;
import de.linusdev.lutils.nat.struct.array.StructureArray;
import org.jetbrains.annotations.NotNull;

import static de.linusdev.lutils.nat.pointer.TypedPointer64.ofArray;
import static de.linusdev.lutils.nat.pointer.TypedPointer64.ref;
import static de.linusdev.lutils.nat.struct.abstracts.Structure.allocate;

public class CommandPool implements AutoCloseable {

    public static @NotNull CommandPool create(
            @NotNull Stack stack,
            @NotNull VkInstance vkInstance,
            @NotNull Device device,
            int commandBufferCount
    ) {
        CommandPool commandPool = new CommandPool(vkInstance, device.getVkDevice(), commandBufferCount);

        VkCommandPoolCreateInfo commandPoolCreateInfo = stack.push(new VkCommandPoolCreateInfo());
        commandPoolCreateInfo.sType.set(VkStructureType.COMMAND_POOL_CREATE_INFO);
        commandPoolCreateInfo.flags.set(VkCommandPoolCreateFlagBits.VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT);
        commandPoolCreateInfo.queueFamilyIndex.set(device.getGraphicsQueueIndex());

        vkInstance.vkCreateCommandPool(device.getVkDevice(), ref(commandPoolCreateInfo), ref(null), ref(commandPool.getVkCommandPool())).check();

        VkCommandBufferAllocateInfo commandBufferAllocateInfo = stack.push(new VkCommandBufferAllocateInfo());
        commandBufferAllocateInfo.sType.set(VkStructureType.COMMAND_BUFFER_ALLOCATE_INFO);
        commandBufferAllocateInfo.commandPool.set(commandPool.getVkCommandPool().get());
        commandBufferAllocateInfo.level.set(VkCommandBufferLevel.PRIMARY);
        commandBufferAllocateInfo.commandBufferCount.set(commandBufferCount);

        vkInstance.vkAllocateCommandBuffers(device.getVkDevice(), ref(commandBufferAllocateInfo), ofArray(commandPool.getVkCommandBuffers())).check();

        for (int i = 0; i < commandBufferCount; i++) {
            commandPool.getVkCommandBuffers().getOrCreate(i); // make sure they are all created, so we can just use get() later.
        }

        stack.pop(); // commandBufferAllocateInfo
        stack.pop(); // commandPoolCreateInfo

        return commandPool;
    }

    private final @NotNull VkInstance vkInstance;
    private final @NotNull VkDevice vkDevice;

    private final @NotNull VkCommandPool vkCommandPool;
    private final @NotNull StructureArray<VkCommandBuffer> vkCommandBuffers;

    protected CommandPool(
            @NotNull VkInstance vkInstance,
            @NotNull VkDevice vkDevice,
            int commandBufferCount
    ) {
        this.vkInstance = vkInstance;
        this.vkDevice = vkDevice;

        this.vkCommandPool = allocate(new VkCommandPool());
        this.vkCommandBuffers = StructureArray.newAllocated(commandBufferCount, VkCommandBuffer.class, VkCommandBuffer::new);
    }

    public @NotNull VkCommandPool getVkCommandPool() {
        return vkCommandPool;
    }

    public @NotNull VkCommandBuffer getVkCommandBuffer(int index) {
        return vkCommandBuffers.getOrCreate(index);
    }

    public @NotNull StructureArray<VkCommandBuffer> getVkCommandBuffers() {
        return vkCommandBuffers;
    }

    @Override
    public void close() {
        vkInstance.vkDestroyCommandPool(vkDevice, vkCommandPool, ref(null));
    }
}
