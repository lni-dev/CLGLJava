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

import de.linusdev.cvg4j.engine.vk.device.Device;
import de.linusdev.cvg4j.nat.vulkan.handles.VkDeviceMemory;
import de.linusdev.lutils.bitfield.IntBitfield;
import de.linusdev.lutils.nat.memory.stack.Stack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Manager for a single {@link Device#findMemoryType(Stack, int, IntBitfield) memory type}.
 */
public interface MemoryTypeManager extends AutoCloseable {

    /**
     * This is called when given object changed and requires different sized memory
     * @param stack {@link Stack}
     * @param object the object that changed.
     */
    void onChanged(@NotNull Stack stack, @NotNull VulkanMemoryBoundObject object, @Nullable MemoryRequirementsChange change);

    /**
     * Add a {@link VulkanMemoryBoundObject} object to this class. When memory will be {@link #allocate(Stack) allocated}
     * some of it will be {@link VulkanMemoryBoundObject#bind(Stack, VkDeviceMemory) bound} to this object.
     * @param object {@link VulkanMemoryBoundObject} to add.
     */
    void addObject(@NotNull VulkanMemoryBoundObject object);

    /**
     * Allocate the required memory for all {@link #addObject(VulkanMemoryBoundObject) added} or
     * {@link #onChanged(Stack, VulkanMemoryBoundObject) changed} objects.
     * @param stack {@link Stack} used during allocation for short-lived structures.
     */
    void allocate(@NotNull Stack stack);

    /**
     * Index of the memory type managed by this device.
     */
    int getMemoryTypeIndex();

    @Override
    void close();


    /*
     * Accessors for protected methods in VulkanMemoryBoundObject
     */


    /**
     * Allows accessing protected method {@link VulkanMemoryBoundObject#mapped(ByteBuffer)}
     */
    default void map(@NotNull VulkanMemoryBoundObject object, @NotNull ByteBuffer byteBuffer) {
        long size = object.getSize();
        if(size == -1)
            size = object.getActualSize().get(); // Some object don't require size to bet set.

        object.mapped(byteBuffer.slice((int) object.getOffset().get(), (int) size).order(ByteOrder.nativeOrder()));
    }

    /**
     * Allows accessing protected method {@link VulkanMemoryBoundObject#setOffset(long)}.
     */
    default void setOffsetOf(@NotNull VulkanMemoryBoundObject object, long offset) {
        object.setOffset(offset);
    }

    /**
     * Allows accessing protected method {@link VulkanMemoryBoundObject#bind(Stack, VkDeviceMemory)}.
     */
    default void bindTo(@NotNull VulkanMemoryBoundObject object, @NotNull Stack stack, @NotNull VkDeviceMemory memory) {
        object.bind(stack, memory);
    }

    default void unbind(@NotNull VulkanMemoryBoundObject object,  @NotNull Stack stack) {
        object.unbind(stack);
    }

    /**
     * Allows accessing protected method {@link VulkanMemoryBoundObject#setMemoryTypeManager(MemoryTypeManager)}.
     */
    default void setMemoryTypeManagerOf(@NotNull VulkanMemoryBoundObject object, @NotNull MemoryTypeManager manager) {
        object.setMemoryTypeManager(manager);
    }
}
