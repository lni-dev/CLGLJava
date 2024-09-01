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

import de.linusdev.cvg4j.engine.vk.device.Device;
import de.linusdev.cvg4j.engine.vk.shader.VulkanShader;
import de.linusdev.cvg4j.engine.vk.swapchain.SwapChain;
import de.linusdev.cvg4j.nat.vulkan.bitmasks.enums.*;
import de.linusdev.cvg4j.nat.vulkan.constants.APIConstants;
import de.linusdev.cvg4j.nat.vulkan.enums.*;
import de.linusdev.cvg4j.nat.vulkan.handles.*;
import de.linusdev.cvg4j.nat.vulkan.structs.*;
import de.linusdev.cvg4j.nat.vulkan.utils.VulkanUtils;
import de.linusdev.lutils.nat.enums.NativeEnumValue32;
import de.linusdev.lutils.nat.memory.Stack;
import de.linusdev.lutils.nat.struct.array.StructureArray;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import static de.linusdev.lutils.nat.pointer.TypedPointer64.ref;
import static de.linusdev.lutils.nat.struct.abstracts.Structure.allocate;

public class RasterizationPipeline implements AutoCloseable {

    public static RasterizationPipeline create(
            @NotNull Stack stack,
            @NotNull VkInstance vkInstance,
            @NotNull Device device,
            @NotNull SwapChain swapChain,
            @NotNull RasterizationPipelineInfo info
    ) throws IOException {
        RasterizationPipeline pipeline = new RasterizationPipeline(vkInstance, device, swapChain);

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

        // dynamic state (viewport and scissors will be dynamic)
        StructureArray<NativeEnumValue32<VkDynamicState>> dynamicStates = stack.pushArray(2, NativeEnumValue32.class, NativeEnumValue32::newUnallocatedT);
        dynamicStates.getOrCreate(0).set(VkDynamicState.VIEWPORT);
        dynamicStates.getOrCreate(1).set(VkDynamicState.SCISSOR);

        VkPipelineDynamicStateCreateInfo dynamicStateCreateInfo = stack.push(new VkPipelineDynamicStateCreateInfo());
        dynamicStateCreateInfo.sType.set(VkStructureType.PIPELINE_DYNAMIC_STATE_CREATE_INFO);
        dynamicStateCreateInfo.dynamicStateCount.set(dynamicStates.length());
        dynamicStateCreateInfo.pDynamicStates.set(dynamicStates.getPointer());

        // Viewport/scissor state
        VkPipelineViewportStateCreateInfo pipelineViewportStateCreateInfo = stack.push(new VkPipelineViewportStateCreateInfo());
        pipelineViewportStateCreateInfo.sType.set(VkStructureType.PIPELINE_VIEWPORT_STATE_CREATE_INFO);
        pipelineViewportStateCreateInfo.viewportCount.set(1);
        pipelineViewportStateCreateInfo.scissorCount.set(1);

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

        vkInstance.vkCreatePipelineLayout(
                device.getVkDevice(),
                ref(pipelineLayoutCreateInfo),
                ref(null),
                ref(pipeline.getVkPipelineLayout())
        ).check();

        // Render Pass
        // TODO: I think render pass should have its own object. https://ogldev.org/www/tutorial50/vulkan.jpg

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

        vkInstance.vkCreateRenderPass(device.getVkDevice(), ref(renderPassCreateInfo), ref(null), ref(pipeline.vkRenderPass)).check();

        // Finally Create the Graphics Pipeline!
        VkGraphicsPipelineCreateInfo graphicsPipelineCreateInfo = stack.push(new VkGraphicsPipelineCreateInfo());
        graphicsPipelineCreateInfo.sType.set(VkStructureType.GRAPHICS_PIPELINE_CREATE_INFO);
        graphicsPipelineCreateInfo.stageCount.set(2);
        graphicsPipelineCreateInfo.pStages.set(shaderStages.getPointer());
        graphicsPipelineCreateInfo.pVertexInputState.set(vkPipelineVertexInputStateCreateInfo);
        graphicsPipelineCreateInfo.pInputAssemblyState.set(vkPipelineInputAssemblyStateCreateInfo);
        graphicsPipelineCreateInfo.pViewportState.set(pipelineViewportStateCreateInfo);
        graphicsPipelineCreateInfo.pRasterizationState.set(pipelineRasterizationStateCreateInfo);
        graphicsPipelineCreateInfo.pMultisampleState.set(pipelineMultisampleStateCreateInfo);
        graphicsPipelineCreateInfo.pDepthStencilState.set(null);
        graphicsPipelineCreateInfo.pColorBlendState.set(pipelineColorBlendStateCreateInfo);
        graphicsPipelineCreateInfo.pDynamicState.set(dynamicStateCreateInfo);
        graphicsPipelineCreateInfo.layout.set(pipeline.getVkPipelineLayout().get());
        graphicsPipelineCreateInfo.renderPass.set(pipeline.vkRenderPass.get());
        graphicsPipelineCreateInfo.subpass.set(0);


        VkPipelineCache cache = stack.push(new VkPipelineCache());
        cache.set(VulkanUtils.VK_NULL_HANDLE);

        vkInstance.vkCreateGraphicsPipelines(
                device.getVkDevice(),
                cache,
                1,
                ref(graphicsPipelineCreateInfo),
                ref(null),
                ref(pipeline.getVkPipeline())
        ).check();

        stack.pop(); // cache
        stack.pop(); // graphicsPipelineCreateInfo
        stack.pop(); // subpassDependency
        stack.pop(); // renderPassCreateInfo
        stack.pop(); // fragmentSubpassDescription
        stack.pop(); // vkAttachmentReference
        stack.pop(); // attachmentDescription
        stack.pop(); // pipelineLayoutCreateInfo
        stack.pop(); // pipelineColorBlendStateCreateInfo
        stack.pop(); // colorBlending
        stack.pop(); // pipelineMultisampleStateCreateInfo
        stack.pop(); // dynamicStateCreateInfo
        stack.pop(); // dynamicStates
        stack.pop(); // pipelineRasterizationStateCreateInfo
        stack.pop(); // pipelineViewportStateCreateInfo
        stack.pop(); // vkPipelineInputAssemblyStateCreateInfo
        stack.pop(); // vkPipelineVertexInputStateCreateInfo
        stack.pop(); // vertexShader.getMainMethodName()
        stack.pop(); // fragmentShader.getMainMethodName()
        stack.pop(); // shaderStages
        vertexShader.close();
        fragmentShader.close();

        return pipeline;
    }

    private final @NotNull VkInstance vkInstance;
    private final @NotNull Device device;
    private final @NotNull SwapChain swapChain;

    /*
     * Managed by this class
     */
    protected final @NotNull VkPipelineLayout vkPipelineLayout;
    protected final @NotNull VkRenderPass vkRenderPass;
    protected final @NotNull VkPipeline vkPipeline;

    protected RasterizationPipeline(
            @NotNull VkInstance vkInstance,
            @NotNull Device device,
            @NotNull SwapChain swapChain
    ) {
        this.vkInstance = vkInstance;
        this.device = device;
        this.swapChain = swapChain;
        this.vkPipelineLayout = allocate(new VkPipelineLayout());
        this.vkRenderPass = allocate(new VkRenderPass());
        this.vkPipeline = allocate(new VkPipeline());
    }

    public @NotNull VkPipeline getVkPipeline() {
        return vkPipeline;
    }

    public @NotNull VkPipelineLayout getVkPipelineLayout() {
        return vkPipelineLayout;
    }

    public @NotNull VkRenderPass getVkRenderPass() {
        return vkRenderPass;
    }

    public @NotNull SwapChain getSwapChain() {
        return swapChain;
    }

    @Override
    public void close() {
        vkInstance.vkDestroyPipelineLayout(device.getVkDevice(), vkPipelineLayout, ref(null));
        vkInstance.vkDestroyRenderPass(device.getVkDevice(), vkRenderPass, ref(null));
        vkInstance.vkDestroyPipeline(device.getVkDevice(), vkPipeline, ref(null));
    }
}
