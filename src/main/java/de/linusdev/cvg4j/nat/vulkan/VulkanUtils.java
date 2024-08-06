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

package de.linusdev.cvg4j.nat.vulkan;

import de.linusdev.cvg4j.nat.NativeFunctions;
import de.linusdev.cvg4j.nat.NativeUtils;
import de.linusdev.cvg4j.nat.glfw3.GLFW;
import de.linusdev.cvg4j.nat.vulkan.constants.APIConstants;
import de.linusdev.cvg4j.nat.vulkan.enums.VkStructureType;
import de.linusdev.cvg4j.nat.vulkan.handles.VkInstance;
import de.linusdev.cvg4j.nat.vulkan.structs.VkAllocationCallbacks;
import de.linusdev.cvg4j.nat.vulkan.structs.VkInstanceCreateInfo;
import de.linusdev.cvg4j.nat.vulkan.structs.VkShaderModuleCreateInfo;
import de.linusdev.lutils.nat.NativeParsable;
import de.linusdev.lutils.nat.struct.utils.BufferUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class VulkanUtils {

    public static int makeVersion(int major, int minor, int patch) {
        return (major << 22) | (minor << 12) | patch;
    }

    public static int makeApiVersion(int variant, int major, int minor, int patch) {
        return (variant << 29) | (major << 22) | (minor << 12) | patch;
    }

    public static boolean vkBool32ToBoolean(int value) {
        return value == APIConstants.VK_TRUE;
    }

    public static boolean vkBool32ToBoolean(@NotNull VkBool32 value) {
        return vkBool32ToBoolean(value.get());
    }

    public static int booleanToVkBool32(boolean value) {
        return value ? APIConstants.VK_TRUE : APIConstants.VK_FALSE;
    }

    public static int VK_API_VERSION_1_0 = makeApiVersion(0, 1, 0, 0);
    public static int VK_API_VERSION_1_1 = makeApiVersion(0, 1, 1, 0);
    public static int VK_API_VERSION_1_2 = makeApiVersion(0, 1, 2, 0);
    public static int VK_API_VERSION_1_3 = makeApiVersion(0, 1, 3, 0);

    public static long VK_NULL_HANDLE = 0L;

    public static ReturnedVkResult vkCreateInstance(
            VkInstanceCreateInfo vkInstanceCreateInfo,
            @Nullable VkAllocationCallbacks pAllocator,
            VkInstance pInstance
    ) {
        long pointer = GLFW.glfwGetInstanceProcAddress(NativeUtils.getNullPointer(), "vkCreateInstance");

        int res = NativeFunctions.callNativeIFunctionPPP(
                pointer,
                vkInstanceCreateInfo.getPointer(),
                pAllocator == null ? NativeUtils.getNullPointer() : pAllocator.getPointer(),
                pInstance.getPointer()
        );

        return new ReturnedVkResult(res);
    }

    //TODO: doc comment
    public static @NotNull NativeParsable readSpirVBinary(
            @NotNull InputStream stream,
            int byteCount
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
            ByteBuffer buffer = BufferUtils.create64BitAligned(byteCount);
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

    public static @NotNull NativeParsable readSpirVBinary(
            @NotNull InputStream stream
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
            ByteBuffer buffer = BufferUtils.create64BitAligned(byteCount);
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

    public static @NotNull VkShaderModuleCreateInfo createShaderModuleInfo(@NotNull InputStream stream) throws IOException {
        var shader = VulkanUtils.readSpirVBinary(stream);

        VkShaderModuleCreateInfo createInfo = new VkShaderModuleCreateInfo();
        createInfo.allocate();
        createInfo.sType.set(VkStructureType.VK_STRUCTURE_TYPE_SHADER_MODULE_CREATE_INFO);
        createInfo.codeSize.set(shader.getRequiredSize());
        createInfo.pCode.set(shader.getPointer());

        return createInfo;
    }

}
