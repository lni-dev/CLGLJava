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
import de.linusdev.lutils.nat.enums.NativeEnumMember32;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static de.linusdev.cvg4j.build.vkregistry.RegistryLoader.VULKAN_PACKAGE;

public class BitMaskEnumType implements Type {
    private final static @NotNull String SUB_PACKAGE = VULKAN_PACKAGE + ".bitmasks.enums";

    private final @NotNull String name;
    private final int bitWidth;
    private final @Nullable String comment;
    private final @NotNull String namePrefixToIgnore;
    private final @NotNull String namePrefixFix;

    private final @NotNull LinkedHashMap<String, Value> values = new LinkedHashMap<>();

    public BitMaskEnumType(
            @NotNull String name,
            int bitWidth,
            @Nullable String comment
    ) {
        this.name = name;
        this.bitWidth = bitWidth;
        this.comment = comment;

        Pattern wordExtractor = Pattern.compile("^(?<word>[A-Z]+[a-z0-9]*)([A-Z]|$)");

        List<String> nameWords = new ArrayList<>();

        while (!name.isBlank()) {
            Matcher matcher = wordExtractor.matcher(name);
            if(!matcher.find()) break;
            String word = matcher.group("word");

            if(!word.equals("KHR") && !word.equals("Bits") && !word.equals("Flag"))
                nameWords.add(word);
            name = name.substring(word.length());
        }

        namePrefixToIgnore = nameWords.stream().reduce((cur, add) -> cur.toUpperCase(Locale.ROOT) + "_" + add.toUpperCase(Locale.ROOT)).orElse("") + "_";
        namePrefixFix = nameWords.get(nameWords.size()-1).toUpperCase(Locale.ROOT) + "_";
        LOG.debug("bitMaskEnumName: " + this.name + ", namePrefixToIgnore: " + namePrefixToIgnore + ", namePrefixFix: " + namePrefixFix);
    }

    public String getEnumValueName(String vkName) {
        if(vkName.startsWith(namePrefixToIgnore))
            vkName = vkName.substring(namePrefixToIgnore.length());

        if(Pattern.compile("^\\d").matcher(vkName).find()) {
            vkName = namePrefixFix + vkName;
        }

        if(vkName.contains("_BIT")) vkName = vkName.replaceFirst("_BIT(_|$)", "$1");

        return vkName;
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

        addValue(new Value(
                getEnumValueName(nameAttr.getNodeValue()),
                bitPostAttr == null ? -1 : Integer.parseInt(bitPostAttr.getNodeValue()),
                valueAttr == null ? (aliasAttr == null ? null : getEnumValueName(aliasAttr.getNodeValue()) + ".getValue()") : valueAttr.getNodeValue(),
                commentAttr == null ? null : commentAttr.getNodeValue(),
                deprecatedAttr == null ? null : deprecatedAttr.getNodeValue(),
                null));
    }

    public void addValue(@NotNull Value value) {
        if(value.stringValue != null && value.stringValue.equals(value.name + ".getValue()")) {
            // Some alias are the same name as the enum value bit without the "_BIT", we remove the _BIT anyway
            // so we can discard these aliases.
            return;
        }
        values.put(value.name, value);
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
    public @NotNull CTypes getAsBaseType() {
        if(bitWidth == 32)
            return CTypes.INT32;
        else if (bitWidth == 64)
            return CTypes.INT64;

        throw new Error();
    }

    @Override
    public void generate(@NotNull RegistryLoader registry, @NotNull SourceGenerator generator) {
        var clazz = generator.addJavaFile(SUB_PACKAGE);
        clazz.setName(name);
        clazz.setType(JavaClassType.ENUM);
        clazz.setVisibility(JavaVisibility.PUBLIC);
        if(comment != null) clazz.setJavaDoc(comment);

        if(bitWidth == 32)
            clazz.setImplementedClasses(new JavaClass[]{
                    JavaClass.ofClass(IntBitFieldValue.class),
                    JavaClass.ofClass(NativeEnumMember32.class)
            });
        else if (bitWidth == 64)
            clazz.setImplementedClasses(new JavaClass[]{
                    JavaClass.ofClass(LongBitFieldValue.class)
            });
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

        // Sort, so that aliases are in last place.
        var sortedVals = values.values().stream().sorted(
                (o1, o2) -> {
                    boolean bo1 = o1.stringValue != null && o1.stringValue.contains(".getValue()");
                    boolean bo2 = o2.stringValue != null && o2.stringValue.contains(".getValue()");
                    if(bo1 && !bo2) return 1;
                    else if(!bo1 && bo2) return -1;
                    return 0;
                }
        ).collect(Collectors.toCollection(ArrayList::new));
        for (Value value : sortedVals) {
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
            var doc = member.setJavaDoc(value.comment == null ? "" : value.comment);
            if(value.writeDoc != null)
                value.writeDoc.accept(doc);
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
        private final @Nullable Consumer<JavaDocGenerator> writeDoc;

        public Value(
                @NotNull String name,
                int bitPos,
                @Nullable String stringValue,
                @Nullable String comment,
                @Nullable String deprecated, @Nullable Consumer<JavaDocGenerator> writeDoc
        ) {
            this.name = name;
            this.bitPos = bitPos;
            this.stringValue = stringValue;
            this.comment = comment;
            this.deprecated = deprecated;
            this.writeDoc = writeDoc;
        }
    }
}
