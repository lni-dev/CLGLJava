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

package de.linusdev.cvg4j.engine.vk.selector.surface.format;

import de.linusdev.cvg4j.nat.vulkan.enums.VkColorSpaceKHR;
import de.linusdev.cvg4j.nat.vulkan.enums.VkFormat;
import de.linusdev.cvg4j.nat.vulkan.structs.VkSurfaceFormatKHR;
import de.linusdev.cvg4j.engine.vk.selector.priority.Priority;
import de.linusdev.lutils.nat.struct.array.StructureArray;
import de.linusdev.lutils.result.BiResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

class BasicSurfaceFormatSelector implements SurfaceFormatSelector {

    private final List<FormatWithPriority> formats;

    BasicSurfaceFormatSelector(List<FormatWithPriority> formats) {
        this.formats = formats;
    }

    @Override
    public @NotNull BiResult<@Nullable VkSurfaceFormatKHR, @NotNull Priority> select(
            int formatCounts,
            @NotNull StructureArray<VkSurfaceFormatKHR> formats
    ) {
        int bestFormatPriority = NOTHING_SELECTED_PRIORITY;
        VkSurfaceFormatKHR best = null;

        for (int i = 0; i < formatCounts; i++) {
            VkSurfaceFormatKHR availableFormat = formats.get(i);

            for (FormatWithPriority fc : this.formats) {
                if(fc.allows(availableFormat) && fc.priority().priority() > bestFormatPriority) {
                    bestFormatPriority = fc.priority().priority();
                    best = availableFormat;
                }
            }
        }

        return new BiResult<>(best, Priority.of(bestFormatPriority));
    }

    public static class Builder {
        private final List<FormatWithPriority> formats = new ArrayList<>();

        Builder() {}

        public Builder add(@Nullable VkFormat format, @Nullable VkColorSpaceKHR colorSpace, @NotNull Priority priority) {
            formats.add(new FormatWithPriority(format, colorSpace, priority));
            return this;
        }

        public SurfaceFormatSelector build() {
            return new BasicSurfaceFormatSelector(formats);
        }
    }



}
