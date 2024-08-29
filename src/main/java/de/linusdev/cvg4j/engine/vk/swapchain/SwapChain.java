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

package de.linusdev.cvg4j.engine.vk.swapchain;

import de.linusdev.cvg4j.nat.vulkan.bitmasks.enums.VkImageAspectFlagBits;
import de.linusdev.cvg4j.nat.vulkan.enums.VkComponentSwizzle;
import de.linusdev.cvg4j.nat.vulkan.enums.VkImageViewType;
import de.linusdev.cvg4j.nat.vulkan.enums.VkStructureType;
import de.linusdev.cvg4j.nat.vulkan.handles.*;
import de.linusdev.cvg4j.nat.vulkan.structs.VkImageViewCreateInfo;
import de.linusdev.cvg4j.nat.vulkan.structs.VkSwapchainCreateInfoKHR;
import de.linusdev.lutils.math.vector.buffer.intn.BBUInt1;
import de.linusdev.lutils.nat.memory.Stack;
import de.linusdev.lutils.nat.struct.array.StructureArray;
import org.jetbrains.annotations.NotNull;

import static de.linusdev.lutils.nat.pointer.TypedPointer64.ofArray;
import static de.linusdev.lutils.nat.pointer.TypedPointer64.ref;
import static de.linusdev.lutils.nat.struct.abstracts.Structure.allocate;

public class SwapChain implements AutoCloseable {

    private final @NotNull VkInstance vkInstance;
    private final @NotNull VkDevice device;
    private final @NotNull VkSwapchainKHR swapChain;
    private final @NotNull StructureArray<VkImageView> swapChainImageViews;



    public SwapChain(
            @NotNull VkInstance vkInstance,
            @NotNull VkDevice device,
            @NotNull VkSwapchainKHR swapChain,
            @NotNull StructureArray<VkImageView> swapChainImageViews
    ) {
        this.vkInstance = vkInstance;
        this.device = device;
        this.swapChain = swapChain;


        this.swapChainImageViews = swapChainImageViews;
    }

    public static @NotNull SwapChain create(
            @NotNull Stack stack,
            @NotNull VkInstance vkInstance,
            @NotNull VkDevice device,
            @NotNull VkSwapchainCreateInfoKHR swapChainCreateInfo
    ) {
        VkSwapchainKHR swapChain = allocate(new VkSwapchainKHR());
        vkInstance.vkCreateSwapchainKHR(device, ref(swapChainCreateInfo), ref(null), ref(swapChain)).check();

        BBUInt1 integer = stack.pushUnsignedInt();

        vkInstance.vkGetSwapchainImagesKHR(device, swapChain, ref(integer), ref(null));
        StructureArray<VkImage> swapchainImages = stack.pushArray(integer.get(), VkImage.class, VkImage::new);

        vkInstance.vkGetSwapchainImagesKHR(device, swapChain, ref(integer), ofArray(swapchainImages));
        StructureArray<VkImageView> swapchainImageViews = StructureArray.newAllocated(swapchainImages.length(), VkImageView.class, VkImageView::new);

        VkImageViewCreateInfo imageViewCreateInfo = stack.push(new VkImageViewCreateInfo());
        for (int i = 0; i < swapchainImageViews.length(); i++) {

            imageViewCreateInfo.sType.set(VkStructureType.IMAGE_VIEW_CREATE_INFO);
            imageViewCreateInfo.image.set(swapchainImages.getOrCreate(i).get());
            imageViewCreateInfo.viewType.set(VkImageViewType.TYPE_2D);
            imageViewCreateInfo.format.set(swapChainCreateInfo.imageFormat);
            imageViewCreateInfo.components.r.set(VkComponentSwizzle.IDENTITY);
            imageViewCreateInfo.components.g.set(VkComponentSwizzle.IDENTITY);
            imageViewCreateInfo.components.b.set(VkComponentSwizzle.IDENTITY);
            imageViewCreateInfo.components.a.set(VkComponentSwizzle.IDENTITY);
            imageViewCreateInfo.subresourceRange.aspectMask.set(VkImageAspectFlagBits.VK_IMAGE_ASPECT_COLOR_BIT);
            imageViewCreateInfo.subresourceRange.baseMipLevel.set(0);
            imageViewCreateInfo.subresourceRange.levelCount.set(1);
            imageViewCreateInfo.subresourceRange.baseArrayLayer.set(0);
            imageViewCreateInfo.subresourceRange.layerCount.set(1);

            vkInstance.vkCreateImageView(device, ref(imageViewCreateInfo), ref(null), ref(swapchainImageViews.getOrCreate(i))).check();
        }
        stack.pop(); // vkImageViewCreateInfo

        stack.pop(); // swapchainImages
        stack.pop(); // integer

        return new SwapChain(vkInstance, device, swapChain, swapchainImageViews);
    }

    @Override
    public void close() {
        for (VkImageView view : swapChainImageViews) {
            vkInstance.vkDestroyImageView(device, view, ref(null));
        }
        vkInstance.vkDestroySwapchainKHR(device, swapChain, ref(null));
    }
}
