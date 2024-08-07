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

import de.linusdev.cvg4j.engine.Engine;
import de.linusdev.cvg4j.nat.NativeUtils;
import de.linusdev.cvg4j.nat.glfw3.GLFW;
import de.linusdev.cvg4j.nat.glfw3.GLFWValues;
import de.linusdev.cvg4j.nat.glfw3.custom.RenderAPI;
import de.linusdev.cvg4j.nat.glfw3.exceptions.GLFWException;
import de.linusdev.cvg4j.nat.glfw3.objects.GLFWWindow;
import de.linusdev.cvg4j.nat.vulkan.VkBool32;
import de.linusdev.cvg4j.nat.vulkan.VulkanUtils;
import de.linusdev.cvg4j.nat.vulkan.bitmasks.enums.*;
import de.linusdev.cvg4j.nat.vulkan.constants.APIConstants;
import de.linusdev.cvg4j.nat.vulkan.enums.*;
import de.linusdev.cvg4j.nat.vulkan.handles.*;
import de.linusdev.cvg4j.nat.vulkan.structs.*;
import de.linusdev.lutils.ansi.sgr.SGR;
import de.linusdev.lutils.ansi.sgr.SGRParameters;
import de.linusdev.lutils.math.vector.buffer.floatn.BBFloat1;
import de.linusdev.lutils.math.vector.buffer.intn.BBInt2;
import de.linusdev.lutils.math.vector.buffer.intn.BBUInt1;
import de.linusdev.lutils.nat.array.NativeInt32Array;
import de.linusdev.lutils.nat.enums.NativeEnumValue32;
import de.linusdev.lutils.nat.pointer.BBTypedPointer64;
import de.linusdev.lutils.nat.pointer.TypedPointer64;
import de.linusdev.lutils.nat.string.NullTerminatedUTF8String;
import de.linusdev.lutils.nat.struct.annos.SVWrapper;
import de.linusdev.lutils.nat.struct.array.StructureArray;
import de.linusdev.lutils.nat.struct.utils.BufferUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

public class VulkanTest {


    @Test
    void test() throws GLFWException, IOException {
        Engine.StaticSetup.setup();
        GLFWWindow window = new GLFWWindow(RenderAPI.VULKAN, null);

        if(GLFW.glfwVulkanSupported() != GLFWValues.GLFW_TRUE) {
            System.err.println("Cannot run Vulkan test: Vulkan is not supported on this machine.");
            return;
        }

        var array = GLFW.glfwGetRequiredInstanceExtensions();

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
        vLayerStrings.getOrCreate(0).set(NullTerminatedUTF8String.ofString("VK_LAYER_KHRONOS_validation"));

        // VkApplicationInfo
        VkApplicationInfo vkApplicationInfo = new VkApplicationInfo();
        vkApplicationInfo.allocate();

        vkApplicationInfo.sType.set(VkStructureType.VK_STRUCTURE_TYPE_APPLICATION_INFO);
        vkApplicationInfo.pNext.set(0);
        vkApplicationInfo.pApplicationName.set(NullTerminatedUTF8String.ofString("Test Application"));
        vkApplicationInfo.applicationVersion.set(VulkanUtils.makeVersion(1, 0, 0));
        vkApplicationInfo.pEngineName.set(NullTerminatedUTF8String.ofString("CVG4J"));
        vkApplicationInfo.engineVersion.set(VulkanUtils.makeVersion(0, 1, 0));
        vkApplicationInfo.apiVersion.set(VulkanUtils.VK_API_VERSION_1_3);

        // VkInstanceCreateInfo
        VkInstanceCreateInfo vkInstanceCreateInfo = new VkInstanceCreateInfo();
        vkInstanceCreateInfo.allocate();

        vkInstanceCreateInfo.sType.set(VkStructureType.VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO);
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
        VulkanUtils.vkCreateInstance(vkInstanceCreateInfo, null, vkInstance).check();
        vkInstance.initMethodPointers();

        // Create window surface
        VkSurfaceKHR vkSurfaceKHR = new VkSurfaceKHR();
        vkSurfaceKHR.allocate();

        window.createVkWindowSurface(vkInstance, null, vkSurfaceKHR).check();

        // Pick GPU
        BBUInt1 count = BBUInt1.newAllocated(null);
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
            requiredDeviceExtensions.getOrCreate(i).set(NullTerminatedUTF8String.ofString(_requiredDeviceExtensions.get(i)));
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
            System.out.println("\t\t+ Extensions:");
            vkInstance.vkEnumerateDeviceExtensionProperties(
                    vkPhysicalDevice,
                    TypedPointer64.of(null),
                    TypedPointer64.of(count),
                    TypedPointer64.of(null)
            );
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
            System.out.println("\t\t\t* maxImageCount: (" + vkSurfaceCapabilitiesKHR.currentExtent.width.get() + ", " + vkSurfaceCapabilitiesKHR.currentExtent.height.get() + ")");

            // Surface Formats
            System.out.println("\t\t+ Surface Capabilities:");
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
                if(vkSurfaceFormat.format.get() == VkFormat.VK_FORMAT_B8G8R8A8_SRGB.getValue()
                && vkSurfaceFormat.colorSpace.get() == VkColorSpaceKHR.VK_COLOR_SPACE_SRGB_NONLINEAR_KHR.getValue()) {
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
                System.out.println("\t\t\t* " + vkSurfaceFormat.format.get(VkFormat.class) + SGR.reset());
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
                if(vkPresentMode.get() == VkPresentModeKHR.VK_PRESENT_MODE_MAILBOX_KHR.getValue()) {
                    selectedPresentMode = VkPresentModeKHR.VK_PRESENT_MODE_MAILBOX_KHR;
                    break;
                }
            }

            // if we didn't find it, select the one that is always there.
            if(selectedPresentMode == null) {
                selectedPresentMode = VkPresentModeKHR.VK_PRESENT_MODE_FIFO_KHR;
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
                //TODO: add min, max, clamp to VMath
            }

            // Chose image count
            selectedImageCount = vkSurfaceCapabilitiesKHR.minImageCount.get() + 1; // One more is better
            int maxImageCount = vkSurfaceCapabilitiesKHR.maxImageCount.get();
            if(maxImageCount != 0 && maxImageCount < selectedImageCount){ // If we cant to +1, do the max/min
                selectedImageCount = maxImageCount;
            }

            // Chose transform (choosing current is enough)
            selectedTransform = vkSurfaceCapabilitiesKHR.currentTransform.get(VkSurfaceTransformFlagBitsKHR.class);

            // TODO: Physical device selection:
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
        vkDeviceQueueCreateInfo.sType.set(VkStructureType.VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO);
        vkDeviceQueueCreateInfo.queueFamilyIndex.set(graphicsQueueIndex);
        vkDeviceQueueCreateInfo.queueCount.set(1);
        vkDeviceQueueCreateInfo.pQueuePriorities.set(prio);

        if(differentQueueIndices) {
            // Presentation Queue create info
            vkDeviceQueueCreateInfo = vkDeviceQueueCreateInfos.getOrCreate(1);
            vkDeviceQueueCreateInfo.sType.set(VkStructureType.VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO);
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
        vkDeviceCreateInfo.sType.set(VkStructureType.VK_STRUCTURE_TYPE_DEVICE_CREATE_INFO);
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
        swapchainCreateInfo.sType.set(VkStructureType.VK_STRUCTURE_TYPE_SWAPCHAIN_CREATE_INFO_KHR);
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
            swapchainCreateInfo.imageSharingMode.set(VkSharingMode.VK_SHARING_MODE_CONCURRENT);
            swapchainCreateInfo.queueFamilyIndexCount.set(2);
            NativeInt32Array queueFamilyIndices = NativeInt32Array.newAllocated(SVWrapper.length(2));
            queueFamilyIndices.setInt(0, graphicsQueueIndex);
            queueFamilyIndices.setInt(1, presentationQueueIndex);
            swapchainCreateInfo.pQueueFamilyIndices.set(queueFamilyIndices.getPointer());
        } else {
            swapchainCreateInfo.imageSharingMode.set(VkSharingMode.VK_SHARING_MODE_EXCLUSIVE);
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
            vkImageViewCreateInfo.sType.set(VkStructureType.VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO);
            vkImageViewCreateInfo.image.set(swapchainImages.getOrCreate(i).get());
            vkImageViewCreateInfo.viewType.set(VkImageViewType.VK_IMAGE_VIEW_TYPE_2D);
            vkImageViewCreateInfo.format.set(selectedSurfaceFormat.format.get());
            vkImageViewCreateInfo.components.r.set(VkComponentSwizzle.VK_COMPONENT_SWIZZLE_IDENTITY);
            vkImageViewCreateInfo.components.g.set(VkComponentSwizzle.VK_COMPONENT_SWIZZLE_IDENTITY);
            vkImageViewCreateInfo.components.b.set(VkComponentSwizzle.VK_COMPONENT_SWIZZLE_IDENTITY);
            vkImageViewCreateInfo.components.a.set(VkComponentSwizzle.VK_COMPONENT_SWIZZLE_IDENTITY);
            vkImageViewCreateInfo.subresourceRange.aspectMask.set(VkImageAspectFlagBits.VK_IMAGE_ASPECT_COLOR_BIT);
            vkImageViewCreateInfo.subresourceRange.baseMipLevel.set(0);
            vkImageViewCreateInfo.subresourceRange.levelCount.set(1);
            vkImageViewCreateInfo.subresourceRange.baseArrayLayer.set(0);
            vkImageViewCreateInfo.subresourceRange.layerCount.set(1);

            vkInstance.vkCreateImageView(device, TypedPointer64.of(vkImageViewCreateInfo), TypedPointer64.of(null), TypedPointer64.of(swapchainImageViews.getOrCreate(i))).check();
        }

        // Read binary shaders and create Shader Modules
        VkShaderModule fragShader = new VkShaderModule();
        fragShader.allocate();
        vkInstance.vkCreateShaderModule(
                device,
                TypedPointer64.of(VulkanUtils.createShaderModuleInfo(
                        getClass().getResourceAsStream("/de/linusdev/cvg4j/vulkan/shaders/vulkanTest1.frag.spv")
                )),
                TypedPointer64.of(null),
                TypedPointer64.of(fragShader)
        );
        VkShaderModule vertShader = new VkShaderModule();
        vertShader.allocate();
        vkInstance.vkCreateShaderModule(
                device,
                TypedPointer64.of(VulkanUtils.createShaderModuleInfo(
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
        vkPipelineShaderStageCreateInfo.sType.set(VkStructureType.VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO);
        vkPipelineShaderStageCreateInfo.stage.set(VkShaderStageFlagBits.VK_SHADER_STAGE_VERTEX_BIT);
        vkPipelineShaderStageCreateInfo.module.set(vertShader.get());
        vkPipelineShaderStageCreateInfo.pName.set(NullTerminatedUTF8String.ofString("main"));

        vkPipelineShaderStageCreateInfo = shaderStages.getOrCreate(1);
        vkPipelineShaderStageCreateInfo.sType.set(VkStructureType.VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO);
        vkPipelineShaderStageCreateInfo.stage.set(VkShaderStageFlagBits.VK_SHADER_STAGE_FRAGMENT_BIT);
        vkPipelineShaderStageCreateInfo.module.set(fragShader.get());
        vkPipelineShaderStageCreateInfo.pName.set(NullTerminatedUTF8String.ofString("main")); //TODO: This string "main" is unsafe. The JVM may garbage collect it at any time

        VkPipelineVertexInputStateCreateInfo vkPipelineVertexInputStateCreateInfo = new VkPipelineVertexInputStateCreateInfo();
        vkPipelineVertexInputStateCreateInfo.allocate();
        vkPipelineVertexInputStateCreateInfo.sType.set(VkStructureType.VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO);
        vkPipelineVertexInputStateCreateInfo.vertexAttributeDescriptionCount.set(0);
        vkPipelineVertexInputStateCreateInfo.vertexBindingDescriptionCount.set(0);

        VkPipelineInputAssemblyStateCreateInfo vkPipelineInputAssemblyStateCreateInfo = new VkPipelineInputAssemblyStateCreateInfo();
        vkPipelineInputAssemblyStateCreateInfo.allocate();
        vkPipelineInputAssemblyStateCreateInfo.sType.set(VkStructureType.VK_STRUCTURE_TYPE_PIPELINE_INPUT_ASSEMBLY_STATE_CREATE_INFO);
        vkPipelineInputAssemblyStateCreateInfo.topology.set(VkPrimitiveTopology.VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST);
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
        vkPipelineViewportStateCreateInfo.sType.set(VkStructureType.VK_STRUCTURE_TYPE_PIPELINE_VIEWPORT_STATE_CREATE_INFO);
        vkPipelineViewportStateCreateInfo.viewportCount.set(1);
        vkPipelineViewportStateCreateInfo.pViewports.set(vkViewport);
        vkPipelineViewportStateCreateInfo.scissorCount.set(1);
        vkPipelineViewportStateCreateInfo.pScissors.set(scissor);

        VkPipelineRasterizationStateCreateInfo vkPipelineRasterizationStateCreateInfo = new VkPipelineRasterizationStateCreateInfo();
        vkPipelineRasterizationStateCreateInfo.allocate();
        vkPipelineRasterizationStateCreateInfo.sType.set(VkStructureType.VK_STRUCTURE_TYPE_PIPELINE_RASTERIZATION_STATE_CREATE_INFO);
        vkPipelineRasterizationStateCreateInfo.depthClampEnable.set(VulkanUtils.booleanToVkBool32(false));
        vkPipelineRasterizationStateCreateInfo.rasterizerDiscardEnable.set(VulkanUtils.booleanToVkBool32(false));
        vkPipelineRasterizationStateCreateInfo.polygonMode.set(VkPolygonMode.VK_POLYGON_MODE_FILL);
        vkPipelineRasterizationStateCreateInfo.lineWidth.set(1.0f);
        vkPipelineRasterizationStateCreateInfo.cullMode.set(VkCullModeFlagBits.VK_CULL_MODE_BACK_BIT);
        vkPipelineRasterizationStateCreateInfo.frontFace.set(VkFrontFace.VK_FRONT_FACE_CLOCKWISE);
        vkPipelineRasterizationStateCreateInfo.depthBiasEnable.set(VulkanUtils.booleanToVkBool32(false));

        VkPipelineMultisampleStateCreateInfo vkPipelineMultisampleStateCreateInfo = new VkPipelineMultisampleStateCreateInfo();
        vkPipelineMultisampleStateCreateInfo.allocate();
        vkPipelineMultisampleStateCreateInfo.sType.set(VkStructureType.VK_STRUCTURE_TYPE_PIPELINE_MULTISAMPLE_STATE_CREATE_INFO);
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
        vkPipelineColorBlendAttachmentState.blendEnable.set(VulkanUtils.booleanToVkBool32(false));
        // more color blending options / explanations: https://vulkan-tutorial.com/en/Drawing_a_triangle/Graphics_pipeline_basics/Fixed_functions

        VkPipelineColorBlendStateCreateInfo vkPipelineColorBlendStateCreateInfo = new VkPipelineColorBlendStateCreateInfo();
        vkPipelineColorBlendStateCreateInfo.allocate();
        vkPipelineColorBlendStateCreateInfo.sType.set(VkStructureType.VK_STRUCTURE_TYPE_PIPELINE_COLOR_BLEND_STATE_CREATE_INFO);
        vkPipelineColorBlendStateCreateInfo.logicOpEnable.set(VulkanUtils.booleanToVkBool32(false));
        vkPipelineColorBlendStateCreateInfo.attachmentCount.set(1);
        vkPipelineColorBlendStateCreateInfo.pAttachments.set(vkPipelineColorBlendAttachmentState);

        // Create Pipeline Layout
        VkPipelineLayoutCreateInfo vkPipelineLayoutCreateInfo = new VkPipelineLayoutCreateInfo();
        vkPipelineLayoutCreateInfo.allocate();
        vkPipelineLayoutCreateInfo.sType.set(VkStructureType.VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO);
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
        vkAttachmentDescription.loadOp.set(VkAttachmentLoadOp.VK_ATTACHMENT_LOAD_OP_CLEAR);
        vkAttachmentDescription.storeOp.set(VkAttachmentStoreOp.VK_ATTACHMENT_STORE_OP_STORE);
        vkAttachmentDescription.stencilLoadOp.set(VkAttachmentLoadOp.VK_ATTACHMENT_LOAD_OP_DONT_CARE);
        vkAttachmentDescription.stencilStoreOp.set(VkAttachmentStoreOp.VK_ATTACHMENT_STORE_OP_DONT_CARE);
        vkAttachmentDescription.initialLayout.set(VkImageLayout.VK_IMAGE_LAYOUT_UNDEFINED);
        vkAttachmentDescription.finalLayout.set(VkImageLayout.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR);

        // Render Subpass for fragment shader
        VkAttachmentReference vkAttachmentReference = new VkAttachmentReference();
        vkAttachmentReference.allocate();
        vkAttachmentReference.attachment.set(0);
        vkAttachmentReference.layout.set(VkImageLayout.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);

        VkSubpassDescription fragmentSubpassDescription = new VkSubpassDescription();
        fragmentSubpassDescription.allocate();
        fragmentSubpassDescription.pipelineBindPoint.set(VkPipelineBindPoint.VK_PIPELINE_BIND_POINT_GRAPHICS);
        fragmentSubpassDescription.colorAttachmentCount.set(1);
        fragmentSubpassDescription.pColorAttachments.set(vkAttachmentReference);

        // create the render pass
        VkRenderPassCreateInfo vkRenderPassCreateInfo = new VkRenderPassCreateInfo();
        vkRenderPassCreateInfo.allocate();
        vkRenderPassCreateInfo.sType.set(VkStructureType.VK_STRUCTURE_TYPE_RENDER_PASS_CREATE_INFO);
        vkRenderPassCreateInfo.attachmentCount.set(1);
        vkRenderPassCreateInfo.pAttachments.set(vkAttachmentDescription);
        vkRenderPassCreateInfo.subpassCount.set(1);
        vkRenderPassCreateInfo.pSubpasses.set(fragmentSubpassDescription);

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
        vkGraphicsPipelineCreateInfo.sType.set(VkStructureType.VK_STRUCTURE_TYPE_GRAPHICS_PIPELINE_CREATE_INFO);
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

        VkPipeline vkPipeline = new VkPipeline();
        vkPipeline.allocate();
        VkPipelineCache cache = new VkPipelineCache();
        cache.allocate();
        cache.set(VulkanUtils.VK_NULL_HANDLE);
        vkInstance.vkCreateGraphicsPipelines(
                device,
                cache,
                1,
                TypedPointer64.of(vkGraphicsPipelineCreateInfo),
                TypedPointer64.of(null),
                TypedPointer64.of(vkPipeline)
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
            vkFramebufferCreateInfo.sType.set(VkStructureType.VK_STRUCTURE_TYPE_FRAMEBUFFER_CREATE_INFO);
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

        // Cleanup
        for (VkFramebuffer vkFramebuffer : vkFramebuffers) {
            vkInstance.vkDestroyFramebuffer(device, vkFramebuffer, TypedPointer64.of(null));
        }
        vkInstance.vkDestroyPipeline(device, vkPipeline, TypedPointer64.of(null));
        vkInstance.vkDestroyRenderPass(device, vkRenderPass, TypedPointer64.of(null));
        vkInstance.vkDestroyPipelineLayout(device, vkPipelineLayout, TypedPointer64.of(null));
        vkInstance.vkDestroyShaderModule(device, vertShader, TypedPointer64.of(null));
        vkInstance.vkDestroyShaderModule(device, fragShader, TypedPointer64.of(null));
        for (VkImageView swapchainImageView : swapchainImageViews) {
            vkInstance.vkDestroyImageView(device, swapchainImageView, TypedPointer64.of(null));
        }
        vkInstance.vkDestroySwapchainKHR(device, swapchain, TypedPointer64.of(null));
        vkInstance.vkDestroyDevice(device, TypedPointer64.of(null));
        vkInstance.vkDestroySurfaceKHR(vkSurfaceKHR, TypedPointer64.of(null));
        vkInstance.vkDestroyInstance(TypedPointer64.of(null));
        window.close();

    }

}
