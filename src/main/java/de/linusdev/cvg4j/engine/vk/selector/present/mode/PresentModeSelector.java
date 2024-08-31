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
import de.linusdev.cvg4j.engine.vk.selector.Selector;
import de.linusdev.cvg4j.engine.vk.selector.priority.Priority;
import de.linusdev.lutils.nat.enums.EnumValue32;
import de.linusdev.lutils.nat.enums.NativeEnumValue32;
import de.linusdev.lutils.nat.struct.array.StructureArray;
import de.linusdev.lutils.result.BiResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface PresentModeSelector extends Selector {

    static @NotNull BasicPresentModeSelector.Builder builder() {
        return new BasicPresentModeSelector.Builder();
    }

    /**
     * Select the best present mode.
     * @param presentModeCounts count of formats
     * @param modes array of format with {@code presentModeCounts} formats
     * @return the best present mode and it's priority or {@code null} and {@value #NOTHING_SELECTED_PRIORITY}
     */
    @NotNull BiResult<@Nullable EnumValue32<VkPresentModeKHR>, @NotNull Priority> select(
            int presentModeCounts,
            @NotNull StructureArray<NativeEnumValue32<VkPresentModeKHR>> modes
    );

}
