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

package de.linusdev.ljgel.nat.vulkan.debug.callback;

import de.linusdev.ljgel.nat.vulkan.bitmasks.enums.VkDebugUtilsMessageSeverityFlagBitsEXT;
import de.linusdev.ljgel.nat.vulkan.bitmasks.enums.VkDebugUtilsMessageTypeFlagBitsEXT;
import de.linusdev.ljgel.nat.vulkan.structs.VkDebugUtilsMessengerCallbackDataEXT;
import de.linusdev.lutils.bitfield.IntBitfield;
import de.linusdev.lutils.nat.enums.EnumValue32;
import org.jetbrains.annotations.NotNull;

public interface DebugListener {
    void debug(
            @NotNull EnumValue32<VkDebugUtilsMessageSeverityFlagBitsEXT> messageSeverity,
            @NotNull IntBitfield<VkDebugUtilsMessageTypeFlagBitsEXT> messageType,
            @NotNull VkDebugUtilsMessengerCallbackDataEXT callbackData
    );
}