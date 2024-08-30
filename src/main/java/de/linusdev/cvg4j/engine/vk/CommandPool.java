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

import de.linusdev.cvg4j.engine.vk.swapchain.SwapChain;
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
import org.jetbrains.annotations.NotNull;

import static de.linusdev.lutils.nat.pointer.TypedPointer64.ref;
import static de.linusdev.lutils.nat.struct.abstracts.Structure.allocate;

public class CommandPool implements AutoCloseable {

    public static @NotNull CommandPool create(
            @NotNull Stack stack,
            @NotNull VkInstance vkInstance,
            @NotNull VkDevice vkDevice,
            @NotNull SwapChain swapChain
    ) {
        CommandPool commandPool = new CommandPool(vkInstance, vkDevice);

        VkCommandPoolCreateInfo commandPoolCreateInfo = stack.push(new VkCommandPoolCreateInfo());
        commandPoolCreateInfo.sType.set(VkStructureType.COMMAND_POOL_CREATE_INFO);
        commandPoolCreateInfo.flags.set(VkCommandPoolCreateFlagBits.VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT);
        commandPoolCreateInfo.queueFamilyIndex.set(swapChain.getGraphicsQueueIndex());

        vkInstance.vkCreateCommandPool(vkDevice, ref(commandPoolCreateInfo), ref(null), ref(commandPool.getVkCommandPool())).check();

        VkCommandBufferAllocateInfo commandBufferAllocateInfo = stack.push(new VkCommandBufferAllocateInfo());
        commandBufferAllocateInfo.sType.set(VkStructureType.COMMAND_BUFFER_ALLOCATE_INFO);
        commandBufferAllocateInfo.commandPool.set(commandPool.getVkCommandPool().get());
        commandBufferAllocateInfo.level.set(VkCommandBufferLevel.PRIMARY);
        commandBufferAllocateInfo.commandBufferCount.set(1);

        vkInstance.vkAllocateCommandBuffers(vkDevice,ref(commandBufferAllocateInfo), ref(commandPool.getVkCommandBuffer())).check();

        stack.pop(); // commandBufferAllocateInfo
        stack.pop(); // commandPoolCreateInfo

        return commandPool;
    }

    private final @NotNull VkInstance vkInstance;
    private final @NotNull VkDevice vkDevice;

    private final @NotNull VkCommandPool vkCommandPool;
    private final @NotNull VkCommandBuffer vkCommandBuffer;

    protected CommandPool(@NotNull VkInstance vkInstance, @NotNull VkDevice vkDevice) {
        this.vkInstance = vkInstance;
        this.vkDevice = vkDevice;

        this.vkCommandPool = allocate(new VkCommandPool());
        this.vkCommandBuffer = allocate(new VkCommandBuffer());
    }

    public @NotNull VkCommandPool getVkCommandPool() {
        return vkCommandPool;
    }

    public @NotNull VkCommandBuffer getVkCommandBuffer() {
        return vkCommandBuffer;
    }

    @Override
    public void close() {
        vkInstance.vkDestroyCommandPool(vkDevice, vkCommandPool, ref(null));
    }
}
