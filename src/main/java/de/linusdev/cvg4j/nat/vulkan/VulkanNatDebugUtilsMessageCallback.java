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

import de.linusdev.cvg4j.nat.vulkan.bitmasks.enums.VkDebugUtilsMessageSeverityFlagBitsEXT;
import de.linusdev.cvg4j.nat.vulkan.bitmasks.enums.VkDebugUtilsMessageTypeFlagBitsEXT;
import de.linusdev.cvg4j.nat.vulkan.enums.VkStructureType;
import de.linusdev.cvg4j.nat.vulkan.handles.VkDebugUtilsMessengerEXT;
import de.linusdev.cvg4j.nat.vulkan.handles.VkInstance;
import de.linusdev.cvg4j.nat.vulkan.structs.VkDebugUtilsMessengerCallbackDataEXT;
import de.linusdev.cvg4j.nat.vulkan.structs.VkDebugUtilsMessengerCreateInfoEXT;
import de.linusdev.lutils.bitfield.IntBitfield;
import de.linusdev.lutils.nat.enums.EnumValue32;
import de.linusdev.lutils.nat.memory.stack.Stack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import static de.linusdev.lutils.nat.pointer.TypedPointer64.ref;

public class VulkanNatDebugUtilsMessageCallback {

    public interface DebugListener {
        void debug(
                @NotNull EnumValue32<VkDebugUtilsMessageSeverityFlagBitsEXT> messageSeverity,
                @NotNull IntBitfield<VkDebugUtilsMessageTypeFlagBitsEXT> messageType,
                @NotNull VkDebugUtilsMessengerCallbackDataEXT callbackData
        );
    }

    public static final @NotNull ArrayList<@NotNull DebugListener> listeners = new ArrayList<>(1);
    public static final @NotNull ArrayList<Long> messengers = new ArrayList<>(1);

    public static void addDebugListener(
            @NotNull Stack stack,
            @NotNull VkInstance vkInstance,
            @NotNull DebugListener listener
    ) {
        try(var ignored = stack.popPoint()) {
            int id = listeners.size();
            listeners.add(listener);

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

            VkDebugUtilsMessengerEXT messenger = stack.push(new VkDebugUtilsMessengerEXT());

            vkInstance.vkCreateDebugUtilsMessengerEXT(ref(createInfo), ref(null), ref(messenger)).check();

            messengers.add(messenger.get());
        }

    }

    public static void close(@NotNull Stack stack, @NotNull VkInstance vkInstance) {
        VkDebugUtilsMessengerEXT messenger = stack.push(new VkDebugUtilsMessengerEXT());

        for (Long handle : messengers) {
            messenger.set(handle);
            vkInstance.vkDestroyDebugUtilsMessengerEXT(messenger, ref(null));
        }
    }

    static {
        setCallbackClass(VulkanNatDebugUtilsMessageCallback.class);
    }

    private static native void setCallbackClass(@NotNull Class<?> callbackClass);

    private static native long getVulkanDebugCallbackFunPointer();

    private static boolean callback(
            int messageSeverity,
            int messageType,
            long pCallbackData,
            long pUserData
    ) {
        System.out.println("Callback!");

        //listeners.get((int) pUserData).debug();

        return false;
    }

}
