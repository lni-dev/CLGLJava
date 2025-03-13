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

package de.linusdev.ljgel.build.vkregistry.types;

import de.linusdev.ljgel.build.vkregistry.RegistryLoader;
import de.linusdev.ljgel.build.vkregistry.types.abstracts.PossiblyUnresolvedType;
import de.linusdev.ljgel.build.vkregistry.types.abstracts.Type;
import de.linusdev.ljgel.build.vkregistry.types.abstracts.TypeType;
import de.linusdev.lutils.codegen.SourceGenerator;
import de.linusdev.lutils.codegen.java.JavaClass;
import org.jetbrains.annotations.NotNull;

/**
 * Ghost aliases are types, which alias another type, but an actual alias class cannot be
 * created. They will just be replaced with their alias in the actual code.
 * <br><br>
 * This is required for enum aliases, as enums cannot be extended.
 */
public class GhostAliasType implements Type {

    private final @NotNull String name;
    private final @NotNull PossiblyUnresolvedType alias;

    public GhostAliasType(
            @NotNull String name,
            @NotNull PossiblyUnresolvedType alias
    ) {
        this.name = name;
        this.alias = alias;
    }

    @Override
    public @NotNull String getName() {
        return name;
    }

    @Override
    public @NotNull TypeType getType() {
        return alias.resolve().getType();
    }

    @Override
    public @NotNull CTypes getAsBaseType() {
        return alias.resolve().getAsBaseType();
    }

    @Override
    public void generate(@NotNull RegistryLoader registry, @NotNull SourceGenerator generator) {
        return;
    }

    @Override
    public @NotNull JavaClass getJavaClass(@NotNull RegistryLoader registry, @NotNull SourceGenerator generator) {
        return alias.resolve().getJavaClass(registry, generator);
    }
}
