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

package de.linusdev.cvg4j.engine.vk.scene;

import de.linusdev.cvg4j.engine.ticker.Tickable;
import de.linusdev.cvg4j.engine.ticker.Ticker;
import de.linusdev.cvg4j.engine.vk.instance.Instance;
import de.linusdev.cvg4j.engine.vk.renderer.rast.RasterizationRenderer;
import de.linusdev.cvg4j.engine.vk.renderer.rast.RenderCommandsFunction;
import de.linusdev.cvg4j.engine.vk.swapchain.SwapChain;
import de.linusdev.cvg4j.nat.vulkan.handles.VkCommandBuffer;
import de.linusdev.cvg4j.nat.vulkan.handles.VkInstance;
import de.linusdev.lutils.nat.memory.stack.Stack;
import de.linusdev.lutils.thread.var.SyncVarImpl;
import org.jetbrains.annotations.NotNull;

public class SceneHolder extends SyncVarImpl<@NotNull VkScene<?>> implements Tickable, RenderCommandsFunction {

    private final @NotNull VkInstance vkInstance;
    private final @NotNull SwapChain swapChain;
    private final @NotNull RasterizationRenderer renderer;

    public SceneHolder(
            @NotNull VkScene<?> scene,
            @NotNull Instance instance,
            @NotNull SwapChain swapChain,
            @NotNull RasterizationRenderer renderer
    ) {
        super(scene);
        this.vkInstance = instance.getVkInstance();
        this.swapChain = swapChain;
        this.renderer = renderer;
    }

    @Override
    public void tick(@NotNull Ticker ticker) {
        VkScene<?> scene = get();
        scene.tick(ticker);
    }

    @Override
    public void render(@NotNull Stack stack, int currentFrameBufferImageIndex, int currentFrame, @NotNull VkCommandBuffer commandBuffer) {
        VkScene<?> scene = get();
        scene.render(stack, vkInstance, swapChain.getExtend(), currentFrameBufferImageIndex, currentFrame, commandBuffer, renderer.getFrameBuffers().getFrameBuffer(currentFrameBufferImageIndex));
    }
}
