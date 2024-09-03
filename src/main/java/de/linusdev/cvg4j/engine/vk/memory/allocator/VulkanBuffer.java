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

import de.linusdev.cvg4j.engine.exception.EngineException;
import de.linusdev.cvg4j.engine.vk.device.Device;
import de.linusdev.cvg4j.engine.vk.memory.buffer.VulkanBufferMappingListener;
import de.linusdev.cvg4j.nat.vulkan.VkDeviceSize;
import de.linusdev.cvg4j.nat.vulkan.bitmasks.enums.VkBufferUsageFlagBits;
import de.linusdev.cvg4j.nat.vulkan.bitmasks.enums.VkMemoryPropertyFlagBits;
import de.linusdev.cvg4j.nat.vulkan.enums.VkSharingMode;
import de.linusdev.cvg4j.nat.vulkan.enums.VkStructureType;
import de.linusdev.cvg4j.nat.vulkan.handles.VkBuffer;
import de.linusdev.cvg4j.nat.vulkan.handles.VkInstance;
import de.linusdev.cvg4j.nat.vulkan.structs.VkBufferCreateInfo;
import de.linusdev.cvg4j.nat.vulkan.structs.VkMemoryRequirements;
import de.linusdev.lutils.bitfield.IntBitfield;
import de.linusdev.lutils.nat.memory.Stack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;

import static de.linusdev.lutils.nat.pointer.TypedPointer64.ref;
import static de.linusdev.lutils.nat.struct.abstracts.Structure.allocate;

public class VulkanBuffer implements AutoCloseable {

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

        vkInstance.vkCreateBuffer(device.getVkDevice(), ref(bufferCreateInfo), ref(null), ref(vulkanBuffer.vkBuffer));

        stack.pop(); // bufferCreateInfo

        VkMemoryRequirements memoryRequirements = stack.push(new VkMemoryRequirements());
        vkInstance.vkGetBufferMemoryRequirements(device.getVkDevice(), vulkanBuffer.vkBuffer, ref(memoryRequirements));
        int memoryTypeIndex = device.findMemoryType(stack, memoryRequirements.memoryTypeBits.get(), memFlags);
        vulkanBuffer.memoryRequirements(memoryRequirements, memoryTypeIndex, memFlags);

        stack.pop(); // memoryRequirements
    }

    private final @NotNull VkInstance vkInstance;
    private final @NotNull Device device;

    /*
     * Information stored in this class
     */
    private final @NotNull String debugName;
    /**
     * Whether this device is host visible. Only host visible buffers can be mapped.
     */
    private boolean isHostVisible;
    /**
     * The size (amount of bytes) that was set when creating the buffer
     */
    protected final int size;
    /**
     * The alignment this buffer requires.
     */
    long requiredAlignment = -1;
    /**
     * The offset of this buffer inside the allocated memory
     */
    final @NotNull VkDeviceSize offset;
    /**
     * The actual required size of this buffer. May be bigger than {@link #size}.
     */
    final @NotNull VkDeviceSize actualSize;

    int memoryTypeIndex = -1;

    protected boolean isMapped = false;
    protected ByteBuffer mappedByteBuffer;

    /*
     * Managed by this class
     */
    final @NotNull VkBuffer vkBuffer;

    /*
     * Listener
     */
    private @Nullable VulkanBufferMappingListener mappingListener;

    public VulkanBuffer(
            @NotNull VkInstance vkInstance, @NotNull Device device, @NotNull String debugName,
            int size
    ) {
        this.vkInstance = vkInstance;
        this.device = device;
        this.debugName = debugName;
        this.size = size;

        this.vkBuffer = allocate(new VkBuffer());
        this.offset = allocate(new VkDeviceSize());
        this.actualSize = allocate(new VkDeviceSize());
    }

    void mapped(@NotNull ByteBuffer mappedByteBuffer) {
        this.isMapped = true;
        this.mappedByteBuffer = mappedByteBuffer;
        if(mappingListener != null) mappingListener.vulkanBufferMapped(mappedByteBuffer);
    }

    void memoryRequirements(
            @NotNull VkMemoryRequirements memoryRequirements,
            int memoryTypeIndex,
            @NotNull IntBitfield<VkMemoryPropertyFlagBits> memFlags
    ) {
        actualSize.set(memoryRequirements.size.get());
        requiredAlignment = memoryRequirements.alignment.get();
        this.memoryTypeIndex = memoryTypeIndex;
        this.isHostVisible = memFlags.isSet(VkMemoryPropertyFlagBits.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT);
    }

    public int getSize() {
        return size;
    }

    public long getRequiredAlignment() {
        return requiredAlignment;
    }

    public @NotNull VkDeviceSize getActualSize() {
        return actualSize;
    }

    public @NotNull VkDeviceSize getOffset() {
        return offset;
    }

    public boolean isMapped() {
        return isMapped;
    }

    public ByteBuffer getMappedByteBuffer() {
        return mappedByteBuffer;
    }

    public @NotNull VkBuffer getVkBuffer() {
        return vkBuffer;
    }

    public @NotNull String getDebugName() {
        return debugName;
    }

    public boolean isHostVisible() {
        return isHostVisible;
    }

    public void setMappingListener(@Nullable VulkanBufferMappingListener mappingListener) {
        this.mappingListener = mappingListener;
        if(mappingListener != null && isMapped) mappingListener.vulkanBufferMapped(mappedByteBuffer);
    }

    @Override
    public void close() {
        vkInstance.vkDestroyBuffer(device.getVkDevice(), vkBuffer, ref(null));
    }
}
