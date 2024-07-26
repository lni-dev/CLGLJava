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
import de.linusdev.cvg4j.build.vkregistry.types.abstracts.PossiblyUnresolvedType;
import de.linusdev.cvg4j.build.vkregistry.types.abstracts.Type;
import de.linusdev.cvg4j.build.vkregistry.types.abstracts.TypeType;
import de.linusdev.lutils.codegen.SourceGenerator;
import de.linusdev.lutils.codegen.java.*;
import de.linusdev.lutils.nat.enums.NativeEnumValue32;
import de.linusdev.lutils.nat.pointer.Pointer64;
import de.linusdev.lutils.nat.struct.abstracts.ComplexStructure;
import de.linusdev.lutils.nat.struct.annos.StructValue;
import de.linusdev.lutils.nat.struct.utils.SSMUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.List;

import static de.linusdev.cvg4j.build.vkregistry.RegistryLoader.findInChildren;

public class StructType implements Type {

    private final static @NotNull String SUB_PACKAGE = "structs";

    private final @NotNull RegistryLoader registry;

    private final @NotNull String name;
    private final @Nullable Boolean returnedOnly;
    private final @Nullable String comment;
    private final @Nullable List<PossiblyUnresolvedType> structExtends;

    private final @NotNull List<Member> members = new ArrayList<>();

    public StructType(
            @NotNull RegistryLoader registry,
            @NotNull String name,
            @Nullable Boolean returnedOnly,
            @Nullable String comment,
            @Nullable List<PossiblyUnresolvedType> structExtends
    ) {
        this.registry = registry;
        this.name = name;
        this.returnedOnly = returnedOnly;
        this.comment = comment;
        this.structExtends = structExtends;
    }

    public void addMember(@NotNull Node memberNode) {
        Node optionalAttr = memberNode.getAttributes().getNamedItem("optional");
        Node noAutoValidityAttr = memberNode.getAttributes().getNamedItem("noautovalidity");
        Node limitTypeAttr = memberNode.getAttributes().getNamedItem("limittype");
        Node commentAttr = memberNode.getAttributes().getNamedItem("comment");

        Node nameNode = findInChildren(memberNode, "name");
        Node typeNode = findInChildren(memberNode, "type");
        boolean isPointer = memberNode.getTextContent().contains("*");

        if(nameNode == null || typeNode == null)
            throw new IllegalStateException("<name> or <type> missing for struct member: " + memberNode.getTextContent());

        if(isPointer) {
            //TODO: remove
            System.out.println("Struct member with pointer found: " + memberNode.getTextContent());
        }

        Member member = new Member(
                nameNode.getTextContent(),
                commentAttr == null ? null : commentAttr.getNodeValue(),
                registry.getPUType(typeNode.getTextContent()),
                isPointer,
                optionalAttr == null ? null : optionalAttr.getNodeValue(),
                noAutoValidityAttr == null ? null : noAutoValidityAttr.getNodeValue(),
                limitTypeAttr == null ? null : limitTypeAttr.getNodeValue()
        );

        members.add(member);

    }

    @Override
    public @NotNull String getName() {
        return name;
    }

    @Override
    public @NotNull TypeType getType() {
        return TypeType.STRUCT;
    }

    @Override
    public void generate(@NotNull RegistryLoader registry, @NotNull SourceGenerator generator) {
        var clazz = generator.addJavaFile(SUB_PACKAGE);
        clazz.setName(name);
        clazz.setType(JavaClassType.CLASS);
        clazz.setVisibility(JavaVisibility.PUBLIC);
        if(comment != null)
            clazz.setJavaDoc(comment);
        clazz.setExtendedClass(JavaClass.ofClass(ComplexStructure.class));

        int index = 2;
        JavaExpression[] variables = new JavaExpression[members.size() + 2];
        for (Member member : members) {
            var typeOfMember = member.type.resolve();
            JavaClass clazzOfMember = typeOfMember.getJavaClass(registry, generator);

            if(typeOfMember.getType() == TypeType.ENUM)
                clazzOfMember = JavaClass.ofClass(NativeEnumValue32.class).withGenerics(clazzOfMember);

            if(member.isPointer)
                clazzOfMember = JavaClass.ofClass(Pointer64.class);

            var acClass = clazzOfMember.tryResolveActualClass();
            var newUnallocatedMethod = acClass == null ? null : SSMUtils.getNewUnallocatedMethod(acClass);

            var var = clazz.addVariable(clazzOfMember, member.name);
            var.setVisibility(JavaVisibility.PUBLIC);
            var.setFinal(true);
            var.setDefaultValue(
                    newUnallocatedMethod == null ?
                            JavaExpression.callConstructorOf(clazzOfMember) :
                            JavaExpression.callMethod(newUnallocatedMethod)
            );
            var.addAnnotation(JavaClass.ofClass(StructValue.class)).setValue(JavaVariable.of(StructValue.class, "value"), JavaExpression.numberPrimitive(index-1));
            var.addAnnotation(JavaClass.ofClass(NotNull.class));


            if(member.comment != null
                    || member.optional != null
                    || member.noAutoValidity != null
                    || member.limitType != null
            ) {
                var doc = var.setJavaDoc();
                if(member.comment != null)
                    doc.addText(member.comment);
                if(member.optional != null)
                    doc.addAtText("optional", member.optional);
                if(member.noAutoValidity != null)
                    doc.addAtText("noAutoValidity", member.noAutoValidity);
                if(member.limitType != null)
                    doc.addAtText("limitType", member.limitType);
            }

            variables[index] = var;

            index++;
        }

        var constructor = clazz.addConstructor();
        constructor.setVisibility(JavaVisibility.PUBLIC);
        constructor.body(body -> {
            variables[0] = JavaExpression.nullExpression();
            variables[1] = JavaExpression.booleanPrimitive(false);
            body.addExpression(JavaExpression.callSuper(JavaExpression.booleanPrimitive(false)));
            body.addExpression(
                    JavaExpression.callMethod(
                            JavaMethod.of(ComplexStructure.class, void.class, "init", false),
                            variables
                    )
            );
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

    public static class Member {
        private final @NotNull String name;
        private final @Nullable String comment;
        private final @NotNull PossiblyUnresolvedType type;
        private final boolean isPointer;
        private final @Nullable String optional;
        private final @Nullable String noAutoValidity;
        private final @Nullable String limitType;

        public Member(
                @NotNull String name,
                @Nullable String comment,
                @NotNull PossiblyUnresolvedType type,
                boolean isPointer,
                @Nullable String optional,
                @Nullable String noAutoValidity, @Nullable String limitType
        ) {
            this.name = name;
            this.comment = comment;
            this.type = type;
            this.isPointer = isPointer;
            this.optional = optional;
            this.noAutoValidity = noAutoValidity;
            this.limitType = limitType;
        }
    }
}
