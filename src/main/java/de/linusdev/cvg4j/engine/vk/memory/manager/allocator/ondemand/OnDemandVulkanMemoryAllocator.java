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

package de.linusdev.cvg4j.engine.vk.memory.manager.allocator.ondemand;

import de.linusdev.cvg4j.engine.exception.EngineException;
import de.linusdev.cvg4j.engine.vk.device.Device;
import de.linusdev.cvg4j.engine.vk.memory.buffer.BufferArrayInput;
import de.linusdev.cvg4j.engine.vk.memory.buffer.BufferOutput;
import de.linusdev.cvg4j.engine.vk.memory.buffer.BufferStructInput;
import de.linusdev.cvg4j.engine.vk.memory.buffer.index.IndexBuffer;
import de.linusdev.cvg4j.engine.vk.memory.buffer.uniform.UniformBuffer;
import de.linusdev.cvg4j.engine.vk.memory.buffer.vertex.VertexBuffer;
import de.linusdev.cvg4j.engine.vk.memory.buffer.vertex.VertexElement;
import de.linusdev.cvg4j.engine.vk.memory.image.ImageOutput;
import de.linusdev.cvg4j.engine.vk.memory.image.sampler.Sampler2D;
import de.linusdev.cvg4j.engine.vk.memory.manager.VulkanMemoryBoundObject;
import de.linusdev.cvg4j.engine.vk.memory.manager.allocator.VulkanMemoryAllocator;
import de.linusdev.cvg4j.engine.vk.memory.manager.objects.buffer.VulkanBuffer;
import de.linusdev.cvg4j.engine.vk.memory.manager.objects.image.VulkanImage;
import de.linusdev.cvg4j.engine.vk.memory.manager.objects.image.VulkanSamplerImage;
import de.linusdev.cvg4j.engine.vk.memory.manager.ondemand.OnDemandMemoryTypeManager;
import de.linusdev.cvg4j.nat.vulkan.bitmasks.enums.*;
import de.linusdev.cvg4j.nat.vulkan.enums.VkFormat;
import de.linusdev.cvg4j.nat.vulkan.enums.VkImageLayout;
import de.linusdev.cvg4j.nat.vulkan.enums.VkImageTiling;
import de.linusdev.cvg4j.nat.vulkan.enums.VkVertexInputRate;
import de.linusdev.cvg4j.nat.vulkan.handles.VkCommandBuffer;
import de.linusdev.llog.LLog;
import de.linusdev.llog.base.LogInstance;
import de.linusdev.lutils.bitfield.IntBitfield;
import de.linusdev.lutils.bitfield.IntBitfieldImpl;
import de.linusdev.lutils.image.ImageSize;
import de.linusdev.lutils.image.PixelFormat;
import de.linusdev.lutils.image.buffer.BBInt32Image;
import de.linusdev.lutils.nat.enums.EnumValue32;
import de.linusdev.lutils.nat.memory.stack.Stack;
import de.linusdev.lutils.nat.struct.UStructSupplier;
import de.linusdev.lutils.nat.struct.abstracts.Structure;
import de.linusdev.lutils.nat.struct.info.ArrayInfo;
import de.linusdev.lutils.nat.struct.info.StructureInfo;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class OnDemandVulkanMemoryAllocator extends VulkanMemoryAllocator {

    private final static @NotNull LogInstance LOG = LLog.getLogInstance();

    OnDemandMemoryTypeManager[] typeManagers = new OnDemandMemoryTypeManager[32];

    public OnDemandVulkanMemoryAllocator(@NotNull Device device, @NotNull String debugName) {
        super(device, debugName);
    }

    public <V extends Structure> VertexBuffer<V> createVertexBuffer(
            @NotNull Stack stack,
            @NotNull String debugName,
            @NotNull Class<?> elementClass,
            @NotNull UStructSupplier<V> elementCreator,
            @NotNull List<VertexElement> attributeDescriptors,
            int vertexCount,
            int binding,
            @NotNull VkVertexInputRate vertexInputRate
    ) throws EngineException {

        BufferArrayInput<V> vertexInput = new BufferArrayInput<>(vertexCount, elementClass, elementCreator);
        ArrayInfo info = vertexInput.getBackedArrayInfo();
        VulkanBuffer vulkanBuffer = new VulkanBuffer(device, debugName, info.getRequiredSize(),
                new IntBitfieldImpl<>(VkBufferUsageFlagBits.VERTEX_BUFFER)
        ).create(stack);
        vertexInput.setVulkanBuffer(vulkanBuffer);

        add(stack, vulkanBuffer,
                VkMemoryPropertyFlagBits.HOST_VISIBLE, // staging buffer must be mapped
                VkMemoryPropertyFlagBits.HOST_COHERENT // automatically detect changes
        );

        BufferOutput vertexOutput = new BufferOutput(vulkanBuffer);

        return new VertexBuffer<>(vkInstance, vertexInput, vertexOutput, binding, vertexInputRate, attributeDescriptors);
    }

    public <V extends Structure> VertexBuffer<V> createStagedVertexBuffer(
            @NotNull Stack stack,
            @NotNull String debugName,
            @NotNull Class<?> elementClass,
            @NotNull UStructSupplier<V> elementCreator,
            @NotNull List<VertexElement> attributeDescriptors,
            int vertexCount,
            int binding,
            @NotNull VkVertexInputRate vertexInputRate
    ) throws EngineException {
        BufferArrayInput<V> vertexInput = new BufferArrayInput<>(vertexCount, elementClass, elementCreator);
        ArrayInfo info = vertexInput.getBackedArrayInfo();
        vertexInput.setVulkanBuffer(addStagingBuffer(stack, debugName + "-in", info.getRequiredSize()));


        VulkanBuffer vertexBuffer = new VulkanBuffer(
                device, debugName + "-out", info.getRequiredSize(), new IntBitfieldImpl<>(
                VkBufferUsageFlagBits.TRANSFER_DST,
                VkBufferUsageFlagBits.VERTEX_BUFFER
        )).create(stack);
        add(stack, vertexBuffer, VkMemoryPropertyFlagBits.DEVICE_LOCAL);
        BufferOutput vertexOutput = new BufferOutput(vertexBuffer);


        return new VertexBuffer<>(vkInstance, vertexInput, vertexOutput, binding, vertexInputRate, attributeDescriptors);
    }

    public <V extends Structure> IndexBuffer<V> createStagedInstanceBuffer(
            @NotNull Stack stack,
            @NotNull String debugName,
            @NotNull Class<?> elementClass,
            @NotNull UStructSupplier<V> elementCreator,
            int instanceCount
    ) throws EngineException {

        BufferArrayInput<V> vertexInput = new BufferArrayInput<>(instanceCount, elementClass, elementCreator);
        ArrayInfo info = vertexInput.getBackedArrayInfo();
        vertexInput.setVulkanBuffer(addStagingBuffer(stack, debugName + "-in", info.getRequiredSize()));


        VulkanBuffer vertexBuffer = new VulkanBuffer(device, debugName + "-out", info.getRequiredSize(), new IntBitfieldImpl<>(
                VkBufferUsageFlagBits.TRANSFER_DST,
                VkBufferUsageFlagBits.INDEX_BUFFER
        )).create(stack);
        add(stack, vertexBuffer, VkMemoryPropertyFlagBits.DEVICE_LOCAL);
        BufferOutput vertexOutput = new BufferOutput(vertexBuffer);


        return new IndexBuffer<>(vkInstance, vertexInput, vertexOutput);
    }

    public <S extends Structure> UniformBuffer<S> createUniformBuffer(
            @NotNull Stack stack,
            @NotNull String debugName,
            @NotNull UStructSupplier<S> structCreator,
            int dupeCount,
            int binding
    ) throws EngineException {

        BufferStructInput<S>[] inputs = new BufferStructInput[dupeCount];
        BufferOutput[] outputs = new BufferOutput[dupeCount];

        for (int i = 0; i < dupeCount; i++) {
            S struct = structCreator.supply();

            inputs[i] = new BufferStructInput<>(struct);
            VulkanBuffer vulkanBuffer = new VulkanBuffer(device, debugName + "[" + i + "]", struct.getRequiredSize(), new IntBitfieldImpl<>(
                    VkBufferUsageFlagBits.UNIFORM_BUFFER
            )).create(stack);
            inputs[i].setVulkanBuffer(vulkanBuffer);


            add(stack, vulkanBuffer,
                    VkMemoryPropertyFlagBits.HOST_VISIBLE, // staging buffer must be mapped
                    VkMemoryPropertyFlagBits.HOST_COHERENT // automatically detect changes
            );
            outputs[i] = new BufferOutput(vulkanBuffer);
        }

        return new UniformBuffer<>(vkInstance, device, binding, inputs, outputs);
    }

    /**
     *
     * @param generateMipLevels whether to generate mip level images.
     * @param autoGenerateMipLevels whether the generated mip level images are later going to be automatically filed using {@link VulkanImage#generateMipmaps(Stack, VkCommandBuffer, VkImageLayout)}.
     */
    public Sampler2D<BBInt32Image> createStagedSampler(
            @NotNull Stack stack,
            @NotNull String debugName,
            int binding,
            @NotNull ImageSize size,
            @NotNull VkImageLayout layout,
            boolean generateMipLevels,
            boolean autoGenerateMipLevels
    ) throws EngineException {

        BufferStructInput<BBInt32Image> input = new BufferStructInput<>(BBInt32Image.newAllocatable(size, PixelFormat.R8G8B8A8_SRGB));
        StructureInfo info = input.getBackedStruct().getInfo();
        input.setVulkanBuffer(addStagingBuffer(stack, debugName, info.getRequiredSize()));

        IntBitfield<VkImageUsageFlagBits> usage = new IntBitfieldImpl<>(
                VkImageUsageFlagBits.TRANSFER_DST,
                VkImageUsageFlagBits.SAMPLED
        );

        if(generateMipLevels && autoGenerateMipLevels) usage.set(VkImageUsageFlagBits.TRANSFER_SRC);

        VulkanSamplerImage image = new VulkanSamplerImage(
                device, debugName + "-out", info.getRequiredSize(), size,
                usage,
                new IntBitfieldImpl<>(VkImageAspectFlagBits.COLOR),
                VkImageTiling.OPTIMAL,
                VkFormat.R8G8B8A8_SRGB,
                generateMipLevels
        ).create(stack);
        add(stack, image, VkMemoryPropertyFlagBits.DEVICE_LOCAL);
        ImageOutput output = new ImageOutput(image);


        // TODO: It is common use that the staging buffer is only used once and can be freed after that.
        return new Sampler2D<>(vkInstance, device, input, output, binding, size, layout);
    }

    public @NotNull VulkanImage createDeviceLocalVulkanImage(
            @NotNull Stack stack,
            @NotNull String debugName,
            @NotNull ImageSize size,
            @NotNull VkFormat format,
            @NotNull VkImageTiling tiling,
            @NotNull IntBitfield<VkImageUsageFlagBits> usage,
            @NotNull IntBitfield<VkImageAspectFlagBits> viewAspectMask,
            boolean generateMipLevels,
            @NotNull EnumValue32<VkSampleCountFlagBits> sampleCount
    ) throws EngineException {
        VulkanImage image = new VulkanImage(device, debugName, -1, size, usage, viewAspectMask, tiling, format,
                generateMipLevels,sampleCount).create(stack);
        add(stack, image, VkMemoryPropertyFlagBits.DEVICE_LOCAL);
        return image;
    }

    public void allocate(@NotNull Stack stack) {
        LOG.debug("Start allocating memory for allocator '" + debugName + "'.");
        for (OnDemandMemoryTypeManager typeManager : typeManagers) {
            if(typeManager != null) typeManager.allocate(stack);
        }
    }

    private void add(@NotNull Stack stack, @NotNull VulkanMemoryBoundObject object, VkMemoryPropertyFlagBits... flags) throws EngineException {
        int memoryTypeIndex = object.calculateMemoryTypeIndex(stack, new IntBitfieldImpl<>(flags));
        if(typeManagers[memoryTypeIndex] == null) {
            typeManagers[memoryTypeIndex] = new OnDemandMemoryTypeManager(
                    stack, vkInstance, device,
                    debugName + "[" + memoryTypeIndex + "]",
                    memoryTypeIndex
            );
        }

        typeManagers[memoryTypeIndex].addObject(object);
    }

    private @NotNull VulkanBuffer addStagingBuffer(@NotNull Stack stack, @NotNull String debugName, int size) throws EngineException {
        VulkanBuffer stagingBuffer = new VulkanBuffer(device, debugName + "-in", size, new IntBitfieldImpl<>(
                VkBufferUsageFlagBits.TRANSFER_SRC
        )).create(stack);

        add(stack, stagingBuffer,
                VkMemoryPropertyFlagBits.HOST_VISIBLE, // staging buffer must be mapped
                VkMemoryPropertyFlagBits.HOST_COHERENT
        );
        return stagingBuffer;
    }

    @Override
    public void close() {
        for (OnDemandMemoryTypeManager typeManager : typeManagers) {
            if(typeManager != null) typeManager.close();
        }
    }
}
