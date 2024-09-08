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

package de.linusdev.cvg4j.engine.vk.command.pool;

import de.linusdev.cvg4j.engine.vk.device.Device;
import de.linusdev.cvg4j.nat.vulkan.handles.VkCommandPool;
import de.linusdev.cvg4j.nat.vulkan.handles.VkInstance;
import org.jetbrains.annotations.NotNull;

import static de.linusdev.lutils.nat.pointer.TypedPointer64.ref;
import static de.linusdev.lutils.nat.struct.abstracts.Structure.allocate;

public class CommandPool implements AutoCloseable {

    protected final @NotNull VkInstance vkInstance;
    protected final @NotNull Device device;

    /*
     * Managed by this class
     */
    protected final @NotNull VkCommandPool vkCommandPool;

    public CommandPool(@NotNull VkInstance vkInstance, @NotNull Device device) {
        this.vkInstance = vkInstance;
        this.device = device;

        this.vkCommandPool = allocate(new VkCommandPool());
    }

    @Override
    public void close() {
        vkInstance.vkDestroyCommandPool(device.getVkDevice(), vkCommandPool, ref(null));
    }
}
