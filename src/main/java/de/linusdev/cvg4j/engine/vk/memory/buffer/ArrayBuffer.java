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

package de.linusdev.cvg4j.engine.vk.memory.buffer;

import de.linusdev.cvg4j.nat.vulkan.VkDeviceSize;
import de.linusdev.cvg4j.nat.vulkan.handles.VkBuffer;
import de.linusdev.cvg4j.nat.vulkan.handles.VkCommandBuffer;
import de.linusdev.cvg4j.nat.vulkan.handles.VkInstance;
import de.linusdev.lutils.nat.memory.Stack;
import de.linusdev.lutils.nat.struct.abstracts.Structure;
import org.jetbrains.annotations.NotNull;

import static de.linusdev.lutils.nat.struct.abstracts.Structure.allocate;

public abstract class ArrayBuffer<V extends Structure> {

    protected final @NotNull VkInstance vkInstance;

    protected final @NotNull BufferArrayInput<V> input;
    protected final @NotNull BufferOutput output;

    protected final @NotNull VkDeviceSize offset;

    protected ArrayBuffer(
            @NotNull VkInstance vkInstance,
            @NotNull BufferArrayInput<V> input,
            @NotNull BufferOutput output
    ) {
        this.vkInstance = vkInstance;
        this.input = input;
        this.output = output;
        this.offset = allocate(new VkDeviceSize());
        this.offset.set(0);
    }

    public abstract void bufferCopyCommand(
            @NotNull Stack stack,
            @NotNull VkCommandBuffer vkCommandBuffer
    );

    public @NotNull VkBuffer getVkBuffer() {
        return output.getVulkanBuffer().getVkBuffer();
    }

    public @NotNull VkDeviceSize getOffset() {
        return offset;
    }

    public int getCurrentCount() {
        return input.getCurrentCount();
    }

    public @NotNull BufferArrayInput<V> getInput() {
        return input;
    }
}
