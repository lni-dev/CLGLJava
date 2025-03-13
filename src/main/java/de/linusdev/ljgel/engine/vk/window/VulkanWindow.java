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

package de.linusdev.ljgel.engine.vk.window;

import de.linusdev.ljgel.engine.vk.instance.Instance;
import de.linusdev.ljgel.engine.window.WindowThread;
import de.linusdev.ljgel.nat.glfw3.custom.GLFWWindowHints;
import de.linusdev.ljgel.nat.glfw3.custom.RenderAPI;
import de.linusdev.ljgel.nat.glfw3.exceptions.GLFWException;
import de.linusdev.ljgel.nat.glfw3.objects.GLFWWindow;
import de.linusdev.ljgel.nat.vulkan.ReturnedVkResult;
import de.linusdev.ljgel.nat.vulkan.handles.VkInstance;
import de.linusdev.ljgel.nat.vulkan.handles.VkSurfaceKHR;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static de.linusdev.ljgel.nat.glfw3.GLFW.glfwCreateWindowSurface;
import static de.linusdev.ljgel.nat.glfw3.GLFW.glfwWaitEvents;
import static de.linusdev.ljgel.nat.glfw3.GLFWValues.GLFW_DONT_CARE;
import static de.linusdev.lutils.nat.pointer.Pointer64.refL;
import static de.linusdev.lutils.nat.pointer.TypedPointer64.ref;
import static de.linusdev.lutils.nat.struct.abstracts.Structure.allocate;

public class VulkanWindow extends GLFWWindow {

    private final @NotNull VkInstance vkInstance;
    private final @NotNull VkSurfaceKHR vkSurface;
    private final @NotNull WindowThread<VulkanWindow> windowThread;

    public VulkanWindow(
            @NotNull Instance instance, @Nullable GLFWWindowHints hints,
            @NotNull WindowThread<VulkanWindow> windowThread
    ) throws GLFWException {
        super(RenderAPI.VULKAN, hints);
        this.vkInstance = instance.getVkInstance();
        this.windowThread = windowThread;

        this.vkSurface = allocate(new VkSurfaceKHR());
        createVkWindowSurface().check();

        // Make sure, that the window always has a surface to present on, otherwise vulkan will throw a validation error
        setWindowSizeLimits(1, 1, GLFW_DONT_CARE, GLFW_DONT_CARE);
    }

    @Override
    protected void perFrameOperations() {
        // Wait events instead of just polling. This makes sure, that this thread isn't running all the time
        // with nothing to do. A side effect of this is, that every time, this threads has to do something unrelated to
        // window events glfwPostEmptyEvent() must be called to wake this thread.
        glfwWaitEvents();
    }

    protected @NotNull ReturnedVkResult createVkWindowSurface() {
        return new ReturnedVkResult(glfwCreateWindowSurface(
                vkInstance.get(),
                pointer,
                refL(null),
                refL(vkSurface)
        ));
    }

    public @NotNull WindowThread<VulkanWindow> getWindowThread() {
        return windowThread;
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
