/*
 * Copyright (c) 2024-2025 Linus Andera
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

package de.linusdev.ljgel.engine.vk.swapchain;

import de.linusdev.ljgel.engine.exception.EngineException;
import de.linusdev.ljgel.engine.vk.device.Device;
import de.linusdev.ljgel.engine.vk.memory.manager.allocator.ondemand.OnDemandVulkanMemoryAllocator;
import de.linusdev.ljgel.engine.vk.memory.manager.objects.image.VulkanImage;
import de.linusdev.ljgel.engine.vk.objects.HasRecreationListeners;
import de.linusdev.ljgel.engine.vk.selector.swapchain.HasSwapChainSelectors;
import de.linusdev.ljgel.engine.vk.utils.VkEngineUtils;
import de.linusdev.ljgel.engine.vk.window.VulkanWindow;
import de.linusdev.ljgel.nat.vulkan.bitmasks.enums.*;
import de.linusdev.ljgel.nat.vulkan.enums.*;
import de.linusdev.ljgel.nat.vulkan.handles.VkImage;
import de.linusdev.ljgel.nat.vulkan.handles.VkImageView;
import de.linusdev.ljgel.nat.vulkan.handles.VkInstance;
import de.linusdev.ljgel.nat.vulkan.handles.VkSwapchainKHR;
import de.linusdev.ljgel.nat.vulkan.structs.VkExtent2D;
import de.linusdev.ljgel.nat.vulkan.structs.VkImageViewCreateInfo;
import de.linusdev.ljgel.nat.vulkan.structs.VkSwapchainCreateInfoKHR;
import de.linusdev.ljgel.nat.vulkan.utils.VulkanUtils;
import de.linusdev.llog.LLog;
import de.linusdev.llog.base.LogInstance;
import de.linusdev.lutils.bitfield.IntBitfieldImpl;
import de.linusdev.lutils.math.vector.Vector;
import de.linusdev.lutils.math.vector.buffer.intn.BBUInt1;
import de.linusdev.lutils.nat.array.NativeInt32Array;
import de.linusdev.lutils.nat.enums.EnumValue32;
import de.linusdev.lutils.nat.enums.JavaEnumValue32;
import de.linusdev.lutils.nat.memory.stack.Stack;
import de.linusdev.lutils.nat.struct.annos.SVWrapper;
import de.linusdev.lutils.nat.struct.array.StructureArray;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static de.linusdev.lutils.nat.pointer.TypedPointer64.ofArray;
import static de.linusdev.lutils.nat.pointer.TypedPointer64.ref;
import static de.linusdev.lutils.nat.struct.abstracts.Structure.allocate;

public class SwapChain extends HasRecreationListeners<SwapChainRecreationListener> implements AutoCloseable {

    public static final @NotNull LogInstance LOG = LLog.getLogInstance();

    public static @NotNull SwapChain create(
            @NotNull Stack stack,
            @NotNull VkInstance vkInstance,
            @NotNull VulkanWindow window,
            @NotNull Device device,
            int swapChainImageCount,
            @NotNull EnumValue32<VkFormat> format,
            @NotNull EnumValue32<VkColorSpaceKHR> colorSpace,
            @NotNull Extend2D swapChainExtend,
            EnumValue32<VkSurfaceTransformFlagBitsKHR> surfaceTransform,
            EnumValue32<VkPresentModeKHR> presentMode
    ) throws EngineException {
        return new SwapChain(
                vkInstance, device, window,
                swapChainImageCount, format, colorSpace, swapChainExtend, surfaceTransform, presentMode,
                VkFormat.D32_SFLOAT, //TODO: must be selected/found,
                device.getMaxSupportedSampleCount(true) // TODO: must be set by game
        ).create(stack);
    }

    private final @NotNull VkInstance vkInstance;
    private final @NotNull Device device;
    private final @NotNull VulkanWindow window;

    /*
     * Managed by this class
     */
    private final @NotNull VkSwapchainKHR vkSwapChain;
    private final @NotNull StructureArray<VkImageView> swapChainImageViews;
    private final @NotNull OnDemandVulkanMemoryAllocator allocator;
    private VulkanImage depthImage;
    private VulkanImage colorImage;

    /*
     * Information stored in this class
     */
    private final int swapChainImageCount;
    private final @NotNull EnumValue32<VkFormat> format;
    private final @NotNull EnumValue32<VkColorSpaceKHR> colorSpace;
    private final @NotNull EnumValue32<VkSurfaceTransformFlagBitsKHR> transform;
    private final @NotNull EnumValue32<VkPresentModeKHR> presentMode;
    private final @NotNull Extend2D extend;
    private final @NotNull VkFormat depthFormat;
    private final @NotNull EnumValue32<VkSampleCountFlagBits> sampleCount;


    public SwapChain(
            @NotNull VkInstance vkInstance,
            @NotNull Device device,
            @NotNull VulkanWindow window,
            int swapChainImageCount,
            @NotNull EnumValue32<VkFormat> format,
            @NotNull EnumValue32<VkColorSpaceKHR> colorSpace,
            @NotNull Extend2D extend,
            @NotNull EnumValue32<VkSurfaceTransformFlagBitsKHR> transform,
            @NotNull EnumValue32<VkPresentModeKHR> presentMode,
            @NotNull VkFormat depthFormat,
            @NotNull EnumValue32<VkSampleCountFlagBits> sampleCount
    ) {
        this.vkInstance = vkInstance;
        this.device = device;
        this.window = window;
        this.depthFormat = depthFormat;
        this.sampleCount = sampleCount;

        this.vkSwapChain = allocate(new VkSwapchainKHR());
        this.swapChainImageViews = StructureArray.newAllocated(swapChainImageCount, VkImageView.class, VkImageView::new);
        this.allocator = new OnDemandVulkanMemoryAllocator(device, "swap-chain-memory-allocator");

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

    @Contract("_ -> this")
    public @NotNull SwapChain create(@NotNull Stack stack) throws EngineException {
        depthImage = allocator.createDeviceLocalVulkanImage(stack, "depth-image", extend,
                depthFormat, //TODO: this must be selected
                VkImageTiling.OPTIMAL,
                new IntBitfieldImpl<>(VkImageUsageFlagBits.DEPTH_STENCIL_ATTACHMENT),
                new IntBitfieldImpl<>(VkImageAspectFlagBits.DEPTH),
                false,
                sampleCount
        );

        if(isMultiSamplingEnabled()) {
            colorImage = allocator.createDeviceLocalVulkanImage(stack, "ms-color-image",
                    extend,
                    format.get(VkFormat.class),
                    VkImageTiling.OPTIMAL,
                    new IntBitfieldImpl<>(VkImageUsageFlagBits.TRANSIENT_ATTACHMENT, VkImageUsageFlagBits.COLOR_ATTACHMENT),
                    new IntBitfieldImpl<>(VkImageAspectFlagBits.COLOR),
                    false,
                    sampleCount
            );
        }


        allocator.allocate(stack);

        recreate(false, stack, null, null, null, null, null);

        return this;
    }

    public @NotNull SwapChainRecreationReturn recreate(
            @NotNull Stack stack,
            @NotNull HasSwapChainSelectors selectors
    ) throws EngineException {
        try (var ignored = stack.popPoint()) {
            SwapChainBuilder builder = VkEngineUtils.fillSwapChainBuilder(stack, vkInstance, device, window, selectors);
            if(builder.isExtendAreaZero()) {
                return SwapChainRecreationReturn.ERROR_ZERO_AREA;
            }

            builder.recreateSwapChain(stack, this);
            return SwapChainRecreationReturn.SUCCESS;
        }
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
        LOG.debug("(Re)creating SwapChain. Sampling=" + sampleCount.get(VkSampleCountFlagBits.class));

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
            // recreate Depth Image
            depthImage.recreate(stack, -1, newExtend);
            if(isMultiSamplingEnabled())
                colorImage.recreate(stack, -1, newExtend);
            allocator.allocate(stack);
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
        swapChainCreateInfo.imageUsage.set(VkImageUsageFlagBits.COLOR_ATTACHMENT);

        if(sameQueueIndices) {
            swapChainCreateInfo.imageSharingMode.set(VkSharingMode.EXCLUSIVE);
        } else {
            swapChainCreateInfo.imageSharingMode.set(VkSharingMode.CONCURRENT);
            swapChainCreateInfo.queueFamilyIndexCount.set(2);
            swapChainCreateInfo.pQueueFamilyIndices.set(queueFamilyIndices.getPointer());
        }

        swapChainCreateInfo.preTransform.set(transform);
        swapChainCreateInfo.compositeAlpha.set(VkCompositeAlphaFlagBitsKHR.OPAQUE_KHR);
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

        // TODO: Some hardware combos might not support the request amount of swapchain
        // images. The minimum may be higher than 2. If this happens we should automatically
        // change to use the next best supported count (if enabled by the game) and log
        // a warning message.
        if(swapChainImageCount != swapchainImages.length())
            throw new Error("Set and actual swap chain image count does not match.");

        VkImageViewCreateInfo imageViewCreateInfo = stack.push(new VkImageViewCreateInfo());
        for (int i = 0; i < swapChainImageViews.length(); i++) {

            imageViewCreateInfo.sType.set(VkStructureType.IMAGE_VIEW_CREATE_INFO);
            imageViewCreateInfo.image.set(swapchainImages.get(i).get());
            imageViewCreateInfo.viewType.set(VkImageViewType.TYPE_2D);
            imageViewCreateInfo.format.set(swapChainCreateInfo.imageFormat);
            imageViewCreateInfo.components.r.set(VkComponentSwizzle.IDENTITY);
            imageViewCreateInfo.components.g.set(VkComponentSwizzle.IDENTITY);
            imageViewCreateInfo.components.b.set(VkComponentSwizzle.IDENTITY);
            imageViewCreateInfo.components.a.set(VkComponentSwizzle.IDENTITY);
            imageViewCreateInfo.subresourceRange.aspectMask.set(VkImageAspectFlagBits.COLOR);
            imageViewCreateInfo.subresourceRange.baseMipLevel.set(0);
            imageViewCreateInfo.subresourceRange.levelCount.set(1);
            imageViewCreateInfo.subresourceRange.baseArrayLayer.set(0);
            imageViewCreateInfo.subresourceRange.layerCount.set(1);

            vkInstance.vkCreateImageView(device.getVkDevice(), ref(imageViewCreateInfo), ref(null), ref(swapChainImageViews.get(i))).check();
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

    public @NotNull VkFormat getDepthFormat() {
        return depthFormat;
    }

    public VulkanImage getDepthImage() {
        return depthImage;
    }

    public VulkanImage getColorImage() {
        return colorImage;
    }

    public @NotNull EnumValue32<VkSampleCountFlagBits> getSampleCount() {
        return sampleCount;
    }

    public boolean isMultiSamplingEnabled() {
        return sampleCount.get() != VkSampleCountFlagBits.COUNT_1.getValue();
    }

    public void destroyForRecreation() {
        for (VkImageView view : swapChainImageViews) {
            vkInstance.vkDestroyImageView(device.getVkDevice(), view, ref(null));
        }
    }

    @Override
    public void close() {
        destroyForRecreation();
        allocator.close();
        vkInstance.vkDestroySwapchainKHR(device.getVkDevice(), vkSwapChain, ref(null));
    }
}
