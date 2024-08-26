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

package de.linusdev.cvg4j.nengine.vulkan.selector;

import de.linusdev.cvg4j.nat.glfw3.GLFW;
import de.linusdev.cvg4j.nat.vulkan.structs.VkExtensionProperties;
import de.linusdev.cvg4j.nat.vulkan.utils.VulkanApiVersionUtils;
import de.linusdev.cvg4j.nengine.vulkan.extension.VulkanExtension;
import de.linusdev.lutils.math.vector.buffer.intn.BBUInt1;
import de.linusdev.lutils.nat.memory.Stack;
import de.linusdev.lutils.nat.pointer.BBTypedPointer64;
import de.linusdev.lutils.nat.pointer.Pointer64;
import de.linusdev.lutils.nat.string.NullTerminatedUTF8String;
import de.linusdev.lutils.nat.struct.array.StructureArray;
import de.linusdev.lutils.nat.struct.utils.BufferUtils;
import de.linusdev.lutils.version.SimpleVersion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static de.linusdev.cvg4j.nat.vulkan.utils.VulkanNonInstanceMethods.vkEnumerateInstanceExtensionProperties;
import static de.linusdev.cvg4j.nat.vulkan.utils.VulkanNonInstanceMethods.vkEnumerateInstanceVersion;
import static de.linusdev.lutils.nat.pointer.TypedPointer64.ref;

public class VulkanEngineInfo {

    private @Nullable SimpleVersion maxInstanceVulkanApiVersion = null;
    private @NotNull VulkanExtension @Nullable [] availableInstanceExtensions = null;
    private @NotNull VulkanExtension @Nullable [] glfwRequiredInstanceExtensions = null;

    public void load(@NotNull Stack stack) {
        BBUInt1 integer = stack.pushUnsignedInt();
        vkEnumerateInstanceVersion(integer);
        maxInstanceVulkanApiVersion = VulkanApiVersionUtils.toSimpleVersion(integer.get());

        StructureArray<VkExtensionProperties> extensions = vkEnumerateInstanceExtensionProperties(
                ref(null),
                integer,
                stack::pushArray
        );

        availableInstanceExtensions = new VulkanExtension[extensions.length()];
        for (int i = 0; i < extensions.length(); i++) {
            VkExtensionProperties extension = extensions.getOrCreate(i);
            availableInstanceExtensions[i] = VulkanExtension.of(extension.extensionName.get(), extension.specVersion.get());
        }

        StructureArray<BBTypedPointer64<NullTerminatedUTF8String>> reqExtensions = GLFW.glfwGetRequiredInstanceExtensions(integer);

        glfwRequiredInstanceExtensions = new VulkanExtension[reqExtensions.length()];
        for (int i = 0; i < reqExtensions.length(); i++) {
            Pointer64 pName = reqExtensions.getOrCreate(i);
            glfwRequiredInstanceExtensions[i] = VulkanExtension.of(
                    BufferUtils.readNullTerminatedUtf8String(pName.get()),
                    0
            );
        }

        stack.pop(); // extensions
        stack.pop(); // integer

    }

    public @NotNull SimpleVersion getMaxInstanceVulkanApiVersion() {
        if(maxInstanceVulkanApiVersion == null) {
            throw new IllegalStateException("call load();");
        }
        return maxInstanceVulkanApiVersion;
    }

    public @NotNull VulkanExtension @NotNull [] getAvailableInstanceExtensions() {
        if(availableInstanceExtensions == null) {
            throw new IllegalStateException("call load();");
        }
        return availableInstanceExtensions;
    }

    public @NotNull VulkanExtension @NotNull [] getGlfwRequiredInstanceExtensions() {
        if(glfwRequiredInstanceExtensions == null) {
            throw new IllegalStateException("call load();");
        }
        return glfwRequiredInstanceExtensions;
    }
}
