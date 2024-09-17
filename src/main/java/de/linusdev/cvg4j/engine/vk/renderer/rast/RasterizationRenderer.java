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

import de.linusdev.cvg4j.engine.vk.command.pool.GraphicsQueuePermanentCommandPool;
import de.linusdev.cvg4j.engine.vk.device.Device;
import de.linusdev.cvg4j.engine.vk.frame.buffer.FrameBuffers;
import de.linusdev.cvg4j.engine.vk.instance.Instance;
import de.linusdev.cvg4j.engine.vk.render.RenderState;
import de.linusdev.cvg4j.engine.vk.render.Renderer;
import de.linusdev.cvg4j.engine.vk.renderpass.RenderPass;
import de.linusdev.cvg4j.engine.vk.swapchain.SwapChain;
import de.linusdev.cvg4j.engine.vk.window.VulkanWindow;
import de.linusdev.cvg4j.nat.vulkan.ReturnedVkResult;
import de.linusdev.cvg4j.nat.vulkan.bitmasks.VkCommandBufferResetFlags;
import de.linusdev.cvg4j.nat.vulkan.bitmasks.VkPipelineStageFlags;
import de.linusdev.cvg4j.nat.vulkan.bitmasks.enums.VkFenceCreateFlagBits;
import de.linusdev.cvg4j.nat.vulkan.bitmasks.enums.VkPipelineStageFlagBits;
import de.linusdev.cvg4j.nat.vulkan.enums.VkResult;
import de.linusdev.cvg4j.nat.vulkan.enums.VkStructureType;
import de.linusdev.cvg4j.nat.vulkan.handles.*;
import de.linusdev.cvg4j.nat.vulkan.structs.VkFenceCreateInfo;
import de.linusdev.cvg4j.nat.vulkan.structs.VkPresentInfoKHR;
import de.linusdev.cvg4j.nat.vulkan.structs.VkSemaphoreCreateInfo;
import de.linusdev.cvg4j.nat.vulkan.structs.VkSubmitInfo;
import de.linusdev.cvg4j.nat.vulkan.utils.VulkanUtils;
import de.linusdev.llog.LLog;
import de.linusdev.llog.base.LogInstance;
import de.linusdev.lutils.math.vector.buffer.intn.BBUInt1;
import de.linusdev.lutils.nat.memory.stack.Stack;
import de.linusdev.lutils.nat.struct.array.StructureArray;
import org.jetbrains.annotations.NotNull;

import static de.linusdev.lutils.nat.pointer.TypedPointer64.ref;
import static de.linusdev.lutils.nat.struct.abstracts.Structure.allocate;

public class RasterizationRenderer implements Renderer {

    public static final @NotNull LogInstance LOG = LLog.getLogInstance();

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
    private RenderCommandsFunction renderCommandsFunction;

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
    }

    public void init(
            @NotNull Stack stack,
            @NotNull Device device,
            @NotNull RenderPass renderPass,
            @NotNull SwapChain swapChain,
            int maxFramesInFlight,
            @NotNull RenderCommandsFunction renderCommandsFunction
    ) {
        this.device = device;
        this.swapChain = swapChain;

        this.maxFramesInFlight = maxFramesInFlight;
        this.renderCommandsFunction = renderCommandsFunction;

        this.graphicsQueue = device.getGraphicsQueue();
        this.presentationQueue = device.getPresentationQueue();
        this.frameBuffers = FrameBuffers.create(stack, vkInstance, device, swapChain, renderPass);
        this.commandPool = GraphicsQueuePermanentCommandPool.create(stack, vkInstance, device, maxFramesInFlight);

        this.imageAvailableSemaphores = StructureArray.newAllocated(maxFramesInFlight, VkSemaphore.class, VkSemaphore::new);
        this.renderFinishedSemaphores = StructureArray.newAllocated(maxFramesInFlight, VkSemaphore.class, VkSemaphore::new);
        this.frameSubmittedFences = StructureArray.newAllocated(maxFramesInFlight, VkFence.class, VkFence::new);

        // Synchronization
        VkSemaphoreCreateInfo vkSemaphoreCreateInfo = stack.push(new VkSemaphoreCreateInfo());
        vkSemaphoreCreateInfo.sType.set(VkStructureType.SEMAPHORE_CREATE_INFO);

        VkFenceCreateInfo vkFenceCreateInfo = stack.push(new VkFenceCreateInfo());
        vkFenceCreateInfo.sType.set(VkStructureType.FENCE_CREATE_INFO);
        vkFenceCreateInfo.flags.set(VkFenceCreateFlagBits.SIGNALED);

        for (int i = 0; i < maxFramesInFlight; i++) {
            vkInstance.vkCreateSemaphore(device.getVkDevice(), ref(vkSemaphoreCreateInfo), ref(null), ref(imageAvailableSemaphores.get(i))).check();
            vkInstance.vkCreateSemaphore(device.getVkDevice(), ref(vkSemaphoreCreateInfo), ref(null), ref(renderFinishedSemaphores.get(i))).check();
            vkInstance.vkCreateFence(device.getVkDevice(), ref(vkFenceCreateInfo), ref(null), ref(frameSubmittedFences.get(i))).check();
        }

        stack.pop(); // vkFenceCreateInfo
        stack.pop(); // vkSemaphoreCreateInfo

        // Stuff required in the show loop
        fenceNullHandle.set(VulkanUtils.VK_NULL_HANDLE);
        commandBufferResetFlags.set(0);
        pipelineStageFlags.set(VkPipelineStageFlagBits.COLOR_ATTACHMENT_OUTPUT);

        submitInfo.sType.set(VkStructureType.SUBMIT_INFO);
        submitInfo.waitSemaphoreCount.set(1);
        submitInfo.pWaitDstStageMask.set(pipelineStageFlags);
        submitInfo.commandBufferCount.set(1);
        submitInfo.signalSemaphoreCount.set(1);

        presentInfo.sType.set(VkStructureType.PRESENT_INFO_KHR);
        presentInfo.waitSemaphoreCount.set(1);
        presentInfo.swapchainCount.set(1);
    }

    @Override
    public @NotNull RenderState render(@NotNull Stack stack) {

        // Get the swap chain
        VkSwapchainKHR vkSwapChain = swapChain.getVkSwapChain();

        // wait for previous frame to be submitted
        vkInstance.vkWaitForFences(device.getVkDevice(), 1, ref(frameSubmittedFences.get(currentFrame)), true, Long.MAX_VALUE).check();

        // acquire Image from the swap chain
        ReturnedVkResult result = vkInstance.vkAcquireNextImageKHR(device.getVkDevice(), vkSwapChain, Long.MAX_VALUE, imageAvailableSemaphores.get(currentFrame), fenceNullHandle, ref(currentImageIndex));

        // Check if we need to recreate the swap chain
        if(result.is(VkResult.VK_ERROR_OUT_OF_DATE_KHR)) return RenderState.SWAP_CHAIN_OUT_OF_DATE;
        else result.checkButAllow(VkResult.VK_SUBOPTIMAL_KHR);

        vkInstance.vkResetFences(device.getVkDevice(), 1, ref(frameSubmittedFences.get(currentFrame))).check();

        // reset command buffer
        vkInstance.vkResetCommandBuffer(commandPool.getVkCommandBuffer(currentFrame), commandBufferResetFlags);

        // fill command buffer
        renderCommandsFunction.render(stack, currentImageIndex.get(), currentFrame, commandPool.getVkCommandBuffer(currentFrame));

        // submit
        submitInfo.pCommandBuffers.set(commandPool.getVkCommandBuffer(currentFrame));
        submitInfo.pWaitSemaphores.set(imageAvailableSemaphores.get(currentFrame));
        submitInfo.pSignalSemaphores.set(renderFinishedSemaphores.get(currentFrame));

        vkInstance.vkQueueSubmit(graphicsQueue, 1, ref(submitInfo), frameSubmittedFences.get(currentFrame)).check();

        // present
        presentInfo.pSwapchains.set(vkSwapChain);
        presentInfo.pWaitSemaphores.set(renderFinishedSemaphores.get(currentFrame));
        presentInfo.pImageIndices.set(currentImageIndex);

        result = vkInstance.vkQueuePresentKHR(presentationQueue, ref(presentInfo));
        currentFrame = (currentFrame + 1) % maxFramesInFlight;

        // Check if we need to recreate the swap chain
        if(result.is(VkResult.VK_ERROR_OUT_OF_DATE_KHR)) return RenderState.SWAP_CHAIN_OUT_OF_DATE;
        else if(result.is(VkResult.VK_SUBOPTIMAL_KHR)) return RenderState.SWAP_CHAIN_SUBOPTIMAL;
        else result.check();

        return RenderState.NONE;
    }

    @Override
    public void waitIdle() throws InterruptedException {
        vkInstance.vkDeviceWaitIdle(device.getVkDevice());
    }

    public FrameBuffers getFrameBuffers() {
        return frameBuffers;
    }

    @Override
    public void close() {
        for (int i = 0; i < maxFramesInFlight; i++) {
            vkInstance.vkDestroySemaphore(device.getVkDevice(), imageAvailableSemaphores.get(i), ref(null));
            vkInstance.vkDestroySemaphore(device.getVkDevice(), renderFinishedSemaphores.get(i), ref(null));
            vkInstance.vkDestroyFence(device.getVkDevice(), frameSubmittedFences.get(i), ref(null));
        }

        commandPool.close();
        frameBuffers.close();

    }
}
