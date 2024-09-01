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
import de.linusdev.cvg4j.engine.vk.objects.HasRecreationListeners;
import de.linusdev.cvg4j.nat.vulkan.bitmasks.enums.VkCompositeAlphaFlagBitsKHR;
import de.linusdev.cvg4j.nat.vulkan.bitmasks.enums.VkImageAspectFlagBits;
import de.linusdev.cvg4j.nat.vulkan.bitmasks.enums.VkImageUsageFlagBits;
import de.linusdev.cvg4j.nat.vulkan.bitmasks.enums.VkSurfaceTransformFlagBitsKHR;
import de.linusdev.cvg4j.nat.vulkan.enums.*;
import de.linusdev.cvg4j.nat.vulkan.handles.VkImage;
import de.linusdev.cvg4j.nat.vulkan.handles.VkImageView;
import de.linusdev.cvg4j.nat.vulkan.handles.VkInstance;
import de.linusdev.cvg4j.nat.vulkan.handles.VkSwapchainKHR;
import de.linusdev.cvg4j.nat.vulkan.structs.VkExtent2D;
import de.linusdev.cvg4j.nat.vulkan.structs.VkImageViewCreateInfo;
import de.linusdev.cvg4j.nat.vulkan.structs.VkSwapchainCreateInfoKHR;
import de.linusdev.cvg4j.nat.vulkan.utils.VulkanUtils;
import de.linusdev.lutils.math.vector.Vector;
import de.linusdev.lutils.math.vector.buffer.intn.BBUInt1;
import de.linusdev.lutils.nat.array.NativeInt32Array;
import de.linusdev.lutils.nat.enums.EnumValue32;
import de.linusdev.lutils.nat.enums.JavaEnumValue32;
import de.linusdev.lutils.nat.memory.Stack;
import de.linusdev.lutils.nat.struct.annos.SVWrapper;
import de.linusdev.lutils.nat.struct.array.StructureArray;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static de.linusdev.lutils.nat.pointer.TypedPointer64.ofArray;
import static de.linusdev.lutils.nat.pointer.TypedPointer64.ref;
import static de.linusdev.lutils.nat.struct.abstracts.Structure.allocate;

public class SwapChain extends HasRecreationListeners<SwapChainRecreationListener> implements AutoCloseable {

    public static @NotNull SwapChain create(
            @NotNull Stack stack,
            @NotNull VkInstance vkInstance,
            @NotNull VulkanRasterizationWindow window,
            @NotNull Device device,
            int swapChainImageCount,
            @NotNull EnumValue32<VkFormat> format,
            @NotNull EnumValue32<VkColorSpaceKHR> colorSpace,
            @NotNull Extend2D swapChainExtend,
            EnumValue32<VkSurfaceTransformFlagBitsKHR> surfaceTransform,
            EnumValue32<VkPresentModeKHR> presentMode
    ) {
        SwapChain swapChain = new SwapChain(
                vkInstance, device, window,
                swapChainImageCount, format, colorSpace, swapChainExtend, surfaceTransform, presentMode
        );

        swapChain.recreate(false, stack, null, null, null, null, null);

        return swapChain;
    }

    private final @NotNull VkInstance vkInstance;
    private final @NotNull Device device;
    private final @NotNull VulkanRasterizationWindow window;

    /*
     * Managed by this class
     */
    private final @NotNull VkSwapchainKHR vkSwapChain;
    private final @NotNull StructureArray<VkImageView> swapChainImageViews;

    private final int swapChainImageCount;
    private final @NotNull EnumValue32<VkFormat> format;
    private final @NotNull EnumValue32<VkColorSpaceKHR> colorSpace;
    private final @NotNull EnumValue32<VkSurfaceTransformFlagBitsKHR> transform;
    private final @NotNull EnumValue32<VkPresentModeKHR> presentMode;
    private final @NotNull Extend2D extend;

    public SwapChain(
            @NotNull VkInstance vkInstance,
            @NotNull Device device,
            @NotNull VulkanRasterizationWindow window,
            int swapChainImageCount,
            @NotNull EnumValue32<VkFormat> format,
            @NotNull EnumValue32<VkColorSpaceKHR> colorSpace,
            @NotNull Extend2D extend,
            @NotNull EnumValue32<VkSurfaceTransformFlagBitsKHR> transform,
            @NotNull EnumValue32<VkPresentModeKHR> presentMode
    ) {
        this.vkInstance = vkInstance;
        this.device = device;
        this.window = window;

        this.vkSwapChain = allocate(new VkSwapchainKHR());
        this.swapChainImageViews = StructureArray.newAllocated(swapChainImageCount, VkImageView.class, VkImageView::new);

        /*
         * Information about this swap chain
         */
        this.swapChainImageCount = swapChainImageCount;

        this.format = new JavaEnumValue32<>();
        this.colorSpace = new JavaEnumValue32<>();
        this.transform = new JavaEnumValue32<>();
        this.presentMode = new JavaEnumValue32<>();
        this.extend = new Extend2D(allocate(new VkExtent2D()));

        this.format.set(format);
        this.colorSpace.set(colorSpace);
        this.transform.set(transform);
        this.presentMode.set(presentMode);
        this.extend.xy(extend.x(), extend.y());
    }

    public void recreate(
            @NotNull Stack stack,
            @Nullable EnumValue32<VkFormat> newFormat,
            @Nullable EnumValue32<VkColorSpaceKHR> newColorSpace,
            @Nullable Extend2D newExtend,
            @Nullable EnumValue32<VkSurfaceTransformFlagBitsKHR> newTransform,
            @Nullable EnumValue32<VkPresentModeKHR> newPresentMode
    ) {
        recreate(true, stack, newFormat, newColorSpace, newExtend, newTransform, newPresentMode);
    }

    protected void recreate(
            boolean destroy,
            @NotNull Stack stack,
            @Nullable EnumValue32<VkFormat> newFormat,
            @Nullable EnumValue32<VkColorSpaceKHR> newColorSpace,
            @Nullable Extend2D newExtend,
            @Nullable EnumValue32<VkSurfaceTransformFlagBitsKHR> newTransform,
            @Nullable EnumValue32<VkPresentModeKHR> newPresentMode
    ) {

        if(destroy) destroyForRecreation();

        boolean isFormatTrulyNew = false;
        boolean isColorSpaceTrulyNew = false;
        boolean isExtendTrulyNew = false;
        boolean isTransformTrulyNew = false;
        boolean isPresentModeTrulyNew = false;

        if(newFormat != null && !format.equals(newFormat)) {
            format.set(newFormat);
            isFormatTrulyNew = true;
        }

        if(newColorSpace != null && !colorSpace.equals(newColorSpace)) {
            colorSpace.set(newColorSpace);
            isColorSpaceTrulyNew = true;
        }

        if(newExtend != null && !Vector.equals(extend, newExtend)) {
            extend.xy(newExtend.x(), newExtend.y());
            isExtendTrulyNew = true;
        }

        if(newTransform != null && !transform.equals(newTransform)) {
            transform.set(newTransform);
            isTransformTrulyNew = true;
        }

        if(newPresentMode != null && !presentMode.equals(newPresentMode)) {
            presentMode.set(newPresentMode);
            isPresentModeTrulyNew = true;
        }

        VkSwapchainKHR oldSwapChain = stack.push(new VkSwapchainKHR());
        oldSwapChain.set(vkSwapChain);

        // Create SwapChain
        NativeInt32Array queueFamilyIndices = stack.push(NativeInt32Array.newAllocatable(SVWrapper.length(2)));
        queueFamilyIndices.setInt(0, device.getGraphicsQueueIndex());
        queueFamilyIndices.setInt(1, device.getPresentationQueueIndex());
        boolean sameQueueIndices = device.getGraphicsQueueIndex() == device.getPresentationQueueIndex();

        VkSwapchainCreateInfoKHR swapChainCreateInfo = stack.push(new VkSwapchainCreateInfoKHR());
        swapChainCreateInfo.sType.set(VkStructureType.SWAPCHAIN_CREATE_INFO_KHR);
        swapChainCreateInfo.surface.set(window.getVkSurface().get());
        swapChainCreateInfo.minImageCount.set(swapChainImageCount);
        swapChainCreateInfo.imageFormat.set(format);
        swapChainCreateInfo.imageColorSpace.set(colorSpace);
        swapChainCreateInfo.imageExtent.height.set(extend.height());
        swapChainCreateInfo.imageExtent.width.set(extend.width());
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

        swapChainCreateInfo.preTransform.set(transform);
        swapChainCreateInfo.compositeAlpha.set(VkCompositeAlphaFlagBitsKHR.VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR);
        swapChainCreateInfo.presentMode.set(presentMode);
        swapChainCreateInfo.clipped.set(VulkanUtils.booleanToVkBool32(true));
        swapChainCreateInfo.oldSwapchain.set(oldSwapChain); // required when window was resized. see https://vulkan-tutorial.com/en/Drawing_a_triangle/Swap_chain_recreation

        vkInstance.vkCreateSwapchainKHR(device.getVkDevice(), ref(swapChainCreateInfo), ref(null), ref(vkSwapChain)).check();

        // Create Swap chain image views
        BBUInt1 integer = stack.pushUnsignedInt();

        // Get Swap chain images
        vkInstance.vkGetSwapchainImagesKHR(device.getVkDevice(), vkSwapChain, ref(integer), ref(null));
        StructureArray<VkImage> swapchainImages = stack.pushArray(integer.get(), VkImage.class, VkImage::new);
        vkInstance.vkGetSwapchainImagesKHR(device.getVkDevice(), vkSwapChain, ref(integer), ofArray(swapchainImages));

        if(swapChainImageCount != swapchainImages.length())
            throw new Error("Set and actual swap chain image count does not match.");

        VkImageViewCreateInfo imageViewCreateInfo = stack.push(new VkImageViewCreateInfo());
        for (int i = 0; i < swapChainImageViews.length(); i++) {

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

            vkInstance.vkCreateImageView(device.getVkDevice(), ref(imageViewCreateInfo), ref(null), ref(swapChainImageViews.getOrCreate(i))).check();
        }

        stack.pop(); // vkImageViewCreateInfo
        stack.pop(); // swapchainImages
        stack.pop(); // integer
        stack.pop(); // swapChainCreateInfo
        stack.pop(); // queueFamilyIndices

        if(!oldSwapChain.isNullHandle()) {
            vkInstance.vkDestroySwapchainKHR(device.getVkDevice(), oldSwapChain, ref(null));
        }

        stack.pop(); // oldSwapChain

        recreationListeners.forEach(listener -> listener.swapChainRecreated(stack));
        if(isExtendTrulyNew)
            recreationListeners.forEach(listener -> listener.swapChainExtendChanged(stack, extend));
        if(isColorSpaceTrulyNew)
            recreationListeners.forEach(listener -> listener.swapChainColorSpaceChanged(stack, colorSpace));
    }

    public @NotNull EnumValue32<VkColorSpaceKHR> getColorSpace() {
        return colorSpace;
    }

    public @NotNull EnumValue32<VkFormat> getFormat() {
        return format;
    }

    public @NotNull EnumValue32<VkPresentModeKHR> getPresentMode() {
        return presentMode;
    }

    public @NotNull EnumValue32<VkSurfaceTransformFlagBitsKHR> getTransform() {
        return transform;
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

    public @NotNull Extend2D getExtend() {
        return extend;
    }

    public void destroyForRecreation() {
        for (VkImageView view : swapChainImageViews) {
            vkInstance.vkDestroyImageView(device.getVkDevice(), view, ref(null));
        }
    }

    @Override
    public void close() {
        destroyForRecreation();
        vkInstance.vkDestroySwapchainKHR(device.getVkDevice(), vkSwapChain, ref(null));
    }
}
