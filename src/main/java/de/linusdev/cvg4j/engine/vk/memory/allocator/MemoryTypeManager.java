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

package de.linusdev.cvg4j.engine.vk.memory.allocator;

import de.linusdev.cvg4j.engine.vk.device.Device;
import de.linusdev.cvg4j.nat.vulkan.VkDeviceSize;
import de.linusdev.cvg4j.nat.vulkan.bitmasks.VkMemoryMapFlags;
import de.linusdev.cvg4j.nat.vulkan.enums.VkStructureType;
import de.linusdev.cvg4j.nat.vulkan.handles.VkDeviceMemory;
import de.linusdev.cvg4j.nat.vulkan.handles.VkInstance;
import de.linusdev.cvg4j.nat.vulkan.structs.VkMemoryAllocateInfo;
import de.linusdev.lutils.nat.memory.Stack;
import de.linusdev.lutils.nat.pointer.BBPointer64;
import de.linusdev.lutils.nat.struct.abstracts.Structure;
import de.linusdev.lutils.nat.struct.utils.BufferUtils;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import static de.linusdev.cvg4j.engine.vk.memory.allocator.VulkanMemoryAllocator.LOG;
import static de.linusdev.lutils.nat.pointer.TypedPointer64.ref;

public class MemoryTypeManager implements AutoCloseable {

    private final @NotNull VkInstance vkInstance;
    private final @NotNull Device device;

    private final int memoryTypeIndex;

    private final List<VulkanBuffer> buffers = new ArrayList<>();
    private boolean requiresAllocation = true;

    private final @NotNull VkDeviceMemory vkDeviceMemory;

    public MemoryTypeManager(
            @NotNull VkInstance vkInstance,
            @NotNull Device device,
            int memoryTypeIndex
    ) {
        this.vkInstance = vkInstance;
        this.device = device;
        this.memoryTypeIndex = memoryTypeIndex;
        this.vkDeviceMemory = Structure.allocate(new VkDeviceMemory());
    }

    public void addBuffer(@NotNull VulkanBuffer buffer) {
        requiresAllocation = true;
        buffers.add(buffer);
    }

    public void allocate(
            @NotNull Stack stack,
            @NotNull VkInstance vkInstance,
            @NotNull Device device
    ) {

        // calculate the size and offsets
        long size = 0;
        for (VulkanBuffer buf : buffers) {
            if (size % buf.getRequiredAlignment() != 0) {
                size += buf.getRequiredAlignment() - (size % buf.getRequiredAlignment());
            }

            buf.offset.set(size);
            size += buf.getActualSize().get();
        }

        // Allocate the memory
        VkMemoryAllocateInfo allocInfo = stack.push(new VkMemoryAllocateInfo());
        allocInfo.sType.set(VkStructureType.MEMORY_ALLOCATE_INFO);
        allocInfo.allocationSize.set(size);
        allocInfo.memoryTypeIndex.set(memoryTypeIndex);
        vkInstance.vkAllocateMemory(device.getVkDevice(), ref(allocInfo), ref(null), ref(vkDeviceMemory)).check();
        stack.pop(); // allocInfo

        // Map memory
        VkMemoryMapFlags flags = stack.push(new VkMemoryMapFlags());
        BBPointer64 pointer = stack.pushPointer();
        VkDeviceSize offset = stack.push(new VkDeviceSize());
        VkDeviceSize vkSize = stack.push(new VkDeviceSize());

        offset.set(0);
        vkSize.set(size);

        vkInstance.vkMapMemory(device.getVkDevice(), vkDeviceMemory, offset, vkSize, flags, ref(pointer)).check();
        LOG.logDebug("Memory mapped. offset=" + offset.get() + ", size=" + vkSize.get());

        long pointerToMappedMemory = pointer.get();

        stack.pop(); // vkSize
        stack.pop(); // offset
        stack.pop(); // pointer
        stack.pop(); // flags


        // initialise the buffers
        ByteBuffer mapped = BufferUtils.getByteBufferFromPointer(pointerToMappedMemory, (int) size).order(ByteOrder.nativeOrder());

        for (VulkanBuffer buf : buffers) {
            LOG.logDebug("Binding memory to buffer '" + buf.getDebugName() + "'. offset=" + buf.offset.get() );
            vkInstance.vkBindBufferMemory(device.getVkDevice(), buf.vkBuffer, vkDeviceMemory, buf.offset);
            buf.mapped(mapped.slice((int) buf.getOffset().get(), buf.getSize()).order(ByteOrder.nativeOrder()));
        }


    }

    public boolean doesRequireAllocation() {
        return requiresAllocation;
    }

    @Override
    public void close() {
        for (VulkanBuffer buffer : buffers) {
            buffer.close();
        }

        vkInstance.vkFreeMemory(device.getVkDevice(), vkDeviceMemory, ref(null));
    }
}
