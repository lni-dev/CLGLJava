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

package de.linusdev.cvg4j.engine.vk.memory.buffer.uniform;

import de.linusdev.cvg4j.engine.vk.device.Device;
import de.linusdev.cvg4j.engine.vk.memory.buffer.BufferOutput;
import de.linusdev.cvg4j.engine.vk.memory.buffer.BufferStructInput;
import de.linusdev.cvg4j.engine.vk.pipeline.RasterizationPipeline;
import de.linusdev.cvg4j.nat.vulkan.bitmasks.enums.VkShaderStageFlagBits;
import de.linusdev.cvg4j.nat.vulkan.enums.VkDescriptorType;
import de.linusdev.cvg4j.nat.vulkan.enums.VkPipelineBindPoint;
import de.linusdev.cvg4j.nat.vulkan.enums.VkStructureType;
import de.linusdev.cvg4j.nat.vulkan.handles.*;
import de.linusdev.cvg4j.nat.vulkan.structs.*;
import de.linusdev.lutils.nat.memory.Stack;
import de.linusdev.lutils.nat.struct.abstracts.Structure;
import de.linusdev.lutils.nat.struct.array.StructureArray;
import org.jetbrains.annotations.NotNull;

import static de.linusdev.lutils.nat.pointer.Pointer64.refL;
import static de.linusdev.lutils.nat.pointer.TypedPointer64.ofArray;
import static de.linusdev.lutils.nat.pointer.TypedPointer64.ref;
import static de.linusdev.lutils.nat.struct.abstracts.Structure.allocate;

public class UniformBuffer<S extends Structure> implements AutoCloseable {

    private final @NotNull VkInstance vkInstance;
    private final @NotNull Device device;

    private final @NotNull BufferStructInput<S>[] input;
    private final @NotNull BufferOutput[] output;

    /*
     * Information contained in this class
     */
    private final int binding;

    /*
     * Manged by this class
     */
    private final @NotNull VkDescriptorSetLayout vkDescriptorSetLayout;
    private final @NotNull VkDescriptorPool vkDescriptorPool;
    private final @NotNull StructureArray<VkDescriptorSet> vkdescriptorSets;

    public UniformBuffer(
            @NotNull VkInstance vkInstance,
            @NotNull Device device,
            int binding,
            @NotNull BufferStructInput<S>[] input,
            @NotNull BufferOutput[] output
    ) {
        this.vkInstance = vkInstance;
        this.device = device;
        this.input = input;
        this.output = output;
        this.binding = binding;

        this.vkDescriptorSetLayout = allocate(new VkDescriptorSetLayout());
        this.vkDescriptorPool = allocate(new VkDescriptorPool());
        this.vkdescriptorSets = StructureArray.newAllocated(input.length, VkDescriptorSet.class, VkDescriptorSet::new);
    }

    public void createDescriptorSetLayout(@NotNull Stack stack) {
        VkDescriptorSetLayoutBinding binding = stack.push(new VkDescriptorSetLayoutBinding());
        binding.binding.set(this.binding);
        binding.descriptorType.set(VkDescriptorType.UNIFORM_BUFFER);
        binding.descriptorCount.set(1);
        binding.stageFlags.set(VkShaderStageFlagBits.VK_SHADER_STAGE_VERTEX_BIT);
        binding.pImmutableSamplers.set(null);

        VkDescriptorSetLayoutCreateInfo createInfo = stack.push(new VkDescriptorSetLayoutCreateInfo());
        createInfo.sType.set(VkStructureType.DESCRIPTOR_SET_LAYOUT_CREATE_INFO);
        createInfo.bindingCount.set(1);
        createInfo.pBindings.set(binding);

        vkInstance.vkCreateDescriptorSetLayout(device.getVkDevice(), ref(createInfo), ref(null), ref(vkDescriptorSetLayout)).check();

        stack.pop(); // createInfo
        stack.pop(); // binding

        createDescriptorPool(stack);

    }

    public @NotNull VkDescriptorSetLayout getVkDescriptorSetLayout() {
        return vkDescriptorSetLayout;
    }

    public void createDescriptorPool(@NotNull Stack stack) {
        VkDescriptorPoolSize poolSize = stack.push(new VkDescriptorPoolSize());
        poolSize.type.set(VkDescriptorType.UNIFORM_BUFFER);
        poolSize.descriptorCount.set(input.length);

        VkDescriptorPoolCreateInfo poolCreateInfo = stack.push(new VkDescriptorPoolCreateInfo());
        poolCreateInfo.sType.set(VkStructureType.DESCRIPTOR_POOL_CREATE_INFO);
        poolCreateInfo.poolSizeCount.set(1);
        poolCreateInfo.pPoolSizes.set(poolSize);
        poolCreateInfo.maxSets.set(input.length);

        vkInstance.vkCreateDescriptorPool(device.getVkDevice(), ref(poolCreateInfo), ref(null), ref(vkDescriptorPool)).check();

        stack.pop(); // poolCreateInfo
        stack.pop(); // poolSize

        var layoutArray = stack.pushArray(input.length, VkDescriptorSetLayout.class, VkDescriptorSetLayout::new);
        for (VkDescriptorSetLayout l : layoutArray) {
            l.set(vkDescriptorSetLayout);
        }

        VkDescriptorSetAllocateInfo descriptorSetAllocateInfo = stack.push(new VkDescriptorSetAllocateInfo());
        descriptorSetAllocateInfo.sType.set(VkStructureType.DESCRIPTOR_SET_ALLOCATE_INFO);
        descriptorSetAllocateInfo.descriptorPool.set(vkDescriptorPool);
        descriptorSetAllocateInfo.descriptorSetCount.set(input.length);
        descriptorSetAllocateInfo.pSetLayouts.set(refL(layoutArray));

        vkInstance.vkAllocateDescriptorSets(device.getVkDevice(), ref(descriptorSetAllocateInfo), ofArray(vkdescriptorSets)).check();

        stack.pop(); // descriptorSetAllocateInfo
        stack.pop(); // layoutArray

        int i = 0;
        for (VkDescriptorSet vkdescriptorSet : vkdescriptorSets) {
            VkDescriptorBufferInfo bufferInfo = stack.push(new VkDescriptorBufferInfo());
            bufferInfo.buffer.set(input[i].getVulkanBuffer().getVkBuffer());
            bufferInfo.offset.set(0);
            bufferInfo.range.set(input[i].getVulkanBuffer().getSize());

            VkWriteDescriptorSet writeDescriptorSet = stack.push(new VkWriteDescriptorSet());
            writeDescriptorSet.sType.set(VkStructureType.WRITE_DESCRIPTOR_SET);
            writeDescriptorSet.dstSet.set(vkdescriptorSet);
            writeDescriptorSet.dstBinding.set(binding);
            writeDescriptorSet.dstArrayElement.set(0);
            writeDescriptorSet.descriptorType.set(VkDescriptorType.UNIFORM_BUFFER);
            writeDescriptorSet.descriptorCount.set(1);
            writeDescriptorSet.pBufferInfo.set(bufferInfo);

            vkInstance.vkUpdateDescriptorSets(device.getVkDevice(), 1, ref(writeDescriptorSet), 0, ref(null));

            stack.pop(); // writeDescriptorSet
            stack.pop(); // bufferInfo
            i++;
        }

    }

    public void bindCommand(
            @NotNull Stack stack,
            @NotNull VkCommandBuffer commandBuffer,
            @NotNull RasterizationPipeline pipeline,
            int index
    ) {
        vkInstance.vkCmdBindDescriptorSets(
                commandBuffer,
                VkPipelineBindPoint.GRAPHICS,
                pipeline.getVkPipelineLayout(),
                0, 1,
                ref(vkdescriptorSets.getOrCreate(index)),
                0, ref(null)
        );

    }

    public @NotNull BufferStructInput<S> getInput(int index) {
        return input[index];
    }

    public @NotNull BufferOutput getOutput(int index) {
        return output[index];
    }

    @Override
    public void close() {
        vkInstance.vkDestroyDescriptorPool(device.getVkDevice(), vkDescriptorPool, ref(null));
        vkInstance.vkDestroyDescriptorSetLayout(device.getVkDevice(), vkDescriptorSetLayout, ref(null));
    }
}
