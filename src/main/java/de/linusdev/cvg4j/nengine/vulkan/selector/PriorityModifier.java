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

public class PriorityModifier<T> {

    private final @NotNull T value;
    private final @NotNull PriorityModifierType type;
    private final int modifier;

    public PriorityModifier(
            @NotNull T value,
            @NotNull PriorityModifierType type,
            int modifier
    ) {
        this.value = value;
        this.type = type;
        this.modifier = modifier;
    }
}
