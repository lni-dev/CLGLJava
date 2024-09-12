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

package de.linusdev.cvg4j.engine.vk.memory.image.sampler;

import de.linusdev.cvg4j.engine.vk.descriptor.pool.ShaderBinding;
import de.linusdev.cvg4j.engine.vk.device.Device;
import de.linusdev.cvg4j.engine.vk.memory.buffer.BufferStructInput;
import de.linusdev.cvg4j.engine.vk.memory.image.ImageOutput;
import de.linusdev.cvg4j.nat.vulkan.bitmasks.enums.VkImageAspectFlagBits;
import de.linusdev.cvg4j.nat.vulkan.bitmasks.enums.VkShaderStageFlagBits;
import de.linusdev.cvg4j.nat.vulkan.enums.VkDescriptorType;
import de.linusdev.cvg4j.nat.vulkan.enums.VkImageLayout;
import de.linusdev.cvg4j.nat.vulkan.enums.VkStructureType;
import de.linusdev.cvg4j.nat.vulkan.handles.VkCommandBuffer;
import de.linusdev.cvg4j.nat.vulkan.handles.VkDescriptorSet;
import de.linusdev.cvg4j.nat.vulkan.handles.VkInstance;
import de.linusdev.cvg4j.nat.vulkan.structs.*;
import de.linusdev.lutils.image.ImageSize;
import de.linusdev.lutils.nat.array.NativeArray;
import de.linusdev.lutils.nat.memory.stack.Stack;
import de.linusdev.lutils.nat.struct.abstracts.Structure;
import org.jetbrains.annotations.NotNull;

import static de.linusdev.lutils.nat.pointer.TypedPointer64.ref;

public class Sampler2D<S extends Structure> implements ShaderBinding {

    private final @NotNull VkInstance vkInstance;
    private final @NotNull Device device;

    private final @NotNull BufferStructInput<S> input;
    private final @NotNull ImageOutput output;

    private VkDescriptorSet vkDescriptorSet;

    /*
     * Information stored in this class
     */
    private final int binding;
    private final @NotNull ImageSize imageSize;
    private final @NotNull VkImageLayout layout;

    public Sampler2D(
            @NotNull VkInstance vkInstance, @NotNull Device device,
            @NotNull BufferStructInput<S> input, @NotNull ImageOutput output,
            int binding, @NotNull ImageSize imageSize, @NotNull VkImageLayout layout
    ) {
        this.vkInstance = vkInstance;
        this.device = device;
        this.input = input;
        this.output = output;
        this.binding = binding;
        this.imageSize = imageSize;

        this.layout = layout;
    }

    public void bufferCopyCommand(@NotNull Stack stack, @NotNull VkCommandBuffer vkCommandBuffer) {

        output.getImage().transitionLayoutCommand(stack, vkCommandBuffer, VkImageLayout.TRANSFER_DST_OPTIMAL);

        VkBufferImageCopy region = stack.push(new VkBufferImageCopy());
        region.bufferOffset.set(0);
        region.bufferRowLength.set(0);
        region.bufferImageHeight.set(0);

        region.imageSubresource.aspectMask.set(VkImageAspectFlagBits.VK_IMAGE_ASPECT_COLOR_BIT);
        region.imageSubresource.mipLevel.set(0);
        region.imageSubresource.baseArrayLayer.set(0);
        region.imageSubresource.layerCount.set(1);

        region.imageOffset.x.set(0);
        region.imageOffset.y.set(0);
        region.imageOffset.z.set(0);
        region.imageExtent.width.set(imageSize.getWidth());
        region.imageExtent.height.set(imageSize.getHeight());
        region.imageExtent.depth.set(1);

        vkInstance.vkCmdCopyBufferToImage(
                vkCommandBuffer,
                input.getVkBuffer(),
                output.getVkImage(),
                VkImageLayout.TRANSFER_DST_OPTIMAL,
                1,
                ref(region)
        );

        stack.pop(); // region

        output.getImage().transitionLayoutCommand(stack, vkCommandBuffer, layout);

    }

    public @NotNull BufferStructInput<S> getInput() {
        return input;
    }

    public @NotNull ImageOutput getOutput() {
        return output;
    }

    @Override
    public @NotNull VkDescriptorType descriptorType() {
        return VkDescriptorType.COMBINED_IMAGE_SAMPLER;
    }

    @Override
    public int descriptorCount() {
        return 1;
    }

    @Override
    public void createDescriptorSetBinding(@NotNull VkDescriptorSetLayoutBinding binding) {
        binding.binding.set(this.binding);
        binding.descriptorType.set(VkDescriptorType.COMBINED_IMAGE_SAMPLER);
        binding.descriptorCount.set(1);
        binding.stageFlags.set(VkShaderStageFlagBits.VK_SHADER_STAGE_FRAGMENT_BIT);
        binding.pImmutableSamplers.set(null);
    }

    @Override
    public void updateDescriptorSets(
            @NotNull Stack stack,
            @NotNull NativeArray<VkDescriptorSet> vkDescriptorSets,
            @NotNull NativeArray<VkWriteDescriptorSet> writes
    ) {
        this.vkDescriptorSet = vkDescriptorSets.get(0);
        VkDescriptorImageInfo imageInfo = stack.push(new VkDescriptorImageInfo());
        imageInfo.imageLayout.set(layout);
        imageInfo.imageView.set(output.getImage().getVkImageView());
        imageInfo.sampler.set(output.getImage().getVkSampler());


        VkWriteDescriptorSet writeDescriptorSet = writes.get(0);
        writeDescriptorSet.sType.set(VkStructureType.WRITE_DESCRIPTOR_SET);
        writeDescriptorSet.dstSet.set(vkDescriptorSet);
        writeDescriptorSet.dstBinding.set(binding);
        writeDescriptorSet.dstArrayElement.set(0);
        writeDescriptorSet.descriptorType.set(VkDescriptorType.COMBINED_IMAGE_SAMPLER);
        writeDescriptorSet.descriptorCount.set(1);
        writeDescriptorSet.pImageInfo.set(imageInfo);

    }

    @Override
    public void popUpdateDescriptorSets(@NotNull Stack stack, int count) {
        stack.pop(); // imageInfo
    }

    public VkDescriptorSet getVkDescriptorSet() {
        return vkDescriptorSet;
    }
}
