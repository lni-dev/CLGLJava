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
import de.linusdev.cvg4j.nat.vulkan.bitmasks.enums.VkMemoryPropertyFlagBits;
import de.linusdev.cvg4j.nat.vulkan.handles.VkDeviceMemory;
import de.linusdev.cvg4j.nat.vulkan.handles.VkInstance;
import de.linusdev.cvg4j.nat.vulkan.structs.VkMemoryRequirements;
import de.linusdev.lutils.bitfield.IntBitfield;
import de.linusdev.lutils.nat.memory.Stack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;

import static de.linusdev.lutils.nat.struct.abstracts.Structure.allocate;

public abstract class VulkanMemoryBoundObject implements AutoCloseable {

    protected final @NotNull VkInstance vkInstance;
    protected final @NotNull Device device;

    /*
     * Information stored in this class
     */
    protected final @NotNull String debugName;
    /**
     * Whether this device is host visible. Only host visible buffers can be mapped.
     */
    protected boolean isHostVisible;
    /**
     * The size (amount of bytes) that was set when creating the buffer
     */
    protected final int size;
    /**
     * The alignment this buffer requires.
     */
    protected long requiredAlignment = -1;
    /**
     * The offset of this buffer inside the allocated memory
     */
    protected final @NotNull VkDeviceSize offset;
    /**
     * The actual required size of this buffer. May be bigger than {@link #size}.
     */
    protected final @NotNull VkDeviceSize actualSize;

    int memoryTypeIndex = -1;

    protected boolean isMapped = false;
    protected ByteBuffer mappedByteBuffer;

    /*
     * Listener
     */
    private @Nullable MappingListener mappingListener;

    public VulkanMemoryBoundObject(
            @NotNull VkInstance vkInstance, @NotNull Device device, @NotNull String debugName,
            int size
    ) {
        this.vkInstance = vkInstance;
        this.device = device;
        this.debugName = debugName;
        this.size = size;

        this.offset = allocate(new VkDeviceSize());
        this.actualSize = allocate(new VkDeviceSize());
    }

    protected void initOffset(long offset) {
        this.offset.set(offset);
    }

    protected abstract void bind(@NotNull Stack stack, @NotNull VkDeviceMemory vkDeviceMemory);

    protected void mapped(@NotNull ByteBuffer mappedByteBuffer) {
        this.isMapped = true;
        this.mappedByteBuffer = mappedByteBuffer;
        if(mappingListener != null) mappingListener.vulkanBufferMapped(mappedByteBuffer);
    }

    protected void memoryRequirements(
            @NotNull VkMemoryRequirements memoryRequirements,
            int memoryTypeIndex,
            @NotNull IntBitfield<VkMemoryPropertyFlagBits> memFlags
    ) {
        actualSize.set(memoryRequirements.size.get());
        requiredAlignment = memoryRequirements.alignment.get();
        this.memoryTypeIndex = memoryTypeIndex;
        this.isHostVisible = memFlags.isSet(VkMemoryPropertyFlagBits.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT);
    }

    public long getRequiredAlignment() {
        return requiredAlignment;
    }

    public @NotNull VkDeviceSize getActualSize() {
        return actualSize;
    }

    public @NotNull String getDebugName() {
        return debugName;
    }

    public boolean isHostVisible() {
        return isHostVisible;
    }

    public int getSize() {
        return size;
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

    public void setMappingListener(@Nullable MappingListener mappingListener) {
        this.mappingListener = mappingListener;
        if(mappingListener != null && isMapped) mappingListener.vulkanBufferMapped(mappedByteBuffer);
    }

    @Override
    public void close() {

    }
}
