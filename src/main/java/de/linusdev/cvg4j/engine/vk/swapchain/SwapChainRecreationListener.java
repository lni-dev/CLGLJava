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

package de.linusdev.cvg4j.engine.vk.swapchain;

import de.linusdev.cvg4j.engine.vk.device.Extend2D;
import de.linusdev.cvg4j.nat.vulkan.enums.VkColorSpaceKHR;
import de.linusdev.lutils.nat.enums.EnumValue32;
import de.linusdev.lutils.nat.memory.Stack;
import org.jetbrains.annotations.NotNull;

/**
 * Call order is
 * <ol>
 *     <li>{@link #swapChainRecreated(Stack) swapChainRecreated()}</li>
 *     <li>{@link #swapChainExtendChanged(Stack, Extend2D) swapChainExtendChanged()}</li>
 * </ol>
 */
public interface SwapChainRecreationListener {
    
    default void swapChainRecreated(@NotNull Stack stack) { }
    
    default void swapChainExtendChanged(@NotNull Stack stack, @NotNull Extend2D newExtend) { }

    default void swapChainColorSpaceChanged(@NotNull Stack stack, @NotNull EnumValue32<VkColorSpaceKHR> newColorSpace) { }
    
}
