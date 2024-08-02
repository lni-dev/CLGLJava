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
import de.linusdev.lutils.codegen.SourceGenerator;
import de.linusdev.lutils.codegen.java.*;
import de.linusdev.lutils.nat.pointer.TypedPointer64;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

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
            JavaClass returnClass;

            if(returnType == CTypes.VOID) {
                returnClass = JavaClass.ofClass(void.class);
            } else {
                returnClass = returnType.getJavaClass(registry, generator);
            }


            var method = vkInstanceClassGenerator.addMethod(returnClass, cmd.name);
            method.setVisibility(JavaVisibility.PUBLIC);
            method.setJavaDoc("<pre>{@code " + cmd.cDef + "}</pre>");

            for(Command.Param param : cmd.params) {
                Type actualParamType = param.type.resolve();
                Type paramType = actualParamType;

                if(param.isPointer)
                    paramType = CTypes.POINTER;

                paramTypes.add(paramType);

                JavaClass actualParamClass = actualParamType.getJavaClass(registry, generator);
                JavaClass paramClass = actualParamClass;

                if(param.isPointer)
                    paramClass = JavaClass.ofClass(TypedPointer64.class).withGenerics(actualParamClass);

                var addedParam = method.addParameter(param.name, paramClass);
            }

            NativeFunctionsGenerator.NativeFunction toCall = registry.nativeFunctionsGenerator
                    .getNativeFunction(returnType, paramTypes.toArray(new Type[0]));

            //TODO call in body
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

            params.add(new Param(
                    name,
                    registry.getPUType(type),
                    isPointer
            ));
        }

        public static class Param {
            private final @NotNull String name;
            private final @NotNull PossiblyUnresolvedType type;
            private final boolean isPointer;

            public Param(@NotNull String name, @NotNull PossiblyUnresolvedType type, boolean isPointer) {
                this.name = name;
                this.type = type;
                this.isPointer = isPointer;
            }
        }
    }
}
