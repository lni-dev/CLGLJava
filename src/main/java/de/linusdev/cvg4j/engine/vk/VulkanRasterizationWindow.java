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

import de.linusdev.cvg4j.engine.vk.swapchain.SwapChain;
import de.linusdev.cvg4j.nat.glfw3.custom.FrameInfo;
import de.linusdev.cvg4j.nat.glfw3.custom.GLFWWindowHints;
import de.linusdev.cvg4j.nat.glfw3.custom.RenderAPI;
import de.linusdev.cvg4j.nat.glfw3.custom.UpdateListener;
import de.linusdev.cvg4j.nat.glfw3.exceptions.GLFWException;
import de.linusdev.cvg4j.nat.glfw3.objects.GLFWWindow;
import de.linusdev.cvg4j.nat.vulkan.ReturnedVkResult;
import de.linusdev.cvg4j.nat.vulkan.bitmasks.VkCommandBufferResetFlags;
import de.linusdev.cvg4j.nat.vulkan.bitmasks.VkPipelineStageFlags;
import de.linusdev.cvg4j.nat.vulkan.bitmasks.enums.VkFenceCreateFlagBits;
import de.linusdev.cvg4j.nat.vulkan.bitmasks.enums.VkPipelineStageFlagBits;
import de.linusdev.cvg4j.nat.vulkan.enums.VkStructureType;
import de.linusdev.cvg4j.nat.vulkan.handles.*;
import de.linusdev.cvg4j.nat.vulkan.structs.*;
import de.linusdev.cvg4j.nat.vulkan.utils.VulkanUtils;
import de.linusdev.lutils.math.vector.buffer.intn.BBUInt1;
import de.linusdev.lutils.nat.memory.DirectMemoryStack64;
import de.linusdev.lutils.nat.memory.Stack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static de.linusdev.cvg4j.nat.glfw3.GLFW.glfwCreateWindowSurface;
import static de.linusdev.lutils.nat.pointer.Pointer64.refL;
import static de.linusdev.lutils.nat.pointer.TypedPointer64.ref;
import static de.linusdev.lutils.nat.struct.abstracts.Structure.allocate;

public class VulkanRasterizationWindow extends GLFWWindow implements UpdateListener {

    private final @NotNull VkInstance vkInstance;
    private final @NotNull DirectMemoryStack64 stack;

    private final @NotNull VkSurfaceKHR vkSurface;

    private final @NotNull VkSemaphore imageAvailableSemaphore;
    private final @NotNull VkSemaphore renderFinishedSemaphore;
    private final @NotNull VkFence frameSubmittedFence;

    private final @NotNull BBUInt1 currentImageIndex;
    private final @NotNull VkCommandBufferResetFlags commandBufferResetFlags;
    private final @NotNull VkPipelineStageFlags pipelineStageFlags;
    private final @NotNull VkSubmitInfo submitInfo;
    private final @NotNull VkPresentInfoKHR presentInfo;

    private final @NotNull VkFence fenceNullHandle;

    private VkDevice vkDevice;
    private VkCommandBuffer vkCommandBuffer;
    private VkSwapchainKHR vkSwapChain;
    private VkQueue graphicsQueue;
    private VkQueue presentationQueue;

    private RenderCommandsFunction renderCommandsFunction;
    private UpdateListener updateListener;


    public VulkanRasterizationWindow(
            @Nullable GLFWWindowHints hints,
            @NotNull VkInstance vkInstance,
            @NotNull DirectMemoryStack64 stack
    ) throws GLFWException {
        super(RenderAPI.VULKAN, hints);
        this.vkInstance = vkInstance;
        this.stack = stack;
        this.vkSurface = allocate(new VkSurfaceKHR());

        createVkWindowSurface(null).check();

        this.imageAvailableSemaphore = allocate(new VkSemaphore());
        this.renderFinishedSemaphore = allocate(new VkSemaphore());
        this.frameSubmittedFence = allocate(new VkFence());

        this.currentImageIndex = allocate(BBUInt1.newAllocatable(null));
        this.commandBufferResetFlags = allocate(new VkCommandBufferResetFlags());
        this.pipelineStageFlags = allocate(new VkPipelineStageFlags());
        this.submitInfo = allocate(new VkSubmitInfo());
        this.presentInfo = allocate(new VkPresentInfoKHR());

        this.fenceNullHandle = allocate(new VkFence());

    }

    public void init(
            @NotNull Stack stack,
            @NotNull VkDevice vkDevice,
            @NotNull CommandPool cmdPool,
            @NotNull SwapChain swapChain,
            @NotNull VkQueue graphicsQueue,
            @NotNull VkQueue presentationQueue,
            @NotNull RenderCommandsFunction renderCommandsFunction
    ) {
        this.vkDevice = vkDevice;
        this.vkCommandBuffer = cmdPool.getVkCommandBuffer();
        this.vkSwapChain = swapChain.getSwapChain();
        this.graphicsQueue = graphicsQueue;
        this.presentationQueue = presentationQueue;
        this.renderCommandsFunction = renderCommandsFunction;

        // Synchronization
        VkSemaphoreCreateInfo vkSemaphoreCreateInfo = stack.push(new VkSemaphoreCreateInfo());
        vkSemaphoreCreateInfo.sType.set(VkStructureType.SEMAPHORE_CREATE_INFO);

        VkFenceCreateInfo vkFenceCreateInfo = stack.push(new VkFenceCreateInfo());
        vkFenceCreateInfo.sType.set(VkStructureType.FENCE_CREATE_INFO);
        vkFenceCreateInfo.flags.set(VkFenceCreateFlagBits.VK_FENCE_CREATE_SIGNALED_BIT);

        vkInstance.vkCreateSemaphore(vkDevice, ref(vkSemaphoreCreateInfo), ref(null), ref(imageAvailableSemaphore)).check();
        vkInstance.vkCreateSemaphore(vkDevice, ref(vkSemaphoreCreateInfo), ref(null), ref(renderFinishedSemaphore)).check();
        vkInstance.vkCreateFence(vkDevice, ref(vkFenceCreateInfo), ref(null), ref(frameSubmittedFence)).check();

        stack.pop(); // vkFenceCreateInfo
        stack.pop(); // vkSemaphoreCreateInfo

        // Stuff required in the show loop
        fenceNullHandle.set(VulkanUtils.VK_NULL_HANDLE);
        commandBufferResetFlags.set(0);
        pipelineStageFlags.set(VkPipelineStageFlagBits.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT);

        submitInfo.sType.set(VkStructureType.SUBMIT_INFO);
        submitInfo.waitSemaphoreCount.set(1);
        submitInfo.pWaitSemaphores.set(imageAvailableSemaphore);
        submitInfo.pWaitDstStageMask.set(pipelineStageFlags);
        submitInfo.commandBufferCount.set(1);
        submitInfo.pCommandBuffers.set(cmdPool.getVkCommandBuffer());
        submitInfo.signalSemaphoreCount.set(1);
        submitInfo.pSignalSemaphores.set(renderFinishedSemaphore);

        presentInfo.sType.set(VkStructureType.PRESENT_INFO_KHR);
        presentInfo.waitSemaphoreCount.set(1);
        presentInfo.pWaitSemaphores.set(renderFinishedSemaphore);
        presentInfo.swapchainCount.set(1);
        presentInfo.pSwapchains.set(vkSwapChain);
    }

    @Override
    public void show(@NotNull UpdateListener updateListener) {
        this.updateListener = updateListener;
        super.show(this);
    }

    @Override
    public void update(@NotNull FrameInfo frameInfo) {
    }

    @Override
    protected void perFrameOperations() {

        if(renderCommandsFunction.available()) {
            // wait for previous frame to be submitted
            vkInstance.vkWaitForFences(vkDevice, 1, ref(frameSubmittedFence), true, Long.MAX_VALUE).check();
            vkInstance.vkResetFences(vkDevice, 1, ref(frameSubmittedFence)).check();

            // acquire Image from the swap chain
            vkInstance.vkAcquireNextImageKHR(vkDevice, vkSwapChain, Long.MAX_VALUE, imageAvailableSemaphore, fenceNullHandle, ref(currentImageIndex));
            vkInstance.vkResetCommandBuffer(vkCommandBuffer, commandBufferResetFlags);
            renderCommandsFunction.render(currentImageIndex.get());

            // submit
            vkInstance.vkQueueSubmit(graphicsQueue, 1, ref(submitInfo), frameSubmittedFence).check();

            // present
            presentInfo.pImageIndices.set(currentImageIndex);

            vkInstance.vkQueuePresentKHR(presentationQueue, ref(presentInfo)).check();
        }

        updateListener.update0(frameInfo);
        super.perFrameOperations();
    }

    @Override
    protected void windowCloseOperations() {
        // wait till device has finished
        vkInstance.vkDeviceWaitIdle(vkDevice);
        super.windowCloseOperations();
    }

    protected ReturnedVkResult createVkWindowSurface(
            @Nullable VkAllocationCallbacks allocationCallbacks
    ) {
        return new ReturnedVkResult(glfwCreateWindowSurface(
                vkInstance.get(),
                pointer,
                refL(allocationCallbacks),
                refL(vkSurface)
        ));
    }

    public @NotNull VkSurfaceKHR getVkSurface() {
        return vkSurface;
    }

    @Override
    public void close() {
        super.close();
        vkInstance.vkDestroySemaphore(vkDevice, imageAvailableSemaphore, ref(null));
        vkInstance.vkDestroySemaphore(vkDevice, renderFinishedSemaphore, ref(null));
        vkInstance.vkDestroyFence(vkDevice, frameSubmittedFence, ref(null));
        vkInstance.vkDestroySurfaceKHR(vkSurface, ref(null));
    }

    public interface RenderCommandsFunction {
        boolean available();

        void render(int currentFrameBufferImageIndex);
    }

}
