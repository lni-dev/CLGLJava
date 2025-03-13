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

package de.linusdev.ljgel.engine.vk.memory.buffer.vertex;

import de.linusdev.ljgel.nat.vulkan.enums.VkFormat;
import de.linusdev.lutils.math.vector.buffer.BBVectorInfo;
import de.linusdev.lutils.nat.struct.info.ComplexStructureInfo;
import de.linusdev.lutils.nat.struct.info.StructVarInfo;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public record VertexElement(
        @NotNull VkFormat format,
        int offset,
        int location
) {

    public static List<VertexElement> ofComplexInfo(@NotNull ComplexStructureInfo info) {

        List<VertexElement> elements = new ArrayList<>(info.getChildrenInfo().length);

        int position = 0;
        for (int i = 0; i < info.getChildrenInfo().length; i++) {
            StructVarInfo varInfo = info.getChildrenInfo()[i];

            position += info.getSizes()[i * 2];

            elements.add(new VertexElement(getFormatFromClass(varInfo), position, i));

            position += info.getSizes()[i * 2 + 1];
        }

        return elements;
    }

    private static @NotNull VkFormat getFormatFromClass(@NotNull StructVarInfo varInfo) {
        if(varInfo.getInfo() instanceof BBVectorInfo vecInfo) {
            switch (vecInfo.getType()) {
                case INT8 -> {
                    switch (vecInfo.getLength()) {
                        case 1: return VkFormat.R8_UINT;
                        case 2: return VkFormat.R8G8_UINT;
                        case 3: return VkFormat.R8G8B8_UINT;
                        case 4: return VkFormat.R8G8B8A8_UINT;
                    }
                    
                }
                case INT16 -> {
                    switch (vecInfo.getLength()) {
                        case 1: return VkFormat.R16_UINT;
                        case 2: return VkFormat.R16G16_UINT;
                        case 3: return VkFormat.R16G16B16_UINT;
                        case 4: return VkFormat.R16G16B16A16_UINT;
                    }
                }
                case INT32 -> {
                    switch (vecInfo.getLength()) {
                        case 1: return VkFormat.R32_UINT;
                        case 2: return VkFormat.R32G32_UINT;
                        case 3: return VkFormat.R32G32B32_UINT;
                        case 4: return VkFormat.R32G32B32A32_UINT;
                    }
                }
                case INT64 -> {
                    switch (vecInfo.getLength()) {
                        case 1: return VkFormat.R64_UINT;
                        case 2: return VkFormat.R64G64_UINT;
                        case 3: return VkFormat.R64G64B64_UINT;
                        case 4: return VkFormat.R64G64B64A64_UINT;
                    }
                }
                case FLOAT32 -> {
                    switch (vecInfo.getLength()) {
                        case 1: return VkFormat.R32_SFLOAT;
                        case 2: return VkFormat.R32G32_SFLOAT;
                        case 3: return VkFormat.R32G32B32_SFLOAT;
                        case 4: return VkFormat.R32G32B32A32_SFLOAT;
                    }
                }
                case FLOAT64 -> {
                    switch (vecInfo.getLength()) {
                        case 1: return VkFormat.R64_SFLOAT;
                        case 2: return VkFormat.R64G64_SFLOAT;
                        case 3: return VkFormat.R64G64B64_SFLOAT;
                        case 4: return VkFormat.R64G64B64A64_SFLOAT;
                    }
                }
            }
        }

        throw new IllegalArgumentException("Unsupported type of variable " + varInfo.getVarName());
    }

    public VertexElement(@NotNull VkFormat format, int offset, int location) {
        this.format = format;
        this.offset = offset;
        this.location = location;
    }
}
