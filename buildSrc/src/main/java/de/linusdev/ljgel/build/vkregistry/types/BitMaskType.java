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
import de.linusdev.llog.LLog;
import de.linusdev.llog.base.LogInstance;
import de.linusdev.lutils.codegen.SourceGenerator;
import de.linusdev.lutils.codegen.java.*;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.linusdev.ljgel.build.vkregistry.RegistryLoader.VULKAN_PACKAGE;

public class BitMaskType implements Type {

    public static final LogInstance LOG = LLog.getLogInstance();

    public static final @NotNull String SUB_PACKAGE = VULKAN_PACKAGE + ".bitmasks";

    private final @NotNull String name;
    private final @NotNull Type alias;
    private final @NotNull PossiblyUnresolvedType bitFieldEnum;

    public BitMaskType(
            @NotNull RegistryLoader loader,
            @NotNull String name,
            @NotNull Type alias
    ) {
        this.name = name;
        this.alias = alias;

        Pattern pattern = Pattern.compile("^.*Flags(?<version>.+$)");
        Matcher matcher = pattern.matcher(name);

        String enumName = name.substring(0, name.length()-1);
        if(matcher.find()) {
            String versionString = matcher.group("version");
            this.bitFieldEnum = loader.getPUType(enumName.substring(0, enumName.length() - versionString.length()) + "Bits" + versionString);
        } else {
            this.bitFieldEnum = loader.getPUType(enumName + "Bits");
        }

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
    public void generate(@NotNull RegistryLoader registry, @NotNull SourceGenerator generator) {
        if(alias.getAsBaseType().getJavaStruct() == null)
            throw new IllegalStateException("Trying to generate basic type " + name + " with illegal alias " + alias.getAsBaseType().name());

        var clazz = generator.addJavaFile(SUB_PACKAGE);

        clazz.setType(JavaClassType.CLASS);
        clazz.setName(name);
        clazz.setExtendedClass(alias.getJavaClass(registry, generator));

        JavaClass bitFieldEnumClazz = bitFieldEnum.resolveOrElse(
                () -> {
                    LOG.debug("Generating missing BitMaskEnumType '" + bitFieldEnum.getName() + "'.");
                    return new BitMaskEnumType(bitFieldEnum.getName(), getAsBaseType().getBitWidth(), null);
                }, true
        ).getJavaClass(registry, generator);

        if(getAsBaseType().getBitFieldInterface() == null)
            throw new IllegalStateException("Cannot create bit mask type of c type: " + getAsBaseType());

        clazz.setImplementedClasses(new JavaClass[]{JavaClass.ofClass(getAsBaseType().getBitFieldInterface()).withGenerics(bitFieldEnumClazz)});

        clazz.setVisibility(JavaVisibility.PUBLIC);
        var constructor = clazz.addConstructor();
        constructor.setVisibility(JavaVisibility.PUBLIC);
        constructor.body(body -> {
            body.addExpression(
                    !(alias instanceof CTypes) || alias == CTypes.POINTER ?
                            JavaExpression.callSuper() :
                            JavaExpression.callSuper(JavaExpression.booleanPrimitive(false))
            );
        });

        var getValueMethod = clazz.addMethod(JavaClass.ofClass(getAsBaseType().getJavaClass()), "getValue");
        getValueMethod.setVisibility(JavaVisibility.PUBLIC);
        getValueMethod.addAnnotation(JavaClass.ofClass(Override.class));
        getValueMethod.body(body -> {
            body.addExpression(JavaExpression.returnExpr(JavaExpression.ofCode("get()")));
        });

        var replaceWithMethod = clazz.addMethod(JavaClass.ofClass(void.class), "replaceWith");
        replaceWithMethod.setVisibility(JavaVisibility.PUBLIC);
        replaceWithMethod.addAnnotation(JavaClass.ofClass(Override.class));
        replaceWithMethod.addParameter("value", JavaClass.ofClass(getAsBaseType().getJavaClass()));
        replaceWithMethod.body(body -> {
            body.addExpression(JavaExpression.ofCode("set(value)"));
        });
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
