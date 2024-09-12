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

package de.linusdev.cvg4j.engine.vk.descriptor.pool;

import de.linusdev.cvg4j.engine.vk.device.Device;
import de.linusdev.cvg4j.engine.vk.pipeline.RasterizationPipeline;
import de.linusdev.cvg4j.nat.vulkan.enums.VkDescriptorType;
import de.linusdev.cvg4j.nat.vulkan.enums.VkPipelineBindPoint;
import de.linusdev.cvg4j.nat.vulkan.enums.VkStructureType;
import de.linusdev.cvg4j.nat.vulkan.handles.*;
import de.linusdev.cvg4j.nat.vulkan.structs.VkDescriptorPoolCreateInfo;
import de.linusdev.cvg4j.nat.vulkan.structs.VkDescriptorPoolSize;
import de.linusdev.cvg4j.nat.vulkan.structs.VkDescriptorSetAllocateInfo;
import de.linusdev.llog.LLog;
import de.linusdev.llog.base.LogInstance;
import de.linusdev.llog.base.impl.StandardLogLevel;
import de.linusdev.lutils.nat.memory.stack.Stack;
import de.linusdev.lutils.nat.struct.array.StructureArray;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.Map;

import static de.linusdev.lutils.nat.pointer.Pointer64.refL;
import static de.linusdev.lutils.nat.pointer.TypedPointer64.ofArray;
import static de.linusdev.lutils.nat.pointer.TypedPointer64.ref;
import static de.linusdev.lutils.nat.struct.abstracts.Structure.allocate;

public class FixedSizeDescriptorPool implements AutoCloseable {

    private final static LogInstance LOG = LLog.getLogInstance();

    private final @NotNull VkInstance vkInstance;
    private final @NotNull Device device;

    private final @NotNull ArrayList<DescriptorSet> descriptorSets = new ArrayList<>();

    /*
     * Managed by this class
     */
    private final @NotNull VkDescriptorPool vkDescriptorPool;

    public FixedSizeDescriptorPool(@NotNull VkInstance vkInstance, @NotNull Device device) {
        this.vkInstance = vkInstance;
        this.device = device;

        this.vkDescriptorPool = allocate(new VkDescriptorPool());
    }

    public void add(@NotNull DescriptorSet set) {
        descriptorSets.add(set);
    }

    public void create(@NotNull Stack stack) {
        LOG.debug("Creating descriptor pool for " + descriptorSets.size() + " descriptor sets.");
        EnumMap<VkDescriptorType, Integer> sizes = new EnumMap<>(VkDescriptorType.class);

        int maxSets = 0;
        for (DescriptorSet set : descriptorSets) {
            maxSets += set.getSetCopiesCount();
            for (Map.Entry<VkDescriptorType, Integer> entry : set.getSizes().entrySet()) {
                sizes.compute(entry.getKey(), (type, count) -> (count == null ? 0 : count) + entry.getValue());
            }
        }

        StructureArray<VkDescriptorPoolSize> poolSizes = stack.pushArray(
                sizes.size(), VkDescriptorPoolSize.class, VkDescriptorPoolSize::new
        );

        int i = 0;
        for (Map.Entry<VkDescriptorType, Integer> entry : sizes.entrySet()) {
            VkDescriptorPoolSize poolSize = poolSizes.get(i++);
            poolSize.type.set(entry.getKey());
            poolSize.descriptorCount.set(entry.getValue());
        }



        VkDescriptorPoolCreateInfo poolCreateInfo = stack.push(new VkDescriptorPoolCreateInfo());
        poolCreateInfo.sType.set(VkStructureType.DESCRIPTOR_POOL_CREATE_INFO);
        poolCreateInfo.poolSizeCount.set(poolSizes.length());
        poolCreateInfo.pPoolSizes.set(refL(poolSizes));
        poolCreateInfo.maxSets.set(maxSets);

        LOG.log(StandardLogLevel.DATA, "VkDescriptorPoolCreateInfo: " + poolCreateInfo);
        LOG.log(StandardLogLevel.DATA, "StructureArray<VkDescriptorPoolSize> poolSizes: " + poolSizes);
        vkInstance.vkCreateDescriptorPool(device.getVkDevice(), ref(poolCreateInfo), ref(null), ref(vkDescriptorPool)).check();

        stack.pop(); // poolCreateInfo
        stack.pop(); // poolSizes

        VkDescriptorSetAllocateInfo descriptorSetAllocateInfo = stack.push(new VkDescriptorSetAllocateInfo());
        descriptorSetAllocateInfo.sType.set(VkStructureType.DESCRIPTOR_SET_ALLOCATE_INFO);
        descriptorSetAllocateInfo.descriptorPool.set(vkDescriptorPool);

        for (DescriptorSet set : descriptorSets) {
            set.allocateDescriptorSets(stack, descriptorSetAllocateInfo);
        }

        stack.pop(); // descriptorSetAllocateInfo

    }

    public int getLayoutCount() {
        return descriptorSets.size();
    }

    public void getLayouts(@NotNull StructureArray<VkDescriptorSetLayout> storeLayouts) {
        descriptorSets.sort(Comparator.comparingInt(DescriptorSet::getSetIndex));
        for (int i = 0; i < descriptorSets.size(); i++) {
            storeLayouts.get(i).set(descriptorSets.get(i).getVkDescriptorSetLayout());
        }
    }

    public void bindCommand(
            @NotNull Stack stack,
            @NotNull VkCommandBuffer commandBuffer,
            @NotNull RasterizationPipeline pipeline,
            VkDescriptorSet... sets
    ) {

        var setsArray = stack.pushArray(sets.length, VkDescriptorSet.class, VkDescriptorSet::new);

        for (int i = 0; i < sets.length; i++) {
            setsArray.get(i).set(sets[i]);
        }

        vkInstance.vkCmdBindDescriptorSets(
                commandBuffer,
                VkPipelineBindPoint.GRAPHICS,
                pipeline.getVkPipelineLayout(),
                0, setsArray.length(),
                ofArray(setsArray),
                0, ref(null)
        );

        stack.pop(); // setsArray

    }

    @Override
    public void close() {
        for (DescriptorSet set : descriptorSets) {
            set.close();
        }
        vkInstance.vkDestroyDescriptorPool(device.getVkDevice(), vkDescriptorPool, ref(null));
    }
}
