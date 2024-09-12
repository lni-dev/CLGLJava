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

package de.linusdev.cvg4j.engine.vk.renderpass;

import de.linusdev.cvg4j.engine.vk.device.Device;
import de.linusdev.cvg4j.engine.vk.swapchain.SwapChain;
import de.linusdev.cvg4j.nat.vulkan.bitmasks.enums.VkAccessFlagBits;
import de.linusdev.cvg4j.nat.vulkan.bitmasks.enums.VkPipelineStageFlagBits;
import de.linusdev.cvg4j.nat.vulkan.bitmasks.enums.VkSampleCountFlagBits;
import de.linusdev.cvg4j.nat.vulkan.constants.APIConstants;
import de.linusdev.cvg4j.nat.vulkan.enums.*;
import de.linusdev.cvg4j.nat.vulkan.handles.VkInstance;
import de.linusdev.cvg4j.nat.vulkan.handles.VkRenderPass;
import de.linusdev.cvg4j.nat.vulkan.structs.*;
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

        // Description for the color attachment
        VkAttachmentDescription attachmentDescription = stack.push(new VkAttachmentDescription());
        attachmentDescription.format.set(swapChain.getFormat());
        attachmentDescription.samples.set(VkSampleCountFlagBits.VK_SAMPLE_COUNT_1_BIT);
        attachmentDescription.loadOp.set(VkAttachmentLoadOp.CLEAR);
        attachmentDescription.storeOp.set(VkAttachmentStoreOp.STORE);
        attachmentDescription.stencilLoadOp.set(VkAttachmentLoadOp.DONT_CARE);
        attachmentDescription.stencilStoreOp.set(VkAttachmentStoreOp.DONT_CARE);
        attachmentDescription.initialLayout.set(VkImageLayout.UNDEFINED);
        attachmentDescription.finalLayout.set(VkImageLayout.PRESENT_SRC_KHR);

        // Render Subpass for fragment shader
        VkAttachmentReference vkAttachmentReference = stack.push(new VkAttachmentReference());
        vkAttachmentReference.attachment.set(0);
        vkAttachmentReference.layout.set(VkImageLayout.COLOR_ATTACHMENT_OPTIMAL);

        VkSubpassDescription fragmentSubpassDescription = stack.push(new VkSubpassDescription());
        fragmentSubpassDescription.pipelineBindPoint.set(VkPipelineBindPoint.GRAPHICS);
        fragmentSubpassDescription.colorAttachmentCount.set(1);
        fragmentSubpassDescription.pColorAttachments.set(vkAttachmentReference);

        // Create the render pass
        VkRenderPassCreateInfo renderPassCreateInfo = stack.push(new VkRenderPassCreateInfo());
        renderPassCreateInfo.sType.set(VkStructureType.RENDER_PASS_CREATE_INFO);
        renderPassCreateInfo.attachmentCount.set(1);
        renderPassCreateInfo.pAttachments.set(attachmentDescription);
        renderPassCreateInfo.subpassCount.set(1);
        renderPassCreateInfo.pSubpasses.set(fragmentSubpassDescription);

        VkSubpassDependency subpassDependency = stack.push(new VkSubpassDependency());
        subpassDependency.srcSubpass.set(APIConstants.VK_SUBPASS_EXTERNAL);
        subpassDependency.dstSubpass.set(0);
        subpassDependency.srcStageMask.set(VkPipelineStageFlagBits.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT);
        subpassDependency.srcAccessMask.set(0);
        subpassDependency.dstStageMask.set(VkPipelineStageFlagBits.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT);
        subpassDependency.dstAccessMask.set(VkAccessFlagBits.VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT);

        renderPassCreateInfo.dependencyCount.set(1);
        renderPassCreateInfo.pDependencies.set(subpassDependency);

        vkInstance.vkCreateRenderPass(device.getVkDevice(), ref(renderPassCreateInfo), ref(null), ref(renderPass.vkRenderPass)).check();

        stack.pop(); // subpassDependency
        stack.pop(); // renderPassCreateInfo
        stack.pop(); // fragmentSubpassDescription
        stack.pop(); // vkAttachmentReference
        stack.pop(); // attachmentDescription

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
