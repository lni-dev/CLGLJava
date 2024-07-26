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

import de.linusdev.cvg4j.build.vkregistry.types.*;
import de.linusdev.cvg4j.build.vkregistry.types.abstracts.PossiblyUnresolvedType;
import de.linusdev.cvg4j.build.vkregistry.types.abstracts.Type;
import de.linusdev.lutils.codegen.SourceGenerator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiPredicate;

/**
 * NOT thread safe
 */
public class RegistryLoader {

    public static Iterable<Node> iterableNode(@NotNull Node node) {
        return new Iterable<>() {
            @NotNull
            @Override
            public Iterator<Node> iterator() {
                return new Iterator<>() {
                    private final @NotNull NodeList nodeList = node.getChildNodes();
                    private int index = 0;

                    @Override
                    public boolean hasNext() {
                        return index < nodeList.getLength();
                    }

                    @Override
                    public Node next() {
                        return nodeList.item(index++);
                    }
                };
            }
        };
    }

    public static @Nullable Node findInChildren(@NotNull Node searchIn, @NotNull String search) {
        for(Node node : iterableNode(searchIn)) {
            if(node.getNodeName().equals(search)) {
                return node;
            }
        }

        return null;
    }

    private final @NotNull Map<String, Type> types = new HashMap<>();
    private AtomicInteger iterating = new AtomicInteger(0);
    private final @NotNull Map<String, Type> extraTypes = new HashMap<>();

    private @Nullable SourceGenerator generator = null;

    public RegistryLoader(@NotNull Path xmlLocation) throws ParserConfigurationException, IOException, SAXException {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newDefaultInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        Document document = builder.parse(Files.newInputStream(xmlLocation));
        document.getDocumentElement().normalize();

        addStandardTypes();
        readRegistry(document.getChildNodes().item(0));
    }

    public void addType(@NotNull Type type) {
        addType(type, false);
    }

    public void addType(@NotNull Type type, boolean generate) {
        if(iterating.get() == 0) {
            types.put(type.getName(), type);
            return;
        }

        extraTypes.put(type.getName(), type);

        if(generate) {
            if(generator == null)
                throw new IllegalStateException("Generator is null. Cannot generate.");

            type.generate(this,generator);
        }
    }

    public @Nullable Type getType(@NotNull String name) {
        var type = types.get(name);
        if(type == null)
            type = extraTypes.get(name);

        return type;
    }

    public void iterateTypes(@NotNull BiPredicate<String, Type> consumer) {
        System.out.println("START iterateTypes");
        iterating.incrementAndGet();

        for (Map.Entry<String, Type> entry : types.entrySet()) {
            if(!consumer.test(entry.getKey(), entry.getValue()))
                break;
        }

        iterating.decrementAndGet();

        types.putAll(extraTypes);
        extraTypes.clear();
        System.out.println("END iterateTypes");
    }

    public PossiblyUnresolvedType getPUType(@NotNull String name) {
        return new PossiblyUnresolvedType(this, name);
    }

    private void addStandardTypes() {
        System.out.println("START addStandardTypes");
        types.put(
                "VK_DEFINE_HANDLE",
                new BasicType("VkHandle", CTypes.POINTER)
        );
        types.put(
                "VK_DEFINE_NON_DISPATCHABLE_HANDLE",
                new BasicType("VkNonDispatchableHandle", CTypes.UINT64)
        );

        addType(CTypes.INT32);
        addType(CTypes.UINT32);
        addType(CTypes.FLOAT);
        System.out.println("END addStandardTypes");
    }

    private void readRegistry(@NotNull Node registryNode) {
        if(!registryNode.getNodeName().equals("registry"))
            throw new IllegalStateException("'registry' Node not found");

        for(Node node : iterableNode(registryNode)) {
            if(node.getNodeType() == Node.TEXT_NODE)
                continue;
            else if(node.getNodeName().equals("comment"))
                continue;
            else if(node.getNodeName().equals("types"))
                handleTypes(node);
            else if(node.getNodeName().equals("enums"))
                handleEnums(node);
            else
                System.out.println("Unhandled Node: " + node.getNodeName());
        }
    }

    public void handleEnums(@NotNull Node enumsNode) {
        System.out.println("START handleEnums");
        Node nameAttr = enumsNode.getAttributes().getNamedItem("name");
        Node typeAttr = enumsNode.getAttributes().getNamedItem("type");
        Node commentAttr = enumsNode.getAttributes().getNamedItem("comment");
        Node bitWidthAttr = enumsNode.getAttributes().getNamedItem("bitwidth");

        if(nameAttr == null)
            throw new IllegalStateException("<enums> node without name! " + enumsNode.getTextContent());

        // Remove spaces from some names, so we can create a java class with that name.
        nameAttr.setNodeValue(nameAttr.getNodeValue().replace(" ", ""));

        if(typeAttr == null) {
            throw new IllegalStateException("<enums> node without type: " + enumsNode.getTextContent());
        }

        if(!typeAttr.getNodeValue().equals("enum") && !typeAttr.getNodeValue().equals("bitmask") && !typeAttr.getNodeValue().equals("constants"))
            throw new IllegalStateException("<enums> node with type '" + typeAttr.getNodeValue() + "', which is not 'enum' or 'bitmask': " + enumsNode.getTextContent());

        if(typeAttr.getNodeValue().equals("constants")) {
            // #Define-Group
            GroupedDefinesType n = new GroupedDefinesType(
                    nameAttr.getNodeValue(),
                    commentAttr == null ? null : commentAttr.getNodeValue()
            );

            for (Node enumNode : iterableNode(enumsNode)) {
                if(enumNode.getNodeType() == Node.TEXT_NODE)
                    continue;
                n.addDefine(enumNode);
            }

            addType(n);
        } else if(typeAttr.getNodeValue().equals("enum")) {
            // Normal Enum
            EnumType n = new EnumType(
                    nameAttr.getNodeValue(),
                    commentAttr == null ? null : commentAttr.getNodeValue()
            );

            for (Node enumNode : iterableNode(enumsNode)) {
                if(
                        enumNode.getNodeType() == Node.TEXT_NODE
                        || enumNode.getNodeName().equals("comment")
                        || enumNode.getNodeName().equals("unused")
                )
                    continue;
                n.addValue(enumNode);
            }

            addType(n);

        } else if(typeAttr.getNodeValue().equals("bitmask")) {
            // Bitmask Enum
            int bitWidth = 32;
            if(bitWidthAttr != null)
                bitWidth = Integer.parseInt(bitWidthAttr.getNodeValue());

            BitMaskEnumType n = new BitMaskEnumType(
                    nameAttr.getNodeValue(),
                    bitWidth,
                    commentAttr == null ? null : commentAttr.getNodeValue()
            );

            for (Node enumNode : iterableNode(enumsNode)) {
                if(
                        enumNode.getNodeType() == Node.TEXT_NODE
                                || enumNode.getNodeName().equals("comment")
                                || enumNode.getNodeName().equals("unused")
                )
                    continue;
                n.addValue(enumNode);
            }

            addType(n);
        }

        System.out.println("END handleEnums");
    }

    public void handleTypes(@NotNull Node typesNode) {
        System.out.println("START handleTypes");
        for(Node node : iterableNode(typesNode)) {
            if(node.getNodeName().equals("type"))
                handleType(node);
            else if(node.getNodeName().equals("comment"))
                continue;
            else if(node.getNodeType() == Node.TEXT_NODE)
                continue;
            else
                System.out.println("Unhandled Node in 'types': " + node.getNodeName());

        }
        System.out.println("END handleTypes");
    }

    public void handleType(@NotNull Node typeNode) {
        if(!typeNode.hasChildNodes())
            return; // Skip all Nodes that don't add any c-code

        Node categoryNode = typeNode.getAttributes().getNamedItem("category");
        String category = categoryNode == null ? "" : categoryNode.getNodeValue();
        if(category.equals("define"))
            return; // Skip '#define' nodes
        else if(category.equals("basetype")) {
            Node nameNode = findInChildren(typeNode, "name");
            Node aliasTypeNode = findInChildren(typeNode, "type");

            if(aliasTypeNode != null && nameNode != null) {
                BasicType n;
                if(aliasTypeNode.getTextContent().equals("void") && typeNode.getTextContent().contains("void*")) {
                    n = new BasicType(
                            nameNode.getTextContent(),
                            CTypes.POINTER
                    );
                } else {
                    n = new BasicType(
                            nameNode.getTextContent(),
                            CTypes.ofCType(aliasTypeNode.getTextContent())
                    );
                }
                addType(n);
            }

        } else if(category.equals("bitmask")) {
            Node nameNode = findInChildren(typeNode, "name");
            Node aliasTypeNode = findInChildren(typeNode, "type");

            if(aliasTypeNode == null || nameNode == null) {
                throw new IllegalStateException("aliasTypeNode or nameNode is null of bitmask: " + typeNode.getTextContent());
            }

            BitMaskType n = new BitMaskType(
                    this,
                    nameNode.getTextContent(),
                    types.get(aliasTypeNode.getTextContent())
            );

            addType(n);

        } else if(category.equals("handle")) {
            Node parentNode = typeNode.getAttributes().getNamedItem("parent");
            Node objTypeEnumNode = typeNode.getAttributes().getNamedItem("objtypeenum");
            Node nameNode = findInChildren(typeNode, "name");
            Node aliasTypeNode = findInChildren(typeNode, "type");

            if(nameNode == null || aliasTypeNode == null || objTypeEnumNode == null) {
                throw new IllegalStateException("HandleType without name," +
                        " type or objTypeEnum Node/Attribute: " + typeNode.getTextContent());
            }

            HandleType n = new HandleType(
                    nameNode.getTextContent(),
                    types.get(aliasTypeNode.getTextContent()),
                    parentNode == null ? null : parentNode.getNodeValue(),
                    objTypeEnumNode.getNodeValue()
            );

            addType(n);

        } else if(category.equals("funcpointer")) {
            return; // skip function pointers

        } else if(category.equals("struct")) {
            if(structCount >= 10) return;
            structCount++;
            Node nameAttr = typeNode.getAttributes().getNamedItem("name");
            Node commentAttr = typeNode.getAttributes().getNamedItem("comment");
            Node returnedOnlyAttr = typeNode.getAttributes().getNamedItem("returnedonly");
            Node structExtendsAttr = typeNode.getAttributes().getNamedItem("structextends");

            if(nameAttr == null)
                throw new IllegalStateException("struct <type> without name attribute!");

            StructType n = new StructType(
                    this,
                    nameAttr.getNodeValue(),
                    returnedOnlyAttr == null ? null : returnedOnlyAttr.getNodeValue().equals("true"),
                    commentAttr == null ? null : commentAttr.getNodeValue(),
                    structExtendsAttr == null ? null : Arrays.stream(structExtendsAttr.getNodeValue().split(",")).map(this::getPUType).toList()
            );

            addType(n);

            for (Node memberNode : iterableNode(typeNode)) {
                if(memberNode.getNodeType() == Node.TEXT_NODE || memberNode.getNodeName().equals("comment"))
                    continue;
                n.addMember(memberNode);
            }

        } else if(category.equals("union")) {
            // TODO: 1734
        }

    }

    //TODO: remove
    private int structCount = 0;

    public void generate(@NotNull SourceGenerator generator) {
        this.generator = generator;
        iterateTypes((name, type) -> {
            type.generate(this, generator);
            return true;
        });
    }
}
