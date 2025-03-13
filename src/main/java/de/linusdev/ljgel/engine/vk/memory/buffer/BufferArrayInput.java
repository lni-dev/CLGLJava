/*
 * Copyright (c) 2024-2025 Linus Andera
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

package de.linusdev.ljgel.engine.vk.memory.buffer;

import de.linusdev.ljgel.engine.vk.memory.manager.MappingListener;
import de.linusdev.lutils.nat.struct.UStructSupplier;
import de.linusdev.lutils.nat.struct.abstracts.Structure;
import de.linusdev.lutils.nat.struct.annos.SVWrapper;
import de.linusdev.lutils.nat.struct.array.StructureArray;
import de.linusdev.lutils.nat.struct.info.ArrayInfo;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

public class BufferArrayInput<V extends Structure> extends BufferInput implements MappingListener {

    private final @NotNull StructureArray<V> backedArray;
    private final int stride;

    private int currentCount = 0;

    public BufferArrayInput(
            int vertexCount,
            @NotNull Class<?> elementClass,
            @NotNull UStructSupplier<V> elementCreator
    ) {
        this.backedArray = StructureArray.newAllocatable(
                false, SVWrapper.of(vertexCount, elementClass), null, elementCreator
        );
        this.stride = getBackedArrayInfo().getStride();
    }

    public int getStride() {
        return stride;
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

    @Override
    public void vulkanBufferMapped(@NotNull ByteBuffer mapped) {
        backedArray.claimBuffer(mapped);
    }
}
