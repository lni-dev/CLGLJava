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

package de.linusdev.ljgel.engine.vk.selector;

import de.linusdev.ljgel.engine.exception.EngineException;
import de.linusdev.ljgel.engine.vk.extension.VulkanExtension;
import de.linusdev.ljgel.nat.glfw3.GLFW;
import de.linusdev.ljgel.nat.vulkan.VulkanApiVersion;
import de.linusdev.ljgel.nat.vulkan.structs.VkExtensionProperties;
import de.linusdev.ljgel.nat.vulkan.utils.VulkanApiVersionUtils;
import de.linusdev.llog.LLog;
import de.linusdev.llog.base.LogInstance;
import de.linusdev.lutils.math.vector.buffer.intn.BBUInt1;
import de.linusdev.lutils.nat.memory.stack.Stack;
import de.linusdev.lutils.nat.pointer.BBTypedPointer64;
import de.linusdev.lutils.nat.pointer.Pointer64;
import de.linusdev.lutils.nat.string.NullTerminatedUTF8String;
import de.linusdev.lutils.nat.struct.array.StructureArray;
import de.linusdev.lutils.nat.struct.utils.BufferUtils;
import de.linusdev.lutils.version.SimpleVersion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

import static de.linusdev.ljgel.nat.vulkan.utils.VulkanNonInstanceMethods.vkEnumerateInstanceExtensionProperties;
import static de.linusdev.ljgel.nat.vulkan.utils.VulkanNonInstanceMethods.vkEnumerateInstanceVersion;
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
            VkExtensionProperties extension = extensions.get(i);
            availableInstanceExtensions[i] = VulkanExtension.of(extension.extensionName.get(), extension.specVersion.get());
        }

        StructureArray<BBTypedPointer64<NullTerminatedUTF8String>> reqExtensions = GLFW.glfwGetRequiredInstanceExtensions(integer);

        glfwRequiredInstanceExtensions = new VulkanExtension[reqExtensions.length()];
        for (int i = 0; i < reqExtensions.length(); i++) {
            Pointer64 pName = reqExtensions.get(i);
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

    /*
     Check methods
     */

    private final static @NotNull LogInstance LOG = LLog.getLogInstance();

    public void isVulkanApiVersionAvailable(@NotNull VulkanApiVersion version) throws EngineException {
        LOG.debug("Checking vulkan api version: " + version + ", maxAvailableApiVersion: " + getMaxInstanceVulkanApiVersion());
        if(getMaxInstanceVulkanApiVersion().compareTo(version) < 0)
            throw new EngineException(version + " is not supported. Maximum supported is: " + getMaxInstanceVulkanApiVersion());

    }

    public void areInstanceExtensionsAvailable(@NotNull List<VulkanExtension> extensions) throws EngineException {
        LOG.debug("Checking if the following instance extensions are available: " + extensions);

        loop: for (VulkanExtension reqExt : extensions) {
            for (VulkanExtension availableExt : getAvailableInstanceExtensions()) {
                if (VulkanExtension.isSufficient(reqExt, availableExt)) {
                    continue loop;
                }
            }

            throw new EngineException("Instance extension '" +  reqExt + "' is not available. available extensions: " + Arrays.toString(getAvailableInstanceExtensions()));
        }
    }
}
