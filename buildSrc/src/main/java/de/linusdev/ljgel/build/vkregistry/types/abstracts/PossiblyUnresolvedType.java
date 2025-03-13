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
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class PossiblyUnresolvedType {

    final @NotNull RegistryLoader registry;
    final @NotNull String name;

    public PossiblyUnresolvedType(
            @NotNull RegistryLoader registry,
            @NotNull String name
    ) {
        this.registry = registry;
        this.name = name;
    }

    public @NotNull Type resolve() {
        var type = registry.getType(name);
        if(type == null)
            throw new IllegalStateException("Cannot find type '" + name + "'.");
        return type;
    }

    public @NotNull Type resolveOrElse(@NotNull Supplier<Type> ifNotResolvable, boolean generate) {
        var type = registry.getType(name);
        if(type == null) {
            type = ifNotResolvable.get();
            registry.addType(type, generate);
        }

        return type;
    }

    public @NotNull String getName() {
        return name;
    }
}
