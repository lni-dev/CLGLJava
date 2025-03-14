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
import de.linusdev.ljgel.build.vkregistry.types.abstracts.Type;
import de.linusdev.ljgel.build.vkregistry.types.abstracts.TypeType;
import de.linusdev.lutils.codegen.SourceGenerator;
import de.linusdev.lutils.codegen.java.*;
import org.jetbrains.annotations.NotNull;

public class AliasOfBasicType implements Type{

    private final @NotNull String name;
    private final @NotNull Type alias;
    private final @NotNull String packageString;

    public AliasOfBasicType(@NotNull String name, @NotNull Type alias, @NotNull String packageString) {
        this.name = name;
        this.alias = alias;
        this.packageString = packageString;
    }


    @Override
    public @NotNull String getName() {
        return name;
    }

    @Override
    public @NotNull TypeType getType() {
        return TypeType.ALIAS_OF_BASIC;
    }

    @Override
    public @NotNull CTypes getAsBaseType() {
        return alias.getAsBaseType();
    }

    @Override
    public void generate(
            @NotNull RegistryLoader registry,
            @NotNull SourceGenerator generator
    ) {
        JavaClassGenerator clazz = generator.addJavaFile(packageString);

        clazz.setName(name);
        clazz.setType(JavaClassType.CLASS);
        clazz.setVisibility(JavaVisibility.PUBLIC);
        clazz.setExtendedClass(alias.getJavaClass(registry, generator));
    }

    @Override
    public @NotNull JavaClass getJavaClass(@NotNull RegistryLoader registry, @NotNull SourceGenerator generator) {

        return new JavaClass() {
            @Override
            public @NotNull JavaPackage getPackage() {
                return generator.getJavaBasePackage().extend(packageString);
            }

            @Override
            public @NotNull String getName() {
                return name;
            }
        };
    }
}
