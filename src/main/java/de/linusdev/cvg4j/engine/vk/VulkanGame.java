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

package de.linusdev.cvg4j.engine.vk;

import de.linusdev.cvg4j.engine.info.Game;
import de.linusdev.cvg4j.engine.vk.extension.VulkanExtension;
import de.linusdev.cvg4j.engine.vk.selector.gpu.VulkanGPUSelector;
import de.linusdev.cvg4j.engine.vk.selector.present.mode.PresentModeSelector;
import de.linusdev.cvg4j.engine.vk.selector.priority.Priorities;
import de.linusdev.cvg4j.engine.vk.selector.priority.Priority;
import de.linusdev.cvg4j.engine.vk.selector.queue.family.QueueFamilySelector;
import de.linusdev.cvg4j.engine.vk.selector.surface.format.SurfaceFormatSelector;
import de.linusdev.cvg4j.nat.vulkan.VulkanApiVersion;
import de.linusdev.cvg4j.nat.vulkan.bitmasks.enums.VkQueueFlagBits;
import de.linusdev.cvg4j.nat.vulkan.constants.APIConstants;
import de.linusdev.cvg4j.nat.vulkan.debug.callback.DebugListener;
import de.linusdev.cvg4j.nat.vulkan.debug.callback.VulkanNatDebugUtilsMessageCallback;
import de.linusdev.cvg4j.nat.vulkan.enums.VkColorSpaceKHR;
import de.linusdev.cvg4j.nat.vulkan.enums.VkFormat;
import de.linusdev.cvg4j.nat.vulkan.enums.VkPhysicalDeviceType;
import de.linusdev.cvg4j.nat.vulkan.enums.VkPresentModeKHR;
import de.linusdev.llog.base.LogLevel;
import de.linusdev.lutils.nat.memory.stack.Stack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.List;

public interface VulkanGame extends Game {

    /**
     * Minimum {@link VulkanApiVersion} this game requires.
     */
    @NotNull VulkanApiVersion minRequiredInstanceVersion();

    /**
     * Instance extensions this game requires. Extensions required by glfw do not have to be contained in this list.
     */
    @NotNull List<VulkanExtension> requiredInstanceExtensions();

    /**
     * These are not checked if they actually exist. It is better to activate vulkan layers globally
     * using the vulkan configurator.
     */
    @NotNull List<String> activatedVulkanLayers();

    /**
     * If this returns {@code true}, a validation layer callback will be set,
     * that {@link de.linusdev.llog.base.LogInstance#log(LogLevel, String) logs}  all validation messages.
     * If this returns {@code true}, the vulkan extension 
     * {@value  APIConstants.VK_EXT_debug_utils#VK_EXT_DEBUG_UTILS_EXTENSION_NAME} will be automatically enabled.
     * @see VulkanNatDebugUtilsMessageCallback#addDebugListener(Stack, DebugListener) 
     */
    boolean logValidationLayerMessages();

    /**
     * How many images the swap chain should contain. By default, returns {@code min(min + 1, max)}, because
     * it is good to require an additional image, so we can write to an image while the graphics card driver uses the other
     * images.
     * @param min min image count
     * @param max max image count, may be very high
     * @return value between min and max
     */
    default int swapChainImageCount(@Range(from = 1, to = Integer.MAX_VALUE) int min, @Range(from = 1, to = Integer.MAX_VALUE) int max) {
        return Math.min(min + 1, max);
    }

    default @NotNull SurfaceFormatSelector surfaceFormatSelector() {

        // Ideally we want the format B8G8R8A8_SRGB with the color space SRGB_NONLINEAR_KHR,
        // but we also allow any other
        return SurfaceFormatSelector.builder()
                .add(null, null, Priorities.DO_NOT_CARE) // allow any
                .add(VkFormat.B8G8R8A8_SRGB, VkColorSpaceKHR.SRGB_NONLINEAR_KHR, Priorities.LOW) // prioritize this one
                .build();
    }

    default @NotNull PresentModeSelector presentModeSelector() {

        // Present mode MAILBOX_KHR is best!
        // but allow any other ...
        return PresentModeSelector.builder()
                .add(null, Priorities.DO_NOT_CARE) // allow any
                .add(VkPresentModeKHR.MAILBOX_KHR, Priorities.MEDIUM) // prioritize this one
                .add(VkPresentModeKHR.FIFO_KHR, Priorities.VERY_LOW) // still better than the others
                .build();
    }

    default @NotNull QueueFamilySelector queueFamilySelector() {
        return QueueFamilySelector.builder()
                .setGraphicsQueueEvaluator(queueFamilyInfo -> {
                    // We need at least one graphics queue
                    // It's good if the graphics and present queue is the same
                    if(queueFamilyInfo.props().queueFlags.isSet(VkQueueFlagBits.GRAPHICS)) {
                        if(queueFamilyInfo.supportsSurface()) return Priorities.LOW;
                        else return Priorities.VERY_LOW;
                    }
                    return Priorities.UNSUPPORTED;
                })
                .setPresentationQueueEvaluator(queueFamilyInfo -> {
                    // We need at least one present queue
                    // It's good if the graphics and present queue is the same
                    if(queueFamilyInfo.supportsSurface()) {
                        if(queueFamilyInfo.props().queueFlags.isSet(VkQueueFlagBits.GRAPHICS)) return Priorities.LOW;
                        else return Priorities.VERY_LOW;
                    }
                    return Priorities.UNSUPPORTED;
                }).build();
    }

    default @NotNull List<VulkanExtension> requiredDeviceExtensions() {
        return List.of(VulkanExtension.of(APIConstants.VK_KHR_swapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME));
    }

    default @NotNull VulkanGPUSelector gpuSelector() {
        return VulkanGPUSelector.builder()
                // Priority with which every gpu starts
                .setStartPriority(Priority.of(1000))

                // No max priority
                .setMaxPriority(Priority.of(Integer.MAX_VALUE))

                // Prioritize discrete gpus; Lower priority for integrated gpus; Disallow cpus
                .deviceType().equals(VkPhysicalDeviceType.DISCRETE_GPU).thenAdd(Priorities.HIGH)
                .deviceType().equals(VkPhysicalDeviceType.INTEGRATED_GPU).thenSubtract(Priorities.LOW)
                .deviceType().equals(VkPhysicalDeviceType.CPU).thenUnsupported()

                // Require the swap-chain extension
                .extensions().not().sufficesAll(requiredDeviceExtensions()).thenUnsupported()

                // It's good to have at least two images available (for swapping them)
                .custom(info -> info.surfaceInfo().surfacesCaps().maxImageCount.get() == 0 || info.surfaceInfo().surfacesCaps().maxImageCount.get() >= 2).thenAdd(Priorities.HIGH)

                // use given surface format and present mode selectors
                .surfaceFormat(surfaceFormatSelector()).thenUnsupportedIfNegativeAndAdd()
                .presentMode(presentModeSelector()).thenUnsupportedIfNegativeAndAdd()


                .queueFamilies(
                        queueFamilySelector(),
                        (priorityGraphics, priorityPresent) -> {

                            if (priorityGraphics.isNegative() || priorityPresent.isNegative())
                                return Priorities.UNSUPPORTED; // We need at least one graphics queue and one present queue
                            return Priority.of(priorityGraphics.priority() + priorityPresent.priority());

                        }
                ).thenUnsupportedIfNegativeAndAdd()

                // Ideally we find a single queue that supports both graphics and presentation
                .custom(info -> info.queueFamilyInfoList().stream().anyMatch(fam ->
                        fam.props().queueFlags.isSet(VkQueueFlagBits.GRAPHICS) && fam.supportsSurface())
                ).thenAdd(Priorities.MEDIUM)
                .build();
    }

}
