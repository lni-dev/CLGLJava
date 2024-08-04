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

package de.linusdev.cvg4j.vulkan;

import de.linusdev.cvg4j.engine.Engine;
import de.linusdev.cvg4j.nat.NativeUtils;
import de.linusdev.cvg4j.nat.glad.GladInitException;
import de.linusdev.cvg4j.nat.glfw3.GLFW;
import de.linusdev.cvg4j.nat.glfw3.GLFWValues;
import de.linusdev.cvg4j.nat.glfw3.custom.RenderAPI;
import de.linusdev.cvg4j.nat.glfw3.exceptions.GLFWException;
import de.linusdev.cvg4j.nat.glfw3.objects.GLFWWindow;
import de.linusdev.cvg4j.nat.vulkan.VulkanUtils;
import de.linusdev.cvg4j.nat.vulkan.enums.VkStructureType;
import de.linusdev.cvg4j.nat.vulkan.handles.VkInstance;
import de.linusdev.cvg4j.nat.vulkan.structs.VkApplicationInfo;
import de.linusdev.cvg4j.nat.vulkan.structs.VkInstanceCreateInfo;
import de.linusdev.lutils.nat.pointer.BBTypedPointer64;
import de.linusdev.lutils.nat.string.NullTerminatedUTF8String;
import de.linusdev.lutils.nat.struct.annos.SVWrapper;
import de.linusdev.lutils.nat.struct.array.StructureArray;
import de.linusdev.lutils.nat.struct.utils.BufferUtils;
import org.junit.jupiter.api.Test;

public class VulkanTest {


    @Test
    void test() throws GLFWException, GladInitException {
        Engine.StaticSetup.setup();
        GLFWWindow window = new GLFWWindow(RenderAPI.VULKAN, null);

        if(GLFW.glfwVulkanSupported() != GLFWValues.GLFW_TRUE) {
            System.err.println("Cannot run Vulkan test: Vulkan is not supported on this machine.");
            return;
        }

        var array = GLFW.glfwGetRequiredInstanceExtensions();

        for (BBTypedPointer64<NullTerminatedUTF8String> pointer : array) {
            System.out.println(BufferUtils.readString(NativeUtils.getBufferFromPointer(pointer.get(), 0), false));
        }

        //Validation layer strings
        StructureArray<BBTypedPointer64<NullTerminatedUTF8String>> vLayerStrings = StructureArray.newAllocated(
                false,
                SVWrapper.of(1, BBTypedPointer64.class),
                null,
                BBTypedPointer64::newUnallocated1
        );
        vLayerStrings.getOrCreate(0).set(NullTerminatedUTF8String.ofString("VK_LAYER_KHRONOS_validation"));

        // VkApplicationInfo
        VkApplicationInfo vkApplicationInfo = new VkApplicationInfo();
        vkApplicationInfo.allocate();

        vkApplicationInfo.sType.set(VkStructureType.VK_STRUCTURE_TYPE_APPLICATION_INFO);
        vkApplicationInfo.pNext.set(0);
        vkApplicationInfo.pApplicationName.set(NullTerminatedUTF8String.ofString("Test Application"));
        vkApplicationInfo.applicationVersion.set(VulkanUtils.makeVersion(1, 0, 0));
        vkApplicationInfo.pEngineName.set(NullTerminatedUTF8String.ofString("CVG4J"));
        vkApplicationInfo.engineVersion.set(VulkanUtils.makeVersion(0, 1, 0));
        vkApplicationInfo.apiVersion.set(VulkanUtils.VK_API_VERSION_1_3);

        // VkInstanceCreateInfo
        VkInstanceCreateInfo vkInstanceCreateInfo = new VkInstanceCreateInfo();
        vkInstanceCreateInfo.allocate();

        vkInstanceCreateInfo.sType.set(VkStructureType.VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO);
        vkInstanceCreateInfo.pNext.set(0);
        vkInstanceCreateInfo.pApplicationInfo.set(vkApplicationInfo);
        vkInstanceCreateInfo.enabledExtensionCount.set(array.length());
        vkInstanceCreateInfo.ppEnabledExtensionNames.set(array.getPointer());
        vkInstanceCreateInfo.enabledLayerCount.set(vLayerStrings.length());
        vkInstanceCreateInfo.ppEnabledLayerNames.set(vLayerStrings.getPointer());
        vkInstanceCreateInfo.flags.set(0);


        VkInstance vkInstance = new VkInstance();
        vkInstance.allocate();
        var result = VulkanUtils.vkCreateInstance(vkInstanceCreateInfo, null, vkInstance);
        vkInstance.initMethodPointers();


        System.out.println(result.getAsVkResult());
        System.out.println(vkInstance.get());

    }

}
