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

package de.linusdev.cvg4j.nengine.vulkan;

import de.linusdev.cvg4j.nat.glfw3.custom.GLFWWindowHints;
import de.linusdev.cvg4j.nat.glfw3.custom.RenderAPI;
import de.linusdev.cvg4j.nat.glfw3.exceptions.GLFWException;
import de.linusdev.cvg4j.nat.glfw3.objects.GLFWWindow;
import de.linusdev.cvg4j.nengine.Engine;
import de.linusdev.lutils.nat.memory.DirectMemoryStack64;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class VulkanRasterizationWindow extends GLFWWindow {

    private final @NotNull Engine<?> engine;
    private final @NotNull DirectMemoryStack64 stack;

    public VulkanRasterizationWindow(
            @Nullable GLFWWindowHints hints,
            @NotNull Engine<?> engine,
            @NotNull DirectMemoryStack64 stack
    ) throws GLFWException {
        super(RenderAPI.VULKAN, hints);
        this.engine = engine;
        this.stack = stack;

        // Create Vulkan Instance


    }
}
