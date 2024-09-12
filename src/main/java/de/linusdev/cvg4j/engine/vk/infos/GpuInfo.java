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

package de.linusdev.cvg4j.engine.vk.infos;

import de.linusdev.cvg4j.nat.vulkan.VkBool32;
import de.linusdev.cvg4j.nat.vulkan.handles.VkInstance;
import de.linusdev.cvg4j.nat.vulkan.handles.VkPhysicalDevice;
import de.linusdev.cvg4j.nat.vulkan.handles.VkSurfaceKHR;
import de.linusdev.cvg4j.nat.vulkan.structs.*;
import de.linusdev.cvg4j.nat.vulkan.utils.VulkanUtils;
import de.linusdev.cvg4j.engine.vk.selector.queue.family.QueueFamilyInfo;
import de.linusdev.lutils.math.vector.buffer.intn.BBUInt1;
import de.linusdev.lutils.nat.pointer.TypedPointer64;
import de.linusdev.lutils.nat.struct.array.StructureArray;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static de.linusdev.lutils.nat.pointer.TypedPointer64.ofArray;
import static de.linusdev.lutils.nat.pointer.TypedPointer64.ref;

public record GpuInfo(
        @NotNull VkPhysicalDevice vkPhysicalDevice,
        @NotNull VkPhysicalDeviceProperties props,
        int extensionCount,
        @NotNull StructureArray<VkExtensionProperties> extensions,
        int queueFamilyCount,
        @NotNull StructureArray<VkQueueFamilyProperties> queueFamilies,
        @NotNull List<QueueFamilyInfo> queueFamilyInfoList,
        @NotNull SurfaceInfo surfaceInfo
) {

    public static @NotNull GpuInfo ofPhysicalDevice(
            @NotNull VkInstance vkInstance,
            @NotNull VkSurfaceKHR vkSurface,
            @NotNull VkPhysicalDevice dev,
            @NotNull BBUInt1 integer,
            @NotNull VkPhysicalDeviceProperties props,
            @NotNull StructureArray<VkExtensionProperties> extensions,
            @NotNull StructureArray<VkQueueFamilyProperties> queueFamilies,
            @NotNull VkBool32 queueFamilySupportsSurface,
            @NotNull SurfaceInfo surfaceInfo
    ) {
        // Props
        vkInstance.vkGetPhysicalDeviceProperties(dev, TypedPointer64.of(props));

        // Extensions
        vkInstance.vkEnumerateDeviceExtensionProperties(dev, ref(null), ref(integer), ref(null));
        int extensionCount = integer.get();
        if(extensionCount > extensions.length()) {
            // unlikely, if this happens just allocate one outside the stack
            extensions = StructureArray.newAllocated(extensionCount, VkExtensionProperties.class, VkExtensionProperties::new);
        }
        vkInstance.vkEnumerateDeviceExtensionProperties(dev, ref(null), ref(integer), ofArray(extensions));

        // Queue Family properties
        vkInstance.vkGetPhysicalDeviceQueueFamilyProperties(dev, ref(integer), ref(null));
        int queueFamilyCount = integer.get();
        if(queueFamilyCount > queueFamilies.length()) {
            // unlikely, if this happens just allocate one outside the stack
            queueFamilies = StructureArray.newAllocated(queueFamilyCount, VkQueueFamilyProperties.class, VkQueueFamilyProperties::new);
        }
        vkInstance.vkGetPhysicalDeviceQueueFamilyProperties(dev, ref(integer), ofArray(queueFamilies));

        List<QueueFamilyInfo> queueFamilyInfoList = new ArrayList<>(queueFamilyCount);
        // Check which queue families support the surface
        for (int i = 0; i < queueFamilyCount; i++) {
            vkInstance.vkGetPhysicalDeviceSurfaceSupportKHR(dev, i, vkSurface, ref(queueFamilySupportsSurface)).check();
            queueFamilyInfoList.add(new QueueFamilyInfo(i, queueFamilies.get(i), VulkanUtils.vkBool32ToBoolean(queueFamilySupportsSurface.get())));
        }

        // Fill GpuInfo
        return new GpuInfo(
                dev,
                props,
                extensionCount,
                extensions,
                queueFamilyCount,
                queueFamilies,
                queueFamilyInfoList,
                surfaceInfo
        );
    }
    
}