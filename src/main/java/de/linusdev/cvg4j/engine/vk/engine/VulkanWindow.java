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

package de.linusdev.cvg4j.engine.vk.engine;

import de.linusdev.cvg4j.engine.vk.instance.Instance;
import de.linusdev.cvg4j.nat.glfw3.custom.GLFWWindowHints;
import de.linusdev.cvg4j.nat.glfw3.custom.RenderAPI;
import de.linusdev.cvg4j.nat.glfw3.exceptions.GLFWException;
import de.linusdev.cvg4j.nat.glfw3.objects.GLFWWindow;
import de.linusdev.cvg4j.nat.vulkan.ReturnedVkResult;
import de.linusdev.cvg4j.nat.vulkan.handles.VkInstance;
import de.linusdev.cvg4j.nat.vulkan.handles.VkSurfaceKHR;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static de.linusdev.cvg4j.nat.glfw3.GLFW.glfwCreateWindowSurface;
import static de.linusdev.cvg4j.nat.glfw3.GLFWValues.GLFW_DONT_CARE;
import static de.linusdev.lutils.nat.pointer.Pointer64.refL;
import static de.linusdev.lutils.nat.pointer.TypedPointer64.ref;
import static de.linusdev.lutils.nat.struct.abstracts.Structure.allocate;

public class VulkanWindow extends GLFWWindow {

    private final @NotNull VkInstance vkInstance;
    private final @NotNull VkSurfaceKHR vkSurface;

    public VulkanWindow(@NotNull Instance instance, @Nullable GLFWWindowHints hints) throws GLFWException {
        super(RenderAPI.VULKAN, hints);
        this.vkInstance = instance.getVkInstance();

        this.vkSurface = allocate(new VkSurfaceKHR());
        createVkWindowSurface().check();

        // Make sure, that the window always has a surface to present on, otherwise vulkan will throw a validation error
        setWindowSizeLimits(1, 1, GLFW_DONT_CARE, GLFW_DONT_CARE);
    }

    protected @NotNull ReturnedVkResult createVkWindowSurface() {
        return new ReturnedVkResult(glfwCreateWindowSurface(
                vkInstance.get(),
                pointer,
                refL(null),
                refL(vkSurface)
        ));
    }

    public @NotNull VkSurfaceKHR getVkSurface() {
        return vkSurface;
    }

    @Override
    public void close() {
        vkInstance.vkDestroySurfaceKHR(vkSurface, ref(null));
        super.close();
    }
}
