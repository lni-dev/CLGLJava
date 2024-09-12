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

package de.linusdev.cvg4j.engine.vk.selector.present.mode;

import de.linusdev.cvg4j.nat.vulkan.enums.VkPresentModeKHR;
import de.linusdev.cvg4j.engine.vk.selector.priority.Priority;
import de.linusdev.lutils.nat.enums.EnumValue32;
import de.linusdev.lutils.nat.enums.NativeEnumValue32;
import de.linusdev.lutils.nat.struct.array.StructureArray;
import de.linusdev.lutils.result.BiResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

class BasicPresentModeSelector implements PresentModeSelector {

    private final @NotNull List<PresentModeWithPriority> modes;

    BasicPresentModeSelector(@NotNull List<PresentModeWithPriority> modes) {
        this.modes = modes;
    }

    @Override
    public @NotNull BiResult<@Nullable EnumValue32<VkPresentModeKHR>, @NotNull Priority> select(
            int presentModeCounts,
            @NotNull StructureArray<NativeEnumValue32<VkPresentModeKHR>> formats
    ) {
        int bestModePriority = NOTHING_SELECTED_PRIORITY;
        NativeEnumValue32<VkPresentModeKHR> best = null;

        for (int i = 0; i < presentModeCounts; i++) {
            NativeEnumValue32<VkPresentModeKHR> availableMode = formats.get(i);

            for (PresentModeWithPriority pm : this.modes) {
                if(pm.allows(availableMode) && pm.priority().priority() > bestModePriority) {
                    bestModePriority = pm.priority().priority();
                    best = availableMode;
                }
            }
        }

        return new BiResult<>(
                best,
                Priority.of(bestModePriority)
        );
    }

    public static class Builder {
        private final List<PresentModeWithPriority> modes = new ArrayList<>();

        Builder() {}

        public Builder add(
                @Nullable VkPresentModeKHR mode,
                @NotNull Priority priority
        ) {
            modes.add(new PresentModeWithPriority(mode, priority));
            return this;
        }

        public PresentModeSelector build() {
            return new BasicPresentModeSelector(modes);
        }
    }

}
