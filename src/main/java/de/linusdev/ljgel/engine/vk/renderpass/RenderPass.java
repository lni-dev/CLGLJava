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

package de.linusdev.ljgel.engine.vk.renderpass;

import de.linusdev.ljgel.engine.vk.device.Device;
import de.linusdev.ljgel.engine.vk.swapchain.SwapChain;
import de.linusdev.ljgel.nat.vulkan.bitmasks.enums.VkAccessFlagBits;
import de.linusdev.ljgel.nat.vulkan.bitmasks.enums.VkPipelineStageFlagBits;
import de.linusdev.ljgel.nat.vulkan.bitmasks.enums.VkSampleCountFlagBits;
import de.linusdev.ljgel.nat.vulkan.constants.APIConstants;
import de.linusdev.ljgel.nat.vulkan.enums.*;
import de.linusdev.ljgel.nat.vulkan.handles.VkInstance;
import de.linusdev.ljgel.nat.vulkan.handles.VkRenderPass;
import de.linusdev.ljgel.nat.vulkan.structs.*;
import de.linusdev.lutils.nat.memory.stack.Stack;
import org.jetbrains.annotations.NotNull;

import static de.linusdev.lutils.nat.pointer.TypedPointer64.ref;
import static de.linusdev.lutils.nat.struct.abstracts.Structure.allocate;

public class RenderPass implements AutoCloseable {

    public static RenderPass create(
            @NotNull Stack stack,
            @NotNull VkInstance vkInstance,
            @NotNull Device device,
            @NotNull SwapChain swapChain
    ) {
        RenderPass renderPass = new RenderPass(vkInstance, device);

        try(var ignored = stack.popPoint()) {

            var attachments = stack.pushArray(3, VkAttachmentDescription.class, VkAttachmentDescription::new);
            int attachmentCount = 2;

            // Description for the color attachment
            VkAttachmentDescription attachmentDescription = attachments.get(0);
            attachmentDescription.format.set(swapChain.getFormat());
            attachmentDescription.samples.set(swapChain.getSampleCount());
            attachmentDescription.loadOp.set(VkAttachmentLoadOp.CLEAR);
            attachmentDescription.storeOp.set(VkAttachmentStoreOp.STORE);
            attachmentDescription.stencilLoadOp.set(VkAttachmentLoadOp.DONT_CARE);
            attachmentDescription.stencilStoreOp.set(VkAttachmentStoreOp.DONT_CARE);
            attachmentDescription.initialLayout.set(VkImageLayout.UNDEFINED);

            VkAttachmentReference colorAttachmentResolveRef = null;
            if(swapChain.isMultiSamplingEnabled()) {
                // Cant present directly from a multi sampled image.
                attachmentDescription.finalLayout.set(VkImageLayout.COLOR_ATTACHMENT_OPTIMAL);

                // Add resolve attachment. We will resolve the multiple samplers to this image.
                // And then present this image, which has only a single sample.
                VkAttachmentDescription colorAttachmentResolve = attachments.get(2);
                attachmentCount = 3;
                colorAttachmentResolve.format.set(swapChain.getFormat());
                colorAttachmentResolve.samples.set(VkSampleCountFlagBits.COUNT_1);
                colorAttachmentResolve.loadOp.set(VkAttachmentLoadOp.DONT_CARE);
                colorAttachmentResolve.storeOp.set(VkAttachmentStoreOp.STORE);
                colorAttachmentResolve.stencilLoadOp.set(VkAttachmentLoadOp.DONT_CARE);
                colorAttachmentResolve.stencilStoreOp.set(VkAttachmentStoreOp.DONT_CARE);
                colorAttachmentResolve.initialLayout.set(VkImageLayout.UNDEFINED);
                colorAttachmentResolve.finalLayout.set(VkImageLayout.PRESENT_SRC_KHR);

                colorAttachmentResolveRef = stack.push(new VkAttachmentReference());
                colorAttachmentResolveRef.attachment.set(2);
                colorAttachmentResolveRef.layout.set(VkImageLayout.COLOR_ATTACHMENT_OPTIMAL);
            } else {
                attachmentDescription.finalLayout.set(VkImageLayout.PRESENT_SRC_KHR);
            }


            // Render Subpass for fragment shader
            VkAttachmentReference vkAttachmentReference = stack.push(new VkAttachmentReference());
            vkAttachmentReference.attachment.set(0);
            vkAttachmentReference.layout.set(VkImageLayout.COLOR_ATTACHMENT_OPTIMAL);

            // Depth Attachment
            VkAttachmentDescription depthAttDesc = attachments.get(1);
            depthAttDesc.format.set(swapChain.getDepthFormat());
            depthAttDesc.samples.set(swapChain.getSampleCount());
            depthAttDesc.loadOp.set(VkAttachmentLoadOp.CLEAR);
            depthAttDesc.storeOp.set(VkAttachmentStoreOp.DONT_CARE);
            depthAttDesc.stencilLoadOp.set(VkAttachmentLoadOp.DONT_CARE);
            depthAttDesc.stencilStoreOp.set(VkAttachmentStoreOp.DONT_CARE);
            depthAttDesc.initialLayout.set(VkImageLayout.UNDEFINED);
            depthAttDesc.finalLayout.set(VkImageLayout.DEPTH_STENCIL_ATTACHMENT_OPTIMAL);

            VkAttachmentReference depthAttRef = stack.push(new VkAttachmentReference());
            depthAttRef.attachment.set(1);
            depthAttRef.layout.set(VkImageLayout.DEPTH_STENCIL_ATTACHMENT_OPTIMAL);



            // Fragment subpass
            VkSubpassDescription fragmentSubpassDescription = stack.push(new VkSubpassDescription());
            fragmentSubpassDescription.pipelineBindPoint.set(VkPipelineBindPoint.GRAPHICS);
            fragmentSubpassDescription.colorAttachmentCount.set(1);
            fragmentSubpassDescription.pColorAttachments.set(vkAttachmentReference);
            fragmentSubpassDescription.pDepthStencilAttachment.set(depthAttRef);
            if(swapChain.isMultiSamplingEnabled()) {
                fragmentSubpassDescription.pResolveAttachments.set(colorAttachmentResolveRef);
            }


            // Create the render pass
            VkRenderPassCreateInfo renderPassCreateInfo = stack.push(new VkRenderPassCreateInfo());
            renderPassCreateInfo.sType.set(VkStructureType.RENDER_PASS_CREATE_INFO);
            renderPassCreateInfo.attachmentCount.set(attachmentCount);
            renderPassCreateInfo.pAttachments.setOfArray(attachments);
            renderPassCreateInfo.subpassCount.set(1);
            renderPassCreateInfo.pSubpasses.set(fragmentSubpassDescription);

            VkSubpassDependency subpassDependency = stack.push(new VkSubpassDependency());
            subpassDependency.srcSubpass.set(APIConstants.VK_SUBPASS_EXTERNAL);
            subpassDependency.dstSubpass.set(0);
            subpassDependency.srcStageMask.set(
                    VkPipelineStageFlagBits.COLOR_ATTACHMENT_OUTPUT,
                    VkPipelineStageFlagBits.LATE_FRAGMENT_TESTS // Depth testing
            );
            subpassDependency.srcAccessMask.set(VkAccessFlagBits.DEPTH_STENCIL_ATTACHMENT_WRITE);

            if(swapChain.isMultiSamplingEnabled()) {
                // Since there is only one color attachment, we have to wait till the last write was completed.
                subpassDependency.srcAccessMask.set(VkAccessFlagBits.COLOR_ATTACHMENT_WRITE);
            }

            subpassDependency.dstStageMask.set(
                    VkPipelineStageFlagBits.COLOR_ATTACHMENT_OUTPUT,
                    VkPipelineStageFlagBits.EARLY_FRAGMENT_TESTS // Depth testing
            );
            subpassDependency.dstAccessMask.set(
                    VkAccessFlagBits.COLOR_ATTACHMENT_WRITE,
                    VkAccessFlagBits.DEPTH_STENCIL_ATTACHMENT_WRITE
            );

            renderPassCreateInfo.dependencyCount.set(1);
            renderPassCreateInfo.pDependencies.set(subpassDependency);

            vkInstance.vkCreateRenderPass(device.getVkDevice(), ref(renderPassCreateInfo), ref(null), ref(renderPass.vkRenderPass)).check();
        }

        return renderPass;
    }

    private final @NotNull VkInstance vkInstance;
    private final @NotNull Device device;

    /*
     * Managed by this class
     */
    protected final @NotNull VkRenderPass vkRenderPass;

    public RenderPass(
            @NotNull VkInstance vkInstance,
            @NotNull Device device
    ) {
        this.vkInstance = vkInstance;
        this.device = device;
        this.vkRenderPass = allocate(new VkRenderPass());
    }

    public @NotNull VkRenderPass getVkRenderPass() {
        return vkRenderPass;
    }

    @Override
    public void close() {
        vkInstance.vkDestroyRenderPass(device.getVkDevice(), vkRenderPass, ref(null));
    }
}
