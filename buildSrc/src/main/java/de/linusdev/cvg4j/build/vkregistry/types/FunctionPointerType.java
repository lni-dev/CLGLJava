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

package de.linusdev.cvg4j.build.vkregistry.types;

import de.linusdev.cvg4j.build.vkregistry.RegistryLoader;
import de.linusdev.cvg4j.build.vkregistry.types.abstracts.Type;
import de.linusdev.cvg4j.build.vkregistry.types.abstracts.TypeType;
import de.linusdev.lutils.codegen.SourceGenerator;
import de.linusdev.lutils.codegen.java.*;
import de.linusdev.lutils.nat.pointer.Pointer64;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Node;

import static de.linusdev.cvg4j.build.vkregistry.RegistryLoader.VULKAN_PACKAGE;
import static de.linusdev.cvg4j.build.vkregistry.RegistryLoader.findInChildren;

public class FunctionPointerType implements Type {

    private final static @NotNull String SUB_PACKAGE = VULKAN_PACKAGE + ".funcpointer";

    private final @NotNull RegistryLoader registry;

    private final @NotNull String name;
    private final @NotNull String cDefinition;

    public FunctionPointerType(
            @NotNull RegistryLoader registry,
            Node functionPointerNode
    ) {
        this.registry = registry;
        Node nameNode = findInChildren(functionPointerNode, "name");

        if(nameNode == null)
            throw new IllegalStateException("Function-pointer without name: " + functionPointerNode.getTextContent());

        this.name = nameNode.getTextContent();
        this.cDefinition = functionPointerNode.getTextContent();

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
    public void generate(@NotNull RegistryLoader registry, @NotNull SourceGenerator generator) {
        System.out.println("GEN FunctionPointerType");

        var clazz = generator.addJavaFile(SUB_PACKAGE);
        clazz.setName(name);
        clazz.setType(JavaClassType.CLASS);
        clazz.setVisibility(JavaVisibility.PUBLIC);
        clazz.setJavaDoc("<pre>{@code " + cDefinition + "}</pre>");
        clazz.setExtendedClass(JavaClass.ofClass(Pointer64.class));
        var constructor = clazz.addConstructor();
        constructor.body(block ->
                block.addExpression(JavaExpression.callSuper(
                        JavaExpression.booleanPrimitive(false),
                        JavaExpression.nullExpression()
                ))
        );
        constructor.setVisibility(JavaVisibility.PUBLIC);

        System.out.println("END GEN FunctionPointerType");
    }

    @Override
    public @NotNull JavaClass getJavaClass(@NotNull RegistryLoader registry, @NotNull SourceGenerator generator) {
        return new JavaClass() {
            @Override
            public @NotNull JavaPackage getPackage() {
                return generator.getJavaBasePackage().extend(SUB_PACKAGE);
            }

            @Override
            public @NotNull String getName() {
                return name;
            }
        };
    }
}
