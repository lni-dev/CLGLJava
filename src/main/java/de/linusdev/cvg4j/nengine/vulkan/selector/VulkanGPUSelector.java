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

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public interface VulkanGPUSelector {

    @NotNull Map<PriorityModifierType, List<PriorityModifier>> modifiers();

    default int getPriority(
            @NotNull PriorityModifier.GpuInfo info
    ) {
        Map<PriorityModifierType, List<PriorityModifier>> modifiers = modifiers();

        List<PriorityModifier> add = modifiers.get(PriorityModifierType.ADD);
        List<PriorityModifier> multi = modifiers.get(PriorityModifierType.MULTIPLY);
        List<PriorityModifier> min = modifiers.get(PriorityModifierType.MIN);

        int value = 100;

        for (PriorityModifier mod : add)
            value = mod.apply(value, info);

        for (PriorityModifier mod : multi)
            value = mod.apply(value, info);

        for (PriorityModifier mod : min)
            value = mod.apply(value, info);

        return value;
    }

}
