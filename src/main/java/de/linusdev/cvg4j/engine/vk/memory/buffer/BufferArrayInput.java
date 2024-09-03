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

import de.linusdev.cvg4j.engine.vk.memory.allocator.VulkanBuffer;
import de.linusdev.lutils.nat.struct.abstracts.Structure;
import de.linusdev.lutils.nat.struct.annos.SVWrapper;
import de.linusdev.lutils.nat.struct.array.StructureArray;
import de.linusdev.lutils.nat.struct.info.ArrayInfo;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

public class BufferArrayInput<V extends Structure> implements VulkanBufferMappingListener {

    private VulkanBuffer vulkanBuffer;
    private final @NotNull StructureArray<V> backedArray;

    private int currentCount = 0;

    public BufferArrayInput(
            int vertexCount,
            @NotNull Class<?> elementClass,
            @NotNull StructureArray.ElementCreator<V> elementCreator
    ) {
        this.backedArray = StructureArray.newAllocatable(
                false, SVWrapper.of(vertexCount, elementClass), null, elementCreator
        );
    }

    @ApiStatus.Internal
    public void setVulkanBuffer(@NotNull VulkanBuffer vulkanBuffer) {
        this.vulkanBuffer = vulkanBuffer;
        vulkanBuffer.setMappingListener(this);
    }

    public @NotNull StructureArray<V> getBackedArray() {
        return backedArray;
    }

    public void setCurrentCount(int count) {
        this.currentCount = count;
    }

    public int getCurrentCount() {
        return currentCount;
    }

    public @NotNull ArrayInfo getBackedArrayInfo() {
        return backedArray.getInfo();
    }

    public VulkanBuffer getVulkanBuffer() {
        return vulkanBuffer;
    }

    @Override
    public void vulkanBufferMapped(@NotNull ByteBuffer mapped) {
        backedArray.claimBuffer(mapped);
    }
}
