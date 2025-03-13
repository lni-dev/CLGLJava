/*
 * Copyright (c) 2024-2025 Linus Andera
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

package de.linusdev.ljgel.engine.vk.descriptor.pool;

import de.linusdev.ljgel.engine.vk.device.Device;
import de.linusdev.ljgel.nat.vulkan.enums.VkDescriptorType;
import de.linusdev.ljgel.nat.vulkan.enums.VkStructureType;
import de.linusdev.ljgel.nat.vulkan.handles.VkDescriptorSet;
import de.linusdev.ljgel.nat.vulkan.handles.VkDescriptorSetLayout;
import de.linusdev.ljgel.nat.vulkan.handles.VkInstance;
import de.linusdev.ljgel.nat.vulkan.structs.VkDescriptorSetAllocateInfo;
import de.linusdev.ljgel.nat.vulkan.structs.VkDescriptorSetLayoutBinding;
import de.linusdev.ljgel.nat.vulkan.structs.VkDescriptorSetLayoutCreateInfo;
import de.linusdev.ljgel.nat.vulkan.structs.VkWriteDescriptorSet;
import de.linusdev.lutils.nat.memory.stack.Stack;
import de.linusdev.lutils.nat.struct.array.StructureArray;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;

import static de.linusdev.lutils.nat.pointer.Pointer64.refL;
import static de.linusdev.lutils.nat.pointer.TypedPointer64.ofArray;
import static de.linusdev.lutils.nat.pointer.TypedPointer64.ref;
import static de.linusdev.lutils.nat.struct.abstracts.Structure.allocate;

public class DescriptorSet implements AutoCloseable {

    private final @NotNull VkInstance vkInstance;
    private final @NotNull Device device;

    private final @NotNull ShaderBinding @NotNull [] bindings;

    /**
     * Information stored in this class
     */
    private final int set;
    private final int setCopiesCount;
    private final EnumMap<VkDescriptorType, Integer> sizes = new EnumMap<>(VkDescriptorType.class);

    /*
     * Managed by this class
     */
    private final @NotNull VkDescriptorSetLayout vkDescriptorSetLayout;
    private final @NotNull StructureArray<VkDescriptorSet> vkDescriptorSets;

    public DescriptorSet(
            @NotNull VkInstance vkInstance,
            @NotNull Device device,
            int set,
            @NotNull ShaderBinding @NotNull ... bindings
    ) {
        this.vkInstance = vkInstance;
        this.device = device;
        this.set = set;
        this.bindings = bindings;

        this.setCopiesCount = bindings[0].descriptorCount();

        for (ShaderBinding binding : bindings)
            sizes.compute(binding.descriptorType(), (type, count) -> (count == null ? 0 : count) + binding.descriptorCount());

        this.vkDescriptorSetLayout = allocate(new VkDescriptorSetLayout());
        this.vkDescriptorSets = StructureArray.newAllocated(setCopiesCount, VkDescriptorSet.class, VkDescriptorSet::new);
    }

    public void allocateDescriptorSets(
            @NotNull Stack stack,
            @NotNull VkDescriptorSetAllocateInfo allocInfo
    ) {
        createLayout(stack);

        var layoutArray = stack.pushArray(setCopiesCount, VkDescriptorSetLayout.class, VkDescriptorSetLayout::new);

        for (VkDescriptorSetLayout layout : layoutArray)
            layout.set(vkDescriptorSetLayout);

        allocInfo.descriptorSetCount.set(setCopiesCount);
        allocInfo.pSetLayouts.set(refL(layoutArray));

        vkInstance.vkAllocateDescriptorSets(device.getVkDevice(), ref(allocInfo), ofArray(vkDescriptorSets)).check();

        stack.pop(); // layoutArray

        StructureArray<VkWriteDescriptorSet> writes = stack.pushArray(setCopiesCount * bindings.length,
                VkWriteDescriptorSet.class, VkWriteDescriptorSet::new
        );

        int i = 0;
        for (ShaderBinding binding : bindings)
            binding.updateDescriptorSets(stack, vkDescriptorSets, writes.getView(i++ * setCopiesCount, setCopiesCount));

        vkInstance.vkUpdateDescriptorSets(device.getVkDevice(), writes.length(), ofArray(writes), 0, ref(null));

        for (ShaderBinding binding : bindings)
            binding.popUpdateDescriptorSets(stack, vkDescriptorSets.length());

        stack.pop(); // writes
    }

    public void createLayout(@NotNull Stack stack) {
        StructureArray<VkDescriptorSetLayoutBinding> bindings = stack.pushArray(
                this.bindings.length, VkDescriptorSetLayoutBinding.class, VkDescriptorSetLayoutBinding::new
        );

        for (int i = 0; i < this.bindings.length; i++)
            this.bindings[i].createDescriptorSetBinding(bindings.get(i++));

        VkDescriptorSetLayoutCreateInfo createInfo = stack.push(new VkDescriptorSetLayoutCreateInfo());
        createInfo.sType.set(VkStructureType.DESCRIPTOR_SET_LAYOUT_CREATE_INFO);
        createInfo.bindingCount.set(1);
        createInfo.pBindings.set(refL(bindings));

        vkInstance.vkCreateDescriptorSetLayout(device.getVkDevice(), ref(createInfo), ref(null), ref(vkDescriptorSetLayout)).check();

        stack.pop(); // createInfo
        stack.pop(); // bindings
    }

    public EnumMap<VkDescriptorType, Integer> getSizes() {
        return sizes;
    }

    public int getSetIndex() {
        return set;
    }

    public @NotNull VkDescriptorSetLayout getVkDescriptorSetLayout() {
        return vkDescriptorSetLayout;
    }

    public int getSetCopiesCount() {
        return setCopiesCount;
    }

    @Override
    public void close() {
        vkInstance.vkDestroyDescriptorSetLayout(device.getVkDevice(), vkDescriptorSetLayout, ref(null));
    }
}
