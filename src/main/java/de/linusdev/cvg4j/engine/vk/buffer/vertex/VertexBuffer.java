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

package de.linusdev.cvg4j.engine.vk.buffer.vertex;

import de.linusdev.cvg4j.nat.vulkan.enums.VkVertexInputRate;
import de.linusdev.cvg4j.nat.vulkan.structs.VkVertexInputAttributeDescription;
import de.linusdev.cvg4j.nat.vulkan.structs.VkVertexInputBindingDescription;
import de.linusdev.lutils.nat.memory.Stack;
import de.linusdev.lutils.nat.struct.abstracts.Structure;
import de.linusdev.lutils.nat.struct.array.StructureArray;
import de.linusdev.lutils.nat.struct.info.StructureInfo;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class VertexBuffer <V extends Structure> {

    public static <V extends Structure> VertexBuffer<V> create(
            @NotNull Stack stack,
            int binding,
            @NotNull StructureInfo elementInfo,
            @NotNull List<VertexElement> elements
    ) {

        // ensure it is correctly aligned.
        int stride = elementInfo.getRequiredSize();
        int alignment = elementInfo.getAlignment();
        if(stride % alignment != 0) {
            stride += alignment - (stride % alignment);
        }

        VertexBuffer<V> vertexBuffer = new VertexBuffer<>(elementInfo);

        VkVertexInputBindingDescription vertInputDescription = stack.push(new VkVertexInputBindingDescription());
        vertInputDescription.binding.set(binding);
        vertInputDescription.stride.set(stride);
        vertInputDescription.inputRate.set(VkVertexInputRate.VERTEX);

        StructureArray<VkVertexInputAttributeDescription> attributeDescriptions = stack.pushArray(
                elements.size(), VkVertexInputAttributeDescription.class, VkVertexInputAttributeDescription::new
        );

        int i = 0;
        for (VkVertexInputAttributeDescription attributeDescription : attributeDescriptions) {
            VertexElement element = elements.get(i++);
            attributeDescription.binding.set(binding);
            attributeDescription.location.set(element.location());
            attributeDescription.format.set(element.format());
            attributeDescription.offset.set(element.offset());
        }

        stack.pop(); // attributeDescriptions
        stack.pop(); // vertInputDescription

        return vertexBuffer;
    }

    private final @NotNull StructureInfo elementInfo;

    public VertexBuffer(@NotNull StructureInfo elementInfo) {
        this.elementInfo = elementInfo;
    }

}
