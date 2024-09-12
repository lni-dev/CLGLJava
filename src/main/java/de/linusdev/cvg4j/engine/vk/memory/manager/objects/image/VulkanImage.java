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

package de.linusdev.cvg4j.engine.vk.memory.manager.objects.image;

import de.linusdev.cvg4j.engine.exception.EngineException;
import de.linusdev.cvg4j.engine.vk.device.Device;
import de.linusdev.cvg4j.engine.vk.memory.manager.MemoryRequirementsChange;
import de.linusdev.cvg4j.engine.vk.memory.manager.VulkanMemoryBoundObject;
import de.linusdev.cvg4j.nat.vulkan.bitmasks.VkDependencyFlags;
import de.linusdev.cvg4j.nat.vulkan.bitmasks.VkPipelineStageFlags;
import de.linusdev.cvg4j.nat.vulkan.bitmasks.enums.*;
import de.linusdev.cvg4j.nat.vulkan.constants.APIConstants;
import de.linusdev.cvg4j.nat.vulkan.enums.*;
import de.linusdev.cvg4j.nat.vulkan.handles.VkCommandBuffer;
import de.linusdev.cvg4j.nat.vulkan.handles.VkDeviceMemory;
import de.linusdev.cvg4j.nat.vulkan.handles.VkImage;
import de.linusdev.cvg4j.nat.vulkan.handles.VkImageView;
import de.linusdev.cvg4j.nat.vulkan.structs.VkImageCreateInfo;
import de.linusdev.cvg4j.nat.vulkan.structs.VkImageMemoryBarrier;
import de.linusdev.cvg4j.nat.vulkan.structs.VkImageViewCreateInfo;
import de.linusdev.cvg4j.nat.vulkan.structs.VkMemoryRequirements;
import de.linusdev.lutils.bitfield.IntBitfield;
import de.linusdev.lutils.image.ImageSize;
import de.linusdev.lutils.nat.memory.stack.Stack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static de.linusdev.cvg4j.engine.vk.memory.manager.VulkanMemoryBoundObject.State.*;
import static de.linusdev.lutils.nat.pointer.TypedPointer64.ref;
import static de.linusdev.lutils.nat.struct.abstracts.Structure.allocate;

public class VulkanImage extends VulkanMemoryBoundObject {

    /*
     * Information stored in this class
     */
    protected @NotNull ImageSize imageSize;
    protected final @NotNull IntBitfield<VkImageUsageFlagBits> usage;
    protected final @NotNull IntBitfield<VkImageAspectFlagBits> viewAspectMask;
    protected final @NotNull VkImageTiling vkImageTiling;
    /**
     * {@link VkFormat} {@link #vkImage} uses.
     */
    protected final @NotNull VkFormat vkFormat;
    /**
     * The {@link VkImageLayout} {@link #vkImage} is currently in.
     */
    protected @NotNull VkImageLayout currentLayout;

    /*
     * Managed by this class
     */
    protected final @NotNull VkImage vkImage;
    protected final VkImageView vkImageView;

    public VulkanImage(
            @NotNull Device device, @NotNull String debugName, int size, @NotNull ImageSize imageSize,
            @NotNull IntBitfield<VkImageUsageFlagBits> usage,
            @NotNull IntBitfield<VkImageAspectFlagBits> viewAspectMask, @NotNull VkImageTiling vkImageTiling,
            @NotNull VkFormat vkFormat
    ) {
        super(device, debugName, size);
        this.imageSize = imageSize;
        this.usage = usage;
        this.viewAspectMask = viewAspectMask;
        this.vkImageTiling = vkImageTiling;
        this.vkFormat = vkFormat;
        this.currentLayout = VkImageLayout.UNDEFINED;

        this.vkImageView = allocate(new VkImageView());
        this.vkImage = allocate(new VkImage());
    }

    public VulkanImage create(@NotNull Stack stack) {
        assert assertState(NOT_CREATED);
        recreate(stack, null, null);
        return this;
    }

    public void recreate(
            @NotNull Stack stack,
            @Nullable Integer newSizeInBytes,
            @Nullable ImageSize newImageSize
    ) {
        assert (newSizeInBytes == null && newImageSize == null) || (newSizeInBytes != null && newImageSize != null);

        close();
        state = State.RECREATED;

        MemoryRequirementsChange change = null;
        if(newImageSize != null) {
             change = new MemoryRequirementsChange(
                    offset.get(),
                    actualSize.get(), requiredAlignment
             );

            // update information
            imageSize = newImageSize;
            size = newSizeInBytes;
        }

        VkImageCreateInfo imageCreateInfo = stack.push(new VkImageCreateInfo());
        imageCreateInfo.sType.set(VkStructureType.IMAGE_CREATE_INFO);
        imageCreateInfo.imageType.set(VkImageType.TYPE_2D);
        imageCreateInfo.extent.width.set(imageSize.getWidth());
        imageCreateInfo.extent.height.set(imageSize.getHeight());
        imageCreateInfo.extent.depth.set(1);
        imageCreateInfo.mipLevels.set(1);
        imageCreateInfo.arrayLayers.set(1);
        imageCreateInfo.format.set(vkFormat);
        imageCreateInfo.tiling.set(vkImageTiling);
        imageCreateInfo.initialLayout.set(VkImageLayout.UNDEFINED);
        imageCreateInfo.usage.replaceWith(usage);
        imageCreateInfo.samples.set(VkSampleCountFlagBits.COUNT_1);
        imageCreateInfo.sharingMode.set(VkSharingMode.EXCLUSIVE);

        vkInstance.vkCreateImage(device.getVkDevice(), ref(imageCreateInfo), ref(null), ref(vkImage)).check();

        stack.pop(); // imageCreateInfo

        if(change != null) {
            VkMemoryRequirements memoryRequirements = stack.push(new VkMemoryRequirements());
            vkInstance.vkGetImageMemoryRequirements(device.getVkDevice(), getVkImage(), ref(memoryRequirements));
            memoryRequirements(memoryRequirements);

            change.newRequiredSize = memoryRequirements.size.get();
            change.newRequiredAlignment = memoryRequirements.alignment.get();

            stack.pop(); // memoryRequirements
        }

        if(memoryTypeManager != null)
            memoryTypeManager.onChanged(stack, this, change);
    }

    @Override
    protected void bind(@NotNull Stack stack, @NotNull VkDeviceMemory vkDeviceMemory) {
        super.bind(stack, vkDeviceMemory);
        vkInstance.vkBindImageMemory(device.getVkDevice(), vkImage, vkDeviceMemory, offset);
        createImageView(stack);
    }

    @Override
    protected void unbind(@NotNull Stack stack) {
        super.unbind(stack);
        recreate(stack, null, null);
    }

    @Override
    public int calculateMemoryTypeIndex(
            @NotNull Stack stack,
            @NotNull IntBitfield<VkMemoryPropertyFlagBits> memFlags
    ) throws EngineException {
        assert assertStatePast(RECREATED);
        VkMemoryRequirements memoryRequirements = stack.push(new VkMemoryRequirements());
        vkInstance.vkGetImageMemoryRequirements(device.getVkDevice(), vkImage, ref(memoryRequirements));
        int memoryTypeIndex = device.findMemoryType(stack, memoryRequirements.memoryTypeBits.get(), memFlags);
        memoryRequirements(memoryRequirements);
        stack.pop(); // memoryRequirements
        return memoryTypeIndex;
    }

    protected void createImageView(@NotNull Stack stack) {
        assert assertStatePast(BOUND);

        VkImageViewCreateInfo viewCreateInfo = stack.push(new VkImageViewCreateInfo());
        viewCreateInfo.sType.set(VkStructureType.IMAGE_VIEW_CREATE_INFO);
        viewCreateInfo.image.set(vkImage);
        viewCreateInfo.viewType.set(VkImageViewType.TYPE_2D);
        viewCreateInfo.format.set(vkFormat);
        viewCreateInfo.subresourceRange.aspectMask.replaceWith(viewAspectMask);
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
        assert assertStatePast(BOUND);

        VkImageMemoryBarrier barrier = stack.push(new VkImageMemoryBarrier());
        barrier.sType.set(VkStructureType.IMAGE_MEMORY_BARRIER);
        barrier.oldLayout.set(currentLayout);
        barrier.newLayout.set(layoutToTransitionTo);
        barrier.srcQueueFamilyIndex.set(APIConstants.VK_QUEUE_FAMILY_IGNORED);
        barrier.dstQueueFamilyIndex.set(APIConstants.VK_QUEUE_FAMILY_IGNORED);
        barrier.image.set(vkImage);
        barrier.subresourceRange.aspectMask.set(VkImageAspectFlagBits.COLOR);
        barrier.subresourceRange.baseMipLevel.set(0);
        barrier.subresourceRange.levelCount.set(1);
        barrier.subresourceRange.baseArrayLayer.set(0);
        barrier.subresourceRange.layerCount.set(1);

        VkPipelineStageFlags sourceStage = stack.push(new VkPipelineStageFlags());
        VkPipelineStageFlags destinationStage = stack.push(new VkPipelineStageFlags());
        VkDependencyFlags vkDependencyFlags = stack.push(new VkDependencyFlags());

        if (currentLayout == VkImageLayout.UNDEFINED && layoutToTransitionTo == VkImageLayout.TRANSFER_DST_OPTIMAL) {
            barrier.srcAccessMask.set(0);
            barrier.dstAccessMask.set(VkAccessFlagBits.TRANSFER_WRITE);

            sourceStage.set(VkPipelineStageFlagBits.TOP_OF_PIPE);
            destinationStage.set(VkPipelineStageFlagBits.TRANSFER);

        } else if (currentLayout == VkImageLayout.TRANSFER_DST_OPTIMAL && layoutToTransitionTo == VkImageLayout.SHADER_READ_ONLY_OPTIMAL) {
            barrier.srcAccessMask.set(VkAccessFlagBits.TRANSFER_WRITE);
            barrier.dstAccessMask.set(VkAccessFlagBits.SHADER_READ);

            sourceStage.set(VkPipelineStageFlagBits.TRANSFER);
            destinationStage.set(VkPipelineStageFlagBits.FRAGMENT_SHADER);

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
    public void close() {
        if(state.isPast(RECREATED))
            vkInstance.vkDestroyImageView(device.getVkDevice(), vkImageView, ref(null));
        if(state.isPast(BOUND))
            vkInstance.vkDestroyImage(device.getVkDevice(), vkImage, ref(null));
        super.close();
    }

    public @NotNull VkImage getVkImage() {
        return vkImage;
    }

    public VkImageView getVkImageView() {
        return vkImageView;
    }

    public @NotNull VkFormat getFormat() {
        return vkFormat;
    }

    public @NotNull VkImageTiling getImageTiling() {
        return vkImageTiling;
    }
}
