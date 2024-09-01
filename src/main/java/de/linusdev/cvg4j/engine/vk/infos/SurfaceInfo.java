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

import de.linusdev.cvg4j.nat.vulkan.enums.VkPresentModeKHR;
import de.linusdev.cvg4j.nat.vulkan.handles.VkInstance;
import de.linusdev.cvg4j.nat.vulkan.handles.VkPhysicalDevice;
import de.linusdev.cvg4j.nat.vulkan.handles.VkSurfaceKHR;
import de.linusdev.cvg4j.nat.vulkan.structs.VkSurfaceCapabilitiesKHR;
import de.linusdev.cvg4j.nat.vulkan.structs.VkSurfaceFormatKHR;
import de.linusdev.lutils.math.vector.buffer.intn.BBUInt1;
import de.linusdev.lutils.nat.enums.NativeEnumValue32;
import de.linusdev.lutils.nat.struct.array.StructureArray;
import org.jetbrains.annotations.NotNull;

import static de.linusdev.lutils.nat.pointer.TypedPointer64.ofArray;
import static de.linusdev.lutils.nat.pointer.TypedPointer64.ref;

public record SurfaceInfo(
        @NotNull VkSurfaceCapabilitiesKHR surfacesCaps,
        int surfaceFormatCount,
        @NotNull StructureArray<VkSurfaceFormatKHR> surfaceFormats,
        int presentModeCount,
        @NotNull StructureArray<NativeEnumValue32<VkPresentModeKHR>> presentModes
) {

    public static @NotNull SurfaceInfo ofVkSurface(
            @NotNull VkInstance vkInstance,
            @NotNull VkSurfaceKHR vkSurface,
            @NotNull VkPhysicalDevice dev,
            @NotNull BBUInt1 integer,
            
            @NotNull VkSurfaceCapabilitiesKHR surfacesCaps,
            @NotNull StructureArray<VkSurfaceFormatKHR> surfaceFormats,
            @NotNull StructureArray<NativeEnumValue32<VkPresentModeKHR>> presentModes
    ) {
        // Surface caps
        vkInstance.vkGetPhysicalDeviceSurfaceCapabilitiesKHR(dev, vkSurface, ref(surfacesCaps));

        // Surface formats
        vkInstance.vkGetPhysicalDeviceSurfaceFormatsKHR(dev, vkSurface, ref(integer), ref(null));
        int surfaceFormatCount = integer.get();
        if(surfaceFormatCount > surfaceFormats.length()) {
            // unlikely, if this happens just allocate one outside the stack
            surfaceFormats = StructureArray.newAllocated(surfaceFormatCount, VkSurfaceFormatKHR.class, VkSurfaceFormatKHR::new);
        }
        vkInstance.vkGetPhysicalDeviceSurfaceFormatsKHR(dev, vkSurface, ref(integer), ofArray(surfaceFormats));

        // Presentation Modes
        vkInstance.vkGetPhysicalDeviceSurfacePresentModesKHR(dev, vkSurface, ref(integer), ref(null));
        int presentModeCount = integer.get();
        if(presentModeCount > presentModes.length()) {
            // unlikely, if this happens just allocate one outside the stack
            presentModes = StructureArray.newAllocated(presentModeCount, NativeEnumValue32.class, NativeEnumValue32::newUnallocatedT);
        }
        vkInstance.vkGetPhysicalDeviceSurfacePresentModesKHR(dev, vkSurface, ref(integer), ofArray(presentModes));

        return new SurfaceInfo(surfacesCaps, surfaceFormatCount, surfaceFormats, presentModeCount, presentModes);
    }
}
