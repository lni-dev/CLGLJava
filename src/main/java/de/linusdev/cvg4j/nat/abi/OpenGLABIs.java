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

package de.linusdev.cvg4j.nat.abi;

import de.linusdev.lutils.nat.MemorySizeable;
import de.linusdev.lutils.nat.NativeType;
import de.linusdev.lutils.nat.abi.ABI;
import de.linusdev.lutils.nat.abi.DefaultABIs;
import de.linusdev.lutils.nat.abi.Types;
import de.linusdev.lutils.nat.struct.info.ArrayInfo;
import de.linusdev.lutils.nat.struct.info.StructureInfo;
import de.linusdev.lutils.nat.struct.info.UnionInfo;
import org.jetbrains.annotations.NotNull;

public enum OpenGLABIs implements ABI, Types {

    /**
     * @see <a href="https://registry.khronos.org/vulkan/specs/1.3-extensions/html/chap15.html#interfaces-resources-layout">Vulkan spec</a>
     * @see <a href="https://registry.khronos.org/OpenGL/specs/gl/glspec46.core.pdf">OpenGL Specification</a> Section 7.6.2.2
     */
    STANDARD_UNIFORM_BLOCK_LAYOUT {
        @Override
        public @NotNull ABI getAbi() {
            return this;
        }

        @Override
        public @NotNull MemorySizeable integer() {
            return int32();
        }

        @Override
        public @NotNull MemorySizeable pointer() {
            return int64();
        }

        @Override
        public @NotNull String identifier() {
            return "OPEN_GL_STANDARD_UNIFORM_BLOCK_LAYOUT";
        }

        @Override
        public @NotNull StructureInfo calculateStructureLayout(boolean compress, @NotNull MemorySizeable @NotNull ... children) {
            int alignment = getBiggestStructAlignment(16, Integer.MAX_VALUE, children);
            int[] sizes = new int[children.length * 2 + 1];
            int padding;
            int position = 0;

            for(int i = 0; i < children.length; ) {
                MemorySizeable structure = children[i];

                int itemSize = structure.getRequiredSize();
                int itemAlignment = structure.getAlignment();
                if(!compress && (position % itemAlignment) != 0) {
                    int offset = (itemAlignment - (position % itemAlignment));
                    position += offset;
                    padding = offset;
                }else {
                    padding = 0;
                }

                sizes[i * 2] = padding;
                sizes[i * 2 + 1] = itemSize;
                position += itemSize;
                i++;
            }

            if(position % alignment != 0) {
                sizes[sizes.length - 1] = (alignment - (position % alignment));
                position += sizes[sizes.length - 1];
            }
            else sizes[sizes.length - 1] = 0;

            return new StructureInfo(alignment, compress, position, sizes);
        }

        @Override
        public @NotNull UnionInfo calculateUnionLayout(boolean compress, @NotNull MemorySizeable @NotNull ... children) {
            return DefaultABIs.MSVC_X64.calculateUnionLayout(compress, children);
        }

        @Override
        public @NotNull ArrayInfo calculateArrayLayout(boolean compress, @NotNull MemorySizeable children, int length, int stride) {
            int alignment = children.getAlignment();
            // base alignment in an array is at least that of a vec4.
            // All data types are either 4 byte (bool, int, float) or 8 byte (double).
            // The alignment of a struct is at least 16 byte.
            // This means if the child-alignment is 4 we definitely have a scalar type (bool, int, float) -> array alignment is 16.
            // If we have a child-alignment of 8, it could be a (bool, int, float)vec2 or a double,
            // which would result in an alignment of 16 and 32 respectively. This is bad, we cant differ between those too
            // here.
            // So let's assume no doubles for now!
            alignment = Math.max(16, alignment);

            if (stride == -1) {
                stride = children.getRequiredSize();
                if (!compress && stride < alignment) {
                    stride = alignment;
                }

                if (!compress && stride % alignment != 0) {
                    stride += alignment - (stride % alignment);
                }
            }

            int finalStride = stride;
            return new ArrayInfo(
                    alignment,
                    compress,
                    stride * length,
                    new int[]{0, stride * length, 0},
                    length,
                    stride,
                    (index) -> index * finalStride
            );
        }

        @Override
        public @NotNull ArrayInfo calculateVectorLayout(@NotNull NativeType componentType, int length) {
            MemorySizeable sizeable = componentType.getMemorySizeable(types());

            int stride = sizeable.getRequiredSize();
            int size = length * stride;
            int padding = 0;

            if(length == 3) padding = stride;

            return new ArrayInfo(
                    size + padding,
                    false,
                    size + padding,
                    new int[]{0, size, padding},
                    length,
                    stride,
                    index -> stride * index
            );
        }

        @Override
        public @NotNull ArrayInfo calculateMatrixLayout(@NotNull NativeType componentType, int width, int height) {

            ArrayInfo vectorInfo = calculateVectorLayout(componentType, width);

            ArrayInfo matrixInfo = calculateArrayLayout(false, vectorInfo, height, -1);

            return new ArrayInfo(
                    matrixInfo.getAlignment(),
                    matrixInfo.isCompressed(),
                    matrixInfo.getRequiredSize(),
                    matrixInfo.getSizes(),
                    matrixInfo.getLength(),
                    matrixInfo.getStride(),
                    index -> {
                        int y = index / width;
                        int x = index - (y * width);

                        return matrixInfo.getPositions().position(y) + vectorInfo.getPositions().position(x);
                    }
            );
        }

        @Override
        public @NotNull Types types() {
            return this;
        }
    }

    ;

    /**
     * Returns the alignment of the biggest {@link MemorySizeable} with the biggest alignment in given array.
     * The size will be at least {@code min} and at most {@code max}.
     * @param min minimum size
     * @param max maximum size
     * @param vars array of {@link MemorySizeable}
     * @return clamp(min, max, biggestStruct.getRequiredSize())
     */
    @SuppressWarnings("SameParameterValue")
    private static int getBiggestStructAlignment(int min, int max, @NotNull MemorySizeable @NotNull ... vars) {
        int biggest = min;
        for(MemorySizeable structure : vars)
            biggest = Math.max(biggest, structure.getAlignment());
        return Math.min(max, biggest);
    }

}
