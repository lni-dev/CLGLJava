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

package de.linusdev.cvg4j.build.vkregistry;

import de.linusdev.cvg4j.build.vkregistry.types.CTypes;
import de.linusdev.cvg4j.build.vkregistry.types.abstracts.PossiblyUnresolvedType;
import de.linusdev.cvg4j.build.vkregistry.types.abstracts.Type;
import de.linusdev.cvg4j.build.vkregistry.types.abstracts.TypeType;
import de.linusdev.lutils.codegen.SourceGenerator;
import de.linusdev.lutils.codegen.java.*;
import de.linusdev.lutils.nat.enums.NativeEnumValue32;
import de.linusdev.lutils.nat.pointer.BBPointer64;
import de.linusdev.lutils.nat.pointer.BBTypedPointer64;
import de.linusdev.lutils.nat.pointer.Pointer64;
import de.linusdev.lutils.nat.pointer.TypedPointer64;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static de.linusdev.cvg4j.build.vkregistry.ClassesOfMainProject.VULKAN_UTILS_CLASS;
import static de.linusdev.cvg4j.build.vkregistry.RegistryLoader.*;

public class CommandsGenerator {

    private final @NotNull RegistryLoader registry;

    private final @NotNull LinkedHashMap<String, Command> commands = new LinkedHashMap<>();

    public CommandsGenerator(@NotNull RegistryLoader registry) {
        this.registry = registry;
    }

    public void addCommand(@NotNull Node cmdNode) {

        // Check if command is alias
        Node aliasAttr = cmdNode.getAttributes().getNamedItem("alias");
        Node nameAttr = cmdNode.getAttributes().getNamedItem("name");
        if(aliasAttr != null) {
            System.out.println("Skipping command alias with name: " + nameAttr.getNodeValue());
            return;
        }

        // Read command Node
        Node protoNode = findInChildren(cmdNode, "proto");

        if(protoNode == null)
            throw new IllegalStateException("Command missing <proto> node: " + cmdNode.getTextContent());

        Node nameNode = findInChildren(protoNode, "name");
        Node typeNode = findInChildren(protoNode, "type");

        if(nameNode == null || typeNode == null)
            throw new IllegalStateException("Command missing name or type!");

        String name = nameNode.getTextContent();
        String type = typeNode.getTextContent();

        if(protoNode.getTextContent().contains("*"))
            throw new RuntimeException("Pointer in proto node of: " + nameNode.getTextContent());

        // C-Definition

        StringBuilder cDef = new StringBuilder(protoNode.getTextContent() + "(\n");


        System.out.println("Adding command with name: " + name);

        Command cmd = new Command(
                registry, name,
                registry.getPUType(type)
        );

        // Add Params
        for (Node node : iterableNode(cmdNode)) {
            if (node.getNodeName().equals("proto"))
                continue;
            else if (node.getNodeType() == Node.TEXT_NODE)
                continue;
            else if (node.getNodeName().equals("param")) {
                if(RegistryLoader.checkApiAttr(node))
                    continue;
                cmd.addParam(node);
                cDef.append("\t").append(node.getTextContent()).append(", \n");
            } else
                System.out.println("Unhandled Node in '<command>': " + node.getNodeName());
        }

        cDef.setLength(cDef.length()-3);//remove last ", \n"
        cDef.append("\n);");
        cmd.cDef = cDef.toString();

        // Add command
        commands.put(name, cmd);
    }


    public void generate(
            @NotNull RegistryLoader registry,
            @NotNull SourceGenerator generator,
            @NotNull JavaClassGenerator vkInstanceClassGenerator
    ) {

        // Some stuff we may need later
        JavaMethod booleanToVkBool32Method = JavaMethod.of(VULKAN_UTILS_CLASS, JavaClass.ofClass(int.class), "booleanToVkBool32", true);
        JavaMethod vkBool32ToBooleanMethod = JavaMethod.of(VULKAN_UTILS_CLASS, JavaClass.ofClass(boolean.class), "vkBool32ToBoolean", true);

        // code
        JavaClassGenerator vulkanMethodPointersClazz = generator.addJavaFile(VULKAN_PACKAGE + ".commands");

        vulkanMethodPointersClazz.setName("VulkanMethodPointers");
        vulkanMethodPointersClazz.setType(JavaClassType.CLASS);
        vulkanMethodPointersClazz.setVisibility(JavaVisibility.PUBLIC);
        var constructor = vulkanMethodPointersClazz.addConstructor();

        var vkInstanceParam = constructor.addParameter("vkInstance", vkInstanceClassGenerator);
        constructor.setVisibility(JavaVisibility.PUBLIC);

        for (Command cmd : commands.values()) {
            // methodPointerVariable
            var var = vulkanMethodPointersClazz.addVariable(JavaClass.ofClass(long.class), cmd.name);
            var.setFinal(true);
            var.setVisibility(JavaVisibility.PUBLIC);

            cmd.methodPointerVariable = var;


            // Actual method
            Type returnType = cmd.type.resolve();
            List<Type> paramTypes = new ArrayList<>();
            List<JavaExpression> nativeMethodParams = new ArrayList<>(List.of(JavaExpression.ofCode("methodPointers." + cmd.name)));
            JavaClass returnClass;

            if(returnType == CTypes.VOID) {
                returnClass = JavaClass.ofClass(void.class);
            } else if (returnType.getName().equals("VkResult")) {
                returnClass = JavaClass.custom("de.linusdev.cvg4j.nat.vulkan", "ReturnedVkResult");
            } else if (returnType.getName().equals("VkBool32")) {
                returnClass = JavaClass.ofClass(boolean.class);
            } else {
                returnClass = JavaClass.ofClass(returnType.getAsBaseType().getJavaClass());
            }


            var method = vkInstanceClassGenerator.addMethod(returnClass, cmd.name);
            method.setVisibility(JavaVisibility.PUBLIC);
            method.setJavaDoc("<pre>{@code " + cmd.cDef + "}</pre>");

            for(Command.Param param : cmd.params) {
                Type actualParamType = param.type.resolve();

                {
                    // Param type for native function
                    Type paramType = actualParamType;
                    if(param.isPointer)
                        paramType = CTypes.POINTER;
                    paramTypes.add(paramType);
                }


                JavaClass actualParamClass = actualParamType.getJavaClass(registry, generator);

                if(param.isPointer && actualParamType.getType() == TypeType.ENUM)
                    actualParamClass = JavaClass.ofClass(NativeEnumValue32.class).withGenerics(actualParamClass);

                JavaClass paramClass = actualParamClass;

                //TODO pointer to char (string): currently Byte*
                if(param.isPointer && actualParamType != CTypes.VOID)
                    paramClass = JavaClass.ofClass(TypedPointer64.class).withGenerics(actualParamClass);
                else if(param.isPointer && actualParamType == CTypes.VOID)
                    paramClass = JavaClass.ofClass(Pointer64.class);

                if(param.isPointerPointer && actualParamType != CTypes.VOID)
                    paramClass = JavaClass.ofClass(TypedPointer64.class).withGenerics(
                                    JavaClass.ofClass(BBTypedPointer64.class).withGenerics(actualParamClass)
                    );
                else if(param.isPointerPointer && actualParamType == CTypes.VOID)
                    paramClass = JavaClass.ofClass(TypedPointer64.class).withGenerics(JavaClass.ofClass(BBPointer64.class));

                if(!param.isPointer) {
                    if(actualParamType == CTypes.INT32 || actualParamType == CTypes.UINT32) {
                        paramClass = JavaClass.ofClass(int.class);
                        nativeMethodParams.add(JavaExpression.ofCode(param.name));

                    } else if (actualParamType == CTypes.INT64 || actualParamType == CTypes.UINT64) {
                        paramClass = JavaClass.ofClass(long.class);
                        nativeMethodParams.add(JavaExpression.ofCode(param.name));

                    } else if (actualParamType == CTypes.INT) {
                        paramClass = JavaClass.ofClass(long.class);
                        nativeMethodParams.add(JavaExpression.ofCode(param.name));

                    } else if (actualParamType.getType() == TypeType.ENUM) {
                        nativeMethodParams.add(JavaExpression.ofCode(param.name + ".getValue()"));

                    } else if (actualParamType.getName().equals("VkInstance")) {
                        nativeMethodParams.add(JavaExpression.ofCode("get()"));
                    } else if (actualParamType.getName().equals("VkBool32")) {
                        paramClass = JavaClass.ofClass(boolean.class);
                        nativeMethodParams.add(JavaExpression.callMethod(booleanToVkBool32Method, JavaExpression.ofCode(param.name)));
                    } else {
                        nativeMethodParams.add(JavaExpression.ofCode(param.name + ".get()"));

                    }
                } else {
                    // isPointer = true
                    if(actualParamType.getName().equals("VkInstance") && cmd.name.equals("vkCreateInstance"))
                        nativeMethodParams.add(JavaExpression.ofCode("getPointer()"));
                    else
                        nativeMethodParams.add(JavaExpression.ofCode(param.name + ".get()"));
                }

                if(!actualParamType.getName().equals("VkInstance"))
                    method.addParameter(param.name, paramClass);
            }

            NativeFunctionsGenerator.NativeFunction toCall = registry.nativeFunctionsGenerator
                    .getNativeFunction(returnType, paramTypes.toArray(new Type[0]));

            method.body(block -> {
                var methodCall = JavaExpression.callMethod(toCall.getNativeMethod(registry, generator), nativeMethodParams.toArray(new JavaExpression[0]));

                if(returnType.getName().equals("VkResult")) {
                    block.addExpression(
                            JavaExpression.returnExpr(
                                    JavaExpression.callConstructorOf(
                                            JavaClass.custom("de.linusdev.cvg4j.nat.vulkan", "ReturnedVkResult"),
                                            methodCall
                                    )
                            )
                    );
                }  else if (returnType.getName().equals("VkBool32")) {
                    block.addExpression(
                            JavaExpression.returnExpr(
                                    JavaExpression.callMethod(
                                            vkBool32ToBooleanMethod,
                                            methodCall
                                    )
                            )
                    );
                } else if(returnType == CTypes.VOID) {
                    block.addExpression(methodCall);
                } else {
                    block.addExpression(JavaExpression.returnExpr(methodCall));
                }
            });

        }

        var glfwGetVkProcAddressMethod = JavaMethod.of(
                JavaClass.custom("de.linusdev.cvg4j.nat.glfw3", "GLFW"),
                JavaClass.ofClass(long.class),
                "glfwGetInstanceProcAddress",
                true
        );

        constructor.body(block -> {
            for (Command cmd : commands.values()) {
                block.addExpression(
                        JavaExpression.assign(cmd.methodPointerVariable,
                                JavaExpression.callMethod(glfwGetVkProcAddressMethod,
                                        JavaExpression.ofCode(vkInstanceParam.getName() + ".get()"),
                                        JavaExpression.ofString(cmd.name)
                                )
                        )
                );
            }
        });

        // Add VulkanMethodPointers as instance variable to VkInstance and a method to load the message pointers
        var methodPointersVariable = vkInstanceClassGenerator.addVariable(vulkanMethodPointersClazz, "methodPointers");
        methodPointersVariable.setVisibility(JavaVisibility.PROTECTED);
        var initMethodPointersFunction = vkInstanceClassGenerator.addMethod(JavaClass.ofClass(void.class), "initMethodPointers");
        initMethodPointersFunction.setVisibility(JavaVisibility.PUBLIC);
        initMethodPointersFunction.body(block -> {
            block.addExpression(JavaExpression.ofCode("assert !isNullPtr()" ));
            block.addExpression(
                    JavaExpression.assign(methodPointersVariable,
                            JavaExpression.callConstructorOf(vulkanMethodPointersClazz,
                                    JavaExpression.thisExpression()
                            )
                    )
            );
        });
    }

    public static class Command {

        private final @NotNull RegistryLoader registry;

        private final @NotNull String name;
        private final @NotNull PossiblyUnresolvedType type;
        public String cDef;

        public JavaVariable methodPointerVariable;

        private final List<Param> params = new ArrayList<>();

        public Command(
                @NotNull RegistryLoader registry, @NotNull String name,
                @NotNull PossiblyUnresolvedType type
        ) {
            this.registry = registry;
            this.name = name;
            this.type = type;
        }

        public void addParam(@NotNull Node paramNode) {
            Node nameNode = findInChildren(paramNode, "name");
            Node typeNode = findInChildren(paramNode, "type");

            if(nameNode == null || typeNode == null)
                throw new IllegalStateException("Param missing name or type!");

            String name = nameNode.getTextContent();
            String type = typeNode.getTextContent();
            boolean isPointer = paramNode.getTextContent().contains("*");
            boolean isPointerPointer = isPointer && paramNode.getTextContent().replaceFirst("\\*", "").contains("*");

            params.add(new Param(
                    name,
                    registry.getPUType(type),
                    isPointer,
                    isPointerPointer
            ));
        }

        public static class Param {
            private final @NotNull String name;
            private final @NotNull PossiblyUnresolvedType type;
            private final boolean isPointer;
            private final boolean isPointerPointer;

            public Param(@NotNull String name, @NotNull PossiblyUnresolvedType type, boolean isPointer, boolean isPointerPointer) {
                this.name = name;
                this.type = type;
                this.isPointer = isPointer;
                this.isPointerPointer = isPointerPointer;
            }
        }
    }
}
