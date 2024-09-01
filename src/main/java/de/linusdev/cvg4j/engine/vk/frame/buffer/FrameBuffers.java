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

package de.linusdev.cvg4j.engine.vk.frame.buffer;

import de.linusdev.cvg4j.engine.vk.device.Device;
import de.linusdev.cvg4j.engine.vk.renderpass.RenderPass;
import de.linusdev.cvg4j.engine.vk.swapchain.SwapChain;
import de.linusdev.cvg4j.engine.vk.swapchain.SwapChainRecreationListener;
import de.linusdev.cvg4j.nat.vulkan.enums.VkStructureType;
import de.linusdev.cvg4j.nat.vulkan.handles.VkFramebuffer;
import de.linusdev.cvg4j.nat.vulkan.handles.VkInstance;
import de.linusdev.cvg4j.nat.vulkan.handles.VkRenderPass;
import de.linusdev.cvg4j.nat.vulkan.structs.VkFramebufferCreateInfo;
import de.linusdev.lutils.nat.memory.Stack;
import de.linusdev.lutils.nat.struct.array.StructureArray;
import org.jetbrains.annotations.NotNull;

import static de.linusdev.lutils.nat.pointer.TypedPointer64.ref;

public class FrameBuffers implements AutoCloseable, SwapChainRecreationListener {

    public static @NotNull FrameBuffers create(
            @NotNull Stack stack,
            @NotNull VkInstance vkInstance,
            @NotNull Device device,
            @NotNull SwapChain swapChain,
            @NotNull RenderPass renderPass
    ) {
        FrameBuffers frameBuffers = new FrameBuffers(vkInstance, device, swapChain, renderPass.getVkRenderPass());
        frameBuffers.recreate(false, stack);
        return frameBuffers;
    }

    private final @NotNull VkInstance vkInstance;
    private final @NotNull Device device;
    private final @NotNull SwapChain swapChain;
    private final @NotNull VkRenderPass vkRenderPass;

    /*
     * Managed by this class
     */
    private final @NotNull StructureArray<VkFramebuffer> frameBuffers;

    public FrameBuffers(
            @NotNull VkInstance vkInstance,
            @NotNull Device device,
            @NotNull SwapChain swapChain,
            @NotNull VkRenderPass vkRenderPass
    ) {
        this.vkInstance = vkInstance;
        this.device = device;
        this.swapChain = swapChain;

        this.frameBuffers = StructureArray.newAllocated(swapChain.getSwapChainImageCount(), VkFramebuffer.class, VkFramebuffer::new);
        this.vkRenderPass = vkRenderPass;

        swapChain.addRecreationListener(this);
    }

    public void recreate(@NotNull Stack stack) {
        recreate(true, stack);
    }

    protected void recreate(boolean destroy, @NotNull Stack stack) {
        if(destroy)
            destroyForRecreation();

        // Create Framebuffers
        VkFramebufferCreateInfo frameBufferCreateInfo = stack.push(new VkFramebufferCreateInfo());
        int i = 0;
        for (VkFramebuffer vkFramebuffer : frameBuffers) {

            frameBufferCreateInfo.sType.set(VkStructureType.FRAMEBUFFER_CREATE_INFO);
            frameBufferCreateInfo.renderPass.set(vkRenderPass);
            frameBufferCreateInfo.attachmentCount.set(1);
            frameBufferCreateInfo.pAttachments.set(swapChain.getSwapChainImageViews().get(i));
            frameBufferCreateInfo.width.set(swapChain.getExtend().width());
            frameBufferCreateInfo.height.set(swapChain.getExtend().height());
            frameBufferCreateInfo.layers.set(1);

            vkInstance.vkCreateFramebuffer(
                    device.getVkDevice(),
                    ref(frameBufferCreateInfo),
                    ref(null),
                    ref(vkFramebuffer)
            ).check();

            i++;
        }

        stack.pop(); // frameBufferCreateInfo
    }

    public @NotNull VkFramebuffer getFrameBuffer(int index) {
        return frameBuffers.get(index);
    }

    public @NotNull StructureArray<VkFramebuffer> getFrameBuffers() {
        return frameBuffers;
    }

    @Override
    public void swapChainRecreated(@NotNull Stack stack) {
        recreate(stack);
    }

    @Override
    public void close() {
        swapChain.removeRecreationListener(this);
        destroyForRecreation();
    }

    private void destroyForRecreation() {
        for (VkFramebuffer frameBuffer : frameBuffers)
            vkInstance.vkDestroyFramebuffer(device.getVkDevice(), frameBuffer, ref(null));
    }


}
