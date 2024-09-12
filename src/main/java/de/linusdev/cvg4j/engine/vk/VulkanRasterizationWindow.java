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

import de.linusdev.cvg4j.engine.vk.command.pool.GraphicsQueuePermanentCommandPool;
import de.linusdev.cvg4j.engine.vk.device.Device;
import de.linusdev.cvg4j.engine.vk.frame.buffer.FrameBuffers;
import de.linusdev.cvg4j.engine.vk.renderpass.RenderPass;
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
import de.linusdev.cvg4j.nat.vulkan.enums.VkResult;
import de.linusdev.cvg4j.nat.vulkan.enums.VkStructureType;
import de.linusdev.cvg4j.nat.vulkan.handles.*;
import de.linusdev.cvg4j.nat.vulkan.structs.*;
import de.linusdev.cvg4j.nat.vulkan.utils.VulkanUtils;
import de.linusdev.lutils.math.vector.buffer.intn.BBUInt1;
import de.linusdev.lutils.nat.memory.stack.Stack;
import de.linusdev.lutils.nat.memory.stack.impl.DirectMemoryStack64;
import de.linusdev.lutils.nat.struct.array.StructureArray;
import de.linusdev.lutils.thread.var.SyncVar;
import de.linusdev.lutils.thread.var.SyncVarImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static de.linusdev.cvg4j.nat.glfw3.GLFW.glfwCreateWindowSurface;
import static de.linusdev.cvg4j.nat.glfw3.GLFW.glfwWaitEvents;
import static de.linusdev.cvg4j.nat.glfw3.GLFWValues.GLFW_DONT_CARE;
import static de.linusdev.lutils.nat.pointer.Pointer64.refL;
import static de.linusdev.lutils.nat.pointer.TypedPointer64.ref;
import static de.linusdev.lutils.nat.struct.abstracts.Structure.allocate;

public class VulkanRasterizationWindow extends GLFWWindow implements UpdateListener {

    private final @NotNull VkInstance vkInstance;
    private final @NotNull DirectMemoryStack64 stack;

    private final @NotNull VkSurfaceKHR vkSurface;

    private final @NotNull BBUInt1 currentImageIndex;
    private final @NotNull VkCommandBufferResetFlags commandBufferResetFlags;
    private final @NotNull VkPipelineStageFlags pipelineStageFlags;
    private final @NotNull VkSubmitInfo submitInfo;
    private final @NotNull VkPresentInfoKHR presentInfo;

    private final @NotNull VkFence fenceNullHandle;

    private Device device;
    private SwapChain swapChain;

    private VkQueue graphicsQueue;
    private VkQueue presentationQueue;

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
    private UpdateListener updateListener;

    private int currentFrame = 0;
    private int maxFramesInFlight;

    private final @NotNull SyncVar<@NotNull Boolean> recreateSwapChain = new SyncVarImpl<>(false);
    private final @NotNull SyncVar<@NotNull Boolean> minimized = new SyncVarImpl<>(false);


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

        this.currentImageIndex = allocate(BBUInt1.newAllocatable(null));
        this.commandBufferResetFlags = allocate(new VkCommandBufferResetFlags());
        this.pipelineStageFlags = allocate(new VkPipelineStageFlags());
        this.submitInfo = allocate(new VkSubmitInfo());
        this.presentInfo = allocate(new VkPresentInfoKHR());

        this.fenceNullHandle = allocate(new VkFence());

        this.listeners().addFramebufferSizeListener((width, height) -> recreateSwapChain.set(true));
        this.listeners().addWindowIconificationListener(minimized::set);
        this.listeners().addWindowRefreshListener(this::render);

        setWindowSizeLimits(1, 1, GLFW_DONT_CARE, GLFW_DONT_CARE);
    }

    public void createFrameBuffers(@NotNull RenderPass renderPass) {
        this.frameBuffers = FrameBuffers.create(stack, vkInstance, device, swapChain, renderPass);
    }

    public void init(
            @NotNull Stack stack,
            @NotNull Device device,
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
    public void show(@NotNull UpdateListener updateListener) {
        this.updateListener = updateListener;
        super.show(this);
    }

    @Override
    public void update(@NotNull FrameInfo frameInfo) {
    }

    protected void render() {
        if(renderCommandsFunction.available()) {

            // Check if window is minimized or the swapChain needs to be recreated ...
            if(minimized.computeSynchronised(ignored -> {
                recreateSwapChain.doSynchronised(ignored2 -> {
                    if(recreateSwapChain.get() && !minimized.get()) {
                        recreateSwapChain.set(false);
                        vkInstance.vkDeviceWaitIdle(device.getVkDevice());
                        renderCommandsFunction.recreateSwapChain(stack);
                    }
                });

                return minimized.get();
            })) {
                glfwWaitEvents();
                return;
            }


            // Get the swap chain
            VkSwapchainKHR vkSwapChain = swapChain.getVkSwapChain();

            // wait for previous frame to be submitted
            vkInstance.vkWaitForFences(device.getVkDevice(), 1, ref(frameSubmittedFences.get(currentFrame)), true, Long.MAX_VALUE).check();


            // acquire Image from the swap chain
            ReturnedVkResult result = vkInstance.vkAcquireNextImageKHR(device.getVkDevice(), vkSwapChain, Long.MAX_VALUE, imageAvailableSemaphores.get(currentFrame), fenceNullHandle, ref(currentImageIndex));

            // Check if we need to recreate the swap chain
            if(result.is(VkResult.VK_ERROR_OUT_OF_DATE_KHR)) {
                recreateSwapChain.set(true);
                return;
            } else {
                result.checkButAllow(VkResult.VK_SUBOPTIMAL_KHR);
            }

            vkInstance.vkResetFences(device.getVkDevice(), 1, ref(frameSubmittedFences.get(currentFrame))).check();

            // reset command buffer
            vkInstance.vkResetCommandBuffer(commandPool.getVkCommandBuffer(currentFrame), commandBufferResetFlags);

            // fill command buffer
            renderCommandsFunction.render(currentImageIndex.get(), currentFrame, commandPool.getVkCommandBuffer(currentFrame));

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

            // Check if we need to recreate the swap chain
            if(result.is(VkResult.VK_ERROR_OUT_OF_DATE_KHR) || result.is(VkResult.VK_SUBOPTIMAL_KHR)) {
                recreateSwapChain.set(true);
                return;
            } else {
                result.check();
            }

            currentFrame = (currentFrame + 1) % maxFramesInFlight;
        }
    }

    @Override
    protected void perFrameOperations() {
        render();
        updateListener.update0(frameInfo);
        super.perFrameOperations();
    }

    @Override
    protected void windowCloseOperations() {
        // wait till device has finished
        vkInstance.vkDeviceWaitIdle(device.getVkDevice());
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

    public FrameBuffers getFrameBuffers() {
        return frameBuffers;
    }

    @Override
    public void close() {
        super.close();
        for (int i = 0; i < maxFramesInFlight; i++) {
            vkInstance.vkDestroySemaphore(device.getVkDevice(), imageAvailableSemaphores.get(i), ref(null));
            vkInstance.vkDestroySemaphore(device.getVkDevice(), renderFinishedSemaphores.get(i), ref(null));
            vkInstance.vkDestroyFence(device.getVkDevice(), frameSubmittedFences.get(i), ref(null));
        }

        commandPool.close();
        frameBuffers.close();
        vkInstance.vkDestroySurfaceKHR(vkSurface, ref(null));
    }

    public interface RenderCommandsFunction {
        boolean available();

        void render(
                int currentFrameBufferImageIndex,
                int currentFrame,
                @NotNull VkCommandBuffer commandBuffer
        );

        void recreateSwapChain(@NotNull Stack stack);
    }

}
