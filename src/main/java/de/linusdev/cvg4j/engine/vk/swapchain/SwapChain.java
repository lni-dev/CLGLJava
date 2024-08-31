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

import de.linusdev.cvg4j.engine.vk.VulkanRasterizationWindow;
import de.linusdev.cvg4j.engine.vk.device.Device;
import de.linusdev.cvg4j.engine.vk.device.Extend2D;
import de.linusdev.cvg4j.nat.vulkan.bitmasks.enums.VkCompositeAlphaFlagBitsKHR;
import de.linusdev.cvg4j.nat.vulkan.bitmasks.enums.VkImageAspectFlagBits;
import de.linusdev.cvg4j.nat.vulkan.bitmasks.enums.VkImageUsageFlagBits;
import de.linusdev.cvg4j.nat.vulkan.bitmasks.enums.VkSurfaceTransformFlagBitsKHR;
import de.linusdev.cvg4j.nat.vulkan.enums.*;
import de.linusdev.cvg4j.nat.vulkan.handles.*;
import de.linusdev.cvg4j.nat.vulkan.structs.VkImageViewCreateInfo;
import de.linusdev.cvg4j.nat.vulkan.structs.VkSwapchainCreateInfoKHR;
import de.linusdev.cvg4j.nat.vulkan.utils.VulkanUtils;
import de.linusdev.lutils.math.vector.buffer.intn.BBUInt1;
import de.linusdev.lutils.nat.array.NativeInt32Array;
import de.linusdev.lutils.nat.enums.EnumValue32;
import de.linusdev.lutils.nat.enums.JavaEnumValue32;
import de.linusdev.lutils.nat.memory.Stack;
import de.linusdev.lutils.nat.struct.annos.SVWrapper;
import de.linusdev.lutils.nat.struct.array.StructureArray;
import org.jetbrains.annotations.NotNull;

import static de.linusdev.lutils.nat.pointer.TypedPointer64.ofArray;
import static de.linusdev.lutils.nat.pointer.TypedPointer64.ref;
import static de.linusdev.lutils.nat.struct.abstracts.Structure.allocate;

public class SwapChain implements AutoCloseable {

    public static @NotNull SwapChain create(
            @NotNull Stack stack,
            @NotNull VkInstance vkInstance,
            @NotNull VulkanRasterizationWindow window,
            @NotNull Device device,
            int graphicsQueueIndex,
            int presentationQueueIndex,
            int swapChainImageCount,
            @NotNull EnumValue32<VkFormat> format,
            @NotNull EnumValue32<VkColorSpaceKHR> colorSpace,
            @NotNull Extend2D swapChainExtend,
            EnumValue32<VkSurfaceTransformFlagBitsKHR> surfaceTransform,
            EnumValue32<VkPresentModeKHR> presentMode
    ) {
        SwapChain swapChain = new SwapChain(vkInstance, device, swapChainImageCount, format, colorSpace);

        // Create SwapChain
        NativeInt32Array queueFamilyIndices = stack.push(NativeInt32Array.newAllocatable(SVWrapper.length(2)));
        queueFamilyIndices.setInt(0, graphicsQueueIndex);
        queueFamilyIndices.setInt(1, presentationQueueIndex);
        boolean sameQueueIndices = graphicsQueueIndex == presentationQueueIndex;

        VkSwapchainCreateInfoKHR swapChainCreateInfo = stack.push(new VkSwapchainCreateInfoKHR());
        swapChainCreateInfo.sType.set(VkStructureType.SWAPCHAIN_CREATE_INFO_KHR);
        swapChainCreateInfo.surface.set(window.getVkSurface().get());
        swapChainCreateInfo.minImageCount.set(swapChainImageCount);
        swapChainCreateInfo.imageFormat.set(format);
        swapChainCreateInfo.imageColorSpace.set(colorSpace);
        swapChainCreateInfo.imageExtent.height.set(swapChainExtend.height());
        swapChainCreateInfo.imageExtent.width.set(swapChainExtend.width());
        swapChainCreateInfo.imageArrayLayers.set(1);
        // Write directly to this image
        // TODO: make it possible to enable post processing
        // If we want to do postprocessing, we would need to write to a different image and then
        // transfer into this one. That would mean we would need to set this to VK_IMAGE_USAGE_TRANSFER_DST_BIT
        swapChainCreateInfo.imageUsage.set(VkImageUsageFlagBits.VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT);

        if(sameQueueIndices) {
            swapChainCreateInfo.imageSharingMode.set(VkSharingMode.EXCLUSIVE);
        } else {
            swapChainCreateInfo.imageSharingMode.set(VkSharingMode.CONCURRENT);
            swapChainCreateInfo.queueFamilyIndexCount.set(2);
            swapChainCreateInfo.pQueueFamilyIndices.set(queueFamilyIndices.getPointer());
        }

        swapChainCreateInfo.preTransform.set(surfaceTransform);
        swapChainCreateInfo.compositeAlpha.set(VkCompositeAlphaFlagBitsKHR.VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR);
        swapChainCreateInfo.presentMode.set(presentMode);
        swapChainCreateInfo.clipped.set(VulkanUtils.booleanToVkBool32(true));
        swapChainCreateInfo.oldSwapchain.set(0L); // required when window was resized. see https://vulkan-tutorial.com/en/Drawing_a_triangle/Swap_chain_recreation

        vkInstance.vkCreateSwapchainKHR(device.getVkDevice(), ref(swapChainCreateInfo), ref(null), ref(swapChain.vkSwapChain)).check();

        // Create Swap chain image views
        BBUInt1 integer = stack.pushUnsignedInt();

        // Get Swap chain images
        vkInstance.vkGetSwapchainImagesKHR(device.getVkDevice(), swapChain.vkSwapChain, ref(integer), ref(null));
        StructureArray<VkImage> swapchainImages = stack.pushArray(integer.get(), VkImage.class, VkImage::new);
        vkInstance.vkGetSwapchainImagesKHR(device.getVkDevice(), swapChain.vkSwapChain, ref(integer), ofArray(swapchainImages));

        if(swapChainImageCount != swapchainImages.length())
            throw new Error("Set and actual swap chain image count does not match.");

        VkImageViewCreateInfo imageViewCreateInfo = stack.push(new VkImageViewCreateInfo());
        for (int i = 0; i < swapChain.swapChainImageViews.length(); i++) {

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

            vkInstance.vkCreateImageView(device.getVkDevice(), ref(imageViewCreateInfo), ref(null), ref(swapChain.swapChainImageViews.getOrCreate(i))).check();
        }

        stack.pop(); // vkImageViewCreateInfo
        stack.pop(); // swapchainImages
        stack.pop(); // integer
        stack.pop(); // swapChainCreateInfo
        stack.pop(); // queueFamilyIndices

        return swapChain;
    }

    private final @NotNull VkInstance vkInstance;
    private final @NotNull Device device;

    /*
     * Managed by this class
     */
    private final @NotNull VkSwapchainKHR vkSwapChain;
    private final @NotNull StructureArray<VkImageView> swapChainImageViews;

    /*
     * Information about this swap chain
     */
    private final int swapChainImageCount;
    private final @NotNull EnumValue32<VkFormat> format;
    private final @NotNull EnumValue32<VkColorSpaceKHR> colorSpace;

    public SwapChain(
            @NotNull VkInstance vkInstance,
            @NotNull Device device,
            int swapChainImageCount,
            @NotNull EnumValue32<VkFormat> format,
            @NotNull EnumValue32<VkColorSpaceKHR> colorSpace
    ) {
        this.vkInstance = vkInstance;
        this.device = device;

        this.vkSwapChain = allocate(new VkSwapchainKHR());
        this.swapChainImageViews = StructureArray.newAllocated(swapChainImageCount, VkImageView.class, VkImageView::new);

        this.swapChainImageCount = swapChainImageCount;

        this.format = new JavaEnumValue32<>();
        this.colorSpace = new JavaEnumValue32<>();

        this.format.set(format);
        this.colorSpace.set(colorSpace);
    }

    public @NotNull EnumValue32<VkColorSpaceKHR> getColorSpace() {
        return colorSpace;
    }

    public @NotNull EnumValue32<VkFormat> getFormat() {
        return format;
    }

    public @NotNull StructureArray<VkImageView> getSwapChainImageViews() {
        return swapChainImageViews;
    }

    public @NotNull VkSwapchainKHR getVkSwapChain() {
        return vkSwapChain;
    }

    public int getSwapChainImageCount() {
        return swapChainImageCount;
    }

    @Override
    public void close() {
        for (VkImageView view : swapChainImageViews) {
            vkInstance.vkDestroyImageView(device.getVkDevice(), view, ref(null));
        }
        vkInstance.vkDestroySwapchainKHR(device.getVkDevice(), vkSwapChain, ref(null));
    }
}
