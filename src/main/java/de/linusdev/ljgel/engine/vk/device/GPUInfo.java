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

package de.linusdev.ljgel.engine.vk.device;

import de.linusdev.ljgel.engine.vk.selector.queue.family.QueueFamilyInfo;
import de.linusdev.ljgel.nat.vulkan.bool.VkBool32;
import de.linusdev.ljgel.nat.vulkan.handles.VkInstance;
import de.linusdev.ljgel.nat.vulkan.handles.VkPhysicalDevice;
import de.linusdev.ljgel.nat.vulkan.handles.VkSurfaceKHR;
import de.linusdev.ljgel.nat.vulkan.structs.VkExtensionProperties;
import de.linusdev.ljgel.nat.vulkan.structs.VkPhysicalDeviceProperties;
import de.linusdev.ljgel.nat.vulkan.structs.VkQueueFamilyProperties;
import de.linusdev.ljgel.nat.vulkan.utils.VulkanUtils;
import de.linusdev.lutils.math.vector.buffer.intn.BBUInt1;
import de.linusdev.lutils.nat.memory.stack.Stack;
import de.linusdev.lutils.nat.pointer.TypedPointer64;
import de.linusdev.lutils.nat.struct.array.StructureArray;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static de.linusdev.lutils.nat.pointer.TypedPointer64.ofArray;
import static de.linusdev.lutils.nat.pointer.TypedPointer64.ref;

/**
 * This class can only be used with {@link Stack#popPoint()}, as it will push stuff onto the stack,
 * which it won't pop again.
 */
public class GPUInfo {
    public final @NotNull VkBool32 bool = new VkBool32();
    public final @NotNull BBUInt1 integer = BBUInt1.newUnallocated();

    public VkPhysicalDevice vkPhysicalDevice;
    public final @NotNull VkPhysicalDeviceProperties props = new VkPhysicalDeviceProperties();


    public StructureArray<VkExtensionProperties> extensions;
    public int extensionCount;
    public StructureArray<VkQueueFamilyProperties> queueFamilies;
    public int queueFamilyCount;


    public List<QueueFamilyInfo> queueFamilyInfoList;
    public final SurfaceInfo surfaceInfo;

    public GPUInfo(@NotNull Stack stack) {
        this.surfaceInfo = new SurfaceInfo(stack);
        stack.push(bool);
        stack.push(integer);
        stack.push(props);
    }

    public void fillOfDevice(
            @NotNull Stack stack,
            @NotNull VkInstance vkInstance,
            @NotNull VkPhysicalDevice dev,
            @NotNull VkSurfaceKHR vkSurface
    ) {
        vkPhysicalDevice = dev;

        // Props
        vkInstance.vkGetPhysicalDeviceProperties(dev, TypedPointer64.of(props));

        // Extensions
        vkInstance.vkEnumerateDeviceExtensionProperties(dev, ref(null), ref(integer), ref(null));
        extensionCount = integer.get();
        if(extensions == null || extensionCount > extensions.length()) {
            extensions = stack.pushArray(extensionCount, VkExtensionProperties.class, VkExtensionProperties::new);
        }
        vkInstance.vkEnumerateDeviceExtensionProperties(dev, ref(null), ref(integer), ofArray(extensions));

        // Queue Family properties
        vkInstance.vkGetPhysicalDeviceQueueFamilyProperties(dev, ref(integer), ref(null));
        queueFamilyCount = integer.get();
        if(queueFamilies == null || queueFamilyCount > queueFamilies.length()) {
            queueFamilies = stack.pushArray(queueFamilyCount, VkQueueFamilyProperties.class, VkQueueFamilyProperties::new);
        }
        vkInstance.vkGetPhysicalDeviceQueueFamilyProperties(dev, ref(integer), ofArray(queueFamilies));

        queueFamilyInfoList = new ArrayList<>(queueFamilyCount);
        // Check which queue families support the surface
        for (int i = 0; i < queueFamilyCount; i++) {
            vkInstance.vkGetPhysicalDeviceSurfaceSupportKHR(dev, i, vkSurface, ref(bool)).check();
            queueFamilyInfoList.add(new QueueFamilyInfo(i, queueFamilies.get(i), VulkanUtils.vkBool32ToBoolean(bool.get())));
        }

        surfaceInfo.fillOfDevice(stack, vkInstance, dev, vkSurface);
    }
}
