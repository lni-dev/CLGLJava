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
import de.linusdev.lutils.bitfield.IntBitFieldValue;
import de.linusdev.lutils.bitfield.LongBitFieldValue;
import de.linusdev.lutils.codegen.SourceGenerator;
import de.linusdev.lutils.codegen.java.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Node;

import java.util.LinkedHashMap;
import java.util.Map;

public class BitMaskEnumType implements Type {
    private final static @NotNull String SUB_PACKAGE = "bitmasks.enums";

    private final @NotNull String name;
    private final int bitWidth;
    private final @Nullable String comment;

    private final @NotNull Map<String, Value> values = new LinkedHashMap<>();

    public BitMaskEnumType(
            @NotNull String name,
            int bitWidth,
            @Nullable String comment
    ) {
        this.name = name;
        this.bitWidth = bitWidth;
        this.comment = comment;
    }

    public void addValue(@NotNull Node enumNode) {
        if(enumNode.getAttributes() == null) {
            throw new IllegalStateException("<enum> node without any attributes: " + enumNode.getTextContent());
        }

        Node nameAttr = enumNode.getAttributes().getNamedItem("name");
        Node bitPostAttr = enumNode.getAttributes().getNamedItem("bitpos");
        Node valueAttr = enumNode.getAttributes().getNamedItem("value");
        Node aliasAttr = enumNode.getAttributes().getNamedItem("alias");
        Node commentAttr = enumNode.getAttributes().getNamedItem("comment");
        Node deprecatedAttr = enumNode.getAttributes().getNamedItem("deprecated");

        if(nameAttr == null || (valueAttr == null && aliasAttr == null && bitPostAttr == null))
            throw new IllegalStateException("<enum> node without name or value/alias/bitPos: " + enumNode.getTextContent());

        Value v = new Value(
                nameAttr.getNodeValue(),
                bitPostAttr == null ? -1 : Integer.parseInt(bitPostAttr.getNodeValue()),
                valueAttr == null ? (aliasAttr == null ? null : aliasAttr.getNodeValue() + ".getValue()") : valueAttr.getNodeValue(),
                commentAttr == null ? null : commentAttr.getNodeValue(),
                deprecatedAttr == null ? null : deprecatedAttr.getNodeValue()
        );

        values.put(v.name, v);

    }

    @Override
    public @NotNull String getName() {
        return name;
    }

    @Override
    public @NotNull TypeType getType() {
        return TypeType.ENUM;
    }

    @Override
    public void generate(@NotNull RegistryLoader registry, @NotNull SourceGenerator generator) {
        var clazz = generator.addJavaFile(SUB_PACKAGE);
        clazz.setName(name);
        clazz.setType(JavaClassType.ENUM);
        clazz.setVisibility(JavaVisibility.PUBLIC);
        if(comment != null) clazz.setJavaDoc(comment);

        if(bitWidth == 32)
            clazz.setImplementedClasses(new JavaClass[]{JavaClass.ofClass(IntBitFieldValue.class)});
        else if (bitWidth == 64)
            clazz.setImplementedClasses(new JavaClass[]{JavaClass.ofClass(LongBitFieldValue.class)});
        else
            throw new UnsupportedOperationException("Unsupported bit width: " + bitWidth);

        var valueVar = clazz.addVariable(JavaClass.ofClass(bitWidth == 32 ? int.class : long.class), "value");
        valueVar.setFinal(true);
        valueVar.setVisibility(JavaVisibility.PRIVATE);
        var method = clazz.addGetter(valueVar);
        method.addAnnotation(JavaClass.ofClass(Override.class));
        method.setVisibility(JavaVisibility.PUBLIC);
        var constructor = clazz.addConstructor();
        var valueParameter = constructor.addParameter("valueParam", valueVar.getType());
        constructor.body(body -> body.addExpression(JavaExpression.assign(valueVar, valueParameter)));

        for (Value value : values.values()) {
            JavaEnumMemberGenerator member;
            if (value.stringValue == null) {
                member = clazz.addEnumMember(value.name,
                        JavaExpression.numberPrimitive(
                                bitWidth == 32 ?
                                        (Number) IntBitFieldValue.bitPosToValue(value.bitPos) :
                                        (Number) LongBitFieldValue.bitPosToValue(value.bitPos)
                        )
                );
            } else {
                member = clazz.addEnumMember(value.name,
                        JavaExpression.ofCode(value.stringValue)
                );
            }

            if(value.deprecated != null)
                member.addAnnotation(JavaClass.ofClass(Deprecated.class))
                        .setValue(
                                JavaVariable.of(Deprecated.class, "since"),
                                JavaExpression.ofString(value.deprecated)
                        );
            if(value.comment != null)
                member.setJavaDoc(value.comment);
        }
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

    public static class Value {
        private final @NotNull String name;
        private final int bitPos;
        private final @Nullable String stringValue;
        private final @Nullable String comment;
        private final @Nullable String deprecated;

        public Value(
                @NotNull String name,
                int bitPos,
                @Nullable String stringValue,
                @Nullable String comment,
                @Nullable String deprecated
        ) {
            this.name = name;
            this.bitPos = bitPos;
            this.stringValue = stringValue;
            this.comment = comment;
            this.deprecated = deprecated;
        }
    }
}
