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

package de.linusdev.cvg4j.nengine.vulkan.device;

import de.linusdev.cvg4j.nat.vulkan.enums.VkColorSpaceKHR;
import de.linusdev.cvg4j.nat.vulkan.enums.VkFormat;
import de.linusdev.cvg4j.nat.vulkan.enums.VkPresentModeKHR;
import de.linusdev.cvg4j.nat.vulkan.structs.VkExtent2D;
import de.linusdev.cvg4j.nat.vulkan.structs.VkSurfaceFormatKHR;
import de.linusdev.cvg4j.nengine.exception.EngineException;
import de.linusdev.llog.LLog;
import de.linusdev.llog.base.LogInstance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class VkDeviceBuilder {

    private final @NotNull LogInstance LOG = LLog.getLogInstance();

    private @Nullable VkSurfaceFormatKHR surfaceFormat = null;
    private @Nullable VkPresentModeKHR presentMode = null;
    private @Nullable VkExtent2D swapExtend = null;
    private @Nullable Integer swapChainImageCount = null;
    private @Nullable Integer surfaceTransform = null;
    private @Nullable Integer graphicsQueueIndex = null;
    private @Nullable Integer presentationQueueIndex = null;

    public VkDeviceBuilder() {
        LOG.logDebug("Start building VkDevice.");
    }

    public VkDeviceBuilder setSurfaceFormat(
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

    public VkDeviceBuilder setPresentMode(@Nullable VkPresentModeKHR presentMode) throws EngineException {
        this.presentMode = presentMode;

        if(presentMode == null)
            throw new EngineException("PresentModeSelector could not select a suitable mode.");

        LOG.logDebug("Present mode chosen: " + presentMode);

        return this;
    }

    public VkDeviceBuilder setSwapExtend(@NotNull VkExtent2D swapExtend) {
        this.swapExtend = swapExtend;
        LOG.logDebug("Swap extend chosen: width=" + swapExtend.width.get() + ", height=" + swapExtend.height.get());

        return this;
    }

    public void setSwapChainImageCount(@NotNull Integer swapChainImageCount) {
        this.swapChainImageCount = swapChainImageCount;
        LOG.logDebug("Swap chain image count chosen: " + swapChainImageCount);
    }

    public void setSurfaceTransform(@NotNull Integer surfaceTransform) {
        this.surfaceTransform = surfaceTransform;
    }

    public void setGraphicsQueueIndex(@Nullable Integer graphicsQueueIndex) {
        this.graphicsQueueIndex = graphicsQueueIndex;
    }

    public void setPresentationQueueIndex(@Nullable Integer presentationQueueIndex) {
        this.presentationQueueIndex = presentationQueueIndex;
    }
}
