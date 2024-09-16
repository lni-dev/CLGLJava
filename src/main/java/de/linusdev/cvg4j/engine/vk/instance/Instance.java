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

package de.linusdev.cvg4j.engine.vk.instance;

import de.linusdev.cvg4j.engine.Engine;
import de.linusdev.cvg4j.engine.exception.EngineException;
import de.linusdev.cvg4j.engine.vk.VulkanGame;
import de.linusdev.cvg4j.engine.vk.extension.VulkanExtension;
import de.linusdev.cvg4j.engine.vk.extension.VulkanExtensionList;
import de.linusdev.cvg4j.engine.vk.selector.VulkanEngineInfo;
import de.linusdev.cvg4j.nat.vulkan.bitmasks.enums.VkDebugUtilsMessageSeverityFlagBitsEXT;
import de.linusdev.cvg4j.nat.vulkan.constants.APIConstants;
import de.linusdev.cvg4j.nat.vulkan.debug.callback.VulkanNatDebugUtilsMessageCallback;
import de.linusdev.cvg4j.nat.vulkan.enums.VkStructureType;
import de.linusdev.cvg4j.nat.vulkan.handles.VkInstance;
import de.linusdev.cvg4j.nat.vulkan.structs.VkApplicationInfo;
import de.linusdev.cvg4j.nat.vulkan.structs.VkInstanceCreateInfo;
import de.linusdev.cvg4j.nat.vulkan.utils.VulkanVersionUtils;
import de.linusdev.llog.LLog;
import de.linusdev.llog.base.LogInstance;
import de.linusdev.llog.base.impl.StandardLogLevel;
import de.linusdev.lutils.nat.memory.stack.Stack;
import de.linusdev.lutils.nat.pointer.BBTypedPointer64;
import de.linusdev.lutils.nat.string.NullTerminatedUTF8String;
import de.linusdev.lutils.nat.struct.array.StructureArray;
import de.linusdev.lutils.nat.struct.utils.BufferUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static de.linusdev.cvg4j.engine.vk.VulkanEngine.LOG;
import static de.linusdev.cvg4j.nat.vulkan.utils.VulkanNonInstanceMethods.vkCreateInstance;
import static de.linusdev.lutils.nat.pointer.Pointer64.refL;
import static de.linusdev.lutils.nat.pointer.TypedPointer64.ref;
import static de.linusdev.lutils.nat.struct.abstracts.Structure.allocate;

public class Instance implements AutoCloseable {

    private final @NotNull VkInstance vkInstance;
    private @Nullable VulkanNatDebugUtilsMessageCallback debugMsgCallback;

    public Instance(
            @NotNull Stack stack,
            @NotNull VulkanGame game,
            @NotNull VulkanEngineInfo vulkanInfo
    ) throws EngineException {
        this.vkInstance = allocate(new VkInstance());
        create(stack, game, vulkanInfo);
    }

    public void create(
            @NotNull Stack stack,
            @NotNull VulkanGame game,
            @NotNull VulkanEngineInfo vulkanInfo
    ) throws EngineException {
        LOG.debug("Start creating VkInstance.");

        VulkanExtensionList extensions = new VulkanExtensionList();

        try (var ignored = stack.popPoint()) {
            vulkanInfo.load(stack);

            // Check minRequiredInstanceVersion
            vulkanInfo.isVulkanApiVersionAvailable(game.minRequiredInstanceVersion());
            vulkanInfo.areInstanceExtensionsAvailable(game.requiredInstanceExtensions());

            if (game.logValidationLayerMessages()) {
                VulkanExtension debugMsgExt = VulkanExtension.of(APIConstants.VK_EXT_debug_utils.VK_EXT_DEBUG_UTILS_EXTENSION_NAME);
                vulkanInfo.areInstanceExtensionsAvailable(List.of(debugMsgExt));
                extensions.add(debugMsgExt);
            }

            // add all required vulkan extensions
            extensions.addAll(game.requiredInstanceExtensions());
            extensions.addAll(vulkanInfo.getGlfwRequiredInstanceExtensions());

            // VkApplicationInfo
            NullTerminatedUTF8String appName = stack.pushString(game.name());
            NullTerminatedUTF8String engineName = stack.pushString(Engine.name());

            VkApplicationInfo vkApplicationInfo = stack.push(new VkApplicationInfo());
            vkApplicationInfo.sType.set(VkStructureType.APPLICATION_INFO);
            vkApplicationInfo.pNext.set(0);
            vkApplicationInfo.pApplicationName.set(appName);
            vkApplicationInfo.applicationVersion.set(VulkanVersionUtils.makeVersion(game.version().version()));
            vkApplicationInfo.pEngineName.set(engineName);
            vkApplicationInfo.engineVersion.set(VulkanVersionUtils.makeVersion(Engine.version().version()));
            vkApplicationInfo.apiVersion.set(game.minRequiredInstanceVersion().getAsInt());
            LOG.log(StandardLogLevel.DATA, "VkApplicationInfo: " + vkApplicationInfo);

            // VkInstanceCreateInfo
            StructureArray<BBTypedPointer64<NullTerminatedUTF8String>> enabledExtensionsNatArray = extensions.toNativeArray(stack::pushArray, stack::pushString);
            @Nullable StructureArray<BBTypedPointer64<NullTerminatedUTF8String>> enabledLayersNatArray = null;

            if (!game.activatedVulkanLayers().isEmpty()) {
                enabledLayersNatArray = stack.pushArray(game.activatedVulkanLayers().size(), BBTypedPointer64.class, BBTypedPointer64::newUnallocated1);
                int i = 0;
                for (String ext : game.activatedVulkanLayers())
                    enabledLayersNatArray.get(i++).set(stack.pushString(ext));
            }


            VkInstanceCreateInfo vkInstanceCreateInfo = stack.push(new VkInstanceCreateInfo());
            vkInstanceCreateInfo.sType.set(VkStructureType.INSTANCE_CREATE_INFO);
            vkInstanceCreateInfo.pNext.set(0);
            vkInstanceCreateInfo.pApplicationInfo.set(vkApplicationInfo);
            vkInstanceCreateInfo.enabledExtensionCount.set(enabledExtensionsNatArray.length());
            vkInstanceCreateInfo.ppEnabledExtensionNames.set(enabledExtensionsNatArray.getPointer());
            vkInstanceCreateInfo.enabledLayerCount.set(enabledLayersNatArray == null ? 0 : enabledLayersNatArray.length());
            vkInstanceCreateInfo.ppEnabledLayerNames.set(refL(enabledLayersNatArray));
            vkInstanceCreateInfo.flags.set(0);
            LOG.log(StandardLogLevel.DATA, "VkInstanceCreateInfo: " + vkInstanceCreateInfo);

            // Create VkInstance
            vkCreateInstance(vkInstanceCreateInfo, null, vkInstance).check();
            vkInstance.initMethodPointers();
        }

        LOG.debug("Finished creating VkInstance.");

        if(game.logValidationLayerMessages())
            enableVulkanValidationDebugListener(stack);

    }

    public void enableVulkanValidationDebugListener(@NotNull Stack stack) {
        debugMsgCallback = new VulkanNatDebugUtilsMessageCallback(vkInstance);
        LogInstance log = LLog.getLogInstance("VkValidation", null);
        debugMsgCallback.addDebugListener(stack, (messageSeverity, messageType, callbackData) -> {
            switch (messageSeverity.get(VkDebugUtilsMessageSeverityFlagBitsEXT.class)) {
                case VK_DEBUG_UTILS_MESSAGE_SEVERITY_VERBOSE_EXT ->
                        log.debug(BufferUtils.readNullTerminatedUtf8String(callbackData.pMessage.get()));
                case VK_DEBUG_UTILS_MESSAGE_SEVERITY_INFO_EXT ->
                        log.info(BufferUtils.readNullTerminatedUtf8String(callbackData.pMessage.get()));
                case VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_EXT ->
                        log.warning(BufferUtils.readNullTerminatedUtf8String(callbackData.pMessage.get()));
                case VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_EXT ->
                        log.throwable(new Exception(BufferUtils.readNullTerminatedUtf8String(callbackData.pMessage.get())));
            }
        });
    }

    public @NotNull VkInstance getVkInstance() {
        return vkInstance;
    }

    @Override
    public void close() {
        if(debugMsgCallback != null)
            debugMsgCallback.close();
        vkInstance.vkDestroyInstance(ref(null));
    }
}
