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

package de.linusdev.cvg4j.nengine.vulkan.selector.surface.format;

import de.linusdev.cvg4j.nat.vulkan.structs.VkSurfaceFormatKHR;
import de.linusdev.cvg4j.nengine.vulkan.selector.Selector;
import de.linusdev.cvg4j.nengine.vulkan.selector.priority.Priority;
import de.linusdev.lutils.nat.struct.array.StructureArray;
import de.linusdev.lutils.result.BiResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface SurfaceFormatSelector extends Selector {

    static @NotNull BasicSurfaceFormatSelector.Builder builder() {
        return new BasicSurfaceFormatSelector.Builder();
    }

    /**
     * Select the best format.
     * @param formatCounts count of formats
     * @param formats array of format with {@code formatCounts} formats
     * @return the best format and it's priority or {@code null} and {@value NOTHING_SELECTED_PRIORITY}
     */
    @NotNull BiResult<@Nullable VkSurfaceFormatKHR, @NotNull Priority> select(
            int formatCounts,
            @NotNull StructureArray<VkSurfaceFormatKHR> formats
    );

}
