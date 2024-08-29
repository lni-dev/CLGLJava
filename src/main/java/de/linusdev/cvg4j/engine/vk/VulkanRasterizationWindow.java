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

package de.linusdev.cvg4j.engine.vk;

import de.linusdev.cvg4j.nat.glfw3.custom.GLFWWindowHints;
import de.linusdev.cvg4j.nat.glfw3.custom.RenderAPI;
import de.linusdev.cvg4j.nat.glfw3.exceptions.GLFWException;
import de.linusdev.cvg4j.nat.glfw3.objects.GLFWWindow;
import de.linusdev.cvg4j.nat.vulkan.ReturnedVkResult;
import de.linusdev.cvg4j.nat.vulkan.handles.VkInstance;
import de.linusdev.cvg4j.nat.vulkan.handles.VkSurfaceKHR;
import de.linusdev.cvg4j.nat.vulkan.structs.VkAllocationCallbacks;
import de.linusdev.lutils.nat.memory.DirectMemoryStack64;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static de.linusdev.cvg4j.nat.glfw3.GLFW.glfwCreateWindowSurface;
import static de.linusdev.lutils.nat.pointer.Pointer64.refL;
import static de.linusdev.lutils.nat.pointer.TypedPointer64.ref;
import static de.linusdev.lutils.nat.struct.abstracts.Structure.allocate;

public class VulkanRasterizationWindow extends GLFWWindow {

    private final @NotNull VkInstance vkInstance;
    private final @NotNull DirectMemoryStack64 stack;

    private final @NotNull VkSurfaceKHR vkSurface;

    public VulkanRasterizationWindow(
            @Nullable GLFWWindowHints hints,
            @NotNull VkInstance vkInstance,
            @NotNull DirectMemoryStack64 stack
    ) throws GLFWException {
        super(RenderAPI.VULKAN, hints);
        this.vkInstance = vkInstance;
        this.stack = stack;
        this.vkSurface = allocate(new VkSurfaceKHR());

        createVkWindowSurface(null);

    }

    public ReturnedVkResult createVkWindowSurface(
            @Nullable VkAllocationCallbacks allocationCallbacks
    ) {
        return new ReturnedVkResult(glfwCreateWindowSurface(
                vkInstance.get(),
                pointer,
                refL(allocationCallbacks),
                refL(vkSurface)
        ));
    }

    public @NotNull VkSurfaceKHR getVkSurface() {
        return vkSurface;
    }

    @Override
    public void close() {
        super.close();
        vkInstance.vkDestroySurfaceKHR(vkSurface, ref(null));
    }
}
