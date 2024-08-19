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

package de.linusdev.cvg4j.nat.vulkan.utils;

import de.linusdev.cvg4j.nat.vulkan.enums.VkStructureType;
import de.linusdev.cvg4j.nat.vulkan.structs.VkShaderModuleCreateInfo;
import de.linusdev.lutils.nat.NativeParsable;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public interface VulkanSpirVUtils {

    @FunctionalInterface
    interface BufferCreator {
        @NotNull ByteBuffer create(int size, int alignment);
    }

    /**
     * Reads spir-v binary from given stream. The binary size will automatically be extended to confirm to
     * the vulkan specification for creating a shader module. That means, the binary size will be a multiple of
     * 4 and the alignment will be at least 4 (in reality it will always be 8).
     * @param stream stream containing spir-v binary.
     * @param byteCount exactly the amount of bytes given {@code stream} provides
     * @param bufferCreator function to create a {@link ByteBuffer} with given size and alignment
     * @return {@link NativeParsable} containing the binary data
     * @throws IOException possible while reading {@code stream}.
     */
    static @NotNull NativeParsable readSpirVBinary(
            @NotNull InputStream stream,
            int byteCount,
            @NotNull BufferCreator bufferCreator
    ) throws IOException {
        try (stream) {
            // size must be a multiple of 4 (https://registry.khronos.org/vulkan/specs/1.3-extensions/man/html/VkShaderModuleCreateInfo.html)
            if((byteCount % 4) != 0) {
                byteCount = byteCount + (4 - (byteCount % 4));
            }

            int pos = 0;
            int read;
            byte[] bytes = new byte[byteCount];
            while((read = stream.read(bytes, pos, bytes.length - pos)) != -1) {
                pos += read;
            }

            // the pointer to the byte data is an int32_t pointer (https://registry.khronos.org/vulkan/specs/1.3-extensions/man/html/VkShaderModuleCreateInfo.html)
            // and must point to a valid int32_t aligned value. that means, the bytes must be aligned to the next 4 byte boundary.
            // Since all Structures are aligned to 8 byte, we will align this to 8 byte too, to avoid complications
            ByteBuffer buffer = bufferCreator.create(byteCount, 8);
            buffer.put(bytes);
            buffer.clear(); // reset buffer position

            return new NativeParsable() {
                @Override
                public boolean isInitialised() {
                    return true;
                }

                @Override
                public ByteBuffer getByteBuffer() {
                    return buffer;
                }

                @Override
                public int getRequiredSize() {
                    return buffer.capacity();
                }

                @Override
                public int getAlignment() {
                    return 8;
                }

                @Override
                public String toString() {
                    return "Spir-V binary { byteCount=" + buffer.capacity() + "}";
                }
            };
        }
    }

    /**
     * Reads spir-v binary from given stream. The binary size will automatically be extended to confirm to
     * the vulkan specification for creating a shader module. That means, the binary size will be a multiple of
     * 4 and the alignment will be at least 4 (in reality it will always be 8).
     * @param stream {@link InputStream} containing spir-v binary data.
     * @param bufferCreator function to create a {@link ByteBuffer} with given size and alignment
     * @return {@link NativeParsable} containing the binary data
     * @throws IOException possible while reading {@code stream}.
     */
    static @NotNull NativeParsable readSpirVBinary(
            @NotNull InputStream stream,
            @NotNull BufferCreator bufferCreator
    ) throws IOException {
        try (stream) {

            List<byte[]> readBuffers = new ArrayList<>();

            int byteCount = 0;
            int read = 0;
            int pos = 0;
            while (read != -1) {
                byte[] readBuffer = new byte[2048];
                pos = 0;
                while((read = stream.read(readBuffer, pos, readBuffer.length - pos)) != 0 && read != -1) {
                    pos += read;
                }

                byteCount += pos;
                readBuffers.add(readBuffer);
            }

            // size must be a multiple of 4 (https://registry.khronos.org/vulkan/specs/1.3-extensions/man/html/VkShaderModuleCreateInfo.html)
            if((byteCount % 4) != 0) {
                byteCount = byteCount + (4 - (byteCount % 4));
            }

            // the pointer to the byte data is an int32_t pointer (https://registry.khronos.org/vulkan/specs/1.3-extensions/man/html/VkShaderModuleCreateInfo.html)
            // and must point to a valid int32_t aligned value. that means, the bytes must be aligned to the next 4 byte boundary.
            // Since all Structures are aligned to 8 byte, we will align this to 8 byte too, to avoid complications
            ByteBuffer buffer = bufferCreator.create(byteCount, 8);
            for (int i = 0; i < readBuffers.size() - 1; i++) {
                buffer.put(readBuffers.get(i));
            }
            buffer.put(readBuffers.get(readBuffers.size() - 1), 0, pos);
            buffer.clear(); // reset buffer position

            return new NativeParsable() {
                @Override
                public boolean isInitialised() {
                    return true;
                }

                @Override
                public ByteBuffer getByteBuffer() {
                    return buffer;
                }

                @Override
                public int getRequiredSize() {
                    return buffer.capacity();
                }

                @Override
                public int getAlignment() {
                    return 8;
                }

                @Override
                public String toString() {
                    return "Spir-V binary { byteCount=" + buffer.capacity() + "}";
                }
            };
        }

    }

    /**
     *
     * @param createInfo already allocated {@link VkShaderModuleCreateInfo}.
     * @param bufferCreator function to create a {@link ByteBuffer} with given size and alignment
     * @param stream stream to read spir-v binary from
     * @return given {@code createInfo} filled with required data, including sType, codeSize and pCode.
     * @throws IOException see {@link #readSpirVBinary(InputStream, BufferCreator) readSpirVBinary}.
     */
    static @NotNull VkShaderModuleCreateInfo createShaderModuleInfo(
            @NotNull VkShaderModuleCreateInfo createInfo,
            @NotNull BufferCreator bufferCreator,
            @NotNull InputStream stream
    ) throws IOException {
        var shader = readSpirVBinary(stream, bufferCreator);

        createInfo.sType.set(VkStructureType.VK_STRUCTURE_TYPE_SHADER_MODULE_CREATE_INFO);
        createInfo.codeSize.set(shader.getRequiredSize());
        createInfo.pCode.set(shader.getPointer());

        return createInfo;
    }

}
