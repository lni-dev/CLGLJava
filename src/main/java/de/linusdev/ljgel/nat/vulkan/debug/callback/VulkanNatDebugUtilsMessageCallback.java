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

package de.linusdev.ljgel.nat.vulkan.debug.callback;

import de.linusdev.ljgel.nat.vulkan.bitmasks.enums.VkDebugUtilsMessageSeverityFlagBitsEXT;
import de.linusdev.ljgel.nat.vulkan.bitmasks.enums.VkDebugUtilsMessageTypeFlagBitsEXT;
import de.linusdev.ljgel.nat.vulkan.enums.VkStructureType;
import de.linusdev.ljgel.nat.vulkan.handles.VkDebugUtilsMessengerEXT;
import de.linusdev.ljgel.nat.vulkan.handles.VkInstance;
import de.linusdev.ljgel.nat.vulkan.structs.VkDebugUtilsMessengerCallbackDataEXT;
import de.linusdev.ljgel.nat.vulkan.structs.VkDebugUtilsMessengerCreateInfoEXT;
import de.linusdev.lutils.bitfield.IntBitfieldImpl;
import de.linusdev.lutils.nat.enums.JavaEnumValue32;
import de.linusdev.lutils.nat.memory.stack.Stack;
import de.linusdev.lutils.nat.struct.utils.BufferUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import static de.linusdev.lutils.nat.pointer.TypedPointer64.ref;
import static de.linusdev.lutils.nat.struct.abstracts.Structure.allocate;

public class VulkanNatDebugUtilsMessageCallback implements AutoCloseable {

    public static final int DATA_SIZE = allocate(new VkDebugUtilsMessengerCallbackDataEXT()).getRequiredSize();
    private final static @NotNull ArrayList<@NotNull DebugListener> LISTENERS = new ArrayList<>(1);

    private static native void setCallbackClass(@NotNull Class<?> callbackClass);

    private static native long getVulkanDebugCallbackFunPointer();

    @SuppressWarnings("unused") // Called natively only
    private static boolean callback(
            int messageSeverity,
            int messageType,
            long pCallbackData,
            long pUserData
    ) {

        var msgSeverity = new JavaEnumValue32<VkDebugUtilsMessageSeverityFlagBitsEXT>(messageSeverity);
        var msgType = new IntBitfieldImpl<VkDebugUtilsMessageTypeFlagBitsEXT>(messageType);

        VkDebugUtilsMessengerCallbackDataEXT data = new VkDebugUtilsMessengerCallbackDataEXT();
        data.claimBuffer(BufferUtils.getByteBufferFromPointer(pCallbackData, DATA_SIZE));

        LISTENERS.get((int) pUserData).debug(msgSeverity, msgType, data);

        return false;
    }

    static {
        setCallbackClass(VulkanNatDebugUtilsMessageCallback.class);
    }

    private final @NotNull VkInstance vkInstance;
    private final @NotNull ArrayList<VkDebugUtilsMessengerEXT> messengers = new ArrayList<>(1);

    public VulkanNatDebugUtilsMessageCallback(@NotNull VkInstance vkInstance) {
        this.vkInstance = vkInstance;
    }

    public void addDebugListener(
            @NotNull Stack stack,
            @NotNull DebugListener listener
    ) {
        try(var ignored = stack.popPoint()) {
            int id = LISTENERS.size();
            LISTENERS.add(listener);

            VkDebugUtilsMessengerCreateInfoEXT createInfo = stack.push(new VkDebugUtilsMessengerCreateInfoEXT());
            createInfo.sType.set(VkStructureType.DEBUG_UTILS_MESSENGER_CREATE_INFO_EXT);
            createInfo.messageSeverity.set(
                    VkDebugUtilsMessageSeverityFlagBitsEXT.VK_DEBUG_UTILS_MESSAGE_SEVERITY_VERBOSE_EXT,
                    VkDebugUtilsMessageSeverityFlagBitsEXT.VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_EXT,
                    VkDebugUtilsMessageSeverityFlagBitsEXT.VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_EXT,
                    VkDebugUtilsMessageSeverityFlagBitsEXT.VK_DEBUG_UTILS_MESSAGE_SEVERITY_INFO_EXT
            );
            createInfo.messageType.set(
                    VkDebugUtilsMessageTypeFlagBitsEXT.VK_DEBUG_UTILS_MESSAGE_TYPE_DEVICE_ADDRESS_BINDING_EXT,
                    VkDebugUtilsMessageTypeFlagBitsEXT.VK_DEBUG_UTILS_MESSAGE_TYPE_GENERAL_EXT,
                    VkDebugUtilsMessageTypeFlagBitsEXT.VK_DEBUG_UTILS_MESSAGE_TYPE_PERFORMANCE_EXT,
                    VkDebugUtilsMessageTypeFlagBitsEXT.VK_DEBUG_UTILS_MESSAGE_TYPE_VALIDATION_EXT
            );
            createInfo.pfnUserCallback.set(getVulkanDebugCallbackFunPointer());
            createInfo.pUserData.set(id);

            VkDebugUtilsMessengerEXT messenger = allocate(new VkDebugUtilsMessengerEXT());

            vkInstance.vkCreateDebugUtilsMessengerEXT(ref(createInfo), ref(null), ref(messenger)).check();

            messengers.add(messenger);
        }

    }

    public void close() {
        for (var handle : messengers)
            vkInstance.vkDestroyDebugUtilsMessengerEXT(handle, ref(null));
    }

}
