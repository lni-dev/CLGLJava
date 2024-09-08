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

package de.linusdev.cvg4j.engine.vk.memory.allocator.image;

import de.linusdev.cvg4j.engine.exception.EngineException;
import de.linusdev.cvg4j.engine.vk.device.Device;
import de.linusdev.cvg4j.engine.vk.memory.allocator.VulkanMemoryBoundObject;
import de.linusdev.cvg4j.nat.vulkan.bitmasks.VkDependencyFlags;
import de.linusdev.cvg4j.nat.vulkan.bitmasks.VkPipelineStageFlags;
import de.linusdev.cvg4j.nat.vulkan.bitmasks.enums.*;
import de.linusdev.cvg4j.nat.vulkan.constants.APIConstants;
import de.linusdev.cvg4j.nat.vulkan.enums.*;
import de.linusdev.cvg4j.nat.vulkan.handles.*;
import de.linusdev.cvg4j.nat.vulkan.structs.*;
import de.linusdev.cvg4j.nat.vulkan.utils.VulkanUtils;
import de.linusdev.lutils.bitfield.IntBitfield;
import de.linusdev.lutils.image.ImageSize;
import de.linusdev.lutils.nat.memory.Stack;
import org.jetbrains.annotations.NotNull;

import static de.linusdev.lutils.nat.pointer.TypedPointer64.ref;
import static de.linusdev.lutils.nat.struct.abstracts.Structure.allocate;

public class VulkanSamplerImage extends VulkanMemoryBoundObject {

    public static @NotNull VulkanSamplerImage create(
            @NotNull Stack stack,
            @NotNull VkInstance vkInstance,
            @NotNull Device device,
            @NotNull String debugName,
            @NotNull ImageSize imageSize,
            int sizeInBytes,
            @NotNull VkFormat format,
            @NotNull VkImageTiling tiling,
            @NotNull IntBitfield<VkImageUsageFlagBits> usage,
            @NotNull IntBitfield<VkMemoryPropertyFlagBits> memFlags
    ) throws EngineException {
        VulkanSamplerImage vulkanImage = new VulkanSamplerImage(vkInstance, device, debugName, sizeInBytes, format);

        VkImageCreateInfo imageCreateInfo = stack.push(new VkImageCreateInfo());
        imageCreateInfo.sType.set(VkStructureType.IMAGE_CREATE_INFO);
        imageCreateInfo.imageType.set(VkImageType.TYPE_2D);
        imageCreateInfo.extent.width.set(imageSize.getWidth());
        imageCreateInfo.extent.height.set(imageSize.getHeight());
        imageCreateInfo.extent.depth.set(1);
        imageCreateInfo.mipLevels.set(1);
        imageCreateInfo.arrayLayers.set(1);
        imageCreateInfo.format.set(format);
        imageCreateInfo.tiling.set(tiling);
        imageCreateInfo.initialLayout.set(VkImageLayout.UNDEFINED);
        imageCreateInfo.usage.replaceWith(usage);
        imageCreateInfo.samples.set(VkSampleCountFlagBits.VK_SAMPLE_COUNT_1_BIT);
        imageCreateInfo.sharingMode.set(VkSharingMode.EXCLUSIVE);

        vkInstance.vkCreateImage(device.getVkDevice(), ref(imageCreateInfo), ref(null), ref(vulkanImage.vkImage)).check();

        stack.pop(); // imageCreateInfo

        VkMemoryRequirements memoryRequirements = stack.push(new VkMemoryRequirements());
        vkInstance.vkGetImageMemoryRequirements(device.getVkDevice(), vulkanImage.vkImage, ref(memoryRequirements));
        int memoryTypeIndex = device.findMemoryType(stack, memoryRequirements.memoryTypeBits.get(), memFlags);
        vulkanImage.memoryRequirements(memoryRequirements, memoryTypeIndex, memFlags);

        stack.pop(); // memoryRequirements

        return vulkanImage;
    }

    private final @NotNull VkFormat vkFormat;
    private @NotNull VkImageLayout currentLayout;

    /*
     * Managed by this class
     */
    protected final @NotNull VkImage vkImage;
    protected final @NotNull VkSampler vkSampler;
    protected final VkImageView vkImageView;


    public VulkanSamplerImage(
            @NotNull VkInstance vkInstance, @NotNull Device device,
            @NotNull String debugName, int size, @NotNull VkFormat vkFormat
    ) {
        super(vkInstance, device, debugName, size);
        this.vkFormat = vkFormat;
        this.currentLayout = VkImageLayout.UNDEFINED;

        this.vkImageView = allocate(new VkImageView());

        this.vkImage = allocate(new VkImage());
        this.vkSampler = allocate(new VkSampler());
    }

    public void createSampler(@NotNull Stack stack) {
        VkSamplerCreateInfo samplerCreateInfo = stack.push(new VkSamplerCreateInfo());
        samplerCreateInfo.sType.set(VkStructureType.SAMPLER_CREATE_INFO);
        samplerCreateInfo.magFilter.set(VkFilter.NEAREST);
        samplerCreateInfo.minFilter.set(VkFilter.NEAREST);

        samplerCreateInfo.addressModeU.set(VkSamplerAddressMode.REPEAT);
        samplerCreateInfo.addressModeV.set(VkSamplerAddressMode.REPEAT);
        samplerCreateInfo.addressModeW.set(VkSamplerAddressMode.REPEAT);

        samplerCreateInfo.anisotropyEnable.set(VulkanUtils.booleanToVkBool32(true));

        //TODO: this should be something the game specifies
        samplerCreateInfo.maxAnisotropy.set(device.getPhysicalDeviceProperties().limits.maxSamplerAnisotropy.get());

        samplerCreateInfo.borderColor.set(VkBorderColor.INT_OPAQUE_BLACK);

        samplerCreateInfo.unnormalizedCoordinates.set(VulkanUtils.booleanToVkBool32(false));

        samplerCreateInfo.compareEnable.set(VulkanUtils.booleanToVkBool32(false));
        samplerCreateInfo.compareOp.set(VkCompareOp.ALWAYS);

        samplerCreateInfo.mipmapMode.set(VkSamplerMipmapMode.LINEAR);
        samplerCreateInfo.mipLodBias.set(0f);
        samplerCreateInfo.minLod.set(0f);
        samplerCreateInfo.maxLod.set(0f);

        vkInstance.vkCreateSampler(device.getVkDevice(), ref(samplerCreateInfo), ref(null), ref(vkSampler)).check();

        stack.pop(); // samplerCreateInfo
    }

    public void createImageView(@NotNull Stack stack) {
        VkImageViewCreateInfo viewCreateInfo = stack.push(new VkImageViewCreateInfo());
        viewCreateInfo.sType.set(VkStructureType.IMAGE_VIEW_CREATE_INFO);
        viewCreateInfo.image.set(vkImage);
        viewCreateInfo.viewType.set(VkImageViewType.TYPE_2D);
        viewCreateInfo.format.set(vkFormat);
        viewCreateInfo.subresourceRange.aspectMask.set(VkImageAspectFlagBits.VK_IMAGE_ASPECT_COLOR_BIT);
        viewCreateInfo.subresourceRange.baseMipLevel.set(0);
        viewCreateInfo.subresourceRange.levelCount.set(1);
        viewCreateInfo.subresourceRange.baseArrayLayer.set(0);
        viewCreateInfo.subresourceRange.layerCount.set(1);

        vkInstance.vkCreateImageView(device.getVkDevice(), ref(viewCreateInfo), ref(null), ref(vkImageView)).check();

        stack.pop(); // viewCreateInfo

    }

    public void transitionLayoutCommand(
            @NotNull Stack stack,
            @NotNull VkCommandBuffer vkCommandBuffer,
            @NotNull VkImageLayout layoutToTransitionTo
    ) {
        VkImageMemoryBarrier barrier = stack.push(new VkImageMemoryBarrier());
        barrier.sType.set(VkStructureType.IMAGE_MEMORY_BARRIER);
        barrier.oldLayout.set(currentLayout);
        barrier.newLayout.set(layoutToTransitionTo);
        barrier.srcQueueFamilyIndex.set(APIConstants.VK_QUEUE_FAMILY_IGNORED);
        barrier.dstQueueFamilyIndex.set(APIConstants.VK_QUEUE_FAMILY_IGNORED);
        barrier.image.set(vkImage);
        barrier.subresourceRange.aspectMask.set(VkImageAspectFlagBits.VK_IMAGE_ASPECT_COLOR_BIT);
        barrier.subresourceRange.baseMipLevel.set(0);
        barrier.subresourceRange.levelCount.set(1);
        barrier.subresourceRange.baseArrayLayer.set(0);
        barrier.subresourceRange.layerCount.set(1);

        VkPipelineStageFlags sourceStage = stack.push(new VkPipelineStageFlags());
        VkPipelineStageFlags destinationStage = stack.push(new VkPipelineStageFlags());
        VkDependencyFlags vkDependencyFlags = stack.push(new VkDependencyFlags());

        if (currentLayout == VkImageLayout.UNDEFINED && layoutToTransitionTo == VkImageLayout.TRANSFER_DST_OPTIMAL) {
            barrier.srcAccessMask.set(0);
            barrier.dstAccessMask.set(VkAccessFlagBits.VK_ACCESS_TRANSFER_WRITE_BIT);

            sourceStage.set(VkPipelineStageFlagBits.VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT);
            destinationStage.set(VkPipelineStageFlagBits.VK_PIPELINE_STAGE_TRANSFER_BIT);

        } else if (currentLayout == VkImageLayout.TRANSFER_DST_OPTIMAL && layoutToTransitionTo == VkImageLayout.SHADER_READ_ONLY_OPTIMAL) {
            barrier.srcAccessMask.set(VkAccessFlagBits.VK_ACCESS_TRANSFER_WRITE_BIT);
            barrier.dstAccessMask.set(VkAccessFlagBits.VK_ACCESS_SHADER_READ_BIT);

            sourceStage.set(VkPipelineStageFlagBits.VK_PIPELINE_STAGE_TRANSFER_BIT);
            destinationStage.set(VkPipelineStageFlagBits.VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT);

        } else {
            throw new IllegalArgumentException("Unsupported layout transition!");
        }

        vkInstance.vkCmdPipelineBarrier(
                vkCommandBuffer,
                sourceStage,
                destinationStage,
                vkDependencyFlags,
                0, ref(null),
                0, ref(null),
                1, ref(barrier)
        );

        currentLayout = layoutToTransitionTo;

        stack.pop(); // vkDependencyFlags
        stack.pop(); // destinationStage
        stack.pop(); // sourceStage
        stack.pop(); // barrier
    }



    @Override
    protected void bind(@NotNull Stack stack, @NotNull VkDeviceMemory vkDeviceMemory) {
        vkInstance.vkBindImageMemory(device.getVkDevice(), vkImage, vkDeviceMemory, offset);

        createImageView(stack);
        createSampler(stack);
    }

    public @NotNull VkImage getVkImage() {
        return vkImage;
    }

    @Override
    public void close() {
        super.close();
        vkInstance.vkDestroySampler(device.getVkDevice(), vkSampler, ref(null));
        vkInstance.vkDestroyImageView(device.getVkDevice(), vkImageView, ref(null));
        vkInstance.vkDestroyImage(device.getVkDevice(), vkImage, ref(null));
    }
}
