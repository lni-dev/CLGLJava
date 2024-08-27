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

package de.linusdev.cvg4j.nengine.vulkan.selector;

import de.linusdev.cvg4j.nat.vulkan.enums.VkPresentModeKHR;
import de.linusdev.cvg4j.nat.vulkan.handles.VkPhysicalDevice;
import de.linusdev.cvg4j.nat.vulkan.structs.VkExtensionProperties;
import de.linusdev.cvg4j.nat.vulkan.structs.VkPhysicalDeviceProperties;
import de.linusdev.cvg4j.nat.vulkan.structs.VkSurfaceCapabilitiesKHR;
import de.linusdev.cvg4j.nat.vulkan.structs.VkSurfaceFormatKHR;
import de.linusdev.lutils.nat.enums.NativeEnumValue32;
import de.linusdev.lutils.nat.struct.array.StructureArray;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GPUSelectionProgress {

    private final @NotNull VulkanGPUSelector selector;

    private @Nullable VkPhysicalDevice best;
    private int bestValue = 0;

    public GPUSelectionProgress(@NotNull VulkanGPUSelector selector) {
        this.selector = selector;
    }

    public int addGpu(
            @NotNull VkPhysicalDevice handle,
            @NotNull VkPhysicalDeviceProperties props,
            int extensionCount,
            StructureArray<VkExtensionProperties> extensions,
            @NotNull VkSurfaceCapabilitiesKHR surfacesCaps,
            int surfaceFormatCount,
            @NotNull StructureArray<VkSurfaceFormatKHR> surfaceFormats,
            int presentModeCount,
            @NotNull StructureArray<NativeEnumValue32<VkPresentModeKHR>> presentModes
    ) {
        GpuInfo info = new GpuInfo(
                props,
                extensionCount,
                extensions,
                surfacesCaps,
                surfaceFormatCount,
                surfaceFormats,
                presentModeCount,
                presentModes
        );

        int p = selector.getPriority(info);
        if(p > bestValue) {
            bestValue = p;
            best = handle;
        }

        return p;
    }

    public @Nullable VkPhysicalDevice getBestGPU() {
        return best;
    }

}
