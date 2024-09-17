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

package de.linusdev.cvg4j.engine.vk.selector.gpu;

import de.linusdev.cvg4j.engine.vk.device.GPUInfo;
import de.linusdev.cvg4j.nat.vulkan.handles.VkPhysicalDevice;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GPUSelectionProgress {

    private final @NotNull VulkanGPUSelector selector;

    private @Nullable VkPhysicalDevice best;
    private int bestValue = 0;

    GPUSelectionProgress(@NotNull VulkanGPUSelector selector) {
        this.selector = selector;
    }

    public int addGpu(
            @NotNull VkPhysicalDevice handle,
            @NotNull GPUInfo info
    ) {
        int p = selector.getPriority(info);
        if(p > bestValue) {
            bestValue = p;
            best = handle;
        }

        return p;
    }

    public boolean canSelectionStop() {
        return bestValue >= selector.maxPriority().priority();
    }

    public @Nullable VkPhysicalDevice getBestGPU() {
        return best;
    }

}
