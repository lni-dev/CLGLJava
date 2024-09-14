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

import de.linusdev.cvg4j.engine.vk.device.Device;
import de.linusdev.cvg4j.nat.vulkan.bitmasks.enums.VkImageAspectFlagBits;
import de.linusdev.cvg4j.nat.vulkan.bitmasks.enums.VkImageUsageFlagBits;
import de.linusdev.cvg4j.nat.vulkan.bitmasks.enums.VkSampleCountFlagBits;
import de.linusdev.cvg4j.nat.vulkan.enums.*;
import de.linusdev.cvg4j.nat.vulkan.handles.VkDeviceMemory;
import de.linusdev.cvg4j.nat.vulkan.handles.VkSampler;
import de.linusdev.cvg4j.nat.vulkan.structs.VkSamplerCreateInfo;
import de.linusdev.cvg4j.nat.vulkan.utils.VulkanUtils;
import de.linusdev.lutils.bitfield.IntBitfield;
import de.linusdev.lutils.image.ImageSize;
import de.linusdev.lutils.nat.enums.JavaEnumValue32;
import de.linusdev.lutils.nat.memory.stack.Stack;
import org.jetbrains.annotations.NotNull;

import static de.linusdev.cvg4j.engine.vk.memory.manager.VulkanMemoryBoundObject.State.BOUND;
import static de.linusdev.lutils.nat.pointer.TypedPointer64.ref;
import static de.linusdev.lutils.nat.struct.abstracts.Structure.allocate;

public class VulkanSamplerImage extends VulkanImage {

    /*
     * Managed by this class
     */
    protected final @NotNull VkSampler vkSampler;

    public VulkanSamplerImage(
            @NotNull Device device, @NotNull String debugName, int sizeInBytes, @NotNull ImageSize imageSize,
            @NotNull IntBitfield<VkImageUsageFlagBits> usage, @NotNull IntBitfield<VkImageAspectFlagBits> viewAspectMask,
            @NotNull VkImageTiling vkImageTiling, @NotNull VkFormat vkFormat, boolean generateMipLevels
    ) {
        super(device, debugName, sizeInBytes, imageSize, usage, viewAspectMask, vkImageTiling, vkFormat, generateMipLevels,
                new JavaEnumValue32<>(VkSampleCountFlagBits.COUNT_1));

        this.vkSampler = allocate(new VkSampler());
    }

    @Override
    public VulkanSamplerImage create(@NotNull Stack stack) {
        super.create(stack);
        return this;
    }

    @Override
    protected void bind(@NotNull Stack stack, @NotNull VkDeviceMemory vkDeviceMemory) {
        super.bind(stack, vkDeviceMemory);
        createSampler(stack);
    }

    protected void createSampler(@NotNull Stack stack) {
        assert assertStatePast(BOUND);
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

        //TODO: this must be settable when creating the sampler.
        samplerCreateInfo.mipmapMode.set(VkSamplerMipmapMode.NEAREST);
        samplerCreateInfo.mipLodBias.set(0);
        samplerCreateInfo.minLod.set(0);
        samplerCreateInfo.maxLod.set(mipLevels);

        vkInstance.vkCreateSampler(device.getVkDevice(), ref(samplerCreateInfo), ref(null), ref(vkSampler)).check();

        stack.pop(); // samplerCreateInfo
    }

    public @NotNull VkSampler getVkSampler() {
        return vkSampler;
    }

    @Override
    public void close() {
        if(state.isPast(BOUND))
            vkInstance.vkDestroySampler(device.getVkDevice(), vkSampler, ref(null));
        super.close();
    }
}
