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

package de.linusdev.cvg4j.engine.vk.device;

import de.linusdev.cvg4j.engine.exception.EngineException;
import de.linusdev.cvg4j.engine.vk.extension.VulkanExtension;
import de.linusdev.cvg4j.nat.vulkan.bitmasks.enums.VkMemoryPropertyFlagBits;
import de.linusdev.cvg4j.nat.vulkan.enums.VkStructureType;
import de.linusdev.cvg4j.nat.vulkan.handles.VkDevice;
import de.linusdev.cvg4j.nat.vulkan.handles.VkInstance;
import de.linusdev.cvg4j.nat.vulkan.handles.VkPhysicalDevice;
import de.linusdev.cvg4j.nat.vulkan.handles.VkQueue;
import de.linusdev.cvg4j.nat.vulkan.structs.*;
import de.linusdev.cvg4j.nat.vulkan.utils.VulkanUtils;
import de.linusdev.lutils.bitfield.IntBitfield;
import de.linusdev.lutils.bitfield.IntBitfieldImpl;
import de.linusdev.lutils.math.vector.buffer.floatn.BBFloat1;
import de.linusdev.lutils.nat.memory.stack.Stack;
import de.linusdev.lutils.nat.pointer.BBTypedPointer64;
import de.linusdev.lutils.nat.string.NullTerminatedUTF8String;
import de.linusdev.lutils.nat.struct.array.StructureArray;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static de.linusdev.lutils.nat.pointer.Pointer64.refL;
import static de.linusdev.lutils.nat.pointer.TypedPointer64.ref;
import static de.linusdev.lutils.nat.struct.abstracts.Structure.allocate;

public class Device implements AutoCloseable {

    public static @NotNull Device create(
            @NotNull Stack stack,
            @NotNull VkInstance vkInstance,
            @NotNull VkPhysicalDevice vkPhysicalDevice,
            int graphicsQueueIndex,
            int presentationQueueIndex,
            @NotNull List<@NotNull VulkanExtension> requiredDeviceExtensions,
            @NotNull List<@NotNull String> requiredVulkanLayers
    ) {
        Device device = new Device(vkInstance, vkPhysicalDevice, graphicsQueueIndex, presentationQueueIndex);

        boolean sameQueueIndices = graphicsQueueIndex == presentationQueueIndex;

        // Queue Create Infos
        StructureArray<VkDeviceQueueCreateInfo> queueCreateInfos = stack.pushArray(
                sameQueueIndices ? 1 : 2,
                VkDeviceQueueCreateInfo.class,
                VkDeviceQueueCreateInfo::new
        );

        VkDeviceQueueCreateInfo queueCreateInfo;
        BBFloat1 prio = stack.push(BBFloat1.newAllocatable(null));
        prio.set(1.0f);

        // Graphics Queue create info
        queueCreateInfo = queueCreateInfos.get(0);
        queueCreateInfo.sType.set(VkStructureType.DEVICE_QUEUE_CREATE_INFO);
        queueCreateInfo.queueFamilyIndex.set(graphicsQueueIndex);
        queueCreateInfo.queueCount.set(1);
        queueCreateInfo.pQueuePriorities.set(prio);

        if(!sameQueueIndices) {
            // Presentation Queue create info
            queueCreateInfo = queueCreateInfos.get(1);
            queueCreateInfo.sType.set(VkStructureType.DEVICE_QUEUE_CREATE_INFO);
            queueCreateInfo.queueFamilyIndex.set(presentationQueueIndex);
            queueCreateInfo.queueCount.set(1);
            queueCreateInfo.pQueuePriorities.set(prio);
        }

        // Required Device extensions
        StructureArray<BBTypedPointer64<NullTerminatedUTF8String>> reqDevExtNatArray = stack.pushArray(requiredDeviceExtensions.size(), BBTypedPointer64.class, BBTypedPointer64::newUnallocated1);

        int i = 0;
        for (VulkanExtension ext : requiredDeviceExtensions)
            reqDevExtNatArray.get(i++).set(stack.pushString(ext.extensionName()));

        // Vulkan Layers
        @Nullable StructureArray<BBTypedPointer64<NullTerminatedUTF8String>> enabledLayersNatArray = null;

        if(!requiredVulkanLayers.isEmpty()) {
            enabledLayersNatArray = stack.pushArray(requiredVulkanLayers.size(), BBTypedPointer64.class, BBTypedPointer64::newUnallocated1);
            i = 0;
            for (String ext : requiredVulkanLayers)
                enabledLayersNatArray.get(i++).set(stack.pushString(ext));
        }

        // Device features
        VkPhysicalDeviceFeatures features = stack.push(new VkPhysicalDeviceFeatures());

        //TODO: add this as game requirements and to the gpu selector!
        features.geometryShader.set(VulkanUtils.booleanToVkBool32(true));
        features.samplerAnisotropy.set(VulkanUtils.booleanToVkBool32(true));

        // Device Create Info
        VkDeviceCreateInfo deviceCreateInfo = stack.push(new VkDeviceCreateInfo());
        deviceCreateInfo.allocate();
        deviceCreateInfo.sType.set(VkStructureType.DEVICE_CREATE_INFO);
        deviceCreateInfo.queueCreateInfoCount.set(queueCreateInfos.length());
        deviceCreateInfo.pQueueCreateInfos.set(queueCreateInfos.getPointer());
        deviceCreateInfo.pEnabledFeatures.set(features);
        deviceCreateInfo.enabledExtensionCount.set(reqDevExtNatArray.length());
        deviceCreateInfo.ppEnabledExtensionNames.set(refL(reqDevExtNatArray));
        deviceCreateInfo.enabledLayerCount.set(enabledLayersNatArray == null ? 0 :enabledLayersNatArray.length());
        deviceCreateInfo.ppEnabledLayerNames.set(refL(enabledLayersNatArray));

        // Create device
        vkInstance.vkCreateDevice(vkPhysicalDevice, ref(deviceCreateInfo), ref(null), ref(device.vkDevice)).check();

        // Pop stuff we don't need anymore
        stack.pop(); // deviceCreateInfo
        stack.pop(); // features

        if(enabledLayersNatArray != null) {
            for (i = 0; i < enabledLayersNatArray.length(); i++)
                stack.pop(); // string in enabledLayersNatArray
            stack.pop(); // enabledLayersNatArray
        }

        for (i = 0; i < reqDevExtNatArray.length(); i++)
            stack.pop(); // string in reqDevExtNatArray
        stack.pop(); // reqDevExtNatArray

        stack.pop(); // prio
        stack.pop(); // queueCreateInfos

        // Create queues
        vkInstance.vkGetDeviceQueue(device.vkDevice, graphicsQueueIndex, 0, ref(device.graphicsQueue));
        vkInstance.vkGetDeviceQueue(device.vkDevice, presentationQueueIndex, 0, ref(device.presentationQueue));

        return device;
    }

    private final @NotNull VkInstance vkInstance;

    /*
     * Managed by this class
     */
    protected final @NotNull VkPhysicalDevice vkPhysicalDevice;
    protected final @NotNull VkDevice vkDevice;
    protected final @NotNull VkQueue graphicsQueue;
    protected final @NotNull VkQueue presentationQueue;

    /*
     * Information stored in Device
     */
    private final int graphicsQueueIndex;
    private final int presentationQueueIndex;

    protected final @NotNull VkPhysicalDeviceProperties deviceProperties;
    protected final @NotNull VkPhysicalDeviceFeatures deviceFeatures;

    protected Device(
            @NotNull VkInstance vkInstance,
            @NotNull VkPhysicalDevice vkPhysicalDevice,
            int graphicsQueueIndex,
            int presentationQueueIndex
    ) {
        this.vkInstance = vkInstance;
        this.vkPhysicalDevice = allocate(new VkPhysicalDevice());
        this.vkDevice = allocate(new VkDevice());
        this.graphicsQueue = allocate(new VkQueue());
        this.presentationQueue = allocate(new VkQueue());
        this.deviceProperties = allocate(new VkPhysicalDeviceProperties());
        this.deviceFeatures = allocate(new VkPhysicalDeviceFeatures());

        this.graphicsQueueIndex = graphicsQueueIndex;
        this.presentationQueueIndex = presentationQueueIndex;

        // Store vkPhysicalDevice
        this.vkPhysicalDevice.set(vkPhysicalDevice.get());

        // Get physical device properties.
        vkInstance.vkGetPhysicalDeviceProperties(vkPhysicalDevice, ref(deviceProperties));
        vkInstance.vkGetPhysicalDeviceFeatures(vkPhysicalDevice, ref(deviceFeatures));
    }

    /**
     * {@link VkPhysicalDeviceFeatures} supported by this device.
     */
    public @NotNull VkPhysicalDeviceFeatures getPhysicalDeviceFeatures() {
        return deviceFeatures;
    }

    /**
     * {@link VkPhysicalDeviceProperties} containing the physical device properties of this device.
     */
    public @NotNull VkPhysicalDeviceProperties getPhysicalDeviceProperties() {
        return deviceProperties;
    }

    /**
     * Find a suitable memory type.
     * @param stack {@link Stack}
     * @param allowedTypes Each bit of this int represents an index of memoryTypes. If Bit N is set, index N is allowed
     *                     to be returned by this function.
     * @param requiredPropertyFlags required {@link VkMemoryPropertyFlagBits} for the memory
     * @return index of memory type, that is in {@code allowedTypes} and has all property flags given by {@code requiredPropertyFlags}.
     */
    public int findMemoryType(
            @NotNull Stack stack,
            int allowedTypes,
            @NotNull IntBitfield<VkMemoryPropertyFlagBits> requiredPropertyFlags
    ) throws EngineException {
        VkPhysicalDeviceMemoryProperties memProps = stack.push(new VkPhysicalDeviceMemoryProperties());
        vkInstance.vkGetPhysicalDeviceMemoryProperties(vkPhysicalDevice, ref(memProps));

        for (int i = 0; i < memProps.memoryTypeCount.get(); i++) {
            if((allowedTypes & (1 << i)) > 0 && memProps.memoryTypes.get(i).propertyFlags.isSet(requiredPropertyFlags)) {
                stack.pop(); // memProps
                return i;
            }
        }

        stack.pop(); // memProps

        throw new EngineException("No suitable memory type found");
    }

    public @NotNull IntBitfield<VkMemoryPropertyFlagBits> getMemoryPropFlagsOf(
            @NotNull Stack stack, int memoryTypeIndex
    ) {
        VkPhysicalDeviceMemoryProperties memProps = stack.push(new VkPhysicalDeviceMemoryProperties());
        vkInstance.vkGetPhysicalDeviceMemoryProperties(vkPhysicalDevice, ref(memProps));

        IntBitfieldImpl<VkMemoryPropertyFlagBits> ret = new IntBitfieldImpl<>(
                memProps.memoryTypes.get(memoryTypeIndex).propertyFlags.getValue()
        );

        stack.pop(); // memProps

        return ret;
    }

    public @NotNull VkDevice getVkDevice() {
        return vkDevice;
    }

    public @NotNull VkInstance getVkInstance() {
        return vkInstance;
    }

    public int getGraphicsQueueIndex() {
        return graphicsQueueIndex;
    }

    public int getPresentationQueueIndex() {
        return presentationQueueIndex;
    }

    public @NotNull VkQueue getGraphicsQueue() {
        return graphicsQueue;
    }

    public @NotNull VkQueue getPresentationQueue() {
        return presentationQueue;
    }

    public @NotNull VkPhysicalDevice getVkPhysicalDevice() {
        return vkPhysicalDevice;
    }

    @Override
    public void close() {
        vkInstance.vkDestroyDevice(vkDevice, ref(null));
    }
}
