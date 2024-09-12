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

package de.linusdev.cvg4j.engine.vk.memory.manager;

import de.linusdev.cvg4j.engine.exception.EngineException;
import de.linusdev.cvg4j.engine.vk.device.Device;
import de.linusdev.cvg4j.nat.vulkan.VkDeviceSize;
import de.linusdev.cvg4j.nat.vulkan.bitmasks.enums.VkMemoryPropertyFlagBits;
import de.linusdev.cvg4j.nat.vulkan.handles.VkDeviceMemory;
import de.linusdev.cvg4j.nat.vulkan.handles.VkInstance;
import de.linusdev.cvg4j.nat.vulkan.structs.VkMemoryRequirements;
import de.linusdev.lutils.bitfield.IntBitfield;
import de.linusdev.lutils.nat.memory.stack.Stack;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;

import static de.linusdev.lutils.nat.struct.abstracts.Structure.allocate;

public abstract class VulkanMemoryBoundObject implements AutoCloseable {

    public enum State {
        /**
         * Initial state. Nothing has been created
         * <br><br>
         * Destruction: Nothing has to be destroyed.
         */
        NOT_CREATED,
        /**
         * Object has been (re)created.
         * <br><br>
         * Destruction: Already created stuff must be destroyed.
         * <br><br>
         * {@link MemoryTypeManager}: No memory has been bound to this object. {@link #bind(Stack, VkDeviceMemory) bind}
         * can safely be called.
         */
        RECREATED,
        /**
         * Object has been (re)created and memory has been bound.
         * <br><br>
         * Destruction: Already created stuff must be destroyed.
         * <br><br>
         * {@link MemoryTypeManager}: memory has been bound to this object. {@link #bind(Stack, VkDeviceMemory) bind}
         * cannot be called. {@link #mapped(ByteBuffer)} can be called.
         */
        BOUND,
        /**
         * Optional State. Object has been (re)created, memory has been bound and mapped to host memory.
         * <br><br>
         * Destruction: Already created stuff must be destroyed.
         * <br><br>
         * {@link MemoryTypeManager}: memory has been bound to this object and mapped. {@link #bind(Stack, VkDeviceMemory) bind}
         * cannot be called. {@link #mapped(ByteBuffer) map} cannot be called.
         */
        MAPPED
        ;

        /**
         * If this state can be considered past given {@code state}.
         * A state is considered past, if it is the same state or a later state.
         */
        public boolean isPast(@NotNull State state) {
            return this.ordinal() >= state.ordinal();
        }
    }

    protected final @NotNull VkInstance vkInstance;
    protected final @NotNull Device device;

    /*
     * Information stored in this class
     */
    protected @NotNull State state = State.NOT_CREATED;
    protected final @NotNull String debugName;
    /**
     * The size (amount of bytes) that was set when creating the buffer. This can also be {@code -1}, if
     * the object calculates the size based on other attributes (for example image width, height).
     */
    protected int size;
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
    /**
     * The memory type manager this object's memory belongs to.
     */
    protected MemoryTypeManager memoryTypeManager;
    /**
     * Whether this object is mapped to host memory.
     */
    protected boolean isMapped = false;
    /**
     * The memory this object is mapped to, if it is mapped.
     */
    protected ByteBuffer mappedByteBuffer;

    /*
     * Listener
     */
    private @Nullable MappingListener mappingListener;

    public VulkanMemoryBoundObject(
            @NotNull Device device, @NotNull String debugName,
            int size
    ) {
        this.vkInstance = device.getVkInstance();
        this.device = device;
        this.debugName = debugName;
        this.size = size;

        this.offset = allocate(new VkDeviceSize());
        this.actualSize = allocate(new VkDeviceSize());
    }

    /**
     * The {@link MemoryTypeManager} must reallocate memory and free the old memory. That is why the memory this object
     * currently uses must be unbound. This generally means this object has to be destroyed and recreated.
     */
    @MustBeInvokedByOverriders
    protected void unbind(@NotNull Stack stack) {
        assert assertStatePast(State.BOUND);
    }

    /**
     * Allows {@link MemoryTypeManager} to set the offset of this object in the allocated memory.
     */
    protected void setOffset(long offset) {
        this.offset.set(offset);
    }

    protected void setMemoryTypeManager(MemoryTypeManager memoryTypeManager) {
        this.memoryTypeManager = memoryTypeManager;
    }

    @MustBeInvokedByOverriders
    protected void bind(@NotNull Stack stack, @NotNull VkDeviceMemory vkDeviceMemory) {
        assert assertState(State.RECREATED);
        state = State.BOUND;
    }

    protected void mapped(@NotNull ByteBuffer mappedByteBuffer) {
        assert assertState(State.BOUND);
        this.isMapped = true;
        this.mappedByteBuffer = mappedByteBuffer;
        if(mappingListener != null) mappingListener.vulkanBufferMapped(mappedByteBuffer);
        state = State.MAPPED;
    }

    protected void memoryRequirements(@NotNull VkMemoryRequirements memoryRequirements) {
        actualSize.set(memoryRequirements.size.get());
        requiredAlignment = memoryRequirements.alignment.get();
    }

    public abstract int calculateMemoryTypeIndex(
            @NotNull Stack stack,
            @NotNull IntBitfield<VkMemoryPropertyFlagBits> memFlags
    ) throws EngineException;

    protected boolean assertState(@NotNull State state) {
        if(this.state == state) return true;

        throw new IllegalStateException("This object must be in the state '" + state + "' for this function call" +
                ", but is in the state '" + this.state + "'.");

    }

    protected boolean assertStatePast(@NotNull State state) {
        if(this.state.isPast(state)) return true;

        throw new IllegalStateException("This object must be past the state '" + state
                + "' for this function call, but is in the state '" + this.state + "'.");

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

    public @NotNull VkInstance getVkInstance() {
        return vkInstance;
    }

    public @NotNull Device getDevice() {
        return device;
    }

    public @NotNull State getState() {
        return state;
    }

    @Override
    public void close() {
        state = State.NOT_CREATED;
    }
}
