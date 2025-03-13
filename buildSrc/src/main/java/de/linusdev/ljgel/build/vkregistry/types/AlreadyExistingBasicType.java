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
import de.linusdev.lutils.codegen.java.JavaClass;
import de.linusdev.lutils.codegen.java.JavaPackage;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;

public class AlreadyExistingBasicType implements Type {

    private final @NotNull String name;
    private final @NotNull CTypes alias;
    private final @NotNull String packageString;

    public AlreadyExistingBasicType(
            @NotNull CTypes alias,
            @Language("jvm-class-name")
            @NotNull String truePackageStringAndName
    ) {
        int split = truePackageStringAndName.lastIndexOf('.');
        this.name = truePackageStringAndName.substring(split+1);
        this.alias = alias;
        this.packageString = truePackageStringAndName.substring(0, split);
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
        return alias;
    }

    @Override
    public void generate(@NotNull RegistryLoader registry, @NotNull SourceGenerator generator) {
        // Do nothing
    }

    @Override
    public @NotNull JavaClass getJavaClass(@NotNull RegistryLoader registry, @NotNull SourceGenerator generator) {
        return new JavaClass() {
            @Override
            public @NotNull JavaPackage getPackage() {
                return new JavaPackage(packageString);
            }

            @Override
            public @NotNull String getName() {
                return name;
            }
        };
    }
}
