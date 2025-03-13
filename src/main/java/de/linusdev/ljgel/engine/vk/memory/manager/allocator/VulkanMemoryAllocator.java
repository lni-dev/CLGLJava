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

package de.linusdev.ljgel.engine.vk.memory.manager.allocator;

import de.linusdev.ljgel.engine.vk.device.Device;
import de.linusdev.ljgel.nat.vulkan.handles.VkInstance;
import org.jetbrains.annotations.NotNull;

public abstract class VulkanMemoryAllocator implements AutoCloseable {

    protected final @NotNull VkInstance vkInstance;
    protected final @NotNull Device device;
    protected final @NotNull String debugName;

    protected VulkanMemoryAllocator(@NotNull Device device, @NotNull String debugName) {
        this.vkInstance = device.getVkInstance();
        this.device = device;
        this.debugName = debugName;
    }

    @Override
    public abstract void close();
}