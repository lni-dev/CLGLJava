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
import de.linusdev.cvg4j.build.vkregistry.types.abstracts.PossiblyUnresolvedDefine;
import de.linusdev.cvg4j.build.vkregistry.types.abstracts.PossiblyUnresolvedType;
import de.linusdev.cvg4j.build.vkregistry.types.abstracts.Type;
import de.linusdev.lutils.ansi.sgr.SGR;
import de.linusdev.lutils.ansi.sgr.SGRParameters;
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
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiPredicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public static int calculateExtensionValue(int extNumber, int offset, boolean negative) {
        int value = EXTENSION_VALUE_BASE + (extNumber-1) * EXTENSION_VALUE_RANGE_SIZE + offset;
        if(negative)
            value *= -1;
        return value;
    }

    private static int vkMakeVideoStdVersion(int major, int minor, int patch) {
        return (major << 22) | (minor << 12) | patch;
    }

    /**
     * create custom java doc tag with given name.
     */
    public static String jdTag(@NotNull String name) {
        return CUSTOM_JAVADOC_TAG_PREFIX + "." + name;
    }

    /**
     * @return {@code true} if given node should be skipped, because it does not contain "vulkan" in it's api attribute.
     */
    public static boolean checkApiAttr(@NotNull Node node) {
        Node apiAttr = node.getAttributes().getNamedItem("api");
        if(apiAttr == null) return false;

        String apis = apiAttr.getNodeValue();
        List<String> apisList = List.of(apis.split(","));

        if(!apisList.contains("vulkan")) {
            System.out.println("Skipping a node, because it does not contain the api 'vulkan'.");
            return true;
        }

        return false;
    }

    public final static String COLOR_ORANGE = new SGR(SGRParameters.BACKGROUND_YELLOW).construct();

    public final static int EXTENSION_VALUE_BASE = 1000000000;
    public final static int EXTENSION_VALUE_RANGE_SIZE = 1000;

    public final static String VULKAN_PACKAGE = "vulkan";
    public final static String WIN32_PACKAGE = "win32";

    public final static String CUSTOM_JAVADOC_TAG_PREFIX = "cvg4j";

    private final @NotNull Map<String, Type> types = new HashMap<>();
    private final @NotNull List<GroupedDefinesType> defines = new ArrayList<>();
    private AtomicInteger iterating = new AtomicInteger(0);
    private final @NotNull Map<String, Type> extraTypes = new HashMap<>();
    private final @NotNull CommandsGenerator commandsGenerator = new CommandsGenerator(this);
    public final @NotNull NativeFunctionsGenerator nativeFunctionsGenerator = new NativeFunctionsGenerator(this);

    private @Nullable SourceGenerator generator = null;

    public RegistryLoader(
            @NotNull Path vkXmlLocation,
            @NotNull Path videoXmlLocation
    ) throws ParserConfigurationException, IOException, SAXException {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newDefaultInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        Document documentVk = builder.parse(Files.newInputStream(vkXmlLocation));
        documentVk.getDocumentElement().normalize();

        Document documentVideo = builder.parse(Files.newInputStream(videoXmlLocation));
        documentVideo.getDocumentElement().normalize();

        addStandardTypes();

        SGR sgr = new SGR(SGRParameters.BACKGROUND_GREEN);
        System.out.println(sgr.construct() + "READ documentVideo" + SGR.reset());
        readRegistry(documentVideo.getChildNodes().item(0));

        System.out.println(sgr.construct() + "READ documentVk" + SGR.reset());
        readRegistry(documentVk.getChildNodes().item(0));
    }

    public void addType(@NotNull Type type) {
        addType(type, false);
    }

    public void addType(@NotNull Type type, boolean generate) {
        if(type instanceof GroupedDefinesType td)
            defines.add(td);

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

    public @Nullable GroupedDefinesType.Define getDefine(@NotNull String name) {
        for (GroupedDefinesType group : defines) {
            GroupedDefinesType.Define def = group.getDefines().get(name);
            if(def != null)
                return def;
        }

        return null;
    }

    /**
     * Note: this can only find DefineGroups, that have already been handled.
     */
    public @Nullable GroupedDefinesType getDefineGroup(@NotNull String name) {
        for (GroupedDefinesType group : defines) {
            if(group.getName().equals(name))
                return group;
        }

        return null;
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

    public PossiblyUnresolvedDefine getPUDefine(@NotNull String name) {
        return new PossiblyUnresolvedDefine(this, name);
    }

    private void addStandardTypes() {
        System.out.println("START addStandardTypes");

        //Some basic Vulkan types
        types.put(
                "VK_DEFINE_HANDLE",
                new BasicType("VkHandle", CTypes.POINTER, VULKAN_PACKAGE)
        );
        types.put(
                "VK_DEFINE_NON_DISPATCHABLE_HANDLE",
                new BasicType("VkNonDispatchableHandle", CTypes.UINT64, VULKAN_PACKAGE)
        );

        // VkVideo Version
        addVKVideoCodecVersions();

        // Win32
        addWin32Types();

        // Other types the Vulkan API Depends on, which I will probably never need tho
        // We add some types as VOID. These are types which are only used as pointers, so we create
        // the actual type as subclass of UnknownStructure, which cannot be instantiated nor does it
        // have a StructureInfo.
        // Zircon: https://github.com/vsrinivas/zircon/blob/master/system/public/zircon/types.h
        types.put("zx_handle_t", new BasicType("ZXHandle", CTypes.UINT32, "zircon"));

        // QNX: https://www.qnx.com/developers/docs/8.0/com.qnx.doc.screen/topic/screen_context_t.html
        types.put("_screen_context", new BasicType("ScreenContext", CTypes.VOID, "qnx"));
        types.put("_screen_window", new BasicType("ScreenWindow", CTypes.VOID, "qnx"));
        types.put("_screen_buffer", new BasicType("ScreenBuffer", CTypes.VOID, "qnx"));

        // ggp_c: I did not find an official doc/GitHub
        // My current information is from:
        // https://github.com/terrafx/terrafx.interop.vulkan/blob/main/generation/Vulkan/vulkan/vulkan_ggp/ggp_c/vulkan_types.h
        types.put("GgpStreamDescriptor", new BasicType("GgpStreamDescriptor", CTypes.UINT32, "ggp"));
        types.put("GgpFrameToken", new BasicType("GgpFrameToken", CTypes.UINT32, "ggp"));

        //XCB: https://xcb.freedesktop.org/manual/xcb_8h.html and https://xcb.freedesktop.org/manual/xproto_8h_source.html
        types.put("xcb_connection_t", new BasicType("XCBConnection", CTypes.VOID, "xcb"));
        types.put("xcb_visualid_t", new BasicType("XCBVisualId", CTypes.UINT32, "xcb"));
        types.put("xcb_window_t", new BasicType("XCBWindow", CTypes.UINT32, "xcb"));

        //Android (line 231): https://android.googlesource.com/platform/frameworks/native/+/refs/heads/main/libs/nativewindow/include/android/hardware_buffer.h
        // https://android.googlesource.com/platform/frameworks/native/+/refs/heads/main/libs/nativewindow/include/android/native_window.h
        types.put("AHardwareBuffer", new BasicType("AHardwareBuffer", CTypes.VOID, "android"));
        types.put("ANativeWindow", new BasicType("ANativeWindow", CTypes.VOID, "android"));

        //NVIDIA
        // https://developer.nvidia.com/docs/drive/drive-os/6.0.8.1/public/drive-os-linux-sdk/api_reference/nvscibuf_8h_source.html
        types.put("NvSciBufObj", new BasicType("NvSciBufObj", CTypes.POINTER, "nvidia"));
        types.put("NvSciBufAttrList", new BasicType("NvSciBufAttrList", CTypes.POINTER, "nvidia"));
        // https://developer.nvidia.com/docs/drive/drive-os/6.0.8.1/public/drive-os-linux-sdk/api_reference/nvscisync_8h_source.html
        types.put("NvSciSyncAttrList", new BasicType("NvSciSyncAttrList", CTypes.POINTER, "nvidia"));
        types.put("NvSciSyncObj", new BasicType("NvSciSyncObj", CTypes.POINTER, "nvidia"));
        StructType nvSciSyncFence = new StructType(this, "NvSciSyncFence", null, null, null, false);
        nvSciSyncFence.addMember(new StructType.Member(
                "payload", null,
                getPUType("uint64_t"),
                false, false, null, null, null,
                true, null, 6, null,
                "uint64_t payload[6];"
        ));
        types.put("NvSciSyncFence", nvSciSyncFence);

        // DirectFB
        types.put("IDirectFB", new BasicType("IDirectFB", CTypes.VOID, "directfb"));
        types.put("IDirectFBSurface", new BasicType("IDirectFBSurface", CTypes.VOID, "directfb"));

        // wayland: https://github.com/nobled/wayland/blob/master/src/wayland-client.h
        types.put("wl_display", new BasicType("WLDisplay", CTypes.VOID, "wayland"));
        types.put("wl_surface", new BasicType("WLSurface", CTypes.VOID, "wayland"));

        // X11: https://gitlab.freedesktop.org/xorg/lib/libx11/-/blob/master/include/X11/Xlib.h
        types.put("Display", new BasicType("X11Display", CTypes.VOID, "x11"));
        types.put("VisualID", new BasicType("X11VisualID", CTypes.POINTER, "x11"));
        types.put("Window", new BasicType("X11Window", CTypes.POINTER, "x11"));
        // https://codebrowser.dev/boost/include/X11/X.h.html
        Type xid = new BasicType("XID", CTypes.UINT32, "x11");
        types.put("XID", xid);
        // https://github.com/D-Programming-Deimos/libX11/blob/master/c/X11/extensions/Xrandr.h
        types.put("RROutput", new AliasOfBasicType("X11RROutput", xid, "x11"));

        // Whatever this is: IOSurfaceRef
        types.put("IOSurfaceRef", new BasicType("IOSurfaceRef", CTypes.POINTER, "vulkan"));


        // Default C types
        addType(CTypes.INT8);
        addType(CTypes.UINT8);
        addType(CTypes.UINT16);
        addType(CTypes.INT16);
        addType(CTypes.INT32);
        addType(CTypes.UINT32);
        addType(CTypes.FLOAT);
        addType(CTypes.DOUBLE);
        addType(CTypes.CHAR);
        addType(CTypes.VOID);
        addType(CTypes.SIZE);
        addType(CTypes.UINT64);
        addType(CTypes.INT64);
        addType(CTypes.INT);
        System.out.println("END addStandardTypes");
    }

    private void addWin32Types() {
        //We also need some Win32 types
        //Win32: https://learn.microsoft.com/en-us/windows/win32/winprog/windows-data-types
        Type pvoid = new BasicType("PointerToVoid", CTypes.POINTER, WIN32_PACKAGE);
        Type lpvoid = new BasicType("LongPointerToVoid", CTypes.POINTER, WIN32_PACKAGE);
        Type handle =new AliasOfBasicType("Handle", pvoid, WIN32_PACKAGE);
        Type dword = new BasicType("DoubleWord", CTypes.UINT32, WIN32_PACKAGE);
        types.put("PVOID", pvoid);
        types.put("LPVOID", pvoid);
        types.put("HANDLE", handle);
        types.put("DWORD", dword);
        types.put("HINSTANCE", new AliasOfBasicType("HandleToInstance", handle, WIN32_PACKAGE));
        types.put("HWND", new AliasOfBasicType("HandleToWindow", handle, WIN32_PACKAGE));
        types.put("HMONITOR", new AliasOfBasicType("HandleToMonitor", handle, WIN32_PACKAGE));
        types.put("LPCWSTR", new BasicType("LPConstantWideString", CTypes.STRING_UTF16, WIN32_PACKAGE));

        // https://learn.microsoft.com/en-us/openspecs/windows_protocols/ms-dtyp/9d81be47-232e-42cf-8f0d-7a3b29bf2eb2
        types.put("BOOL", new BasicType("Bool", CTypes.INT32, WIN32_PACKAGE));

        // Win32 SECURITY_ATTRIBUTES: https://learn.microsoft.com/en-us/windows/win32/api/wtypesbase/ns-wtypesbase-security_attributes
        StructType securityAttributes = new StructType(
                this,
                "SecurityAttributes",
                null,
                null,
                null,
                false
        );

        securityAttributes.setCustomSubPackage(WIN32_PACKAGE);

        securityAttributes.addMember(new StructType.Member(
                "nLength",
                "The size, in bytes, of this structure. Set this value to the size of the SECURITY_ATTRIBUTES structure",
                getPUType("DWORD"),
                false,
                false, null, null, null,
                false, null, null, null,
                "DWORD  nLength;"
        ));

        securityAttributes.addMember(new StructType.Member(
                "lpSecurityDescriptor",
                "A pointer to a SECURITY_DESCRIPTOR structure that controls access to the object. If the value of this member is NULL, the object is assigned the default security descriptor associated with the access token of the calling process. This is not the same as granting access to everyone by assigning a NULL discretionary access control list (DACL). By default, the default DACL in the access token of a process allows access only to the user represented by the access token.",
                getPUType("LPVOID"),
                false,
                false, null, null, null,
                false, null, null, null,
                "LPVOID lpSecurityDescriptor;"
        ));

        securityAttributes.addMember(new StructType.Member(
                "bInheritHandle",
                "A Boolean value that specifies whether the returned handle is inherited when a new process is created. If this member is TRUE, the new process inherits the handle.",
                getPUType("BOOL"),
                false,
                false, null, null, null,
                false, null, null, null,
                "BOOL bInheritHandle;"
        ));

        types.put("SECURITY_ATTRIBUTES", securityAttributes);
    }

    private void addVKVideoCodecVersions() {
        GroupedDefinesType group = new GroupedDefinesType("VkVideoCodecVersions", null, null);
        group.addDefine(new GroupedDefinesType.Define(
                "VK_STD_VULKAN_VIDEO_CODEC_H264_DECODE_API_VERSION_1_0_0",
                null, CTypes.INT32,
                "" + vkMakeVideoStdVersion(1, 0, 0),
                null, false, group
        ));
        group.addDefine(new GroupedDefinesType.Define(
                "VK_STD_VULKAN_VIDEO_CODEC_H264_ENCODE_API_VERSION_1_0_0",
                null, CTypes.INT32,
                "" + vkMakeVideoStdVersion(1, 0, 0),
                null, false, group
        ));
        group.addDefine(new GroupedDefinesType.Define(
                "VK_STD_VULKAN_VIDEO_CODEC_H265_DECODE_API_VERSION_1_0_0",
                null, CTypes.INT32,
                "" + vkMakeVideoStdVersion(1, 0, 0),
                null, false, group
        ));
        group.addDefine(new GroupedDefinesType.Define(
                "VK_STD_VULKAN_VIDEO_CODEC_H265_ENCODE_API_VERSION_1_0_0",
                null, CTypes.INT32,
                "" + vkMakeVideoStdVersion(1, 0, 0),
                null, false, group
        ));
        group.addDefine(new GroupedDefinesType.Define(
                "VK_STD_VULKAN_VIDEO_CODEC_AV1_DECODE_API_VERSION_1_0_0",
                null, CTypes.INT32,
                "" + vkMakeVideoStdVersion(1, 0, 0),
                null, false, group
        ));

        addType(group);
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
            else if(node.getNodeName().equals("extensions"))
                handleExtensions(node);
            else if(node.getNodeName().equals("feature"))
                handleFeature(node);
            else if(node.getNodeName().equals("commands"))
                handleCommands(node);
            else
                System.out.println(COLOR_ORANGE + "Unhandled Node: " + node.getNodeName() + SGR.reset());
        }
    }

    public void handleFeature(@NotNull Node featureNode) {
        System.out.println("START handleFeature");

        if(checkApiAttr(featureNode))
            return; // Only add vulkan api stuff

        Node nameAttr = featureNode.getAttributes().getNamedItem("name");

        if(nameAttr == null)
            throw new IllegalStateException("Extension without name: " + featureNode);

        String name = nameAttr.getNodeValue();

        for(Node reqNode : iterableNode(featureNode)) {
            if (reqNode.getNodeType() == Node.TEXT_NODE)
                continue;

            if (!reqNode.getNodeName().equals("require"))
                throw new IllegalStateException("Unexpected Node in <feature> '" + name + "': " + reqNode.getNodeName());

            for (Node node : iterableNode(reqNode)) {
                if (node.getNodeName().equals("comment"))
                    continue;
                else if (node.getNodeType() == Node.TEXT_NODE)
                    continue;
                else if (node.getNodeName().equals("type"))
                    continue;
                else if (node.getNodeName().equals("enum"))
                    handleEnumExtension(node, name, null, Objects.requireNonNull(getDefineGroup("APIConstants")));
                else
                    System.out.println("Unhandled Node in '<require>' (extensions): " + node.getNodeName());
                //TODO: command Node

            }

        }

        System.out.println("END handleFeature");
    }

    public void handleExtensions(@NotNull Node extensionsNode) {
        System.out.println("START handleExtensions");

        GroupedDefinesType constants = getDefineGroup("APIConstants");

        for(Node extNode : iterableNode(extensionsNode)) {
            if (extNode.getNodeType() == Node.TEXT_NODE)
                continue;

            if(!extNode.getNodeName().equals("extension"))
                throw new IllegalStateException("Unexpected Node in <extensions>: " + extNode.getNodeName());

            Node nameAttr = extNode.getAttributes().getNamedItem("name");
            @Nullable Node numberAttr = extNode.getAttributes().getNamedItem("number");

            if(nameAttr == null)
                throw new IllegalStateException("Extension without name" + extNode);

            String name = nameAttr.getNodeValue();
            @Nullable Integer extensionNumber = numberAttr == null ? null : Integer.parseInt(numberAttr.getNodeValue());

            GroupedDefinesType extensionConstants = new GroupedDefinesType(name, null, constants);
            addType(extensionConstants);

            for (Node reqNode : iterableNode(extNode)) {
                if (reqNode.getNodeType() == Node.TEXT_NODE)
                    continue;

                if (reqNode.getNodeName().equals("remove"))
                    System.out.println("Ignored remove node.");
                else if (!reqNode.getNodeName().equals("require"))
                    throw new IllegalStateException("Unexpected Node in <extension>: " + reqNode.getNodeName());

                for (Node node : iterableNode(reqNode)) {
                    if (node.getNodeName().equals("comment"))
                        continue;
                    else if (node.getNodeType() == Node.TEXT_NODE)
                        continue;
                    else if (node.getNodeName().equals("type"))
                        continue;
                    else if (node.getNodeName().equals("enum"))
                        handleEnumExtension(node, name, extensionNumber, extensionConstants);
                    else if (node.getNodeName().equals("commands"))
                        handleCommands(node);
                    else
                        System.out.println("Unhandled Node in '<require>' (extensions): " + node.getNodeName());
                        //TODO: command Node
                }
            }
        }

        System.out.println("END handleExtensions");
    }

    public void handleCommands(
            @NotNull Node commandsNode
    ) {

        System.out.println("START handleCommands");

        for (Node node : iterableNode(commandsNode)) {
            if (node.getNodeName().equals("comment"))
                continue;
            else if (node.getNodeType() == Node.TEXT_NODE)
                continue;
            else if (node.getNodeName().equals("command"))
                handleCommand(node);
            else
                System.out.println("Unhandled Node in '<commands>': " + node.getNodeName());
        }

        System.out.println("END handleCommands");
    }

    public void handleCommand(
            @NotNull Node cmdNode
    ) {
        commandsGenerator.addCommand(cmdNode);
    }

    public void handleEnumExtension(
            @NotNull Node enumNode,
            @NotNull String extensionOrFeatureName,
            @Nullable Integer extensionNumber,
            @NotNull GroupedDefinesType extensionConstants
    ) {

        Node nameAttr = enumNode.getAttributes().getNamedItem("name");
        Node commentAttr = enumNode.getAttributes().getNamedItem("comment");

        Node extendsAttr = enumNode.getAttributes().getNamedItem("extends");
        Node extnumberAttr = enumNode.getAttributes().getNamedItem("extnumber");

        Node valueAttr = enumNode.getAttributes().getNamedItem("value");
        Node bitposAttr = enumNode.getAttributes().getNamedItem("bitpos");
        Node aliasAttr = enumNode.getAttributes().getNamedItem("alias");

        Node offsetAttr = enumNode.getAttributes().getNamedItem("offset");
        Node dirAttr = enumNode.getAttributes().getNamedItem("dir");

        if(checkApiAttr(enumNode))
            return; // Only add vulkan api stuff

        if(nameAttr == null)
            throw new IllegalStateException("<enum> node without name!");

        String comment = commentAttr == null ? null : commentAttr.getNodeValue();

        if(extendsAttr == null) {
            if(valueAttr == null && bitposAttr == null && aliasAttr == null && offsetAttr == null)
                return; // skip this. It is only a reference to an already existing define.
            // Simple Constant (#define)
            System.out.println("adding enum value '" + nameAttr.getNodeValue() + "' as define-constant due to extension/feature " + extensionOrFeatureName);
            extensionConstants.addDefine(enumNode);
        } else {
            // Extension in a different enum

            String extend = extendsAttr.getNodeValue();

            System.out.println("adding enum value '" + nameAttr.getNodeValue() + "' to '" + extend +  "' due to extension/feature " + extensionOrFeatureName);

            Type type = getType(extend);

            if(type instanceof EnumType enumToExtend) {
                if(valueAttr != null) {
                    enumToExtend.addValue(new EnumType.Value(
                            nameAttr.getNodeValue(),
                            enumToExtend.getEnumValueName(nameAttr.getNodeValue()),
                            valueAttr.getNodeValue(),
                            comment,
                            null,
                            javaDocGenerator -> javaDocGenerator.addAtText(jdTag("addedByExtension"), extensionOrFeatureName)
                    ));
                } else if(aliasAttr != null) {
                    enumToExtend.addValue(new EnumType.Value(
                            nameAttr.getNodeValue(),
                            enumToExtend.getEnumValueName(nameAttr.getNodeValue()),
                            enumToExtend.getEnumValueName(aliasAttr.getNodeValue()) + ".getValue()",
                            comment, null,
                            javaDocGenerator -> javaDocGenerator.addAtText(jdTag("addedByExtension"), extensionOrFeatureName)
                    ));
                } else if(offsetAttr != null) {

                    if(extensionNumber == null && extnumberAttr == null)
                        throw new IllegalStateException("Unknown extension number for extension with name=" + nameAttr.getNodeValue());

                    int extNumber = extnumberAttr == null ? extensionNumber : Integer.parseInt(extnumberAttr.getNodeValue());

                    int offset = Integer.parseInt(offsetAttr.getNodeValue());
                    boolean negative = dirAttr != null && dirAttr.getNodeValue().equals("-");
                    int value = calculateExtensionValue(extNumber, offset, negative);

                    enumToExtend.addValue(new EnumType.Value(
                            nameAttr.getNodeValue(),
                            enumToExtend.getEnumValueName(nameAttr.getNodeValue()),
                            "" + value,
                            comment, null,
                            javaDocGenerator -> javaDocGenerator.addAtText(jdTag("addedByExtension"), extensionOrFeatureName)
                    ));
                }  else {
                    throw new IllegalStateException("Unknown Enum extension type. name=" + nameAttr.getNodeValue());
                }

            } else if (type instanceof BitMaskEnumType bitMaskEnumToExtend) {
                if(bitposAttr != null) {
                    bitMaskEnumToExtend.addValue(new BitMaskEnumType.Value(
                            nameAttr.getNodeValue(),
                            Integer.parseInt(bitposAttr.getNodeValue()),
                            null,
                            comment, null,
                            javaDocGenerator -> javaDocGenerator.addAtText(jdTag("addedByExtension"), extensionOrFeatureName)
                    ));
                    return;
                } else if(aliasAttr != null || valueAttr != null) {
                    bitMaskEnumToExtend.addValue(new BitMaskEnumType.Value(
                            nameAttr.getNodeValue(),
                            -1,
                            valueAttr == null ? aliasAttr.getNodeValue() + ".getValue()" : valueAttr.getNodeValue(),
                            comment, null,
                            javaDocGenerator -> javaDocGenerator.addAtText(jdTag("addedByExtension"), extensionOrFeatureName)
                    ));
                    return;
                } else {
                    throw new IllegalStateException("Unknown BitMaskEnum extension type. name=" + nameAttr.getNodeValue());
                }
            } else {
                throw new IllegalStateException("Trying to extend type " + type.getClass().getSimpleName());
            }

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
                    commentAttr == null ? null : commentAttr.getNodeValue(),
                    null
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
                            CTypes.POINTER,
                            VULKAN_PACKAGE
                    );
                } else {
                    n = new BasicType(
                            nameNode.getTextContent(),
                            CTypes.ofCType(aliasTypeNode.getTextContent()),
                            VULKAN_PACKAGE
                    );
                }
                addType(n);
            } else if(nameNode != null){
                // This might be a type node with an #ifdef. These have the <type>...</type> missing and the alias
                // must be extracted
                // Example (line 260):
                // <type category="basetype">
                // #ifdef ...
                // #else
                // typedef void* <name>MTLTexture_id</name>;
                // #endif
                // </type>

                Pattern pattern = Pattern.compile("^#ifdef __(?<define>\\w+)__(?<true>.+)#else(?<false>.+)#endif$", Pattern.DOTALL);
                Matcher matcher = pattern.matcher(typeNode.getTextContent());

                if(!matcher.find()) return;

                if(matcher.group("define").equals("OBJC")) {
                    // assume __OBJC__ is not defined
                    String typedef = matcher.group("false");
                    pattern = Pattern.compile("typedef (?<type>\\w+\\*?) (?<name>\\w+);");
                    matcher = pattern.matcher(typedef);

                    if(!matcher.find()) return;

                    String type = matcher.group("type");
                    String name = matcher.group("name");

                    if(!name.equals(nameNode.getTextContent()))
                        throw new IllegalStateException("matched name and name node does not match: " + typeNode.getTextContent());

                    BasicType n;
                    if(type.equals("void*")) {
                        n = new BasicType(
                                nameNode.getTextContent(),
                                CTypes.POINTER,
                                VULKAN_PACKAGE
                        );
                    } else {
                        n = new BasicType(
                                nameNode.getTextContent(),
                                CTypes.ofCType(type),
                                VULKAN_PACKAGE
                        );
                    }
                    addType(n);
                }

            }

        }
        else if(category.equals("bitmask")) {
            if(!typeNode.hasChildNodes()) {
                // Aliases have no children, but name and alias
                Node nameAttr = typeNode.getAttributes().getNamedItem("name");
                Node aliasAttr = typeNode.getAttributes().getNamedItem("alias");

                if(nameAttr == null || aliasAttr == null) return; // skip

                addType(new AliasOfBitMaskType(
                        nameAttr.getNodeValue(),
                        getPUType(aliasAttr.getNodeValue())
                ));

                return;
            }

            Node nameNode = findInChildren(typeNode, "name");
            Node aliasTypeNode = findInChildren(typeNode, "type");

            if(aliasTypeNode == null || nameNode == null) {
                throw new IllegalStateException("TypeNode or nameNode is null of bitmask: " + typeNode.getTextContent());
            }

            BitMaskType n = new BitMaskType(
                    this,
                    nameNode.getTextContent(),
                    types.get(aliasTypeNode.getTextContent())
            );

            addType(n);

        }
        else if(category.equals("handle")) {
            if(!typeNode.hasChildNodes())
                return; // Skip all Nodes that don't add any c-code
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

        }
        else if(category.equals("funcpointer")) {
            var f = new FunctionPointerType(this, typeNode);
            addType(f);

        }
        else if(category.equals("struct") || category.equals("union")) {
            boolean union = false;
            if(category.equals("union")) {
                union = true;
                System.out.println("START handle union");
            }
            else System.out.println("START handle struct");

            Node nameAttr = typeNode.getAttributes().getNamedItem("name");
            Node commentAttr = typeNode.getAttributes().getNamedItem("comment");
            Node returnedOnlyAttr = typeNode.getAttributes().getNamedItem("returnedonly");
            Node structExtendsAttr = typeNode.getAttributes().getNamedItem("structextends");

            if(nameAttr == null)
                throw new IllegalStateException("struct <type> without name attribute!");
            System.out.println("Name: " + nameAttr.getNodeValue());

            StructType n = new StructType(
                    this,
                    nameAttr.getNodeValue(),
                    returnedOnlyAttr == null ? null : returnedOnlyAttr.getNodeValue().equals("true"),
                    commentAttr == null ? null : commentAttr.getNodeValue(),
                    structExtendsAttr == null ? null : Arrays.stream(structExtendsAttr.getNodeValue().split(",")).map(this::getPUType).toList(),
                    union
            );

            addType(n);

            for (Node memberNode : iterableNode(typeNode)) {
                if(memberNode.getNodeType() == Node.TEXT_NODE || memberNode.getNodeName().equals("comment"))
                    continue;
                n.addMember(memberNode);
            }

            System.out.println("END handle struct/union");

        }
        else if(category.equals("enum")) {
            // Enum types are defined here before they are defined again later.
            // We can skip these here, but there are also aliases for enums defined,
            // which will not be defined later. We need to store these here.

            Node nameAttr = typeNode.getAttributes().getNamedItem("name");
            Node aliasAttr = typeNode.getAttributes().getNamedItem("alias");

            if(nameAttr == null || aliasAttr == null) return; // skip
            System.out.println("Adding GhostAlias '" + nameAttr.getNodeValue() + "' which refers to '" + aliasAttr.getNodeValue() + "'");
            addType(new GhostAliasType(
                    nameAttr.getNodeValue(),
                    getPUType(aliasAttr.getNodeValue())
            ));

        }

    }

    public void generate(@NotNull SourceGenerator generator) {
        this.generator = generator;

        iterateTypes((name, type) -> {
            type.generate(this, generator);
            return true;
        });

        var vkInstanceFile = generator.getJavaFile("de.linusdev.cvg4j.nat.vulkan.handles", "VkInstance");
        assert vkInstanceFile != null;
        commandsGenerator.generate(this, generator, vkInstanceFile);
        nativeFunctionsGenerator.generate(this, generator);
    }
}
