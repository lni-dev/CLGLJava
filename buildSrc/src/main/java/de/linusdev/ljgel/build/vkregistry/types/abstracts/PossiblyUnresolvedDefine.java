/*
 * Copyright (c) 2024-2025 Linus Andera
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

package de.linusdev.ljgel.build.vkregistry.types.abstracts;

import de.linusdev.ljgel.build.vkregistry.RegistryLoader;
import de.linusdev.ljgel.build.vkregistry.types.GroupedDefinesType;
import org.jetbrains.annotations.NotNull;

public class PossiblyUnresolvedDefine {

    final @NotNull RegistryLoader registry;
    final @NotNull String name;

    public PossiblyUnresolvedDefine(
            @NotNull RegistryLoader registry,
            @NotNull String name
    ) {
        this.registry = registry;
        this.name = name;
    }

    public @NotNull GroupedDefinesType.Define resolve() {
        var define = registry.getDefine(name);
        if(define == null)
            throw new IllegalStateException("Cannot find define '" + name + "'.");
        return define;
    }

    public @NotNull String getName() {
        return name;
    }

}
