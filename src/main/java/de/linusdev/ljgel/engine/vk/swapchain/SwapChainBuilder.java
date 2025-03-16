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

package de.linusdev.ljgel.engine.vk.swapchain;

import de.linusdev.ljgel.engine.exception.EngineException;
import de.linusdev.ljgel.engine.vk.device.Device;
import de.linusdev.ljgel.engine.vk.window.VulkanWindow;
import de.linusdev.ljgel.nat.vulkan.bitmasks.enums.VkSurfaceTransformFlagBitsKHR;
import de.linusdev.ljgel.nat.vulkan.enums.VkColorSpaceKHR;
import de.linusdev.ljgel.nat.vulkan.enums.VkFormat;
import de.linusdev.ljgel.nat.vulkan.enums.VkPresentModeKHR;
import de.linusdev.ljgel.nat.vulkan.handles.VkInstance;
import de.linusdev.ljgel.nat.vulkan.structs.VkSurfaceFormatKHR;
import de.linusdev.llog.LLog;
import de.linusdev.llog.base.LogInstance;
import de.linusdev.lutils.nat.enums.EnumValue32;
import de.linusdev.lutils.nat.memory.stack.Stack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * All allocated structures, which are set with the representing {@code set...(...)} method
 * may be freed after this builder is not used anymore.
 */
@SuppressWarnings("UnusedReturnValue")
public class SwapChainBuilder {

    private final @NotNull LogInstance LOG = LLog.getLogInstance();

    private @Nullable VkSurfaceFormatKHR surfaceFormat = null;
    private @Nullable EnumValue32<VkPresentModeKHR> presentMode = null;
    private @Nullable Extend2D swapExtend = null;
    private @Nullable Integer swapChainImageCount = null;
    private @Nullable EnumValue32<VkSurfaceTransformFlagBitsKHR> surfaceTransform = null;

    public SwapChainBuilder() {
        LOG.debug("Start building SwapChain.");
    }

    public SwapChainBuilder setSurfaceFormat(
            @Nullable VkSurfaceFormatKHR surfaceFormat
    ) throws EngineException {
        this.surfaceFormat = surfaceFormat;

        if(surfaceFormat == null)
            throw new EngineException("SurfaceFormatSelector could not select a suitable format.");

        LOG.debug("Surface format chosen: format=" + surfaceFormat.format.get(VkFormat.class)
                + ", colorSpace=" + surfaceFormat.colorSpace.get(VkColorSpaceKHR.class)
        );

        return this;
    }

    public SwapChainBuilder setPresentMode(@Nullable EnumValue32<VkPresentModeKHR> presentMode) throws EngineException {
        this.presentMode = presentMode;

        if(presentMode == null)
            throw new EngineException("PresentModeSelector could not select a suitable mode.");

        LOG.debug("Present mode chosen: " + presentMode.get());

        return this;
    }

    public SwapChainBuilder setSwapExtend(@NotNull Extend2D swapExtend) {
        this.swapExtend = swapExtend;
        LOG.debug("Swap extend chosen: width=" + swapExtend.width() + ", height=" + swapExtend.height());

        return this;
    }

    public SwapChainBuilder setSwapChainImageCount(@NotNull Integer swapChainImageCount) {
        this.swapChainImageCount = swapChainImageCount;
        LOG.debug("Swap chain image count chosen: " + swapChainImageCount);

        return this;
    }

    public SwapChainBuilder setSurfaceTransform(@NotNull EnumValue32<VkSurfaceTransformFlagBitsKHR> surfaceTransform) {
        this.surfaceTransform = surfaceTransform;
        LOG.debug("Surface transform chosen: " + surfaceTransform.get());

        return this;
    }

    /**
     *
     * @return {@code true} if the area represented by {@link #swapExtend} is zero.
     */
    public boolean isExtendAreaZero() {
        if(swapExtend == null)
            throw new Error("swapExtend is null (call set...).");
        return swapExtend.width() == 0 || swapExtend.height() == 0;
    }

    public @NotNull SwapChain buildSwapChain(
            @NotNull Stack stack,
            @NotNull VulkanWindow window,
            @NotNull VkInstance vkInstance,
            @NotNull Device device
    ) throws EngineException {

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

        return SwapChain.create(
                stack, vkInstance, window, device, swapChainImageCount,
                surfaceFormat.format, surfaceFormat.colorSpace, swapExtend, surfaceTransform, presentMode
        );
    }

    public void recreateSwapChain(
            @NotNull Stack stack,
            @NotNull SwapChain swapChain
    ) {
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

        swapChain.recreate(stack, surfaceFormat.format, surfaceFormat.colorSpace, swapExtend, surfaceTransform, presentMode);
    }
}
