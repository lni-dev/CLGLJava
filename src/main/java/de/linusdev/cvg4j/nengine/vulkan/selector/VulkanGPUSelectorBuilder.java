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

package de.linusdev.cvg4j.nengine.vulkan.selector;

import de.linusdev.cvg4j.nat.vulkan.constants.APIConstants;
import de.linusdev.cvg4j.nat.vulkan.enums.VkColorSpaceKHR;
import de.linusdev.cvg4j.nat.vulkan.enums.VkFormat;
import de.linusdev.cvg4j.nat.vulkan.enums.VkPhysicalDeviceType;
import de.linusdev.cvg4j.nat.vulkan.enums.VkPresentModeKHR;
import de.linusdev.cvg4j.nat.vulkan.structs.VkExtensionProperties;
import de.linusdev.cvg4j.nengine.vulkan.extension.VulkanExtension;
import de.linusdev.cvg4j.nengine.vulkan.selector.present.mode.PresentModeSelector;
import de.linusdev.cvg4j.nengine.vulkan.selector.priority.Priorities;
import de.linusdev.cvg4j.nengine.vulkan.selector.priority.Priority;
import de.linusdev.cvg4j.nengine.vulkan.selector.surface.format.SurfaceFormatSelector;
import de.linusdev.lutils.nat.enums.NativeEnumMember32;
import de.linusdev.lutils.nat.enums.NativeEnumValue32;
import de.linusdev.lutils.nat.struct.abstracts.Structure;
import de.linusdev.lutils.nat.struct.array.StructureArray;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToIntBiFunction;
import java.util.function.ToIntFunction;

public class VulkanGPUSelectorBuilder {

    final @NotNull ModifiableVulkanGPUSelector selector = new ModifiableVulkanGPUSelector();

    public static @NotNull VulkanGPUSelectorBuilder getDefault() {
        return new VulkanGPUSelectorBuilder()
                // Prioritize discrete gpus; Lower priority for integrated gpus; Disallow cpus
                .deviceType().equals(VkPhysicalDeviceType.DISCRETE_GPU).thenAdd(Priorities.HIGH)
                .deviceType().equals(VkPhysicalDeviceType.INTEGRATED_GPU).thenSubtract(Priorities.LOW)
                .deviceType().equals(VkPhysicalDeviceType.CPU).thenUnsupported()

                // Require the swap-chain extension
                .extensions().not().suffices(VulkanExtension.of(APIConstants.VK_KHR_swapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME)).thenUnsupported()

                // It's good to have at least two images available (for swapping them)
                .custom(info -> info.surfacesCaps().maxImageCount.get() == 0 || info.surfacesCaps().maxImageCount.get() >= 2).thenAdd(Priorities.HIGH)

                // Ideally we want the format B8G8R8A8_SRGB with the color space SRGB_NONLINEAR_KHR,
                // but we also allow any other
                .surfaceFormat(SurfaceFormatSelector.builder()
                        .add(null, null, Priorities.DO_NOT_CARE) // allow any
                        .add(VkFormat.B8G8R8A8_SRGB, VkColorSpaceKHR.SRGB_NONLINEAR_KHR, Priorities.LOW) // prioritize this one
                        .build()
                ).thenAdd()

                // Present mode MAILBOX_KHR is best!
                // but allow any other ...
                .presentMode(PresentModeSelector.builder()
                        .add(null, Priorities.DO_NOT_CARE) // allow any
                        .add(VkPresentModeKHR.MAILBOX_KHR, Priorities.MEDIUM) // prioritize this one
                        .add(VkPresentModeKHR.FIFO_KHR, Priorities.VERY_LOW) // still better than the others
                        .build()
                ).thenAdd();
    }

    public EnumModBuilder<VkPhysicalDeviceType> deviceType() {
        return new EnumModBuilder<>(this, info -> info.props().deviceType);
    }

    public StructArrayModBuilder<VkExtensionProperties, VulkanExtension> extensions() {
        return new StructArrayModBuilder<>(
                this,
                GpuInfo::extensionCount,
                GpuInfo::extensions,
                (availableExt, requiredExt) -> {
                    if(availableExt.extensionName.get().equals(requiredExt.extensionName())) {
                        if(availableExt.specVersion.get() > requiredExt.version()) return 1;
                        if(availableExt.specVersion.get() == requiredExt.version()) return 0;
                    }

                    return -1;
                }
        );
    }

    public VariablePriorityApplicator presentMode(@NotNull PresentModeSelector selector) {
        return new VariablePriorityApplicator(this, info -> selector.select(info.presentModeCount(), info.presentModes()).result2());
    }

    public PriorityApplicator custom(@NotNull Predicate<GpuInfo> tester) {
        return new PriorityApplicator(false, this, tester);
    }

    public VariablePriorityApplicator surfaceFormat(@NotNull SurfaceFormatSelector selector) {
        return new VariablePriorityApplicator(this, info -> selector.select(info.surfaceFormatCount(), info.surfaceFormats()).result2());
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

        private final @NotNull ToIntFunction<GpuInfo> getArrayLength;
        private final @NotNull Function<GpuInfo, StructureArray<T>> getArray;
        private final @NotNull ToIntBiFunction<T, I> comparer;

        protected StructArrayModBuilder(
                @NotNull VulkanGPUSelectorBuilder builder,
                @NotNull ToIntFunction<GpuInfo> getArrayLength,
                @NotNull Function<GpuInfo, StructureArray<T>> getArray,
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
                            if(comparer.applyAsInt(array.getOrCreate(i), that) == 0)
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
                            if(comparer.applyAsInt(array.getOrCreate(i), that) >= 0)
                                return true;
                        }
                        return false;
                    }
            );
        }
    }

    public static class EnumModBuilder<T extends NativeEnumMember32> extends NegatableModBuilderBase<EnumModBuilder<T>> {

        private final @NotNull Function<GpuInfo, NativeEnumValue32<T>> member;

        protected EnumModBuilder(
                @NotNull VulkanGPUSelectorBuilder builder,
                @NotNull Function<GpuInfo, NativeEnumValue32<T>> member
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

        private final @NotNull Predicate<GpuInfo> tester;

        protected PriorityApplicator(
                boolean negate,
                @NotNull VulkanGPUSelectorBuilder builder,
                @NotNull Predicate<GpuInfo> tester
        ) {
            super(builder);
            this.tester = negate ? tester.negate() : tester;
        }

        public @NotNull VulkanGPUSelectorBuilder then(
                @NotNull PriorityModifierType type,
                @NotNull Priority value
        ) {
            builder.selector.add(new PriorityModifier.Impl(type, value, tester));
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

        private final @NotNull Function<GpuInfo, Priority> tester;

        protected VariablePriorityApplicator(
                @NotNull VulkanGPUSelectorBuilder builder,
                @NotNull Function<GpuInfo, Priority> tester
        ) {
            super(builder);
            this.tester = tester;
        }

        public @NotNull VulkanGPUSelectorBuilder then(
                @NotNull PriorityModifierType type
        ) {
            builder.selector.add(new PriorityModifier.VariableImpl(type, tester));
            return builder;
        }

        public @NotNull VulkanGPUSelectorBuilder thenAdd() {
            return then(PriorityModifierType.ADD);
        }

    }

    public @NotNull VulkanGPUSelector build() {
        return selector;
    }

}
