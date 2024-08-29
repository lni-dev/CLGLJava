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

package de.linusdev.cvg4j.engine.vk.pipeline;

import de.linusdev.cvg4j.engine.vk.device.Extend2D;
import de.linusdev.cvg4j.engine.vk.shader.VulkanShader;
import de.linusdev.cvg4j.nat.vulkan.bitmasks.enums.*;
import de.linusdev.cvg4j.nat.vulkan.constants.APIConstants;
import de.linusdev.cvg4j.nat.vulkan.enums.*;
import de.linusdev.cvg4j.nat.vulkan.handles.*;
import de.linusdev.cvg4j.nat.vulkan.structs.*;
import de.linusdev.cvg4j.nat.vulkan.utils.VulkanUtils;
import de.linusdev.lutils.nat.memory.Stack;
import de.linusdev.lutils.nat.pointer.TypedPointer64;
import de.linusdev.lutils.nat.struct.array.StructureArray;
import org.jetbrains.annotations.NotNull;

import static de.linusdev.lutils.nat.pointer.TypedPointer64.ref;

public class RasterizationPipeLine {

    public void create(
            @NotNull Stack stack,
            @NotNull VkInstance vkInstance,
            @NotNull VkDevice vkDevice,
            @NotNull Extend2D swapChainExtend,
            @NotNull RasterizationPipelineInfo info
    ) {
        VulkanShader vertexShader = info.loadVertexShader();
        VulkanShader fragmentShader = info.loadFragmentShader();

        // Create Pipeline stages
        StructureArray<VkPipelineShaderStageCreateInfo> shaderStages = stack.pushArray(
                2, VkPipelineShaderStageCreateInfo.class, VkPipelineShaderStageCreateInfo::new
        );

        // Shader stages
        VkPipelineShaderStageCreateInfo pipelineShaderStageCreateInfo = shaderStages.getOrCreate(0);
        pipelineShaderStageCreateInfo.sType.set(VkStructureType.PIPELINE_SHADER_STAGE_CREATE_INFO);
        pipelineShaderStageCreateInfo.stage.set(VkShaderStageFlagBits.VK_SHADER_STAGE_VERTEX_BIT);
        pipelineShaderStageCreateInfo.module.set(vertexShader.getShaderModule().get());
        pipelineShaderStageCreateInfo.pName.set(stack.pushString(vertexShader.getMainMethodName()));

        pipelineShaderStageCreateInfo = shaderStages.getOrCreate(1);
        pipelineShaderStageCreateInfo.sType.set(VkStructureType.PIPELINE_SHADER_STAGE_CREATE_INFO);
        pipelineShaderStageCreateInfo.stage.set(VkShaderStageFlagBits.VK_SHADER_STAGE_FRAGMENT_BIT);
        pipelineShaderStageCreateInfo.module.set(fragmentShader.getShaderModule().get());
        pipelineShaderStageCreateInfo.pName.set(stack.pushString(fragmentShader.getMainMethodName()));

        // Vertex input stage
        VkPipelineVertexInputStateCreateInfo vkPipelineVertexInputStateCreateInfo = stack.push(new VkPipelineVertexInputStateCreateInfo());
        vkPipelineVertexInputStateCreateInfo.sType.set(VkStructureType.PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO);
        vkPipelineVertexInputStateCreateInfo.vertexAttributeDescriptionCount.set(0);
        vkPipelineVertexInputStateCreateInfo.vertexBindingDescriptionCount.set(0);

        // How to assemble vertex data to triangles stage
        VkPipelineInputAssemblyStateCreateInfo vkPipelineInputAssemblyStateCreateInfo = stack.push(new VkPipelineInputAssemblyStateCreateInfo());
        vkPipelineInputAssemblyStateCreateInfo.sType.set(VkStructureType.PIPELINE_INPUT_ASSEMBLY_STATE_CREATE_INFO);
        vkPipelineInputAssemblyStateCreateInfo.topology.set(VkPrimitiveTopology.TRIANGLE_LIST);
        vkPipelineInputAssemblyStateCreateInfo.primitiveRestartEnable.set(VulkanUtils.booleanToVkBool32(false));

        // Viewport and scissors
        VkViewport vkViewport = stack.push(new VkViewport());
        vkViewport.x.set(0.0f);
        vkViewport.y.set(0.0f);
        vkViewport.width.set(swapChainExtend.width());
        vkViewport.height.set(swapChainExtend.height());
        vkViewport.minDepth.set(0.0f);
        vkViewport.maxDepth.set(1.0f);

        VkRect2D scissor = stack.push(new VkRect2D());
        scissor.offset.x.set(0);
        scissor.offset.y.set(0);
        scissor.extent.width.set(swapChainExtend.width());
        scissor.extent.height.set(swapChainExtend.height());

        // Viewport/scissor state
        VkPipelineViewportStateCreateInfo pipelineViewportStateCreateInfo = stack.push(new VkPipelineViewportStateCreateInfo());
        pipelineViewportStateCreateInfo.sType.set(VkStructureType.PIPELINE_VIEWPORT_STATE_CREATE_INFO);
        pipelineViewportStateCreateInfo.viewportCount.set(1);
        pipelineViewportStateCreateInfo.pViewports.set(vkViewport);
        pipelineViewportStateCreateInfo.scissorCount.set(1);
        pipelineViewportStateCreateInfo.pScissors.set(scissor);

        // Rasterization state
        VkPipelineRasterizationStateCreateInfo pipelineRasterizationStateCreateInfo = stack.push(new VkPipelineRasterizationStateCreateInfo());
        pipelineRasterizationStateCreateInfo.sType.set(VkStructureType.PIPELINE_RASTERIZATION_STATE_CREATE_INFO);
        pipelineRasterizationStateCreateInfo.depthClampEnable.set(VulkanUtils.booleanToVkBool32(false));
        pipelineRasterizationStateCreateInfo.rasterizerDiscardEnable.set(VulkanUtils.booleanToVkBool32(false));
        pipelineRasterizationStateCreateInfo.polygonMode.set(VkPolygonMode.FILL);
        pipelineRasterizationStateCreateInfo.lineWidth.set(1.0f);
        pipelineRasterizationStateCreateInfo.cullMode.set(VkCullModeFlagBits.VK_CULL_MODE_BACK_BIT);
        pipelineRasterizationStateCreateInfo.frontFace.set(VkFrontFace.CLOCKWISE);
        pipelineRasterizationStateCreateInfo.depthBiasEnable.set(VulkanUtils.booleanToVkBool32(false));

        // Multisampling currently disabled
        VkPipelineMultisampleStateCreateInfo pipelineMultisampleStateCreateInfo = stack.push(new VkPipelineMultisampleStateCreateInfo());
        pipelineMultisampleStateCreateInfo.sType.set(VkStructureType.PIPELINE_MULTISAMPLE_STATE_CREATE_INFO);
        pipelineMultisampleStateCreateInfo.sampleShadingEnable.set(VulkanUtils.booleanToVkBool32(false));
        pipelineMultisampleStateCreateInfo.rasterizationSamples.set(VkSampleCountFlagBits.VK_SAMPLE_COUNT_1_BIT);

        // Color blending
        // more color blending options / explanations: https://vulkan-tutorial.com/en/Drawing_a_triangle/Graphics_pipeline_basics/Fixed_functions
        VkPipelineColorBlendAttachmentState colorBlending = stack.push(new VkPipelineColorBlendAttachmentState());
        colorBlending.colorWriteMask.set(
                VkColorComponentFlagBits.VK_COLOR_COMPONENT_R_BIT,
                VkColorComponentFlagBits.VK_COLOR_COMPONENT_G_BIT,
                VkColorComponentFlagBits.VK_COLOR_COMPONENT_B_BIT,
                VkColorComponentFlagBits.VK_COLOR_COMPONENT_A_BIT
        );
        colorBlending.blendEnable.set(VulkanUtils.booleanToVkBool32(false)); // currently disabled
        colorBlending.srcColorBlendFactor.set(VkBlendFactor.ONE);
        colorBlending.dstColorBlendFactor.set(VkBlendFactor.ZERO);
        colorBlending.colorBlendOp.set(VkBlendOp.ADD);
        colorBlending.srcAlphaBlendFactor.set(VkBlendFactor.ONE);
        colorBlending.dstAlphaBlendFactor.set(VkBlendFactor.ZERO);
        colorBlending.alphaBlendOp.set(VkBlendOp.ADD);

        VkPipelineColorBlendStateCreateInfo pipelineColorBlendStateCreateInfo = stack.push(new VkPipelineColorBlendStateCreateInfo());
        pipelineColorBlendStateCreateInfo.sType.set(VkStructureType.PIPELINE_COLOR_BLEND_STATE_CREATE_INFO);
        pipelineColorBlendStateCreateInfo.logicOpEnable.set(VulkanUtils.booleanToVkBool32(false));
        pipelineColorBlendStateCreateInfo.attachmentCount.set(1);
        pipelineColorBlendStateCreateInfo.pAttachments.set(colorBlending);

        // Create Pipeline Layout
        VkPipelineLayoutCreateInfo pipelineLayoutCreateInfo = stack.push(new VkPipelineLayoutCreateInfo());
        pipelineLayoutCreateInfo.sType.set(VkStructureType.PIPELINE_LAYOUT_CREATE_INFO);
        pipelineLayoutCreateInfo.setLayoutCount.set(0);
        pipelineLayoutCreateInfo.pSetLayouts.set(null);
        pipelineLayoutCreateInfo.pushConstantRangeCount.set(0);
        pipelineLayoutCreateInfo.pPushConstantRanges.set(null);

        VkPipelineLayout pipelineLayout = stack.push(new VkPipelineLayout());
        vkInstance.vkCreatePipelineLayout(
                vkDevice,
                ref(pipelineLayoutCreateInfo),
                ref(null),
                ref(pipelineLayout)
        ).check();

        // TODO

        // Render Pass
        VkAttachmentDescription vkAttachmentDescription = new VkAttachmentDescription();
        vkAttachmentDescription.allocate();
        vkAttachmentDescription.format.set(selectedSurfaceFormat.format.get());
        vkAttachmentDescription.samples.set(VkSampleCountFlagBits.VK_SAMPLE_COUNT_1_BIT);
        vkAttachmentDescription.loadOp.set(VkAttachmentLoadOp.CLEAR);
        vkAttachmentDescription.storeOp.set(VkAttachmentStoreOp.STORE);
        vkAttachmentDescription.stencilLoadOp.set(VkAttachmentLoadOp.DONT_CARE);
        vkAttachmentDescription.stencilStoreOp.set(VkAttachmentStoreOp.DONT_CARE);
        vkAttachmentDescription.initialLayout.set(VkImageLayout.UNDEFINED);
        vkAttachmentDescription.finalLayout.set(VkImageLayout.PRESENT_SRC_KHR);

        // Render Subpass for fragment shader
        VkAttachmentReference vkAttachmentReference = new VkAttachmentReference();
        vkAttachmentReference.allocate();
        vkAttachmentReference.attachment.set(0);
        vkAttachmentReference.layout.set(VkImageLayout.COLOR_ATTACHMENT_OPTIMAL);

        VkSubpassDescription fragmentSubpassDescription = new VkSubpassDescription();
        fragmentSubpassDescription.allocate();
        fragmentSubpassDescription.pipelineBindPoint.set(VkPipelineBindPoint.GRAPHICS);
        fragmentSubpassDescription.colorAttachmentCount.set(1);
        fragmentSubpassDescription.pColorAttachments.set(vkAttachmentReference);

        // create the render pass
        VkRenderPassCreateInfo vkRenderPassCreateInfo = new VkRenderPassCreateInfo();
        vkRenderPassCreateInfo.allocate();
        vkRenderPassCreateInfo.sType.set(VkStructureType.RENDER_PASS_CREATE_INFO);
        vkRenderPassCreateInfo.attachmentCount.set(1);
        vkRenderPassCreateInfo.pAttachments.set(vkAttachmentDescription);
        vkRenderPassCreateInfo.subpassCount.set(1);
        vkRenderPassCreateInfo.pSubpasses.set(fragmentSubpassDescription);

        VkSubpassDependency vkSubpassDependency = new VkSubpassDependency();
        vkSubpassDependency.allocate();
        vkSubpassDependency.srcSubpass.set(APIConstants.VK_SUBPASS_EXTERNAL);
        vkSubpassDependency.dstSubpass.set(0);
        vkSubpassDependency.srcStageMask.set(VkPipelineStageFlagBits.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT);
        vkSubpassDependency.srcAccessMask.set(0);
        vkSubpassDependency.dstStageMask.set(VkPipelineStageFlagBits.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT);
        vkSubpassDependency.dstAccessMask.set(VkAccessFlagBits.VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT);

        vkRenderPassCreateInfo.dependencyCount.set(1);
        vkRenderPassCreateInfo.pDependencies.set(vkSubpassDependency);

        VkRenderPass vkRenderPass = new VkRenderPass();
        vkRenderPass.allocate();
        vkInstance.vkCreateRenderPass(
                device,
                TypedPointer64.of(vkRenderPassCreateInfo),
                TypedPointer64.of(null),
                TypedPointer64.of(vkRenderPass)
        ).check();

        // Finally Create the Graphics Pipeline!
        VkGraphicsPipelineCreateInfo vkGraphicsPipelineCreateInfo = new VkGraphicsPipelineCreateInfo();
        vkGraphicsPipelineCreateInfo.allocate();
        vkGraphicsPipelineCreateInfo.sType.set(VkStructureType.GRAPHICS_PIPELINE_CREATE_INFO);
        vkGraphicsPipelineCreateInfo.stageCount.set(2);
        vkGraphicsPipelineCreateInfo.pStages.set(shaderStages.getPointer());
        vkGraphicsPipelineCreateInfo.pVertexInputState.set(vkPipelineVertexInputStateCreateInfo);
        vkGraphicsPipelineCreateInfo.pInputAssemblyState.set(vkPipelineInputAssemblyStateCreateInfo);
        vkGraphicsPipelineCreateInfo.pViewportState.set(vkPipelineViewportStateCreateInfo);
        vkGraphicsPipelineCreateInfo.pRasterizationState.set(vkPipelineRasterizationStateCreateInfo);
        vkGraphicsPipelineCreateInfo.pMultisampleState.set(vkPipelineMultisampleStateCreateInfo);
        vkGraphicsPipelineCreateInfo.pDepthStencilState.set(null);
        vkGraphicsPipelineCreateInfo.pColorBlendState.set(vkPipelineColorBlendStateCreateInfo);
        vkGraphicsPipelineCreateInfo.pDynamicState.set(null);
        vkGraphicsPipelineCreateInfo.layout.set(vkPipelineLayout.get());
        vkGraphicsPipelineCreateInfo.renderPass.set(vkRenderPass.get());
        vkGraphicsPipelineCreateInfo.subpass.set(0);

        VkPipeline graphicsPipeline = new VkPipeline();
        graphicsPipeline.allocate();
        VkPipelineCache cache = new VkPipelineCache();
        cache.allocate();
        cache.set(VulkanUtils.VK_NULL_HANDLE);
        vkInstance.vkCreateGraphicsPipelines(
                device,
                cache,
                1,
                TypedPointer64.of(vkGraphicsPipelineCreateInfo),
                TypedPointer64.of(null),
                TypedPointer64.of(graphicsPipeline)
        ).check();

        stack.pop(); // pipelineLayout
        stack.pop(); // pipelineLayoutCreateInfo
        stack.pop(); // pipelineColorBlendStateCreateInfo
        stack.pop(); // colorBlending
        stack.pop(); // pipelineMultisampleStateCreateInfo
        stack.pop(); // pipelineRasterizationStateCreateInfo
        stack.pop(); // pipelineViewportStateCreateInfo
        stack.pop(); // scissor
        stack.pop(); // vkViewport
        stack.pop(); // vkPipelineInputAssemblyStateCreateInfo
        stack.pop(); // vkPipelineVertexInputStateCreateInfo
        stack.pop(); // vertexShader.getMainMethodName()
        stack.pop(); // fragmentShader.getMainMethodName()
        stack.pop(); // shaderStages
        vertexShader.close();
        fragmentShader.close();
    }

}
