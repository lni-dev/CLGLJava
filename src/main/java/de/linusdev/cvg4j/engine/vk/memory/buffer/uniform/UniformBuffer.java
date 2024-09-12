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

import de.linusdev.cvg4j.engine.vk.descriptor.pool.ShaderBinding;
import de.linusdev.cvg4j.engine.vk.device.Device;
import de.linusdev.cvg4j.engine.vk.memory.buffer.BufferOutput;
import de.linusdev.cvg4j.engine.vk.memory.buffer.BufferStructInput;
import de.linusdev.cvg4j.nat.vulkan.bitmasks.enums.VkShaderStageFlagBits;
import de.linusdev.cvg4j.nat.vulkan.enums.VkDescriptorType;
import de.linusdev.cvg4j.nat.vulkan.enums.VkStructureType;
import de.linusdev.cvg4j.nat.vulkan.handles.VkDescriptorSet;
import de.linusdev.cvg4j.nat.vulkan.handles.VkInstance;
import de.linusdev.cvg4j.nat.vulkan.structs.VkDescriptorBufferInfo;
import de.linusdev.cvg4j.nat.vulkan.structs.VkDescriptorSetLayoutBinding;
import de.linusdev.cvg4j.nat.vulkan.structs.VkWriteDescriptorSet;
import de.linusdev.lutils.nat.array.NativeArray;
import de.linusdev.lutils.nat.memory.stack.Stack;
import de.linusdev.lutils.nat.struct.abstracts.Structure;
import org.jetbrains.annotations.NotNull;

public class UniformBuffer<S extends Structure> implements ShaderBinding {

    private final @NotNull VkInstance vkInstance;
    private final @NotNull Device device;

    private final @NotNull BufferStructInput<S>[] input;
    private final @NotNull BufferOutput[] output;

    private NativeArray<VkDescriptorSet> vkDescriptorSets;

    /*
     * Information contained in this class
     */
    private final int binding;

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
    }

    public void createDescriptorSetBinding(
            @NotNull VkDescriptorSetLayoutBinding binding
    ) {
        binding.binding.set(this.binding);
        binding.descriptorType.set(VkDescriptorType.UNIFORM_BUFFER);
        binding.descriptorCount.set(1);
        binding.stageFlags.set(VkShaderStageFlagBits.VK_SHADER_STAGE_VERTEX_BIT);
        binding.pImmutableSamplers.set(null);
    }

    @Override
    public void updateDescriptorSets(
            @NotNull Stack stack,
            @NotNull NativeArray<VkDescriptorSet> vkDescriptorSets,
            @NotNull NativeArray<VkWriteDescriptorSet> writes
    ) {
        this.vkDescriptorSets = vkDescriptorSets;
        int i = 0;
        for (VkDescriptorSet vkdescriptorSet : vkDescriptorSets) {
            VkDescriptorBufferInfo bufferInfo = stack.push(new VkDescriptorBufferInfo());
            bufferInfo.buffer.set(input[i].getVulkanBuffer().getVkBuffer());
            bufferInfo.offset.set(0);
            bufferInfo.range.set(input[i].getVulkanBuffer().getSize());

            VkWriteDescriptorSet writeDescriptorSet = writes.get(i);
            writeDescriptorSet.sType.set(VkStructureType.WRITE_DESCRIPTOR_SET);
            writeDescriptorSet.dstSet.set(vkdescriptorSet);
            writeDescriptorSet.dstBinding.set(binding);
            writeDescriptorSet.dstArrayElement.set(0);
            writeDescriptorSet.descriptorType.set(VkDescriptorType.UNIFORM_BUFFER);
            writeDescriptorSet.descriptorCount.set(1);
            writeDescriptorSet.pBufferInfo.set(bufferInfo);

            i++;
        }
    }

    @Override
    public void popUpdateDescriptorSets(@NotNull Stack stack, int count) {
        for (int i = 0; i < count; i++) {
            stack.pop(); // bufferInfo
        }
    }

    public @NotNull BufferStructInput<S> getInput(int index) {
        return input[index];
    }

    public @NotNull BufferOutput getOutput(int index) {
        return output[index];
    }

    public VkDescriptorSet getVkDescriptorSet(int index) {
        return vkDescriptorSets.get(index);
    }

    @Override
    public @NotNull VkDescriptorType descriptorType() {
        return VkDescriptorType.UNIFORM_BUFFER;
    }

    @Override
    public int descriptorCount() {
        return input.length;
    }
}
