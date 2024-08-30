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

package de.linusdev.cvg4j.engine.vk.device;

import de.linusdev.cvg4j.nat.vulkan.bitmasks.enums.VkCompositeAlphaFlagBitsKHR;
import de.linusdev.cvg4j.nat.vulkan.bitmasks.enums.VkImageUsageFlagBits;
import de.linusdev.cvg4j.nat.vulkan.enums.*;
import de.linusdev.cvg4j.nat.vulkan.handles.*;
import de.linusdev.cvg4j.nat.vulkan.structs.*;
import de.linusdev.cvg4j.nat.vulkan.utils.VulkanUtils;
import de.linusdev.cvg4j.engine.exception.EngineException;
import de.linusdev.cvg4j.engine.vk.VulkanGame;
import de.linusdev.cvg4j.engine.vk.VulkanRasterizationWindow;
import de.linusdev.cvg4j.engine.vk.extension.VulkanExtension;
import de.linusdev.cvg4j.engine.vk.swapchain.SwapChain;
import de.linusdev.llog.LLog;
import de.linusdev.llog.base.LogInstance;
import de.linusdev.llog.base.impl.StandardLogLevel;
import de.linusdev.lutils.math.vector.buffer.floatn.BBFloat1;
import de.linusdev.lutils.nat.array.NativeInt32Array;
import de.linusdev.lutils.nat.memory.Stack;
import de.linusdev.lutils.nat.pointer.BBTypedPointer64;
import de.linusdev.lutils.nat.string.NullTerminatedUTF8String;
import de.linusdev.lutils.nat.struct.annos.SVWrapper;
import de.linusdev.lutils.nat.struct.array.StructureArray;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

import static de.linusdev.lutils.nat.pointer.Pointer64.refL;
import static de.linusdev.lutils.nat.pointer.TypedPointer64.ref;

@SuppressWarnings("UnusedReturnValue")
public class VkDeviceAndSwapChainBuilder {

    private final @NotNull LogInstance LOG = LLog.getLogInstance();

    private @Nullable VkSurfaceFormatKHR surfaceFormat = null;
    private @Nullable VkPresentModeKHR presentMode = null; // TODO: make this an int maybe?
    private @Nullable VkExtent2D swapExtend = null;
    private @Nullable Integer swapChainImageCount = null;
    private @Nullable Integer surfaceTransform = null;
    private @Nullable Integer graphicsQueueIndex = null;
    private @Nullable Integer presentationQueueIndex = null;

    public VkDeviceAndSwapChainBuilder() {
        LOG.logDebug("Start building VkDevice.");
    }

    public VkDeviceAndSwapChainBuilder setSurfaceFormat(
            @Nullable VkSurfaceFormatKHR surfaceFormat
    ) throws EngineException {
        this.surfaceFormat = surfaceFormat;

        if(surfaceFormat == null)
            throw new EngineException("SurfaceFormatSelector could not select a suitable format.");

        LOG.logDebug("Surface format chosen: format=" + surfaceFormat.format.get(VkFormat.class)
                + ", colorSpace=" + surfaceFormat.colorSpace.get(VkColorSpaceKHR.class)
        );

        return this;
    }

    public VkDeviceAndSwapChainBuilder setPresentMode(@Nullable VkPresentModeKHR presentMode) throws EngineException {
        this.presentMode = presentMode;

        if(presentMode == null)
            throw new EngineException("PresentModeSelector could not select a suitable mode.");

        LOG.logDebug("Present mode chosen: " + presentMode);

        return this;
    }

    public VkDeviceAndSwapChainBuilder setSwapExtend(@NotNull VkExtent2D swapExtend) {
        this.swapExtend = swapExtend;
        LOG.logDebug("Swap extend chosen: width=" + swapExtend.width.get() + ", height=" + swapExtend.height.get());

        return this;
    }

    public VkDeviceAndSwapChainBuilder setSwapChainImageCount(@NotNull Integer swapChainImageCount) {
        this.swapChainImageCount = swapChainImageCount;
        LOG.logDebug("Swap chain image count chosen: " + swapChainImageCount);

        return this;
    }

    public VkDeviceAndSwapChainBuilder setSurfaceTransform(@NotNull Integer surfaceTransform) {
        this.surfaceTransform = surfaceTransform;
        LOG.logDebug("Surface transform chosen: " + surfaceTransform);

        return this;
    }

    public VkDeviceAndSwapChainBuilder setGraphicsQueueIndex(@Nullable Integer graphicsQueueIndex) {
        this.graphicsQueueIndex = graphicsQueueIndex;
        LOG.logDebug("Graphics queue index chosen: " + graphicsQueueIndex);

        return this;
    }

    public VkDeviceAndSwapChainBuilder setPresentationQueueIndex(@Nullable Integer presentationQueueIndex) {
        this.presentationQueueIndex = presentationQueueIndex;
        LOG.logDebug("Presentation queue index chosen: " + presentationQueueIndex);

        return this;
    }

    public @NotNull SwapChain build(
            @NotNull Stack stack,
            @NotNull VulkanGame game,
            @NotNull VulkanRasterizationWindow window,
            @NotNull VkInstance vkInstance,
            @NotNull VkPhysicalDevice vkPhysicalDevice,
            @NotNull VkDevice storeDevice,
            @NotNull VkQueue storeGraphicsQueue,
            @NotNull VkQueue storePresentationQueue
    ) {

        if(graphicsQueueIndex == null || presentationQueueIndex == null)
            throw new Error("graphics or presentation queue missing (call set...).");

        if(swapChainImageCount == null)
            throw new Error("swapChainImageCount is null (call set...).");

        if(surfaceFormat == null)
            throw new Error("surfaceFormat is null (call set...).");

        if(swapExtend == null)
            throw new Error("swapExtend is null (call set...).");

        if(surfaceTransform == null)
            throw new Error("surfaceTransform is null (call set...).");

        if(presentMode == null)
            throw new Error("presentMode is null (call set...).");

        LOG.logDebug("Start creating VkDevice.");
        boolean sameQueueIndices = Objects.equals(graphicsQueueIndex, presentationQueueIndex);

        // Queue Create Infos
        StructureArray<VkDeviceQueueCreateInfo> queueCreateInfos = stack.pushArray(
                sameQueueIndices ? 1 : 2,
                VkDeviceQueueCreateInfo.class,
                VkDeviceQueueCreateInfo::new
        );

        VkDeviceQueueCreateInfo queueCreateInfo;
        BBFloat1 prio = stack.push(BBFloat1.newAllocatable(null));
        prio.set(1.0f);

        // Graphics Queue create info
        queueCreateInfo = queueCreateInfos.getOrCreate(0);
        queueCreateInfo.sType.set(VkStructureType.DEVICE_QUEUE_CREATE_INFO);
        queueCreateInfo.queueFamilyIndex.set(graphicsQueueIndex);
        queueCreateInfo.queueCount.set(1);
        queueCreateInfo.pQueuePriorities.set(prio);

        if(!sameQueueIndices) {
            // Presentation Queue create info
            queueCreateInfo = queueCreateInfos.getOrCreate(1);
            queueCreateInfo.sType.set(VkStructureType.DEVICE_QUEUE_CREATE_INFO);
            queueCreateInfo.queueFamilyIndex.set(presentationQueueIndex);
            queueCreateInfo.queueCount.set(1);
            queueCreateInfo.pQueuePriorities.set(prio);
        }

        LOG.log(StandardLogLevel.DATA, "queueCreateInfos: " + queueCreateInfos);

        // Required Device extensions
        List<VulkanExtension> regDevExt = game.requiredDeviceExtensions();
        StructureArray<BBTypedPointer64<NullTerminatedUTF8String>> reqDevExtNatArray = stack.pushArray(regDevExt.size(), BBTypedPointer64.class, BBTypedPointer64::newUnallocated1);

        int i = 0;
        for (VulkanExtension ext : regDevExt)
            reqDevExtNatArray.getOrCreate(i++).set(stack.pushString(ext.extensionName()));

        // Vulkan Layers
        @Nullable StructureArray<BBTypedPointer64<NullTerminatedUTF8String>> enabledLayersNatArray = null;

        if(!game.activatedVulkanLayers().isEmpty()) {
            enabledLayersNatArray = stack.pushArray(game.activatedVulkanLayers().size(), BBTypedPointer64.class, BBTypedPointer64::newUnallocated1);
            i = 0;
            for (String ext : game.activatedVulkanLayers())
                enabledLayersNatArray.getOrCreate(i++).set(stack.pushString(ext));
        }

        // Device features
        VkPhysicalDeviceFeatures features = stack.push(new VkPhysicalDeviceFeatures());

        LOG.log(StandardLogLevel.DATA, "features: " + features);

        // Device Create Info
        VkDeviceCreateInfo deviceCreateInfo = stack.push(new VkDeviceCreateInfo());
        deviceCreateInfo.allocate();
        deviceCreateInfo.sType.set(VkStructureType.DEVICE_CREATE_INFO);
        deviceCreateInfo.queueCreateInfoCount.set(queueCreateInfos.length());
        deviceCreateInfo.pQueueCreateInfos.set(queueCreateInfos.getPointer());
        deviceCreateInfo.pEnabledFeatures.set(features);
        deviceCreateInfo.enabledExtensionCount.set(reqDevExtNatArray.length());
        deviceCreateInfo.ppEnabledExtensionNames.set(refL(reqDevExtNatArray));
        deviceCreateInfo.enabledLayerCount.set(enabledLayersNatArray == null ? 0 :enabledLayersNatArray.length());
        deviceCreateInfo.ppEnabledLayerNames.set(refL(enabledLayersNatArray));

        LOG.log(StandardLogLevel.DATA, "deviceCreateInfo: " + deviceCreateInfo);

        // Create device
        vkInstance.vkCreateDevice(
                vkPhysicalDevice,
                ref(deviceCreateInfo),
                ref(null),
                ref(storeDevice)
        ).check();
        LOG.logDebug("VkDevice created");

        // Pop stuff we don't need anymore
        stack.pop(); // deviceCreateInfo
        stack.pop(); // features

        if(enabledLayersNatArray != null) {
            for (i = 0; i < enabledLayersNatArray.length(); i++)
                stack.pop(); // string in enabledLayersNatArray
            stack.pop(); // enabledLayersNatArray
        }

        for (i = 0; i < reqDevExtNatArray.length(); i++)
            stack.pop(); // string in reqDevExtNatArray
        stack.pop(); // reqDevExtNatArray

        stack.pop(); // prio
        stack.pop(); // queueCreateInfos

        // Create queues
        vkInstance.vkGetDeviceQueue(storeDevice, graphicsQueueIndex, 0, ref(storeGraphicsQueue));
        vkInstance.vkGetDeviceQueue(storeDevice, presentationQueueIndex, 0, ref(storePresentationQueue));
        LOG.logDebug("VkQueues created");

        // Create SwapChain
        NativeInt32Array queueFamilyIndices = stack.push(NativeInt32Array.newAllocatable(SVWrapper.length(2)));
        queueFamilyIndices.setInt(0, graphicsQueueIndex);
        queueFamilyIndices.setInt(1, presentationQueueIndex);

        VkSwapchainCreateInfoKHR swapChainCreateInfo = stack.push(new VkSwapchainCreateInfoKHR());
        swapChainCreateInfo.sType.set(VkStructureType.SWAPCHAIN_CREATE_INFO_KHR);
        swapChainCreateInfo.surface.set(window.getVkSurface().get());
        swapChainCreateInfo.minImageCount.set(swapChainImageCount);
        swapChainCreateInfo.imageFormat.set(surfaceFormat.format.get());
        swapChainCreateInfo.imageColorSpace.set(surfaceFormat.colorSpace.get());
        swapChainCreateInfo.imageExtent.height.set(swapExtend.height.get());
        swapChainCreateInfo.imageExtent.width.set(swapExtend.width.get());
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

        SwapChain swapChain = SwapChain.create(
                stack, vkInstance, storeDevice, swapChainCreateInfo, surfaceFormat.format, surfaceFormat.colorSpace,
                graphicsQueueIndex, presentationQueueIndex
        );

        LOG.logDebug("VkSwapchain created");

        stack.pop(); // swapChainCreateInfo
        stack.pop(); // queueFamilyIndices

        return swapChain;
    }
}
