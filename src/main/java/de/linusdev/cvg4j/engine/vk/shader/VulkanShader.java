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

package de.linusdev.cvg4j.engine.vk.shader;

import de.linusdev.cvg4j.engine.vk.VulkanEngine;
import de.linusdev.cvg4j.nat.vulkan.enums.VkStructureType;
import de.linusdev.cvg4j.nat.vulkan.handles.VkDevice;
import de.linusdev.cvg4j.nat.vulkan.handles.VkInstance;
import de.linusdev.cvg4j.nat.vulkan.handles.VkShaderModule;
import de.linusdev.cvg4j.nat.vulkan.structs.VkShaderModuleCreateInfo;
import de.linusdev.lutils.nat.NativeParsable;
import de.linusdev.lutils.nat.memory.stack.Stack;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;

import static de.linusdev.lutils.nat.pointer.TypedPointer64.ref;

public class VulkanShader implements AutoCloseable {

    private final @NotNull VkInstance vkInstance;
    private final @NotNull VkDevice vkDevice;
    private final @NotNull String mainMethodName;

    private final @NotNull VkShaderModule shaderModule;

    public VulkanShader(
            @NotNull VkInstance vkInstance,
            @NotNull VkDevice vkDevice,
            @NotNull String mainMethodName,
            @NotNull VkShaderModule shaderModule
    ) {
        this.vkInstance = vkInstance;
        this.vkDevice = vkDevice;
        this.mainMethodName = mainMethodName;
        this.shaderModule = shaderModule;
    }

    public static @NotNull VulkanShader createFromSpirVBinaryStream(
            @NotNull Stack stack,
            @NotNull VulkanEngine<?> engine,
            @NotNull InputStream stream,
            @NotNull String mainMethodName,
            @NotNull VkShaderModule store
    ) throws IOException {
        NativeParsable shaderBinary = VulkanSpirVUtils.readSpirVBinary(stream, stack::pushByteBuffer);

        VkShaderModuleCreateInfo createInfo = stack.push(new VkShaderModuleCreateInfo());
        createInfo.sType.set(VkStructureType.SHADER_MODULE_CREATE_INFO);
        createInfo.codeSize.set(shaderBinary.getRequiredSize());
        createInfo.pCode.set(shaderBinary.getPointer());

        engine.getVkInstance().vkCreateShaderModule(engine.getDevice().getVkDevice(), ref(createInfo), ref(null), ref(store)).check();

        stack.pop(); // createInfo
        stack.pop(); // shaderBinary's bytebuffer

        return new VulkanShader(engine.getVkInstance(), engine.getDevice().getVkDevice(), mainMethodName, store);
    }

    public @NotNull String getMainMethodName() {
        return mainMethodName;
    }

    public @NotNull VkShaderModule getShaderModule() {
        return shaderModule;
    }

    @Override
    public void close() {
        vkInstance.vkDestroyShaderModule(vkDevice, shaderModule, ref(null));
    }
}
