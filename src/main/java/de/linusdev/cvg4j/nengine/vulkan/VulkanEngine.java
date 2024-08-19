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

import de.linusdev.cvg4j.nat.glfw3.GLFW;
import de.linusdev.cvg4j.nat.glfw3.custom.FrameInfo;
import de.linusdev.cvg4j.nat.glfw3.custom.UpdateListener;
import de.linusdev.cvg4j.nat.glfw3.exceptions.GLFWException;
import de.linusdev.cvg4j.nat.vulkan.VulkanApiVersion;
import de.linusdev.cvg4j.nat.vulkan.utils.VulkanApiVersionUtils;
import de.linusdev.cvg4j.nat.vulkan.utils.VulkanUtils;
import de.linusdev.cvg4j.nat.vulkan.enums.VkStructureType;
import de.linusdev.cvg4j.nat.vulkan.handles.VkInstance;
import de.linusdev.cvg4j.nat.vulkan.structs.VkApplicationInfo;
import de.linusdev.cvg4j.nat.vulkan.structs.VkInstanceCreateInfo;
import de.linusdev.cvg4j.nat.vulkan.utils.VulkanVersionUtils;
import de.linusdev.cvg4j.nengine.Engine;
import de.linusdev.cvg4j.nengine.RenderThread;
import de.linusdev.cvg4j.nengine.exception.EngineException;
import de.linusdev.cvg4j.nengine.info.Game;
import de.linusdev.lutils.async.Future;
import de.linusdev.lutils.async.Nothing;
import de.linusdev.lutils.async.Task;
import de.linusdev.lutils.async.exception.NonBlockingThreadException;
import de.linusdev.lutils.async.manager.AsyncManager;
import de.linusdev.lutils.math.vector.buffer.intn.BBUInt1;
import de.linusdev.lutils.nat.memory.DirectMemoryStack64;
import de.linusdev.lutils.nat.string.NullTerminatedUTF8String;
import de.linusdev.lutils.version.SimpleVersion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static de.linusdev.cvg4j.nat.glfw3.GLFWValues.GLFW_TRUE;
import static de.linusdev.cvg4j.nat.vulkan.utils.VulkanNonInstanceMethods.*;
import static de.linusdev.lutils.nat.struct.abstracts.Structure.allocate;

public class VulkanEngine<GAME extends Game> implements Engine<GAME>, AsyncManager, UpdateListener {

    private final @NotNull GAME game;
    private final @NotNull VulkanApiVersion minRequiredInstanceVersion;

    private final @NotNull RenderThread<GAME, Nothing, VulkanRasterizationWindow> renderThread;

    public VulkanEngine(
            @NotNull GAME game,
            @NotNull VulkanApiVersion minRequiredInstanceVersion
    ) throws GLFWException, EngineException {
        this.minRequiredInstanceVersion = minRequiredInstanceVersion;

        // Check if Vulkan is supported
        GLFW.glfwInit();
        if(GLFW.glfwVulkanSupported() != GLFW_TRUE) {
            throw new EngineException("Vulkan is not supported!");
        }

        this.game = game;
        this.renderThread = new RenderThread<>(
                this,
                rt -> {
                    DirectMemoryStack64 stack = rt.getStack();
                    assert stack.createSafePoint();

                    // Check minRequiredInstanceVersion
                    BBUInt1 version = stack.push(BBUInt1.newAllocatable(null));
                    vkEnumerateInstanceVersion(version);
                    SimpleVersion maxApiVersion = VulkanApiVersionUtils.toSimpleVersion(version.get());
                    stack.pop(); version = null; // version

                    if(maxApiVersion.compareTo(minRequiredInstanceVersion) < 0)
                        throw new EngineException(minRequiredInstanceVersion + " is not supported. Maximum supported is: " + maxApiVersion);


                    NullTerminatedUTF8String appName = stack.push(NullTerminatedUTF8String.newAllocatable(game.name()));
                    NullTerminatedUTF8String engineName = stack.push(NullTerminatedUTF8String.newAllocatable(Engine.name()));

                    // VkApplicationInfo
                    VkApplicationInfo vkApplicationInfo = stack.push(new VkApplicationInfo());
                    vkApplicationInfo.sType.set(VkStructureType.VK_STRUCTURE_TYPE_APPLICATION_INFO);
                    vkApplicationInfo.pNext.set(0);
                    vkApplicationInfo.pApplicationName.set(appName);
                    vkApplicationInfo.applicationVersion.set(VulkanVersionUtils.makeVersion(game.version().version()));
                    vkApplicationInfo.pEngineName.set(engineName);
                    vkApplicationInfo.engineVersion.set(VulkanVersionUtils.makeVersion(Engine.version().version()));
                    vkApplicationInfo.apiVersion.set(minRequiredInstanceVersion.getAsInt());

                    // VkInstanceCreateInfo
                    VkInstanceCreateInfo vkInstanceCreateInfo = stack.push(new VkInstanceCreateInfo());
                    vkInstanceCreateInfo.sType.set(VkStructureType.VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO);
                    vkInstanceCreateInfo.pNext.set(0);
                    vkInstanceCreateInfo.pApplicationInfo.set(vkApplicationInfo);
                    vkInstanceCreateInfo.enabledExtensionCount.set(array.length());
                    vkInstanceCreateInfo.ppEnabledExtensionNames.set(array.getPointer());
                    vkInstanceCreateInfo.enabledLayerCount.set(vLayerStrings.length());
                    vkInstanceCreateInfo.ppEnabledLayerNames.set(vLayerStrings.getPointer());
                    vkInstanceCreateInfo.flags.set(0);

                    // Create VkInstance
                    VkInstance vkInstance = allocate(new VkInstance());
                    vkCreateInstance(vkInstanceCreateInfo, null, vkInstance).check();
                    vkInstance.initMethodPointers();

                    stack.pop(); // vkInstanceCreateInfo
                    stack.pop(); // vkApplicationInfo
                    stack.pop(); // engineName
                    stack.pop(); // appName
                    assert stack.checkSafePoint();

                    return new VulkanRasterizationWindow(null, this, rt.getStack());
                },
                (rt, win, fut) -> {
                    fut.complete(Nothing.INSTANCE, Nothing.INSTANCE, null);
                },
                (rt, win) -> {
                    win.show(this);
                    win.close();
                }
        );
    }

    @Override
    public @NotNull GAME getGame() {
        return game;
    }

    @Override
    public @NotNull AsyncManager getAsyncManager() {
        return this;
    }

    @Override
    public void checkThread() throws NonBlockingThreadException {

    }

    @Override
    public void onExceptionInListener(@NotNull Future<?, ?> future, @Nullable Task<?, ?> task, @NotNull Throwable throwable) {

    }

    @Override
    public void update(@NotNull FrameInfo frameInfo) {

    }
}
