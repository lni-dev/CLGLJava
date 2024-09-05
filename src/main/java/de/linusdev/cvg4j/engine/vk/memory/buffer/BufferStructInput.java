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

import de.linusdev.lutils.nat.struct.abstracts.Structure;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

public class BufferStructInput<S extends Structure> extends BufferInput implements VulkanBufferMappingListener {

    private final @NotNull S backedStruct;

    public BufferStructInput(@NotNull S backedStruct) {
        this.backedStruct = backedStruct;
    }

    @Override
    public void vulkanBufferMapped(@NotNull ByteBuffer mapped) {
        this.backedStruct.claimBuffer(mapped);
    }

    @NotNull
    public S getBackedStruct() {
        return backedStruct;
    }
}
