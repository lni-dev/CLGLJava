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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModifiableVulkanGPUSelector implements VulkanGPUSelector{

    private final @NotNull Map<PriorityModifierType, List<PriorityModifier>> modifiers;

    public ModifiableVulkanGPUSelector() {
        this.modifiers = new HashMap<>();
        for (PriorityModifierType type : PriorityModifierType.values()) {
            modifiers.put(type, new ArrayList<>());
        }
    }

    @Override
    public @NotNull Map<PriorityModifierType, List<PriorityModifier>> modifiers() {
        return modifiers;
    }

    public void add(@NotNull PriorityModifier mod) {
        modifiers.get(mod.type()).add(mod);
    }
}
