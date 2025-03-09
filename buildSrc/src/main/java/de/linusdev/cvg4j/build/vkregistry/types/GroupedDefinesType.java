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
import de.linusdev.llog.LLog;
import de.linusdev.llog.base.LogInstance;
import de.linusdev.lutils.codegen.SourceGenerator;
import de.linusdev.lutils.codegen.java.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Node;

import java.util.LinkedHashMap;
import java.util.Map;

import static de.linusdev.cvg4j.build.vkregistry.RegistryLoader.VULKAN_PACKAGE;

public class GroupedDefinesType implements Type {

    public static final LogInstance LOG = LLog.getLogInstance();

    private final static @Nullable String SUB_PACKAGE = VULKAN_PACKAGE + ".constants";

    private final @NotNull String name;
    private final @Nullable String comment;
    private final @Nullable GroupedDefinesType parent;
    private final @NotNull Map<String, Define> defines = new LinkedHashMap<>();

    private final @NotNull Object genLock = new Object();
    private boolean isGenereated = false;
    private JavaClassGenerator generated;

    public GroupedDefinesType(
            @NotNull String name,
            @Nullable String comment,
            @Nullable GroupedDefinesType parent
    ) {
        this.name = name;
        this.comment = comment;
        this.parent = parent;
    }

    public void addDefine(@NotNull Node enumNode) {
        Define define = new Define(enumNode, this);
        defines.put(define.name, define);
    }

    public void addDefine(@NotNull Define define) {
        defines.put(define.name, define);
    }

    public @NotNull Map<String, Define> getDefines() {
        return defines;
    }

    @Override
    public @NotNull String getName() {
        return name;
    }

    @Override
    public @NotNull TypeType getType() {
        return TypeType.DEFINE_CLASS;
    }

    @Override
    public void ensureGenerated(@NotNull RegistryLoader registry, @NotNull SourceGenerator generator) {
        generate(registry, generator);
    }

    public JavaClassGenerator getGenerated(@NotNull RegistryLoader registry, @NotNull SourceGenerator generator) {
        ensureGenerated(registry, generator);
        return generated;
    }

    @Override
    public void generate(@NotNull RegistryLoader registry, @NotNull SourceGenerator generator) {
        synchronized (genLock) {
            if(isGenereated)
                return;
            isGenereated = true;
        }

        LOG.debug("START GEN grouped define types '" + name + "'.");

        JavaClassGenerator clazz =
                parent == null ?
                        generator.addJavaFile(SUB_PACKAGE)
                        :
                        parent.getGenerated(registry, generator).addSubClass(true);

        generated = clazz;

        clazz.setType(JavaClassType.CLASS);
        clazz.setVisibility(JavaVisibility.PUBLIC);
        clazz.setName(name);
        if(comment != null)
            clazz.setJavaDoc(comment, true);

        for (Define define : defines.values()) {
            if(define.skip) continue;
            LOG.debug("GEN define " + define);
            var var = clazz.addVariable(JavaClass.ofClass(define.getType(defines).getJavaClass()), define.getName(defines));
            var.setVisibility(JavaVisibility.PUBLIC);
            var.setStatic(true);
            var.setFinal(true);

            if(define.alias != null) {
                var aliasDefine = registry.getPUDefine(define.alias).resolve();
                if(aliasDefine.parent == this)
                    var.setDefaultValue(JavaExpression.ofCode(define.getStringValue(defines)));
                else
                    var.setDefaultValue(JavaExpression.publicStaticVariable(aliasDefine.var));
            } else {
                // check if define is alias. Some defines are aliases, but the alias is set as value instead of alias.
                Define def = registry.getDefine(define.stringValue);
                if(def != null) {
                    // alias
                    def.parent.ensureGenerated(registry, generator);
                    var.setDefaultValue(JavaExpression.publicStaticVariable(def.var));
                } else {
                    // no alias
                    var.setDefaultValue(JavaExpression.ofCode(define.getStringValue(defines)));
                }
            }

            if(define.comment != null) var.setJavaDoc(define.comment, true);
            define.var = var;
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
        private final boolean skip;

        public final @NotNull GroupedDefinesType parent;
        public JavaVariable var;

        public Define(@NotNull Node enumNode, @NotNull GroupedDefinesType parent) {
            this.parent = parent;

            if(enumNode.getAttributes() == null) {
                throw new IllegalStateException("<enum> node without any attributes: " + enumNode.getTextContent());
            }

            Node nameAttr = enumNode.getAttributes().getNamedItem("name");
            Node typeAttr = enumNode.getAttributes().getNamedItem("type");
            Node valueAttr = enumNode.getAttributes().getNamedItem("value");
            Node aliasAttr = enumNode.getAttributes().getNamedItem("alias");
            Node commentAttr = enumNode.getAttributes().getNamedItem("comment");

            if(nameAttr == null)
                throw new IllegalStateException("<enum> node without name or value/alias: " + enumNode.getTextContent());

            comment = commentAttr == null ? null : commentAttr.getNodeValue();
            name = nameAttr.getNodeValue();

            if(valueAttr == null && aliasAttr == null) {
                LOG.debug("Skipping <enum> without value or alias attr.");
                skip = true;
                alias = null;
                type = null;
                stringValue = "";
                return;
            }


            if(aliasAttr != null) {
                alias = aliasAttr.getNodeValue();
                type = null;
                stringValue = alias;
            } else if(typeAttr == null) {
                if(valueAttr.getNodeValue().startsWith("\"")) type = CTypes.STRING_UTF8;
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

            skip = false;
        }

        public Define(
                @NotNull String name,
                @Nullable String alias,
                @Nullable CTypes type,
                @NotNull String stringValue,
                @Nullable String comment,
                boolean skip,
                @NotNull GroupedDefinesType parent
        ) {
            this.name = name;
            this.alias = alias;
            this.type = type;
            this.stringValue = stringValue;
            this.comment = comment;
            this.skip = skip;
            this.parent = parent;
        }

        public @NotNull String getName(@NotNull Map<String, Define> defineMap) {
            return name;
        }

        public @NotNull CTypes getType(@NotNull Map<String, Define> defineMap) {
            if(type == null) {
                if(alias == null)
                    throw new IllegalStateException("alias and type are null for define with name '" + name + "' in group " + parent.getName());

                return defineMap.get(alias).getType(defineMap);
            }
            return type;
        }

        public @NotNull String getStringValue(@NotNull Map<String, Define> defineMap) {
            return stringValue;
        }

        @Override
        public String toString() {
            return "Define{" +
                    "name='" + name + '\'' +
                    ", alias='" + alias + '\'' +
                    ", type=" + type +
                    ", stringValue='" + stringValue + '\'' +
                    ", comment='" + comment + '\'' +
                    ", skip=" + skip +
                    ", parent=" + parent +
                    ", var=" + var +
                    '}';
        }
    }
}
