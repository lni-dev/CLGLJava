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

import de.linusdev.cvg4j.engine.scene.Scene;
import de.linusdev.cvg4j.engine.vk.device.Extend2D;
import de.linusdev.cvg4j.engine.vk.pipeline.RasterizationPipeLine;
import de.linusdev.cvg4j.engine.vk.pipeline.RasterizationPipelineInfo;
import de.linusdev.cvg4j.nat.vulkan.handles.VkCommandBuffer;
import de.linusdev.cvg4j.nat.vulkan.handles.VkFramebuffer;
import de.linusdev.cvg4j.nat.vulkan.handles.VkInstance;
import de.linusdev.lutils.nat.memory.Stack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class VkScene<GAME extends VulkanGame> implements Scene {

    protected final @NotNull VulkanEngine<GAME> engine;

    protected RasterizationPipeLine pipeLine;

    protected VkScene(@NotNull VulkanEngine<GAME> engine) {
        this.engine = engine;
    }

    abstract void render(
            @NotNull Stack stack,
            @NotNull VkInstance vkInstance,
            @NotNull Extend2D extend,
            int frameBufferIndex,
            @NotNull VkCommandBuffer commandBuffer,
            @NotNull VkFramebuffer frameBuffer
    ) ;

    abstract @NotNull RasterizationPipelineInfo pipeline(@NotNull Stack stack);

    @ApiStatus.Internal
    public void setPipeLine(@Nullable RasterizationPipeLine pipeLine) {
        this.pipeLine = pipeLine;
    }

    @Override
    public void close() {
        if(pipeLine != null)
            pipeLine.close(); //TODO: this may require better synchronization
    }
}
