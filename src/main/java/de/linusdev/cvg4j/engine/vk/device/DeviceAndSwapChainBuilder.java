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

package de.linusdev.cvg4j.engine.vk.device;

import de.linusdev.cvg4j.nat.vulkan.bitmasks.enums.VkSurfaceTransformFlagBitsKHR;
import de.linusdev.cvg4j.nat.vulkan.enums.*;
import de.linusdev.cvg4j.nat.vulkan.handles.*;
import de.linusdev.cvg4j.nat.vulkan.structs.*;
import de.linusdev.cvg4j.engine.exception.EngineException;
import de.linusdev.cvg4j.engine.vk.VulkanGame;
import de.linusdev.cvg4j.engine.vk.VulkanRasterizationWindow;
import de.linusdev.cvg4j.engine.vk.swapchain.SwapChain;
import de.linusdev.llog.LLog;
import de.linusdev.llog.base.LogInstance;
import de.linusdev.lutils.nat.enums.EnumValue32;
import de.linusdev.lutils.nat.memory.Stack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * All allocated structures, which are set with the representing {@code set...(...)} method
 * may be freed after this builder is not used anymore.
 */
@SuppressWarnings("UnusedReturnValue")
public class DeviceAndSwapChainBuilder {

    private final @NotNull LogInstance LOG = LLog.getLogInstance();

    private @Nullable VkPhysicalDevice vkPhysicalDevice = null;
    private @Nullable VkSurfaceFormatKHR surfaceFormat = null;
    private @Nullable EnumValue32<VkPresentModeKHR> presentMode = null;
    private @Nullable Extend2D swapExtend = null;
    private @Nullable Integer swapChainImageCount = null;
    private @Nullable EnumValue32<VkSurfaceTransformFlagBitsKHR> surfaceTransform = null;
    private @Nullable Integer graphicsQueueIndex = null;
    private @Nullable Integer presentationQueueIndex = null;

    public DeviceAndSwapChainBuilder() {
        LOG.logDebug("Start building VkDevice.");
    }

    public DeviceAndSwapChainBuilder setVkPhysicalDevice(@Nullable VkPhysicalDevice vkPhysicalDevice) {
        this.vkPhysicalDevice = vkPhysicalDevice;
        return this;
    }

    public DeviceAndSwapChainBuilder setSurfaceFormat(
            @Nullable VkSurfaceFormatKHR surfaceFormat
    ) throws EngineException {
        this.surfaceFormat = surfaceFormat;

        if(surfaceFormat == null)
            throw new EngineException("SurfaceFormatSelector could not select a suitable format.");

        LOG.logDebug("Surface format chosen: format=" + surfaceFormat.format.get(VkFormat.class)
                + ", colorSpace=" + surfaceFormat.colorSpace.get(VkColorSpaceKHR.class)
        );

        return this;
    }

    public DeviceAndSwapChainBuilder setPresentMode(@Nullable EnumValue32<VkPresentModeKHR> presentMode) throws EngineException {
        this.presentMode = presentMode;

        if(presentMode == null)
            throw new EngineException("PresentModeSelector could not select a suitable mode.");

        LOG.logDebug("Present mode chosen: " + presentMode.get());

        return this;
    }

    public DeviceAndSwapChainBuilder setSwapExtend(@NotNull Extend2D swapExtend) {
        this.swapExtend = swapExtend;
        LOG.logDebug("Swap extend chosen: width=" + swapExtend.width() + ", height=" + swapExtend.height());

        return this;
    }

    public DeviceAndSwapChainBuilder setSwapChainImageCount(@NotNull Integer swapChainImageCount) {
        this.swapChainImageCount = swapChainImageCount;
        LOG.logDebug("Swap chain image count chosen: " + swapChainImageCount);

        return this;
    }

    public DeviceAndSwapChainBuilder setSurfaceTransform(@NotNull EnumValue32<VkSurfaceTransformFlagBitsKHR> surfaceTransform) {
        this.surfaceTransform = surfaceTransform;
        LOG.logDebug("Surface transform chosen: " + surfaceTransform.get());

        return this;
    }

    public DeviceAndSwapChainBuilder setGraphicsQueueIndex(@Nullable Integer graphicsQueueIndex) {
        this.graphicsQueueIndex = graphicsQueueIndex;
        LOG.logDebug("Graphics queue index chosen: " + graphicsQueueIndex);

        return this;
    }

    public DeviceAndSwapChainBuilder setPresentationQueueIndex(@Nullable Integer presentationQueueIndex) {
        this.presentationQueueIndex = presentationQueueIndex;
        LOG.logDebug("Presentation queue index chosen: " + presentationQueueIndex);

        return this;
    }

    /**
     *
     * @param stack stack to use
     * @param game game
     * @param vkInstance instance, must remain valid until cleanup
     * @return {@link Device}
     */
    public @NotNull Device buildDevice(
            @NotNull Stack stack,
            @NotNull VulkanGame game,
            @NotNull VkInstance vkInstance
    ) {
        if(graphicsQueueIndex == null || presentationQueueIndex == null)
            throw new Error("graphics or presentation queue missing (call set...).");

        if(vkPhysicalDevice == null)
            throw new Error("vkPhysicalDevice missing (call set...).");


        Device device = Device.create(
                stack, vkInstance, vkPhysicalDevice,
                graphicsQueueIndex, presentationQueueIndex,
                game.requiredDeviceExtensions(),game.activatedVulkanLayers()
        );

        LOG.logDebug("Device created.");

        return device;
    }

    public @NotNull SwapChain buildSwapChain(
            @NotNull Stack stack,
            @NotNull VulkanRasterizationWindow window,
            @NotNull VkInstance vkInstance,
            @NotNull Device device
    ) {

        if(graphicsQueueIndex == null || presentationQueueIndex == null)
            throw new Error("graphics or presentation queue missing (call set...).");

        if(swapChainImageCount == null)
            throw new Error("swapChainImageCount is null (call set...).");

        if(surfaceFormat == null)
            throw new Error("surfaceFormat is null (call set...).");

        if(swapExtend == null)
            throw new Error("swapExtend is null (call set...).");

        if(surfaceTransform == null)
            throw new Error("surfaceTransform is null (call set...).");

        if(presentMode == null)
            throw new Error("presentMode is null (call set...).");

        SwapChain swapChain = SwapChain.create(
                stack, vkInstance, window, device, graphicsQueueIndex, presentationQueueIndex, swapChainImageCount,
                surfaceFormat.format, surfaceFormat.colorSpace, swapExtend, surfaceTransform, presentMode
        );

        LOG.logDebug("Swapchain created");

        return swapChain;
    }
}
