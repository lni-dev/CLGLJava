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

package de.linusdev.cvg4j.engine.vk.memory.manager.ondemand;

import de.linusdev.cvg4j.engine.vk.device.Device;
import de.linusdev.cvg4j.engine.vk.memory.manager.MemoryRequirementsChange;
import de.linusdev.cvg4j.engine.vk.memory.manager.MemoryTypeManager;
import de.linusdev.cvg4j.engine.vk.memory.manager.VulkanMemoryBoundObject;
import de.linusdev.cvg4j.nat.vulkan.VkDeviceSize;
import de.linusdev.cvg4j.nat.vulkan.bitmasks.VkMemoryMapFlags;
import de.linusdev.cvg4j.nat.vulkan.bitmasks.enums.VkMemoryPropertyFlagBits;
import de.linusdev.cvg4j.nat.vulkan.enums.VkStructureType;
import de.linusdev.cvg4j.nat.vulkan.handles.VkDeviceMemory;
import de.linusdev.cvg4j.nat.vulkan.handles.VkInstance;
import de.linusdev.cvg4j.nat.vulkan.structs.VkMemoryAllocateInfo;
import de.linusdev.llog.LLog;
import de.linusdev.llog.base.LogInstance;
import de.linusdev.lutils.bitfield.IntBitfield;
import de.linusdev.lutils.nat.memory.stack.Stack;
import de.linusdev.lutils.nat.pointer.BBPointer64;
import de.linusdev.lutils.nat.struct.abstracts.Structure;
import de.linusdev.lutils.nat.struct.utils.BufferUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import static de.linusdev.cvg4j.engine.vk.memory.manager.VulkanMemoryBoundObject.State.BOUND;
import static de.linusdev.lutils.nat.pointer.TypedPointer64.ref;

public class OnDemandMemoryTypeManager implements MemoryTypeManager {

    private final static @NotNull LogInstance LOG = LLog.getLogInstance();

    private final @NotNull VkInstance vkInstance;
    private final @NotNull Device device;

    private final int memoryTypeIndex;
    private final @NotNull IntBitfield<VkMemoryPropertyFlagBits> memoryTypeFlags;

    private final List<VulkanMemoryBoundObject> objects = new ArrayList<>();
    private boolean requiresAllocation = true;

    private ByteBuffer mappedMemory;

    /*
     * Managed by this class
     */
    private final @NotNull VkDeviceMemory vkDeviceMemory;

    public OnDemandMemoryTypeManager(
            @NotNull Stack stack,
            @NotNull VkInstance vkInstance,
            @NotNull Device device,
            int memoryTypeIndex
    ) {
        this.vkInstance = vkInstance;
        this.device = device;
        this.memoryTypeIndex = memoryTypeIndex;
        this.vkDeviceMemory = Structure.allocate(new VkDeviceMemory());
        this.memoryTypeFlags = device.getMemoryPropFlagsOf(stack, memoryTypeIndex);
    }

    @Override
    public void onChanged(@NotNull Stack stack, @NotNull VulkanMemoryBoundObject object, @Nullable MemoryRequirementsChange change) {
        if(requiresAllocation)
            return; // We have to reallocate anyway, return early.

        if(change == null) {
            // If nothing has changed, just rebind the memory.
            bindTo(object, stack, vkDeviceMemory);
            if(canBeMapped())
                map(object, mappedMemory);
            return;
        }
        long alignmentFix = (change.oldOffset() % change.newRequiredAlignment()) == 0 ? 0 : change.newRequiredAlignment() - (change.oldOffset() % change.newRequiredAlignment());
        long sizeChange = change.oldRequiredSize() - (change.newRequiredSize() + alignmentFix);

        if(sizeChange >= 0) { // It still fits :)
            bindTo(object, stack, vkDeviceMemory);
            if(canBeMapped())
                map(object, mappedMemory);
            return;
        }

        // It doesn't fit anymore, we have to allocate everything again...
        requiresAllocation = true;
    }

    @Override
    public void addObject(@NotNull VulkanMemoryBoundObject object) {
        requiresAllocation = true;
        objects.add(object);
    }

    public void allocate(@NotNull Stack stack) {
        if(!requiresAllocation)
            return;
        requiresAllocation = false;

        if(!vkDeviceMemory.isNullHandle()) {
            freeMemory();
        }

        // calculate the size and offsets
        long size = 0;
        for (VulkanMemoryBoundObject object : objects) {
            if (size % object.getRequiredAlignment() != 0) {
                size += object.getRequiredAlignment() - (size % object.getRequiredAlignment());
            }

            setOffsetOf(object, size);
            size += object.getActualSize().get();
        }

        LOG.debug("Start Allocating " + size + " bytes memory. index=" + memoryTypeIndex + ", properties=" + memoryTypeFlags.toList(VkMemoryPropertyFlagBits.class) + "." );

        // Allocate the memory
        VkMemoryAllocateInfo allocInfo = stack.push(new VkMemoryAllocateInfo());
        allocInfo.sType.set(VkStructureType.MEMORY_ALLOCATE_INFO);
        allocInfo.allocationSize.set(size);
        allocInfo.memoryTypeIndex.set(memoryTypeIndex);
        vkInstance.vkAllocateMemory(device.getVkDevice(), ref(allocInfo), ref(null), ref(vkDeviceMemory)).check();
        stack.pop(); // allocInfo

        LOG.debug("Allocated " + size + " bytes memory. index=" + memoryTypeIndex + ", properties=" + memoryTypeFlags.toList(VkMemoryPropertyFlagBits.class) + "." );

        for (VulkanMemoryBoundObject object : objects) {
            if(object.getState().isPast(BOUND))
                unbind(object, stack);
            LOG.debug("Binding memory to buffer '" + object.getDebugName() + "'. offset=" + object.getOffset().get() );
            bindTo(object, stack, vkDeviceMemory);
        }

        if(canBeMapped()) {
            // Map memory
            VkMemoryMapFlags flags = stack.push(new VkMemoryMapFlags());
            BBPointer64 pointer = stack.pushPointer();
            VkDeviceSize offset = stack.push(new VkDeviceSize());
            VkDeviceSize vkSize = stack.push(new VkDeviceSize());

            offset.set(0);
            vkSize.set(size);

            vkInstance.vkMapMemory(device.getVkDevice(), vkDeviceMemory, offset, vkSize, flags, ref(pointer)).check();
            LOG.debug("Memory mapped. offset=" + offset.get() + ", size=" + vkSize.get());

            long pointerToMappedMemory = pointer.get();

            stack.pop(); // vkSize
            stack.pop(); // offset
            stack.pop(); // pointer
            stack.pop(); // flags


            // initialise the buffers
            mappedMemory = BufferUtils.getByteBufferFromPointer(pointerToMappedMemory, (int) size).order(ByteOrder.nativeOrder());

            for (VulkanMemoryBoundObject object : objects) {
                map(object, mappedMemory);
            }
        }

    }

    public boolean canBeMapped() {
        return memoryTypeFlags.isSet(VkMemoryPropertyFlagBits.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT);
    }

    private void freeMemory() {
        vkInstance.vkFreeMemory(device.getVkDevice(), vkDeviceMemory, ref(null));
        vkDeviceMemory.set(null);
    }

    @Override
    public int getMemoryTypeIndex() {
        return memoryTypeIndex;
    }

    @Override
    public void close() {
        for (VulkanMemoryBoundObject buffer : objects) {
            buffer.close();
        }

        freeMemory();
    }
}
