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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Node;

import java.util.LinkedHashMap;
import java.util.Map;

public class GroupedDefinesType implements Type {

    private final static @Nullable String SUB_PACKAGE = "constants";

    private final @NotNull String name;
    private final @Nullable String comment;
    private final @NotNull Map<String, Define> defines = new LinkedHashMap<>();

    public GroupedDefinesType(@NotNull String name, @Nullable String comment) {
        this.name = name;
        this.comment = comment;
    }

    public void addDefine(@NotNull Node enumNode) {
        Define define = new Define(enumNode);
        defines.put(define.name, define);
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

        clazz.setType(JavaClassType.CLASS);
        clazz.setVisibility(JavaVisibility.PUBLIC);
        clazz.setName(name);
        if(comment != null)
            clazz.setJavaDoc(comment);

        for (Define define : defines.values()) {
            var var = clazz.addVariable(JavaClass.ofClass(define.getType(defines).getJavaClass()), define.getName(defines));
            var.setVisibility(JavaVisibility.PUBLIC);
            var.setStatic(true);
            var.setFinal(true);
            var.setDefaultValue(JavaExpression.ofCode(define.getStringValue(defines)));
            if(define.comment != null) var.setJavaDoc(define.comment);
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

    public static class Define {

        private final @NotNull String name;

        private final @Nullable String alias;
        private final @Nullable CTypes type;

        private final @NotNull String stringValue;

        private final @Nullable String comment;

        public Define(@NotNull Node enumNode) {

            if(enumNode.getAttributes() == null) {
                throw new IllegalStateException("<enum> node without any attributes: " + enumNode.getTextContent());
            }

            Node nameAttr = enumNode.getAttributes().getNamedItem("name");
            Node typeAttr = enumNode.getAttributes().getNamedItem("type");
            Node valueAttr = enumNode.getAttributes().getNamedItem("value");
            Node aliasAttr = enumNode.getAttributes().getNamedItem("alias");
            Node commentAttr = enumNode.getAttributes().getNamedItem("comment");

            if(nameAttr == null || (valueAttr == null && aliasAttr == null))
                throw new IllegalStateException("<enum> node without name or value/alias: " + enumNode.getTextContent());

            comment = commentAttr == null ? null : commentAttr.getNodeValue();

            name = nameAttr.getNodeValue();
            if(aliasAttr != null) {
                alias = aliasAttr.getNodeValue();
                type = null;
                stringValue = alias;
            } else if(typeAttr == null) {
                if(valueAttr.getNodeValue().startsWith("\"")) type = CTypes.STRING;
                else type = CTypes.INT32;
                stringValue = valueAttr.getNodeValue();
                alias = null;
            } else {
                type = CTypes.ofCType(typeAttr.getNodeValue());

                String value = valueAttr.getNodeValue();
                value = value.replace("U", "").replace("LL", "L");
                stringValue = value;
                alias = null;
            }
        }

        public @NotNull String getName(@NotNull Map<String, Define> defineMap) {
            return name;
        }

        public @NotNull CTypes getType(@NotNull Map<String, Define> defineMap) {
            if(type == null) {
                if(alias == null)
                    throw new IllegalStateException("alias and type are null.");

                return defineMap.get(alias).getType(defineMap);
            }
            return type;
        }

        public @NotNull String getStringValue(@NotNull Map<String, Define> defineMap) {
            return stringValue;
        }
    }
}
