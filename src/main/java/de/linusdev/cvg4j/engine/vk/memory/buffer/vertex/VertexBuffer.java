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

package de.linusdev.cvg4j.engine.vk.memory.buffer.vertex;

import de.linusdev.cvg4j.nat.vulkan.VkDeviceSize;
import de.linusdev.cvg4j.nat.vulkan.handles.VkBuffer;
import de.linusdev.cvg4j.nat.vulkan.structs.VkVertexInputAttributeDescription;
import de.linusdev.cvg4j.nat.vulkan.structs.VkVertexInputBindingDescription;
import de.linusdev.lutils.nat.struct.abstracts.Structure;
import de.linusdev.lutils.nat.struct.array.StructureArray;
import de.linusdev.lutils.nat.struct.array.StructureArraySupplier;
import org.jetbrains.annotations.NotNull;

import static de.linusdev.lutils.nat.struct.abstracts.Structure.allocate;

public class VertexBuffer <V extends Structure> {

    private final @NotNull VertexArrayInput<V> vertexInput;
    private final @NotNull VertexOutput vertexOutput;

    private final @NotNull VkDeviceSize offset;

    public VertexBuffer(@NotNull VertexArrayInput<V> vertexInput, @NotNull VertexOutput vertexOutput) {
        this.vertexInput = vertexInput;
        this.vertexOutput = vertexOutput;
        this.offset = allocate(new VkDeviceSize());
        this.offset.set(0);
    }

    public void createdDescriptor(@NotNull VkVertexInputBindingDescription description) {
        vertexOutput.createdDescriptor(description);
    }

    public StructureArray<VkVertexInputAttributeDescription> createAttributeDescriptors(
            @NotNull StructureArraySupplier<VkVertexInputAttributeDescription> arraySupplier
    ) {
        return vertexOutput.createAttributeDescriptors(arraySupplier);
    }

    public @NotNull VkBuffer getVkBuffer() {
        return vertexOutput.getVulkanBuffer().getVkBuffer();
    }

    public @NotNull VkDeviceSize getOffset() {
        return offset;
    }

    public int getVertexCount() {
        return vertexInput.getCurrentVertexCount();
    }

    public @NotNull VertexArrayInput<V> getVertexInput() {
        return vertexInput;
    }
}
