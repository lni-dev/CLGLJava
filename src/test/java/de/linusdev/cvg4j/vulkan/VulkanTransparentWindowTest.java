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

package de.linusdev.cvg4j.vulkan;

import de.linusdev.cvg4j.engine.cl.CLEngine;
import de.linusdev.cvg4j.engine.vk.VulkanRasterizationWindow;
import de.linusdev.cvg4j.nat.NativeUtils;
import de.linusdev.cvg4j.nat.glfw3.GLFW;
import de.linusdev.cvg4j.nat.glfw3.GLFWValues;
import de.linusdev.cvg4j.nat.glfw3.custom.GLFWWindowHints;
import de.linusdev.cvg4j.nat.glfw3.exceptions.GLFWException;
import de.linusdev.cvg4j.nat.vulkan.VkBool32;
import de.linusdev.cvg4j.nat.vulkan.VulkanApiVersion;
import de.linusdev.cvg4j.nat.vulkan.bitmasks.VkCommandBufferResetFlags;
import de.linusdev.cvg4j.nat.vulkan.bitmasks.VkPipelineStageFlags;
import de.linusdev.cvg4j.nat.vulkan.bitmasks.enums.*;
import de.linusdev.cvg4j.nat.vulkan.constants.APIConstants;
import de.linusdev.cvg4j.nat.vulkan.enums.*;
import de.linusdev.cvg4j.nat.vulkan.handles.*;
import de.linusdev.cvg4j.nat.vulkan.structs.*;
import de.linusdev.cvg4j.nat.vulkan.utils.VulkanUtils;
import de.linusdev.cvg4j.nat.vulkan.utils.VulkanVersionUtils;
import de.linusdev.lutils.ansi.sgr.SGR;
import de.linusdev.lutils.ansi.sgr.SGRParameters;
import de.linusdev.lutils.math.vector.buffer.floatn.BBFloat1;
import de.linusdev.lutils.math.vector.buffer.intn.BBInt2;
import de.linusdev.lutils.math.vector.buffer.intn.BBUInt1;
import de.linusdev.lutils.nat.array.NativeInt32Array;
import de.linusdev.lutils.nat.enums.NativeEnumValue32;
import de.linusdev.lutils.nat.memory.DirectMemoryStack64;
import de.linusdev.lutils.nat.pointer.BBTypedPointer64;
import de.linusdev.lutils.nat.pointer.TypedPointer64;
import de.linusdev.lutils.nat.string.NullTerminatedUTF8String;
import de.linusdev.lutils.nat.struct.annos.SVWrapper;
import de.linusdev.lutils.nat.struct.array.StructureArray;
import de.linusdev.lutils.nat.struct.utils.BufferUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

import static de.linusdev.cvg4j.engine.vk.shader.VulkanSpirVUtils.createShaderModuleInfo;
import static de.linusdev.cvg4j.nat.vulkan.utils.VulkanNonInstanceMethods.vkCreateInstance;
import static de.linusdev.lutils.nat.struct.abstracts.Structure.allocate;

public class VulkanTransparentWindowTest {


    @Test
    void test() throws GLFWException, IOException {
        CLEngine.StaticSetup.setup();

        GLFW.glfwInit();
        if(GLFW.glfwVulkanSupported() != GLFWValues.GLFW_TRUE) {
            System.err.println("Cannot run Vulkan test: Vulkan is not supported on this machine.");
            return;
        }
        BBUInt1 count = BBUInt1.newAllocated(null);
        var array = GLFW.glfwGetRequiredInstanceExtensions(count);

        System.out.println("glfwGetRequiredInstanceExtensions:");
        for (BBTypedPointer64<NullTerminatedUTF8String> pointer : array) {
            System.out.println("\t- "+BufferUtils.readString(NativeUtils.getBufferFromPointer(pointer.get(), 0), false));
        }

        //Validation layer strings
        StructureArray<BBTypedPointer64<NullTerminatedUTF8String>> vLayerStrings = StructureArray.newAllocated(
                false,
                SVWrapper.of(1, BBTypedPointer64.class),
                null,
                BBTypedPointer64::newUnallocated1
        );
        vLayerStrings.getOrCreate(0).set(NullTerminatedUTF8String.newAllocated("VK_LAYER_KHRONOS_validation"));

        // VkApplicationInfo
        VkApplicationInfo vkApplicationInfo = new VkApplicationInfo();
        vkApplicationInfo.allocate();

        vkApplicationInfo.sType.set(VkStructureType.APPLICATION_INFO);
        vkApplicationInfo.pNext.set(0);
        vkApplicationInfo.pApplicationName.set(NullTerminatedUTF8String.newAllocated("Test Application"));
        vkApplicationInfo.applicationVersion.set(VulkanVersionUtils.makeVersion(1, 0, 0));
        vkApplicationInfo.pEngineName.set(NullTerminatedUTF8String.newAllocated("CVG4J"));
        vkApplicationInfo.engineVersion.set(VulkanVersionUtils.makeVersion(0, 1, 0));
        vkApplicationInfo.apiVersion.set(VulkanApiVersion.V_1_3_0.getAsInt());

        // VkInstanceCreateInfo
        VkInstanceCreateInfo vkInstanceCreateInfo = new VkInstanceCreateInfo();
        vkInstanceCreateInfo.allocate();

        vkInstanceCreateInfo.sType.set(VkStructureType.INSTANCE_CREATE_INFO);
        vkInstanceCreateInfo.pNext.set(0);
        vkInstanceCreateInfo.pApplicationInfo.set(vkApplicationInfo);
        vkInstanceCreateInfo.enabledExtensionCount.set(array.length());
        vkInstanceCreateInfo.ppEnabledExtensionNames.set(array.getPointer());
        vkInstanceCreateInfo.enabledLayerCount.set(vLayerStrings.length());
        vkInstanceCreateInfo.ppEnabledLayerNames.set(vLayerStrings.getPointer());
        vkInstanceCreateInfo.flags.set(0);

        // Create VkInstance
        VkInstance vkInstance = new VkInstance();
        vkInstance.allocate();
        vkCreateInstance(vkInstanceCreateInfo, null, vkInstance).check();
        vkInstance.initMethodPointers();

        GLFWWindowHints hints = new GLFWWindowHints();
        hints.transparentFrameBuffer = true;
        VulkanRasterizationWindow window = new VulkanRasterizationWindow(hints, vkInstance, new DirectMemoryStack64());

        // Create window surface
        VkSurfaceKHR vkSurfaceKHR = window.getVkSurface();

        // Pick GPU
        vkInstance.vkEnumeratePhysicalDevices(TypedPointer64.of(count), TypedPointer64.of(null)).check();
        System.out.println("Physical Device count: " + count.get());
        StructureArray<VkPhysicalDevice> vkPhysicalDevices = StructureArray.newAllocated(
                false,
                SVWrapper.of(count.get(), VkPhysicalDevice.class),
                null,
                VkPhysicalDevice::new
        );
        vkInstance.vkEnumeratePhysicalDevices(TypedPointer64.of(count), TypedPointer64.ofArray(vkPhysicalDevices)).check();

        // Required Device extensions
        List<String> _requiredDeviceExtensions = List.of(
                APIConstants.VK_KHR_swapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME
        );
        StructureArray<BBTypedPointer64<NullTerminatedUTF8String>> requiredDeviceExtensions = StructureArray.newAllocated(
                false,
                SVWrapper.of(_requiredDeviceExtensions.size(), BBTypedPointer64.class),
                null,
                BBTypedPointer64::newUnallocated1
        );

        for (int i = 0; i < _requiredDeviceExtensions.size(); i++) {
            requiredDeviceExtensions.getOrCreate(i).set(NullTerminatedUTF8String.newAllocated(_requiredDeviceExtensions.get(i)));
        }

        VkSurfaceFormatKHR selectedSurfaceFormat = null;
        VkPresentModeKHR selectedPresentMode = null;
        VkExtent2D selectedExtent = null;
        int selectedImageCount = 0;
        VkSurfaceTransformFlagBitsKHR selectedTransform = null;
        System.out.println("Physical Devices:");
        for (VkPhysicalDevice vkPhysicalDevice : vkPhysicalDevices) {
            VkPhysicalDeviceProperties deviceProperties = new VkPhysicalDeviceProperties();
            deviceProperties.allocate();
            vkInstance.vkGetPhysicalDeviceProperties(vkPhysicalDevice, TypedPointer64.of(deviceProperties));

            System.out.println("\t- " + deviceProperties.deviceName.get() + ": " + deviceProperties.deviceType.get(VkPhysicalDeviceType.class));

            // get count extension props count
            vkInstance.vkEnumerateDeviceExtensionProperties(
                    vkPhysicalDevice,
                    TypedPointer64.of(null),
                    TypedPointer64.of(count),
                    TypedPointer64.of(null)
            );
            System.out.println("\t\t+ Extensions (" + count.get() + "):");
            StructureArray<VkExtensionProperties> vkExtensionPropertiesArray = StructureArray.newAllocated(
                    false,
                    SVWrapper.of(count.get(), VkExtensionProperties.class),
                    null,
                    VkExtensionProperties::new
            );
            vkInstance.vkEnumerateDeviceExtensionProperties(
                    vkPhysicalDevice,
                    TypedPointer64.of(null),
                    TypedPointer64.of(count),
                    TypedPointer64.ofArray(vkExtensionPropertiesArray)
            );

            for (VkExtensionProperties properties : vkExtensionPropertiesArray) {
                if(_requiredDeviceExtensions.contains(properties.extensionName.get())) {
                    SGR sgr = new SGR(SGRParameters.BACKGROUND_GREEN);
                    System.out.print(sgr.construct());
                }
                System.out.println("\t\t\t* " + properties.extensionName.get());
                System.out.print(SGR.reset());
            }

            // Surface Capabilities
            System.out.println("\t\t+ Surface Capabilities:");
            VkSurfaceCapabilitiesKHR vkSurfaceCapabilitiesKHR = new VkSurfaceCapabilitiesKHR();
            vkSurfaceCapabilitiesKHR.allocate();
            vkInstance.vkGetPhysicalDeviceSurfaceCapabilitiesKHR(vkPhysicalDevice, vkSurfaceKHR, TypedPointer64.of(vkSurfaceCapabilitiesKHR));
            System.out.println("\t\t\t* minImageCount: " + vkSurfaceCapabilitiesKHR.minImageCount.get());
            System.out.println("\t\t\t* maxImageCount: " + vkSurfaceCapabilitiesKHR.maxImageCount.get());
            System.out.println("\t\t\t* currentExtent: (" + vkSurfaceCapabilitiesKHR.currentExtent.width.get() + ", " + vkSurfaceCapabilitiesKHR.currentExtent.height.get() + ")");

            // Surface Formats
            System.out.println("\t\t+ Surface Formats:");
            vkInstance.vkGetPhysicalDeviceSurfaceFormatsKHR(vkPhysicalDevice, vkSurfaceKHR, TypedPointer64.of(count), TypedPointer64.of(null));
            StructureArray<VkSurfaceFormatKHR> vkSurfaceFormats = StructureArray.newAllocated(
                    false,
                    SVWrapper.of(count.get(), VkSurfaceFormatKHR.class),
                    null,
                    VkSurfaceFormatKHR::new
            );
            vkInstance.vkGetPhysicalDeviceSurfaceFormatsKHR(vkPhysicalDevice, vkSurfaceKHR, TypedPointer64.of(count), TypedPointer64.ofArray(vkSurfaceFormats));

            // search the one we want
            for (VkSurfaceFormatKHR vkSurfaceFormat : vkSurfaceFormats) {
                if(vkSurfaceFormat.format.get() == VkFormat.B8G8R8A8_SRGB.getValue()
                && vkSurfaceFormat.colorSpace.get() == VkColorSpaceKHR.SRGB_NONLINEAR_KHR.getValue()) {
                    selectedSurfaceFormat = vkSurfaceFormat;
                    break;
                }
            }

            SGR sgr;
            // if we didn't find it, select any.
            if(selectedSurfaceFormat == null) {
                selectedSurfaceFormat = vkSurfaceFormats.get(0);
                sgr = new SGR(SGRParameters.BACKGROUND_BRIGHT_YELLOW);
            } else {
                sgr = new SGR(SGRParameters.BACKGROUND_GREEN);
            }

            for (VkSurfaceFormatKHR vkSurfaceFormat : vkSurfaceFormats) {
                if(vkSurfaceFormat == selectedSurfaceFormat)
                    System.out.print(sgr.construct());
                System.out.println("\t\t\t* " + vkSurfaceFormat.format.get(VkFormat.class) + " | " + vkSurfaceFormat.colorSpace.get(VkColorSpaceKHR.class) + SGR.reset());
            }

            // Presentation Modes
            System.out.println("\t\t+ Presentation Modes:");
            vkInstance.vkGetPhysicalDeviceSurfacePresentModesKHR(vkPhysicalDevice, vkSurfaceKHR, TypedPointer64.of(count), TypedPointer64.of(null));
            StructureArray<NativeEnumValue32<VkPresentModeKHR>> vkPresentModes = StructureArray.newAllocated(
                    false,
                    SVWrapper.of(count.get(), NativeEnumValue32.class),
                    null,
                    NativeEnumValue32::newUnallocatedT
            );
            vkInstance.vkGetPhysicalDeviceSurfacePresentModesKHR(vkPhysicalDevice, vkSurfaceKHR, TypedPointer64.of(count), TypedPointer64.ofArray(vkPresentModes));

            for (NativeEnumValue32<VkPresentModeKHR> vkPresentMode : vkPresentModes) {
                if(vkPresentMode.get() == VkPresentModeKHR.MAILBOX_KHR.getValue()) {
                    selectedPresentMode = VkPresentModeKHR.MAILBOX_KHR;
                    break;
                }
            }

            // if we didn't find it, select the one that is always there.
            if(selectedPresentMode == null) {
                selectedPresentMode = VkPresentModeKHR.FIFO_KHR;
                sgr = new SGR(SGRParameters.BACKGROUND_BRIGHT_YELLOW);
            } else {
                sgr = new SGR(SGRParameters.BACKGROUND_GREEN);
            }

            for (NativeEnumValue32<VkPresentModeKHR> vkPresentMode : vkPresentModes) {
                if(vkPresentMode.get() == selectedPresentMode.getValue())
                    System.out.print(sgr.construct());
                System.out.println("\t\t\t* " + vkPresentMode.get(VkPresentModeKHR.class) + SGR.reset());
            }

            // Chose SwapExtent
            if(vkSurfaceCapabilitiesKHR.currentExtent.width.get() != 0xFFFFFFFF) {
                selectedExtent = vkSurfaceCapabilitiesKHR.currentExtent;
            } else {
                BBInt2 size = window.getFrameBufferSize(null);
                selectedExtent = new VkExtent2D();
                selectedExtent.allocate();

                int maxWidth = vkSurfaceCapabilitiesKHR.maxImageExtent.width.get();
                int maxHeight = vkSurfaceCapabilitiesKHR.maxImageExtent.height.get();
                int minWidth = vkSurfaceCapabilitiesKHR.minImageExtent.width.get();
                int minHeight = vkSurfaceCapabilitiesKHR.minImageExtent.height.get();
                selectedExtent.width.set(Math.max(Math.min(size.x(), maxWidth), minWidth));
                selectedExtent.height.set(Math.max(Math.min(size.y(), maxHeight), minHeight));
            }

            // Chose image count
            selectedImageCount = vkSurfaceCapabilitiesKHR.minImageCount.get() + 1; // One more is better
            int maxImageCount = vkSurfaceCapabilitiesKHR.maxImageCount.get();
            if(maxImageCount != 0 && maxImageCount < selectedImageCount){ // If we cant to +1, do the max/min
                selectedImageCount = maxImageCount;
            }

            // Chose transform (choosing current is enough)
            selectedTransform = vkSurfaceCapabilitiesKHR.currentTransform.get(VkSurfaceTransformFlagBitsKHR.class);

            // Physical device selection Notes:
            // deviceType: Prioritize VK_PHYSICAL_DEVICE_TYPE_DISCRETE_GPU
            // Extensions: VK_KHR_swapchain must be present
            // Surface Format: require at least one. choose SRGB_... (see above)
            // Presentation Modes: Require at least one. Choose best
        }

        VkPhysicalDevice physicalDevice = vkPhysicalDevices.get(0); // Just select the first gpu

        // Find Queue families
        vkInstance.vkGetPhysicalDeviceQueueFamilyProperties(physicalDevice, TypedPointer64.of(count), TypedPointer64.of(null));
        System.out.println("Physical Device Queue Family count: " + count.get());
        StructureArray<VkQueueFamilyProperties> vkQueueFamilyProperties = StructureArray.newAllocated(
                false,
                SVWrapper.of(count.get(), VkQueueFamilyProperties.class),
                null,
                VkQueueFamilyProperties::new
        );

        vkInstance.vkGetPhysicalDeviceQueueFamilyProperties(physicalDevice, TypedPointer64.of(count), TypedPointer64.ofArray(vkQueueFamilyProperties));
        System.out.println("queue family count: " + count.get());
        int graphicsQueueIndex = -1;
        int presentationQueueIndex = -1;
        for (int i = 0; i < vkQueueFamilyProperties.length(); i++) {
            VkQueueFamilyProperties vkQueueFamilyProperty = vkQueueFamilyProperties.getOrCreate(i);
            if(vkQueueFamilyProperty.queueFlags.isSet(VkQueueFlagBits.VK_QUEUE_GRAPHICS_BIT)) {
                graphicsQueueIndex = i;
            }

            VkBool32 supported = new VkBool32();
            supported.allocate();
            vkInstance.vkGetPhysicalDeviceSurfaceSupportKHR(physicalDevice, i, vkSurfaceKHR, TypedPointer64.of(supported)).check();
            if(VulkanUtils.vkBool32ToBoolean(supported)) {
                presentationQueueIndex = i;
            }

            // It's best to use the same queue for both
            if(graphicsQueueIndex != -1 && graphicsQueueIndex == presentationQueueIndex)
                break;
        }

        if(graphicsQueueIndex == -1)
            throw new IllegalStateException("No graphics queue found");

        if(presentationQueueIndex == -1)
            throw new IllegalStateException("No presentation queue found");

        boolean differentQueueIndices = graphicsQueueIndex != presentationQueueIndex;

        // Queue Create Infos
        StructureArray<VkDeviceQueueCreateInfo> vkDeviceQueueCreateInfos = StructureArray.newAllocated(
                false,
                SVWrapper.of(differentQueueIndices ? 2 : 1, VkDeviceQueueCreateInfo.class),
                null,
                VkDeviceQueueCreateInfo::new
        );

        VkDeviceQueueCreateInfo vkDeviceQueueCreateInfo;
        BBFloat1 prio = BBFloat1.newAllocated(null);
        prio.set(1.0f);

        // Graphics Queue create info
        vkDeviceQueueCreateInfo = vkDeviceQueueCreateInfos.getOrCreate(0);
        vkDeviceQueueCreateInfo.sType.set(VkStructureType.DEVICE_QUEUE_CREATE_INFO);
        vkDeviceQueueCreateInfo.queueFamilyIndex.set(graphicsQueueIndex);
        vkDeviceQueueCreateInfo.queueCount.set(1);
        vkDeviceQueueCreateInfo.pQueuePriorities.set(prio);

        if(differentQueueIndices) {
            // Presentation Queue create info
            vkDeviceQueueCreateInfo = vkDeviceQueueCreateInfos.getOrCreate(1);
            vkDeviceQueueCreateInfo.sType.set(VkStructureType.DEVICE_QUEUE_CREATE_INFO);
            vkDeviceQueueCreateInfo.queueFamilyIndex.set(presentationQueueIndex);
            vkDeviceQueueCreateInfo.queueCount.set(1);
            vkDeviceQueueCreateInfo.pQueuePriorities.set(prio);
        }

        // Device features
        VkPhysicalDeviceFeatures vkPhysicalDeviceFeatures = new VkPhysicalDeviceFeatures();
        vkPhysicalDeviceFeatures.allocate();

        // Device Create Info
        VkDeviceCreateInfo vkDeviceCreateInfo = new VkDeviceCreateInfo();
        vkDeviceCreateInfo.allocate();
        vkDeviceCreateInfo.sType.set(VkStructureType.DEVICE_CREATE_INFO);
        vkDeviceCreateInfo.queueCreateInfoCount.set(vkDeviceQueueCreateInfos.length());
        vkDeviceCreateInfo.pQueueCreateInfos.set(vkDeviceQueueCreateInfos.getPointer());
        vkDeviceCreateInfo.pEnabledFeatures.set(vkPhysicalDeviceFeatures);
        vkDeviceCreateInfo.enabledExtensionCount.set(requiredDeviceExtensions.length());
        vkDeviceCreateInfo.ppEnabledExtensionNames.set(requiredDeviceExtensions.getPointer());
        vkDeviceCreateInfo.enabledLayerCount.set(vLayerStrings.length());
        vkDeviceCreateInfo.ppEnabledLayerNames.set(vLayerStrings.getPointer());

        // Create device
        VkDevice device = new VkDevice();
        device.allocate();
        vkInstance.vkCreateDevice(
                physicalDevice,
                TypedPointer64.of(vkDeviceCreateInfo),
                TypedPointer64.of(null),
                TypedPointer64.of(device)
        ).check();

        // Get queue handle
        VkQueue graphicsQueue = new VkQueue();
        VkQueue presentationQueue = new VkQueue();

        graphicsQueue.allocate();
        presentationQueue.allocate();

        vkInstance.vkGetDeviceQueue(device, graphicsQueueIndex, 0, TypedPointer64.of(graphicsQueue));
        vkInstance.vkGetDeviceQueue(device, presentationQueueIndex, 0, TypedPointer64.of(presentationQueue));

        // Create Swap Chain using selectedSurfaceFormat, selectedPresentMode and selectedExtent
        VkSwapchainCreateInfoKHR swapchainCreateInfo = new VkSwapchainCreateInfoKHR();
        swapchainCreateInfo.allocate();
        swapchainCreateInfo.sType.set(VkStructureType.SWAPCHAIN_CREATE_INFO_KHR);
        swapchainCreateInfo.surface.set(vkSurfaceKHR.get());
        swapchainCreateInfo.minImageCount.set(selectedImageCount);
        swapchainCreateInfo.imageFormat.set(selectedSurfaceFormat.format.get());
        swapchainCreateInfo.imageColorSpace.set(selectedSurfaceFormat.colorSpace.get());
        swapchainCreateInfo.imageExtent.height.set(selectedExtent.height.get());
        swapchainCreateInfo.imageExtent.width.set(selectedExtent.width.get());
        swapchainCreateInfo.imageArrayLayers.set(1);
        // Write directly to this image
        // If we want to do postprocessing, we would need to write to a different image and then
        // transfer into this one. That would mean we would need to set this to VK_IMAGE_USAGE_TRANSFER_DST_BIT
        swapchainCreateInfo.imageUsage.set(VkImageUsageFlagBits.VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT);

        if(differentQueueIndices) {
            swapchainCreateInfo.imageSharingMode.set(VkSharingMode.CONCURRENT);
            swapchainCreateInfo.queueFamilyIndexCount.set(2);
            NativeInt32Array queueFamilyIndices = NativeInt32Array.newAllocated(SVWrapper.length(2));
            queueFamilyIndices.setInt(0, graphicsQueueIndex);
            queueFamilyIndices.setInt(1, presentationQueueIndex);
            swapchainCreateInfo.pQueueFamilyIndices.set(queueFamilyIndices.getPointer());
        } else {
            swapchainCreateInfo.imageSharingMode.set(VkSharingMode.EXCLUSIVE);
        }

        swapchainCreateInfo.preTransform.set(selectedTransform);
        swapchainCreateInfo.compositeAlpha.set(VkCompositeAlphaFlagBitsKHR.VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR);
        swapchainCreateInfo.presentMode.set(selectedPresentMode);
        swapchainCreateInfo.clipped.set(VulkanUtils.booleanToVkBool32(true));
        swapchainCreateInfo.oldSwapchain.set(0L); // required when window was resized. see https://vulkan-tutorial.com/en/Drawing_a_triangle/Swap_chain_recreation

        VkSwapchainKHR swapchain = new VkSwapchainKHR();
        swapchain.allocate();
        vkInstance.vkCreateSwapchainKHR(device, TypedPointer64.of(swapchainCreateInfo), TypedPointer64.of(null), TypedPointer64.of(swapchain)).check();

        // Get VkImages
        vkInstance.vkGetSwapchainImagesKHR(device, swapchain, TypedPointer64.of(count), TypedPointer64.of(null));
        StructureArray<VkImage> swapchainImages = StructureArray.newAllocated(
                false,
                SVWrapper.of(count.get(), VkImage.class),
                null,
                VkImage::new
        );
        vkInstance.vkGetSwapchainImagesKHR(device, swapchain, TypedPointer64.of(count), TypedPointer64.ofArray(swapchainImages));
        StructureArray<VkImageView> swapchainImageViews = StructureArray.newAllocated(
                false,
                SVWrapper.of(swapchainImages.length(), VkImageView.class),
                null,
                VkImageView::new
        );

        for (int i = 0; i < swapchainImageViews.length(); i++) {
            VkImageViewCreateInfo vkImageViewCreateInfo = new VkImageViewCreateInfo();
            vkImageViewCreateInfo.allocate();
            vkImageViewCreateInfo.sType.set(VkStructureType.IMAGE_VIEW_CREATE_INFO);
            vkImageViewCreateInfo.image.set(swapchainImages.getOrCreate(i).get());
            vkImageViewCreateInfo.viewType.set(VkImageViewType.TYPE_2D);
            vkImageViewCreateInfo.format.set(selectedSurfaceFormat.format.get());
            vkImageViewCreateInfo.components.r.set(VkComponentSwizzle.IDENTITY);
            vkImageViewCreateInfo.components.g.set(VkComponentSwizzle.IDENTITY);
            vkImageViewCreateInfo.components.b.set(VkComponentSwizzle.IDENTITY);
            vkImageViewCreateInfo.components.a.set(VkComponentSwizzle.IDENTITY);
            vkImageViewCreateInfo.subresourceRange.aspectMask.set(VkImageAspectFlagBits.VK_IMAGE_ASPECT_COLOR_BIT);
            vkImageViewCreateInfo.subresourceRange.baseMipLevel.set(0);
            vkImageViewCreateInfo.subresourceRange.levelCount.set(1);
            vkImageViewCreateInfo.subresourceRange.baseArrayLayer.set(0);
            vkImageViewCreateInfo.subresourceRange.layerCount.set(1);

            vkInstance.vkCreateImageView(device, TypedPointer64.of(vkImageViewCreateInfo), TypedPointer64.of(null), TypedPointer64.of(swapchainImageViews.getOrCreate(i))).check();
        }

        // Read binary shaders and create Shader Modules
        VkShaderModuleCreateInfo fragShaderInfo = allocate(new VkShaderModuleCreateInfo());
        VkShaderModuleCreateInfo vertShaderInfo = allocate(new VkShaderModuleCreateInfo());

        VkShaderModule fragShader = new VkShaderModule();
        fragShader.allocate();
        vkInstance.vkCreateShaderModule(
                device,
                TypedPointer64.of(createShaderModuleInfo(
                        fragShaderInfo,
                        BufferUtils::createAligned,
                        getClass().getResourceAsStream("/de/linusdev/cvg4j/vulkan/shaders/vulkanTest1.frag.spv")
                )),
                TypedPointer64.of(null),
                TypedPointer64.of(fragShader)
        );
        VkShaderModule vertShader = new VkShaderModule();
        vertShader.allocate();
        vkInstance.vkCreateShaderModule(
                device,
                TypedPointer64.of(createShaderModuleInfo(
                        vertShaderInfo,
                        BufferUtils::createAligned,
                        getClass().getResourceAsStream("/de/linusdev/cvg4j/vulkan/shaders/vulkanTest1.vert.spv")
                )),
                TypedPointer64.of(null),
                TypedPointer64.of(vertShader)
        );

        // Create Pipeline stages
        StructureArray<VkPipelineShaderStageCreateInfo> shaderStages = StructureArray.newAllocated(
                false,
                SVWrapper.of(2, VkPipelineShaderStageCreateInfo.class),
                null,
                VkPipelineShaderStageCreateInfo::new
        );
        VkPipelineShaderStageCreateInfo vkPipelineShaderStageCreateInfo = shaderStages.getOrCreate(0);
        vkPipelineShaderStageCreateInfo.sType.set(VkStructureType.PIPELINE_SHADER_STAGE_CREATE_INFO);
        vkPipelineShaderStageCreateInfo.stage.set(VkShaderStageFlagBits.VK_SHADER_STAGE_VERTEX_BIT);
        vkPipelineShaderStageCreateInfo.module.set(vertShader.get());
        vkPipelineShaderStageCreateInfo.pName.set(NullTerminatedUTF8String.newAllocated("main"));

        vkPipelineShaderStageCreateInfo = shaderStages.getOrCreate(1);
        vkPipelineShaderStageCreateInfo.sType.set(VkStructureType.PIPELINE_SHADER_STAGE_CREATE_INFO);
        vkPipelineShaderStageCreateInfo.stage.set(VkShaderStageFlagBits.VK_SHADER_STAGE_FRAGMENT_BIT);
        vkPipelineShaderStageCreateInfo.module.set(fragShader.get());
        vkPipelineShaderStageCreateInfo.pName.set(NullTerminatedUTF8String.newAllocated("main")); //TODO: This string "main" is unsafe. The JVM may garbage collect it at any time

        VkPipelineVertexInputStateCreateInfo vkPipelineVertexInputStateCreateInfo = new VkPipelineVertexInputStateCreateInfo();
        vkPipelineVertexInputStateCreateInfo.allocate();
        vkPipelineVertexInputStateCreateInfo.sType.set(VkStructureType.PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO);
        vkPipelineVertexInputStateCreateInfo.vertexAttributeDescriptionCount.set(0);
        vkPipelineVertexInputStateCreateInfo.vertexBindingDescriptionCount.set(0);

        VkPipelineInputAssemblyStateCreateInfo vkPipelineInputAssemblyStateCreateInfo = new VkPipelineInputAssemblyStateCreateInfo();
        vkPipelineInputAssemblyStateCreateInfo.allocate();
        vkPipelineInputAssemblyStateCreateInfo.sType.set(VkStructureType.PIPELINE_INPUT_ASSEMBLY_STATE_CREATE_INFO);
        vkPipelineInputAssemblyStateCreateInfo.topology.set(VkPrimitiveTopology.TRIANGLE_LIST);
        vkPipelineInputAssemblyStateCreateInfo.primitiveRestartEnable.set(VulkanUtils.booleanToVkBool32(false));

        VkViewport vkViewport = new VkViewport();
        vkViewport.allocate();
        vkViewport.x.set(0.0f);
        vkViewport.y.set(0.0f);
        vkViewport.width.set(selectedExtent.width.get());
        vkViewport.height.set(selectedExtent.height.get());
        vkViewport.minDepth.set(0.0f);
        vkViewport.maxDepth.set(1.0f);

        VkRect2D scissor = new VkRect2D();
        scissor.allocate();
        scissor.offset.x.set(0);
        scissor.offset.y.set(0);
        scissor.extent.width.set(selectedExtent.width.get());
        scissor.extent.height.set(selectedExtent.height.get());

        VkPipelineViewportStateCreateInfo vkPipelineViewportStateCreateInfo = new VkPipelineViewportStateCreateInfo();
        vkPipelineViewportStateCreateInfo.allocate();
        vkPipelineViewportStateCreateInfo.sType.set(VkStructureType.PIPELINE_VIEWPORT_STATE_CREATE_INFO);
        vkPipelineViewportStateCreateInfo.viewportCount.set(1);
        vkPipelineViewportStateCreateInfo.pViewports.set(vkViewport);
        vkPipelineViewportStateCreateInfo.scissorCount.set(1);
        vkPipelineViewportStateCreateInfo.pScissors.set(scissor);

        VkPipelineRasterizationStateCreateInfo vkPipelineRasterizationStateCreateInfo = new VkPipelineRasterizationStateCreateInfo();
        vkPipelineRasterizationStateCreateInfo.allocate();
        vkPipelineRasterizationStateCreateInfo.sType.set(VkStructureType.PIPELINE_RASTERIZATION_STATE_CREATE_INFO);
        vkPipelineRasterizationStateCreateInfo.depthClampEnable.set(VulkanUtils.booleanToVkBool32(false));
        vkPipelineRasterizationStateCreateInfo.rasterizerDiscardEnable.set(VulkanUtils.booleanToVkBool32(false));
        vkPipelineRasterizationStateCreateInfo.polygonMode.set(VkPolygonMode.FILL);
        vkPipelineRasterizationStateCreateInfo.lineWidth.set(1.0f);
        vkPipelineRasterizationStateCreateInfo.cullMode.set(VkCullModeFlagBits.VK_CULL_MODE_BACK_BIT);
        vkPipelineRasterizationStateCreateInfo.frontFace.set(VkFrontFace.CLOCKWISE);
        vkPipelineRasterizationStateCreateInfo.depthBiasEnable.set(VulkanUtils.booleanToVkBool32(false));

        VkPipelineMultisampleStateCreateInfo vkPipelineMultisampleStateCreateInfo = new VkPipelineMultisampleStateCreateInfo();
        vkPipelineMultisampleStateCreateInfo.allocate();
        vkPipelineMultisampleStateCreateInfo.sType.set(VkStructureType.PIPELINE_MULTISAMPLE_STATE_CREATE_INFO);
        vkPipelineMultisampleStateCreateInfo.sampleShadingEnable.set(VulkanUtils.booleanToVkBool32(false));
        vkPipelineMultisampleStateCreateInfo.rasterizationSamples.set(VkSampleCountFlagBits.VK_SAMPLE_COUNT_1_BIT);

        VkPipelineColorBlendAttachmentState vkPipelineColorBlendAttachmentState = new VkPipelineColorBlendAttachmentState();
        vkPipelineColorBlendAttachmentState.allocate();
        vkPipelineColorBlendAttachmentState.colorWriteMask.set(
                VkColorComponentFlagBits.VK_COLOR_COMPONENT_R_BIT,
                VkColorComponentFlagBits.VK_COLOR_COMPONENT_G_BIT,
                VkColorComponentFlagBits.VK_COLOR_COMPONENT_B_BIT,
                VkColorComponentFlagBits.VK_COLOR_COMPONENT_A_BIT
        );
        vkPipelineColorBlendAttachmentState.srcColorBlendFactor.set(VkBlendFactor.ZERO);
        vkPipelineColorBlendAttachmentState.dstColorBlendFactor.set(VkBlendFactor.ZERO);
        vkPipelineColorBlendAttachmentState.colorBlendOp.set(VkBlendOp.ADD);
        vkPipelineColorBlendAttachmentState.srcAlphaBlendFactor.set(VkBlendFactor.ONE);
        vkPipelineColorBlendAttachmentState.dstAlphaBlendFactor.set(VkBlendFactor.ZERO);
        vkPipelineColorBlendAttachmentState.alphaBlendOp.set(VkBlendOp.ADD);
        vkPipelineColorBlendAttachmentState.blendEnable.set(VulkanUtils.booleanToVkBool32(true));
        // more color blending options / explanations: https://vulkan-tutorial.com/en/Drawing_a_triangle/Graphics_pipeline_basics/Fixed_functions

        VkPipelineColorBlendStateCreateInfo vkPipelineColorBlendStateCreateInfo = new VkPipelineColorBlendStateCreateInfo();
        vkPipelineColorBlendStateCreateInfo.allocate();
        vkPipelineColorBlendStateCreateInfo.sType.set(VkStructureType.PIPELINE_COLOR_BLEND_STATE_CREATE_INFO);
        vkPipelineColorBlendStateCreateInfo.logicOpEnable.set(VulkanUtils.booleanToVkBool32(false));
        vkPipelineColorBlendStateCreateInfo.attachmentCount.set(1);
        vkPipelineColorBlendStateCreateInfo.pAttachments.set(vkPipelineColorBlendAttachmentState);

        // Create Pipeline Layout
        VkPipelineLayoutCreateInfo vkPipelineLayoutCreateInfo = new VkPipelineLayoutCreateInfo();
        vkPipelineLayoutCreateInfo.allocate();
        vkPipelineLayoutCreateInfo.sType.set(VkStructureType.PIPELINE_LAYOUT_CREATE_INFO);
        vkPipelineLayoutCreateInfo.setLayoutCount.set(0);
        vkPipelineLayoutCreateInfo.pSetLayouts.set(null);
        vkPipelineLayoutCreateInfo.pushConstantRangeCount.set(0);
        vkPipelineLayoutCreateInfo.pPushConstantRanges.set(null);

        VkPipelineLayout vkPipelineLayout = new VkPipelineLayout();
        vkPipelineLayout.allocate();
        vkInstance.vkCreatePipelineLayout(
                device,
                TypedPointer64.of(vkPipelineLayoutCreateInfo),
                TypedPointer64.of(null),
                TypedPointer64.of(vkPipelineLayout)
        ).check();

        // Render Pass
        VkAttachmentDescription vkAttachmentDescription = new VkAttachmentDescription();
        vkAttachmentDescription.allocate();
        vkAttachmentDescription.format.set(selectedSurfaceFormat.format.get());
        vkAttachmentDescription.samples.set(VkSampleCountFlagBits.VK_SAMPLE_COUNT_1_BIT);
        vkAttachmentDescription.loadOp.set(VkAttachmentLoadOp.CLEAR);
        vkAttachmentDescription.storeOp.set(VkAttachmentStoreOp.STORE);
        vkAttachmentDescription.stencilLoadOp.set(VkAttachmentLoadOp.DONT_CARE);
        vkAttachmentDescription.stencilStoreOp.set(VkAttachmentStoreOp.DONT_CARE);
        vkAttachmentDescription.initialLayout.set(VkImageLayout.UNDEFINED);
        vkAttachmentDescription.finalLayout.set(VkImageLayout.PRESENT_SRC_KHR);

        // Render Subpass for fragment shader
        VkAttachmentReference vkAttachmentReference = new VkAttachmentReference();
        vkAttachmentReference.allocate();
        vkAttachmentReference.attachment.set(0);
        vkAttachmentReference.layout.set(VkImageLayout.COLOR_ATTACHMENT_OPTIMAL);

        VkSubpassDescription fragmentSubpassDescription = new VkSubpassDescription();
        fragmentSubpassDescription.allocate();
        fragmentSubpassDescription.pipelineBindPoint.set(VkPipelineBindPoint.GRAPHICS);
        fragmentSubpassDescription.colorAttachmentCount.set(1);
        fragmentSubpassDescription.pColorAttachments.set(vkAttachmentReference);

        // create the render pass
        VkRenderPassCreateInfo vkRenderPassCreateInfo = new VkRenderPassCreateInfo();
        vkRenderPassCreateInfo.allocate();
        vkRenderPassCreateInfo.sType.set(VkStructureType.RENDER_PASS_CREATE_INFO);
        vkRenderPassCreateInfo.attachmentCount.set(1);
        vkRenderPassCreateInfo.pAttachments.set(vkAttachmentDescription);
        vkRenderPassCreateInfo.subpassCount.set(1);
        vkRenderPassCreateInfo.pSubpasses.set(fragmentSubpassDescription);

        VkSubpassDependency vkSubpassDependency = new VkSubpassDependency();
        vkSubpassDependency.allocate();
        vkSubpassDependency.srcSubpass.set(APIConstants.VK_SUBPASS_EXTERNAL);
        vkSubpassDependency.dstSubpass.set(0);
        vkSubpassDependency.srcStageMask.set(VkPipelineStageFlagBits.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT);
        vkSubpassDependency.srcAccessMask.set(0);
        vkSubpassDependency.dstStageMask.set(VkPipelineStageFlagBits.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT);
        vkSubpassDependency.dstAccessMask.set(VkAccessFlagBits.VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT);

        vkRenderPassCreateInfo.dependencyCount.set(1);
        vkRenderPassCreateInfo.pDependencies.set(vkSubpassDependency);

        VkRenderPass vkRenderPass = new VkRenderPass();
        vkRenderPass.allocate();
        vkInstance.vkCreateRenderPass(
                device,
                TypedPointer64.of(vkRenderPassCreateInfo),
                TypedPointer64.of(null),
                TypedPointer64.of(vkRenderPass)
        ).check();

        // Finally Create the Graphics Pipeline!
        VkGraphicsPipelineCreateInfo vkGraphicsPipelineCreateInfo = new VkGraphicsPipelineCreateInfo();
        vkGraphicsPipelineCreateInfo.allocate();
        vkGraphicsPipelineCreateInfo.sType.set(VkStructureType.GRAPHICS_PIPELINE_CREATE_INFO);
        vkGraphicsPipelineCreateInfo.stageCount.set(2);
        vkGraphicsPipelineCreateInfo.pStages.set(shaderStages.getPointer());
        vkGraphicsPipelineCreateInfo.pVertexInputState.set(vkPipelineVertexInputStateCreateInfo);
        vkGraphicsPipelineCreateInfo.pInputAssemblyState.set(vkPipelineInputAssemblyStateCreateInfo);
        vkGraphicsPipelineCreateInfo.pViewportState.set(vkPipelineViewportStateCreateInfo);
        vkGraphicsPipelineCreateInfo.pRasterizationState.set(vkPipelineRasterizationStateCreateInfo);
        vkGraphicsPipelineCreateInfo.pMultisampleState.set(vkPipelineMultisampleStateCreateInfo);
        vkGraphicsPipelineCreateInfo.pDepthStencilState.set(null);
        vkGraphicsPipelineCreateInfo.pColorBlendState.set(vkPipelineColorBlendStateCreateInfo);
        vkGraphicsPipelineCreateInfo.pDynamicState.set(null);
        vkGraphicsPipelineCreateInfo.layout.set(vkPipelineLayout.get());
        vkGraphicsPipelineCreateInfo.renderPass.set(vkRenderPass.get());
        vkGraphicsPipelineCreateInfo.subpass.set(0);

        VkPipeline graphicsPipeline = new VkPipeline();
        graphicsPipeline.allocate();
        VkPipelineCache cache = new VkPipelineCache();
        cache.allocate();
        cache.set(VulkanUtils.VK_NULL_HANDLE);
        vkInstance.vkCreateGraphicsPipelines(
                device,
                cache,
                1,
                TypedPointer64.of(vkGraphicsPipelineCreateInfo),
                TypedPointer64.of(null),
                TypedPointer64.of(graphicsPipeline)
        ).check();

        // Create Framebuffers
        StructureArray<VkFramebuffer> vkFramebuffers = StructureArray.newAllocated(
                false,
                SVWrapper.of(swapchainImageViews.length(), VkFramebuffer.class),
                null,
                VkFramebuffer::new
        );

        int i = 0;
        for (VkFramebuffer vkFramebuffer : vkFramebuffers) {
            VkFramebufferCreateInfo vkFramebufferCreateInfo = new VkFramebufferCreateInfo();
            vkFramebufferCreateInfo.allocate();
            vkFramebufferCreateInfo.sType.set(VkStructureType.FRAMEBUFFER_CREATE_INFO);
            vkFramebufferCreateInfo.renderPass.set(vkRenderPass.get());
            vkFramebufferCreateInfo.attachmentCount.set(1);
            vkFramebufferCreateInfo.pAttachments.set(swapchainImageViews.get(i));
            vkFramebufferCreateInfo.width.set(selectedExtent.width.get());
            vkFramebufferCreateInfo.height.set(selectedExtent.height.get());
            vkFramebufferCreateInfo.layers.set(1);

            vkInstance.vkCreateFramebuffer(
                    device,
                    TypedPointer64.of(vkFramebufferCreateInfo),
                    TypedPointer64.of(null),
                    TypedPointer64.of(vkFramebuffer)
            ).check();

            i++;
        }

        // Command pool and buffer
        VkCommandPoolCreateInfo vkCommandPoolCreateInfo = new VkCommandPoolCreateInfo();
        vkCommandPoolCreateInfo.allocate();
        vkCommandPoolCreateInfo.sType.set(VkStructureType.COMMAND_POOL_CREATE_INFO);
        vkCommandPoolCreateInfo.flags.set(VkCommandPoolCreateFlagBits.VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT);
        vkCommandPoolCreateInfo.queueFamilyIndex.set(graphicsQueueIndex);

        VkCommandPool vkCommandPool = new VkCommandPool();
        vkCommandPool.allocate();

        vkInstance.vkCreateCommandPool(device, TypedPointer64.of(vkCommandPoolCreateInfo), TypedPointer64.of(null), TypedPointer64.of(vkCommandPool)).check();

        VkCommandBufferAllocateInfo vkCommandBufferAllocateInfo = new VkCommandBufferAllocateInfo();
        vkCommandBufferAllocateInfo.allocate();
        vkCommandBufferAllocateInfo.sType.set(VkStructureType.COMMAND_BUFFER_ALLOCATE_INFO);
        vkCommandBufferAllocateInfo.commandPool.set(vkCommandPool.get());
        vkCommandBufferAllocateInfo.level.set(VkCommandBufferLevel.PRIMARY);
        vkCommandBufferAllocateInfo.commandBufferCount.set(1);

        VkCommandBuffer commandBuffer = new VkCommandBuffer();
        commandBuffer.allocate();

        vkInstance.vkAllocateCommandBuffers(device, TypedPointer64.of(vkCommandBufferAllocateInfo), TypedPointer64.of(commandBuffer)).check();

        VkCommandBufferBeginInfo vkCommandBufferBeginInfo = new VkCommandBufferBeginInfo();
        vkCommandBufferBeginInfo.allocate();
        vkCommandBufferBeginInfo.sType.set(VkStructureType.COMMAND_BUFFER_BEGIN_INFO);
        VkExtent2D finalSelectedExtent = selectedExtent;

        VkClearValue vkClearValue = new VkClearValue();
        vkClearValue.allocate();
        vkClearValue.color.float32.getOrCreate(0).set(0f);
        vkClearValue.color.float32.getOrCreate(1).set(0f);
        vkClearValue.color.float32.getOrCreate(2).set(0f);
        vkClearValue.color.float32.getOrCreate(3).set(1.0f);

        VkRenderPassBeginInfo vkRenderPassBeginInfo = new VkRenderPassBeginInfo();
        vkRenderPassBeginInfo.allocate();
        vkRenderPassBeginInfo.sType.set(VkStructureType.RENDER_PASS_BEGIN_INFO);
        vkRenderPassBeginInfo.renderPass.set(vkRenderPass.get());

        vkRenderPassBeginInfo.renderArea.offset.x.set(0);
        vkRenderPassBeginInfo.renderArea.offset.y.set(0);
        vkRenderPassBeginInfo.renderArea.extent.width.set(finalSelectedExtent.width.get());
        vkRenderPassBeginInfo.renderArea.extent.height.set(finalSelectedExtent.height.get());
        vkRenderPassBeginInfo.clearValueCount.set(1);


        Consumer<BBUInt1> recordCommandBuffer = imageIndex -> {
            vkInstance.vkBeginCommandBuffer(commandBuffer, TypedPointer64.of(vkCommandBufferBeginInfo)).check();

            vkRenderPassBeginInfo.framebuffer.set(vkFramebuffers.get(imageIndex.get()).get());
            vkRenderPassBeginInfo.pClearValues.set(vkClearValue); // make sure it is not GCed

            vkInstance.vkCmdBeginRenderPass(commandBuffer, TypedPointer64.of(vkRenderPassBeginInfo), VkSubpassContents.INLINE);
            vkInstance.vkCmdBindPipeline(commandBuffer, VkPipelineBindPoint.GRAPHICS, graphicsPipeline);
            vkInstance.vkCmdDraw(commandBuffer, 3, 1, 0, 0);
            vkInstance.vkCmdEndRenderPass(commandBuffer);
            vkInstance.vkEndCommandBuffer(commandBuffer).check();
        };

        // Draw the frame!

        // We need Synchronization
        VkSemaphore imageAvailableSemaphore = new VkSemaphore();
        VkSemaphore renderFinishedSemaphore = new VkSemaphore();
        VkFence frameSubmittedFence = new VkFence();

        imageAvailableSemaphore.allocate();
        renderFinishedSemaphore.allocate();
        frameSubmittedFence.allocate();

        VkSemaphoreCreateInfo vkSemaphoreCreateInfo = new VkSemaphoreCreateInfo();
        vkSemaphoreCreateInfo.allocate();
        vkSemaphoreCreateInfo.sType.set(VkStructureType.SEMAPHORE_CREATE_INFO);

        VkFenceCreateInfo vkFenceCreateInfo = new VkFenceCreateInfo();
        vkFenceCreateInfo.allocate();
        vkFenceCreateInfo.sType.set(VkStructureType.FENCE_CREATE_INFO);
        vkFenceCreateInfo.flags.set(VkFenceCreateFlagBits.VK_FENCE_CREATE_SIGNALED_BIT);

        vkInstance.vkCreateSemaphore(
                device,
                TypedPointer64.of(vkSemaphoreCreateInfo),
                TypedPointer64.of(null),
                TypedPointer64.of(imageAvailableSemaphore)
        ).check();
        vkInstance.vkCreateSemaphore(
                device,
                TypedPointer64.of(vkSemaphoreCreateInfo),
                TypedPointer64.of(null),
                TypedPointer64.of(renderFinishedSemaphore)
        ).check();
        vkInstance.vkCreateFence(
                device,
                TypedPointer64.of(vkFenceCreateInfo),
                TypedPointer64.of(null),
                TypedPointer64.of(frameSubmittedFence)
        ).check();

        // variables required in drawable
        BBUInt1 imageIndex = BBUInt1.newAllocated(null);
        VkFence nullHandleFence = new VkFence();
        nullHandleFence.allocate();
        nullHandleFence.set(VulkanUtils.VK_NULL_HANDLE);
        VkCommandBufferResetFlags noResetFlag = new VkCommandBufferResetFlags();
        noResetFlag.allocate();
        noResetFlag.set(0);

        VkSubmitInfo vkSubmitInfo = new VkSubmitInfo();
        vkSubmitInfo.allocate();
        vkSubmitInfo.sType.set(VkStructureType.SUBMIT_INFO);
        vkSubmitInfo.waitSemaphoreCount.set(1);
        vkSubmitInfo.pWaitSemaphores.set(imageAvailableSemaphore);
        VkPipelineStageFlags vkPipelineStageFlags = new VkPipelineStageFlags();
        vkPipelineStageFlags.allocate();
        vkPipelineStageFlags.set(VkPipelineStageFlagBits.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT);
        vkSubmitInfo.pWaitDstStageMask.set(vkPipelineStageFlags);
        vkSubmitInfo.commandBufferCount.set(1);
        vkSubmitInfo.pCommandBuffers.set(commandBuffer);
        vkSubmitInfo.signalSemaphoreCount.set(1);
        vkSubmitInfo.pSignalSemaphores.set(renderFinishedSemaphore);

        window.show((frameInfo) -> {
            // wait for previous frame to be submitted
            vkInstance.vkWaitForFences(device, 1, TypedPointer64.of(frameSubmittedFence), true, Long.MAX_VALUE).check();
            vkInstance.vkResetFences(device, 1, TypedPointer64.of(frameSubmittedFence)).check();

            // acquire Image from the swapchain
            vkInstance.vkAcquireNextImageKHR(device, swapchain, Long.MAX_VALUE, imageAvailableSemaphore, nullHandleFence, TypedPointer64.of(imageIndex));
            vkInstance.vkResetCommandBuffer(commandBuffer, noResetFlag);
            recordCommandBuffer.accept(imageIndex);

            // submit
            vkInstance.vkQueueSubmit(graphicsQueue, 1, TypedPointer64.of(vkSubmitInfo), frameSubmittedFence).check();

            // present
            VkPresentInfoKHR vkPresentInfo = new VkPresentInfoKHR();
            vkPresentInfo.allocate();
            vkPresentInfo.sType.set(VkStructureType.PRESENT_INFO_KHR);
            vkPresentInfo.waitSemaphoreCount.set(1);
            vkPresentInfo.pWaitSemaphores.set(renderFinishedSemaphore);
            vkPresentInfo.swapchainCount.set(1);
            vkPresentInfo.pSwapchains.set(swapchain);
            vkPresentInfo.pImageIndices.set(imageIndex);

            vkInstance.vkQueuePresentKHR(graphicsQueue, TypedPointer64.of(vkPresentInfo)).check();
        });

        // wait till devices has finished
        vkInstance.vkDeviceWaitIdle(device);



        // Cleanup
        vkInstance.vkDestroySemaphore(device, imageAvailableSemaphore, TypedPointer64.of(null));
        vkInstance.vkDestroySemaphore(device, renderFinishedSemaphore, TypedPointer64.of(null));
        vkInstance.vkDestroyCommandPool(device, vkCommandPool, TypedPointer64.of(null));
        vkInstance.vkDestroyFence(device, frameSubmittedFence, TypedPointer64.of(null));
        for (VkFramebuffer vkFramebuffer : vkFramebuffers) {
            vkInstance.vkDestroyFramebuffer(device, vkFramebuffer, TypedPointer64.of(null));
        }
        vkInstance.vkDestroyPipeline(device, graphicsPipeline, TypedPointer64.of(null));
        vkInstance.vkDestroyRenderPass(device, vkRenderPass, TypedPointer64.of(null));
        vkInstance.vkDestroyPipelineLayout(device, vkPipelineLayout, TypedPointer64.of(null));
        vkInstance.vkDestroyShaderModule(device, vertShader, TypedPointer64.of(null));
        vkInstance.vkDestroyShaderModule(device, fragShader, TypedPointer64.of(null));
        for (VkImageView swapchainImageView : swapchainImageViews) {
            vkInstance.vkDestroyImageView(device, swapchainImageView, TypedPointer64.of(null));
        }
        vkInstance.vkDestroySwapchainKHR(device, swapchain, TypedPointer64.of(null));
        vkInstance.vkDestroyDevice(device, TypedPointer64.of(null));
        window.close();

    }

}
