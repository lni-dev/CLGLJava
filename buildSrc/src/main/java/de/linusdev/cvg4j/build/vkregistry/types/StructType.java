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
import de.linusdev.cvg4j.build.vkregistry.types.abstracts.PossiblyUnresolvedDefine;
import de.linusdev.cvg4j.build.vkregistry.types.abstracts.PossiblyUnresolvedType;
import de.linusdev.cvg4j.build.vkregistry.types.abstracts.Type;
import de.linusdev.cvg4j.build.vkregistry.types.abstracts.TypeType;
import de.linusdev.lutils.codegen.SourceGenerator;
import de.linusdev.lutils.codegen.java.*;
import de.linusdev.lutils.nat.enums.NativeEnumValue32;
import de.linusdev.lutils.nat.pointer.BBPointer64;
import de.linusdev.lutils.nat.pointer.BBTypedPointer64;
import de.linusdev.lutils.nat.string.NullTerminatedUTF8String;
import de.linusdev.lutils.nat.struct.abstracts.ComplexStructure;
import de.linusdev.lutils.nat.struct.abstracts.ComplexUnion;
import de.linusdev.lutils.nat.struct.annos.StructValue;
import de.linusdev.lutils.nat.struct.array.StructureArray;
import de.linusdev.lutils.nat.struct.utils.SSMUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static de.linusdev.cvg4j.build.vkregistry.RegistryLoader.*;

public class StructType implements Type {

    private final static @NotNull String SUB_PACKAGE = VULKAN_PACKAGE + ".structs";

    private final @NotNull RegistryLoader registry;

    private final @NotNull String name;
    private final @Nullable Boolean returnedOnly;
    private final @Nullable String comment;
    private final @Nullable List<PossiblyUnresolvedType> structExtends;
    private final boolean union;
    private @Nullable String customSubPackage;

    private final @NotNull List<Member> members = new ArrayList<>();

    public StructType(
            @NotNull RegistryLoader registry,
            @NotNull String name,
            @Nullable Boolean returnedOnly,
            @Nullable String comment,
            @Nullable List<PossiblyUnresolvedType> structExtends,
            boolean union
    ) {
        this.registry = registry;
        this.name = name;
        this.returnedOnly = returnedOnly;
        this.comment = comment;
        this.structExtends = structExtends;
        this.union = union;
    }

    public void addMember(@NotNull Member member) {
        members.add(member);
    }

    public void addMember(@NotNull Node memberNode) {
        if(checkApiAttr(memberNode))
            return; // Only add vulkan api stuff

        Node optionalAttr = memberNode.getAttributes().getNamedItem("optional");
        Node noAutoValidityAttr = memberNode.getAttributes().getNamedItem("noautovalidity");
        Node limitTypeAttr = memberNode.getAttributes().getNamedItem("limittype");
        Node commentAttr = memberNode.getAttributes().getNamedItem("comment");
        Node lenAttr = memberNode.getAttributes().getNamedItem("len");

        Node nameNode = findInChildren(memberNode, "name");
        Node typeNode = findInChildren(memberNode, "type");
        Node commentNode = findInChildren(memberNode, "comment");
        boolean isPointer = memberNode.getTextContent().contains("*");
        boolean isPointerPointer = isPointer && memberNode.getTextContent().replaceFirst("\\*", "").contains("*");
        boolean isArray = false;
        PossiblyUnresolvedDefine arrayLength = null;
        Integer arrayLengthNumeric = null;

        if(nameNode == null || typeNode == null)
            throw new IllegalStateException("<name> or <type> missing for struct member: " + memberNode.getTextContent());

        if(commentNode != null)
            memberNode.removeChild(commentNode);

        if(isPointer) {
            System.out.println("Struct member with pointer found: " + memberNode.getTextContent());
        }

        // Check if array
        // example: <type>char</type> <name>layerName</name>[<enum>VK_MAX_EXTENSION_NAME_SIZE</enum>]
        System.out.println("Member: " + memberNode.getTextContent());
        Pattern pattern = Pattern.compile("(?<type>\\w+) +(?<name>\\w+)\\[(?<length>\\w+)\\]");
        Matcher matcher = pattern.matcher(memberNode.getTextContent());
        if(matcher.find()) {
            System.out.println("Struct member with array found: " + memberNode.getTextContent());
            isArray = true;
            String matchedType = matcher.group("type");
            String matchedName = matcher.group("name");
            String matchedLength = matcher.group("length");

            if(!typeNode.getTextContent().equals(matchedType))
                throw new IllegalStateException("Matched type and type node does not match. matched=" + matchedType + ", typeNode=" + typeNode.getTextContent());
            if(!nameNode.getTextContent().equals(matchedName))
                throw new IllegalStateException("Matched name and name node does not match. matched=" + matchedName + ", nameNode=" + nameNode.getTextContent());

            pattern = Pattern.compile("^(\\d+)$");
            matcher = pattern.matcher(matchedLength);

            if(matcher.find()) arrayLengthNumeric = Integer.parseInt(matcher.group());
            else arrayLength = registry.getPUDefine(matchedLength);
        }

        Member member = new Member(
                nameNode.getTextContent(),
                commentAttr == null ? (commentNode == null ? null : commentNode.getTextContent()) : commentAttr.getNodeValue(),
                registry.getPUType(typeNode.getTextContent()),
                isPointer,
                isPointerPointer,
                optionalAttr == null ? null : optionalAttr.getNodeValue(),
                noAutoValidityAttr == null ? null : noAutoValidityAttr.getNodeValue(),
                limitTypeAttr == null ? null : limitTypeAttr.getNodeValue(),
                isArray,
                arrayLength,
                arrayLengthNumeric,
                lenAttr == null ? null : lenAttr.getNodeValue(),
                memberNode.getTextContent()
        );

        members.add(member);

    }

    public void setCustomSubPackage(@Nullable String customSubPackage) {
        this.customSubPackage = customSubPackage;
    }

    protected String getSubPackage() {
        if(customSubPackage != null) return customSubPackage;
        return SUB_PACKAGE;
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
    public void generate(
            @NotNull RegistryLoader registry,
            @NotNull SourceGenerator generator
    ) {

        System.out.println("GEN Struct " + name + ", isUnion: " + union);
        var clazz = generator.addJavaFile(getSubPackage());
        clazz.setName(name);
        clazz.setType(JavaClassType.CLASS);
        clazz.setVisibility(JavaVisibility.PUBLIC);
        if(comment != null)
            clazz.setJavaDoc(comment);

        if(union) clazz.setExtendedClass(JavaClass.ofClass(ComplexUnion.class));
        else clazz.setExtendedClass(JavaClass.ofClass(ComplexStructure.class));

        int index = 2;
        JavaExpression[] variables = new JavaExpression[members.size() + 2];
        for (Member member : members) {
            System.out.println(
                    "Member: " + member.name
                            + " with type=" + member.type.getName()
                            + " is array=" + member.isArray
                            + ", isPointer=" + member.isPointer
                            + ", isPointerPointer=" + member.isPointerPointer
            );

            Type typeOfMember = member.type.resolve();
            JavaClass javaClassOfMember = typeOfMember.getJavaClass(registry, generator);
            JavaVariable jVariable;
            boolean requiresElementTypeAnnotation = true; // only for array types

            if(member.isPointer && !member.isPointerPointer) {
                if (typeOfMember == CTypes.VOID) {
                    jVariable = clazz.addVariable(JavaClass.ofClass(BBPointer64.class), member.name);
                    jVariable.setDefaultValue(JavaExpression.callMethod(SSMUtils.getNewUnallocatedMethod(BBPointer64.class)));
                } else if (typeOfMember == CTypes.CHAR && member.len != null && member.len.contains("null-terminated")) {
                    jVariable = clazz.addVariable(
                            JavaClass.ofClass(BBTypedPointer64.class).withGenerics(JavaClass.ofClass(NullTerminatedUTF8String.class)),
                            member.name
                    );
                    jVariable.setDefaultValue(JavaExpression.callMethod(SSMUtils.getNewUnallocatedMethod(BBTypedPointer64.class)));
                    requiresElementTypeAnnotation = false;
                } else {
                    // We need to differentiate between arrays of enum types and others
                    if (typeOfMember.getType() == TypeType.ENUM) {
                        jVariable = clazz.addVariable(
                                JavaClass.ofClass(BBTypedPointer64.class).withGenerics(
                                        JavaClass.ofClass(NativeEnumValue32.class).withGenerics(javaClassOfMember)
                                ),
                                member.name
                        );
                        jVariable.setDefaultValue(JavaExpression.callMethod(SSMUtils.getNewUnallocatedMethod(BBTypedPointer64.class)));
                    } else {
                        jVariable = clazz.addVariable(JavaClass.ofClass(BBTypedPointer64.class).withGenerics(javaClassOfMember), member.name);
                        jVariable.setDefaultValue(JavaExpression.callMethod(SSMUtils.getNewUnallocatedMethod(BBTypedPointer64.class)));
                    }
                }

            } else if (member.isPointerPointer) {
                JavaClass tp64 = JavaClass.ofClass(BBTypedPointer64.class);

                if (typeOfMember == CTypes.VOID) {
                    jVariable = clazz.addVariable(tp64.withGenerics(JavaClass.ofClass(BBPointer64.class)), member.name);
                    jVariable.setDefaultValue(JavaExpression.callMethod(SSMUtils.getNewUnallocatedMethod(BBTypedPointer64.class)));
                } else if (typeOfMember == CTypes.CHAR && member.len != null && member.len.contains("null-terminated")) {
                    jVariable = clazz.addVariable(
                            tp64.withGenerics(tp64.withGenerics(JavaClass.ofClass(NullTerminatedUTF8String.class))),
                            member.name
                    );
                    jVariable.setDefaultValue(JavaExpression.callMethod(SSMUtils.getNewUnallocatedMethod(BBTypedPointer64.class)));
                } else {
                    // We need to differentiate between arrays of enum types and others
                    if (typeOfMember.getType() == TypeType.ENUM) {
                        jVariable = clazz.addVariable(
                                tp64.withGenerics(
                                        tp64.withGenerics(
                                                JavaClass.ofClass(NativeEnumValue32.class).withGenerics(javaClassOfMember)
                                        )
                                ),
                                member.name
                        );
                        jVariable.setDefaultValue(JavaExpression.callMethod(SSMUtils.getNewUnallocatedMethod(BBTypedPointer64.class)));
                    } else {
                        jVariable = clazz.addVariable(tp64.withGenerics(tp64.withGenerics(javaClassOfMember)), member.name);
                        jVariable.setDefaultValue(JavaExpression.callMethod(SSMUtils.getNewUnallocatedMethod(BBTypedPointer64.class)));
                    }
                }

            } else if(member.isArray) {

                if(typeOfMember == CTypes.CHAR && member.len != null && member.len.equals("null-terminated")) {
                    // Null Terminated Utf8 String
                    jVariable = clazz.addVariable(JavaClass.ofClass(NullTerminatedUTF8String.class), member.name);
                    jVariable.setDefaultValue(JavaExpression.callMethod(SSMUtils.getNewUnallocatedMethod(NullTerminatedUTF8String.class)));
                    requiresElementTypeAnnotation = false;

                } else if(typeOfMember instanceof CTypes ctype && ctype.getNativeArrayClass() != null) {
                    // NativeInt16Array, ...
                    jVariable = clazz.addVariable(JavaClass.ofClass(ctype.getNativeArrayClass()), member.name);
                    jVariable.setDefaultValue(JavaExpression.callMethod(SSMUtils.getNewUnallocatedMethod(ctype.getNativeArrayClass())));
                    requiresElementTypeAnnotation = false;

                } else {
                    // StructureArray
                    // We need to differentiate between arrays of enum types and others
                    if (typeOfMember.getType() == TypeType.ENUM) {
                        jVariable = clazz.addVariable(
                                JavaClass.ofClass(StructureArray.class).withGenerics(
                                        JavaClass.ofClass(NativeEnumValue32.class).withGenerics(javaClassOfMember)
                                ), member.name);

                        jVariable.setDefaultValue(
                                JavaExpression.callMethod(SSMUtils.getNewUnallocatedMethod(StructureArray.class),
                                        JavaExpression.booleanPrimitive(false), // Parameter 1
                                        JavaExpression.ofCode( // Parameter 2
                                                "() -> " + JavaExpression.callMethod(SSMUtils.getNewUnallocatedMethod(NativeEnumValue32.class)).getExprString(generator.getSg())
                                        )
                                ));

                    } else {
                        jVariable = clazz.addVariable(JavaClass.ofClass(StructureArray.class).withGenerics(javaClassOfMember), member.name);

                        // We need to provide a function to create unallocated instances of the struct-arrays member type
                        Class<?> actualClazz = javaClassOfMember.tryResolveActualClass();
                        var newUnallocatedMethod = actualClazz == null ? null : SSMUtils.getNewUnallocatedMethod(actualClazz);
                        var createUnallocatedExpression =
                                newUnallocatedMethod == null
                                        ?
                                        JavaExpression.callConstructorOf(javaClassOfMember)
                                        :
                                        JavaExpression.callMethod(newUnallocatedMethod);

                        jVariable.setDefaultValue(
                                JavaExpression.callMethod(SSMUtils.getNewUnallocatedMethod(StructureArray.class),
                                        JavaExpression.booleanPrimitive(false), // Parameter 1
                                        JavaExpression.ofCode( // Parameter 2
                                                "() -> " + createUnallocatedExpression.getExprString(generator.getSg())
                                        )
                                ));
                    }
                }

            } else if (typeOfMember.getType() == TypeType.ENUM) {
                jVariable = clazz.addVariable(JavaClass.ofClass(NativeEnumValue32.class).withGenerics(javaClassOfMember), member.name);
                jVariable.setDefaultValue(JavaExpression.callMethod(SSMUtils.getNewUnallocatedMethod(NativeEnumValue32.class)));

            } else {
                jVariable = clazz.addVariable(javaClassOfMember, member.name);

                // Check if this type already exists or if it is a type which will be generated.
                // If it already exists, we must call its NewUnallocatedMethod. If it will be generated, we just call
                // its constructor.
                Class<?> actualClazz = javaClassOfMember.tryResolveActualClass();
                var newUnallocatedMethod = actualClazz == null ? null : SSMUtils.getNewUnallocatedMethod(actualClazz);

                if(newUnallocatedMethod == null)
                    jVariable.setDefaultValue(JavaExpression.callConstructorOf(javaClassOfMember));
                else
                    jVariable.setDefaultValue(JavaExpression.callMethod(newUnallocatedMethod));
            }

            jVariable.setVisibility(JavaVisibility.PUBLIC);
            jVariable.setFinal(true);
            jVariable.addAnnotation(JavaClass.ofClass(NotNull.class));

            // StructValue annotation
            var anno = jVariable.addAnnotation(JavaClass.ofClass(StructValue.class));
            anno.setValue(JavaVariable.of(StructValue.class, "value"), JavaExpression.numberPrimitive(index-2));
            if(member.isArray) {
                if(member.arrayLength == null && member.arrayLengthNumber == null)
                    throw new IllegalStateException("Member without specified arrayLength!");

                // StructValue.length
                if(member.arrayLength != null) {
                    GroupedDefinesType.Define define = member.arrayLength.resolve();
                    define.parent.ensureGenerated(registry, generator);
                    anno.setValue(JavaVariable.of(StructValue.class, "length"), JavaExpression.publicStaticVariable(define.var));
                } else {
                    anno.setValue(
                            JavaVariable.of(StructValue.class, "length"),
                            JavaExpression.numberPrimitive(member.arrayLengthNumber)
                    );
                }

                // StructValue.elementType
                if(requiresElementTypeAnnotation) {
                    if(typeOfMember.getType() == TypeType.ENUM) {
                        anno.setValue(
                                JavaVariable.of(StructValue.class, "elementType"),
                                JavaExpression.classInstanceOfClass(JavaClass.ofClass(NativeEnumValue32.class)));
                    } else {
                        anno.setValue(
                                JavaVariable.of(StructValue.class, "elementType"),
                                JavaExpression.classInstanceOfClass(javaClassOfMember));
                    }
                }


            }

            // JavaDoc
            var doc = jVariable.setJavaDoc();
            if(member.comment != null)
                doc.addText(member.comment);
            if(member.optional != null)
                doc.addAtText(jdTag("vk.optional"), member.optional);
            if(member.noAutoValidity != null)
                doc.addAtText(jdTag("vk.noAutoValidity"), member.noAutoValidity);
            if(member.limitType != null)
                doc.addAtText(jdTag("vk.limitType"), member.limitType);
            if(member.len != null)
                doc.addAtText(jdTag("vk.len"), member.len);
            doc.addAtText(jdTag("cDef"), member.cDefinition);

            variables[index] = jVariable;
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

        System.out.println("END GEN Struct " + name);
    }

    @Override
    public @NotNull JavaClass getJavaClass(@NotNull RegistryLoader registry, @NotNull SourceGenerator generator) {
        return new JavaClass() {
            @Override
            public @NotNull JavaPackage getPackage() {
                return generator.getJavaBasePackage().extend(getSubPackage());
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
        private final boolean isPointerPointer;
        private final @Nullable String optional;
        private final @Nullable String noAutoValidity;
        private final @Nullable String limitType;
        private final boolean isArray;
        private final @Nullable PossiblyUnresolvedDefine arrayLength;
        private final @Nullable Integer arrayLengthNumber;
        private final @Nullable String len;
        private final @NotNull String cDefinition;

        public Member(
                @NotNull String name,
                @Nullable String comment,
                @NotNull PossiblyUnresolvedType type,
                boolean isPointer, boolean isPointerPointer,
                @Nullable String optional,
                @Nullable String noAutoValidity,
                @Nullable String limitType,
                boolean isArray,
                @Nullable PossiblyUnresolvedDefine arrayLength,
                @Nullable Integer arrayLengthNumber,
                @Nullable String len,
                @NotNull String cDefinition
        ) {
            this.name = name;
            this.comment = comment;
            this.type = type;
            this.isPointer = isPointer;
            this.isPointerPointer = isPointerPointer;
            this.optional = optional;
            this.noAutoValidity = noAutoValidity;
            this.limitType = limitType;
            this.isArray = isArray;
            this.arrayLength = arrayLength;
            this.arrayLengthNumber = arrayLengthNumber;
            this.len = len;
            this.cDefinition = cDefinition;
        }
    }
}
