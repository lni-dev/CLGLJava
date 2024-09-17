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

package de.linusdev.cvg4j.engine.vk.selector.gpu;

import de.linusdev.cvg4j.engine.vk.device.GPUInfo;
import de.linusdev.cvg4j.engine.vk.extension.VulkanExtension;
import de.linusdev.cvg4j.engine.vk.selector.PriorityModifier;
import de.linusdev.cvg4j.engine.vk.selector.PriorityModifierType;
import de.linusdev.cvg4j.engine.vk.selector.present.mode.PresentModeSelector;
import de.linusdev.cvg4j.engine.vk.selector.priority.Priorities;
import de.linusdev.cvg4j.engine.vk.selector.priority.Priority;
import de.linusdev.cvg4j.engine.vk.selector.queue.family.QueueFamilySelector;
import de.linusdev.cvg4j.engine.vk.selector.surface.format.SurfaceFormatSelector;
import de.linusdev.cvg4j.nat.vulkan.enums.VkPhysicalDeviceType;
import de.linusdev.cvg4j.nat.vulkan.structs.VkExtensionProperties;
import de.linusdev.lutils.nat.enums.NativeEnumMember32;
import de.linusdev.lutils.nat.enums.NativeEnumValue32;
import de.linusdev.lutils.nat.struct.abstracts.Structure;
import de.linusdev.lutils.nat.struct.array.StructureArray;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.*;

// TODO: documentation
public class VulkanGPUSelectorBuilder {

    private final @NotNull Map<PriorityModifierType, List<PriorityModifier>> modifiers = new EnumMap<>(PriorityModifierType.class);
    private @NotNull Priority maxPriority = Priority.of(Integer.MAX_VALUE);
    private @NotNull Priority startPriority = Priority.of(1000);

    VulkanGPUSelectorBuilder() {
        for (PriorityModifierType type : PriorityModifierType.values()) {
            modifiers.put(type, new ArrayList<>());
        }
    }

    private void add(@NotNull PriorityModifier mod) {
        modifiers.get(mod.type()).add(mod);
    }

    public PriorityApplicator deviceNameEquals(@NotNull String name) {
        return new PriorityApplicator(false, this, info -> info.props.deviceName.get().equals(name));
    }

    public VulkanGPUSelectorBuilder setMaxPriority(@NotNull Priority priority) {
        maxPriority = priority;
        return this;
    }

    public VulkanGPUSelectorBuilder setStartPriority(@NotNull Priority priority) {
        startPriority = priority;
        return this;
    }

    public EnumModBuilder<VkPhysicalDeviceType> deviceType() {
        return new EnumModBuilder<>(this, info -> info.props.deviceType);
    }

    public StructArrayModBuilder<VkExtensionProperties, VulkanExtension> extensions() {
        return new StructArrayModBuilder<>(
                this,
                info -> info.extensionCount,
                info -> info.extensions,
                (availableExt, requiredExt) -> {
                    if(availableExt.extensionName.get().equals(requiredExt.extensionName())) {
                        if(availableExt.specVersion.get() > requiredExt.version()) return 1;
                        if(availableExt.specVersion.get() == requiredExt.version()) return 0;
                    }

                    return -1;
                }
        );
    }

    public VariablePriorityApplicator queueFamilies(
            @NotNull QueueFamilySelector selector,
            @NotNull BiFunction<Priority, Priority, Priority> combiner
    ) {
        return new VariablePriorityApplicator(this,
                info -> combiner.apply(
                        selector.selectGraphicsQueue(info.queueFamilyInfoList).result2(),
                        selector.selectPresentationQueue(info.queueFamilyInfoList).result2()
                )
        );
    }

    public VariablePriorityApplicator presentMode(@NotNull PresentModeSelector selector) {
        return new VariablePriorityApplicator(this, info -> selector.select(info.surfaceInfo.presentModeCount, info.surfaceInfo.presentModes).result2());
    }

    public PriorityApplicator custom(@NotNull Predicate<GPUInfo> tester) {
        return new PriorityApplicator(false, this, tester);
    }

    public VariablePriorityApplicator surfaceFormat(@NotNull SurfaceFormatSelector selector) {
        return new VariablePriorityApplicator(this, info -> selector.select(info.surfaceInfo.surfaceFormatCount, info.surfaceInfo.surfaceFormats).result2());
    }

    public static abstract class ModBuilderBase {
        protected final @NotNull VulkanGPUSelectorBuilder builder;

        protected ModBuilderBase(@NotNull VulkanGPUSelectorBuilder builder) {
            this.builder = builder;
        }
    }

    public static abstract class NegatableModBuilderBase<SELF extends NegatableModBuilderBase<SELF>> {
        protected final @NotNull VulkanGPUSelectorBuilder builder;
        protected boolean negate = false;

        protected NegatableModBuilderBase(@NotNull VulkanGPUSelectorBuilder builder) {
            this.builder = builder;
        }

        public @NotNull SELF not() {
            negate = !negate;
            //noinspection unchecked
            return (SELF) this;
        }
    }

    public static class StructArrayModBuilder<T extends Structure, I> extends NegatableModBuilderBase<StructArrayModBuilder<T, I>>{

        private final @NotNull ToIntFunction<GPUInfo> getArrayLength;
        private final @NotNull Function<GPUInfo, StructureArray<T>> getArray;
        private final @NotNull ToIntBiFunction<T, I> comparer;

        protected StructArrayModBuilder(
                @NotNull VulkanGPUSelectorBuilder builder,
                @NotNull ToIntFunction<GPUInfo> getArrayLength,
                @NotNull Function<GPUInfo, StructureArray<T>> getArray,
                @NotNull ToIntBiFunction<T, I> comparer
        ) {
            super(builder);
            this.getArrayLength = getArrayLength;
            this.getArray = getArray;
            this.comparer = comparer;
        }


        public PriorityApplicator contains(@NotNull I that) {
            return new PriorityApplicator(
                    negate,
                    builder,
                    info -> {
                        var array = getArray.apply(info);
                        for (int i = 0; i < getArrayLength.applyAsInt(info); i++) {
                            if(comparer.applyAsInt(array.get(i), that) == 0)
                                return true;
                        }
                        return false;
                    }
            );
        }

        public PriorityApplicator suffices(@NotNull I that) {
            return new PriorityApplicator(
                    negate,
                    builder,
                    info -> {
                        var array = getArray.apply(info);
                        for (int i = 0; i < getArrayLength.applyAsInt(info); i++) {
                            if(comparer.applyAsInt(array.get(i), that) >= 0)
                                return true;
                        }
                        return false;
                    }
            );
        }

        public PriorityApplicator sufficesAll(@NotNull List<I> that) {
            return new PriorityApplicator(
                    negate,
                    builder,
                    info -> {
                        var array = getArray.apply(info);
                        for (I req : that) {
                            for (int i = 0; i < getArrayLength.applyAsInt(info); i++) {
                                if(comparer.applyAsInt(array.get(i), req) < 0)
                                    return false;
                            }
                        }

                        return true;
                    }
            );
        }
    }

    public static class EnumModBuilder<T extends NativeEnumMember32> extends NegatableModBuilderBase<EnumModBuilder<T>> {

        private final @NotNull Function<GPUInfo, NativeEnumValue32<T>> member;

        protected EnumModBuilder(
                @NotNull VulkanGPUSelectorBuilder builder,
                @NotNull Function<GPUInfo, NativeEnumValue32<T>> member
        ) {
            super(builder);
            this.member = member;
        }

        public PriorityApplicator equals(@NotNull T that) {
            return new PriorityApplicator(
                    negate,
                    builder,
                    props -> member.apply(props).get() == that.getValue()
            );
        }

        public PriorityApplicator notEquals(@NotNull T that) {
            return new PriorityApplicator(
                    negate,
                    builder,
                    info -> member.apply(info).get() != that.getValue()
            );
        }

    }

    public static class PriorityApplicator extends ModBuilderBase {

        private final @NotNull Predicate<GPUInfo> tester;

        protected PriorityApplicator(
                boolean negate,
                @NotNull VulkanGPUSelectorBuilder builder,
                @NotNull Predicate<GPUInfo> tester
        ) {
            super(builder);
            this.tester = negate ? tester.negate() : tester;
        }

        public @NotNull VulkanGPUSelectorBuilder then(
                @NotNull PriorityModifierType type,
                @NotNull Priority value
        ) {
            builder.add(new PriorityModifier.Impl(type, value, tester));
            return builder;
        }

        public @NotNull VulkanGPUSelectorBuilder thenUnsupported() {
            return then(PriorityModifierType.MIN, Priorities.UNSUPPORTED);
        }

        public @NotNull VulkanGPUSelectorBuilder thenAdd(@NotNull Priority value) {
            return then(PriorityModifierType.ADD, value);
        }

        public @NotNull VulkanGPUSelectorBuilder thenSubtract(@NotNull Priority value) {
            return then(PriorityModifierType.SUBTRACT, value);
        }

    }

    public static class VariablePriorityApplicator extends ModBuilderBase {

        private final @NotNull Function<GPUInfo, Priority> tester;

        protected VariablePriorityApplicator(
                @NotNull VulkanGPUSelectorBuilder builder,
                @NotNull Function<GPUInfo, Priority> tester
        ) {
            super(builder);
            this.tester = tester;
        }

        public @NotNull VulkanGPUSelectorBuilder then(
                @NotNull PriorityModifierType type
        ) {
            builder.add(new PriorityModifier.VariableImpl(type, tester));
            return builder;
        }

        public @NotNull VulkanGPUSelectorBuilder thenUnsupportedIfNegativeAndAdd() {
            thenAdd();
            builder.add(new PriorityModifier.Impl(
                    PriorityModifierType.MIN,
                    Priorities.UNSUPPORTED,
                    info -> tester.apply(info).isNegative()
            ));
            return builder;
        }

        public @NotNull VulkanGPUSelectorBuilder thenAdd() {
            return then(PriorityModifierType.ADD);
        }

    }

    public @NotNull VulkanGPUSelector build() {
        return new BasicVulkanGPUSelector(modifiers, maxPriority, startPriority);
    }

}
