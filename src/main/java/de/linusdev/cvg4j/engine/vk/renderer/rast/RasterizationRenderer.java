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

package de.linusdev.cvg4j.engine.vk.renderer.rast;

import de.linusdev.cvg4j.engine.render.Renderer;
import de.linusdev.cvg4j.engine.vk.VulkanRasterizationWindow;
import de.linusdev.cvg4j.engine.vk.command.pool.GraphicsQueuePermanentCommandPool;
import de.linusdev.cvg4j.engine.vk.device.Device;
import de.linusdev.cvg4j.engine.vk.engine.VulkanWindow;
import de.linusdev.cvg4j.engine.vk.frame.buffer.FrameBuffers;
import de.linusdev.cvg4j.engine.vk.instance.Instance;
import de.linusdev.cvg4j.engine.vk.swapchain.SwapChain;
import de.linusdev.cvg4j.nat.glfw3.custom.UpdateListener;
import de.linusdev.cvg4j.nat.vulkan.bitmasks.VkCommandBufferResetFlags;
import de.linusdev.cvg4j.nat.vulkan.bitmasks.VkPipelineStageFlags;
import de.linusdev.cvg4j.nat.vulkan.handles.*;
import de.linusdev.cvg4j.nat.vulkan.structs.VkPresentInfoKHR;
import de.linusdev.cvg4j.nat.vulkan.structs.VkSubmitInfo;
import de.linusdev.lutils.math.vector.buffer.intn.BBUInt1;
import de.linusdev.lutils.nat.memory.stack.Stack;
import de.linusdev.lutils.nat.struct.array.StructureArray;
import de.linusdev.lutils.thread.var.SyncVar;
import de.linusdev.lutils.thread.var.SyncVarImpl;
import org.jetbrains.annotations.NotNull;

import static de.linusdev.lutils.nat.struct.abstracts.Structure.allocate;

public class RasterizationRenderer implements Renderer {

    private final @NotNull VulkanWindow window;
    private final @NotNull VkInstance vkInstance;
    private final @NotNull VkSurfaceKHR vkSurface;
    private Device device;
    private SwapChain swapChain;

    private VkQueue graphicsQueue;
    private VkQueue presentationQueue;

    /*
     * Information stored in this class
     */
    private int maxFramesInFlight;

    /*
     * Structs required during rendering
     */
    private int currentFrame = 0;
    private final @NotNull BBUInt1 currentImageIndex;
    private final @NotNull VkCommandBufferResetFlags commandBufferResetFlags;
    private final @NotNull VkPipelineStageFlags pipelineStageFlags;
    private final @NotNull VkSubmitInfo submitInfo;
    private final @NotNull VkPresentInfoKHR presentInfo;
    private final @NotNull VkFence fenceNullHandle;

    /*
     * Managed by this class
     */
    private FrameBuffers frameBuffers;
    private GraphicsQueuePermanentCommandPool commandPool;

    private StructureArray<VkSemaphore> imageAvailableSemaphores;
    private StructureArray<VkSemaphore> renderFinishedSemaphores;
    private StructureArray<VkFence> frameSubmittedFences;


    /*
     * Other stuff
     */
    private VulkanRasterizationWindow.RenderCommandsFunction renderCommandsFunction;
    private UpdateListener updateListener;

    /*
     * Event related booleans
     */
    private final @NotNull SyncVar<@NotNull Boolean> recreateSwapChain = new SyncVarImpl<>(false);
    private final @NotNull SyncVar<@NotNull Boolean> minimized = new SyncVarImpl<>(false);

    public RasterizationRenderer(
            @NotNull Instance instance,
            @NotNull VulkanWindow window
    ) {
        this.vkInstance = instance.getVkInstance();
        this.vkSurface = window.getVkSurface();
        this.window = window;

        this.currentImageIndex = allocate(BBUInt1.newAllocatable(null));
        this.commandBufferResetFlags = allocate(new VkCommandBufferResetFlags());
        this.pipelineStageFlags = allocate(new VkPipelineStageFlags());
        this.submitInfo = allocate(new VkSubmitInfo());
        this.presentInfo = allocate(new VkPresentInfoKHR());

        this.fenceNullHandle = allocate(new VkFence());

        window.listeners().addFramebufferSizeListener((width, height) -> recreateSwapChain.set(true));
        window.listeners().addWindowIconificationListener(minimized::set);
    }

    @Override
    public void render(@NotNull Stack stack) {

    }
}
