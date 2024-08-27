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
import de.linusdev.cvg4j.nat.vulkan.enums.VkPresentModeKHR;
import de.linusdev.cvg4j.nat.vulkan.enums.VkStructureType;
import de.linusdev.cvg4j.nat.vulkan.handles.VkInstance;
import de.linusdev.cvg4j.nat.vulkan.handles.VkPhysicalDevice;
import de.linusdev.cvg4j.nat.vulkan.handles.VkSurfaceKHR;
import de.linusdev.cvg4j.nat.vulkan.structs.*;
import de.linusdev.cvg4j.nat.vulkan.utils.VulkanVersionUtils;
import de.linusdev.cvg4j.nengine.Engine;
import de.linusdev.cvg4j.nengine.RenderThread;
import de.linusdev.cvg4j.nengine.exception.EngineException;
import de.linusdev.cvg4j.nengine.info.Game;
import de.linusdev.cvg4j.nengine.vulkan.extension.VulkanExtensionList;
import de.linusdev.cvg4j.nengine.vulkan.selector.GPUSelectionProgress;
import de.linusdev.cvg4j.nengine.vulkan.selector.VulkanEngineInfo;
import de.linusdev.cvg4j.nengine.vulkan.selector.VulkanGPUSelector;
import de.linusdev.cvg4j.nengine.vulkan.selector.VulkanRequirements;
import de.linusdev.llog.LLog;
import de.linusdev.llog.base.LogInstance;
import de.linusdev.llog.base.impl.StandardLogLevel;
import de.linusdev.lutils.async.Future;
import de.linusdev.lutils.async.Nothing;
import de.linusdev.lutils.async.Task;
import de.linusdev.lutils.async.exception.ErrorException;
import de.linusdev.lutils.async.exception.NonBlockingThreadException;
import de.linusdev.lutils.async.manager.AsyncManager;
import de.linusdev.lutils.math.vector.buffer.intn.BBUInt1;
import de.linusdev.lutils.nat.enums.NativeEnumValue32;
import de.linusdev.lutils.nat.memory.DirectMemoryStack64;
import de.linusdev.lutils.nat.pointer.TypedPointer64;
import de.linusdev.lutils.nat.string.NullTerminatedUTF8String;
import de.linusdev.lutils.nat.struct.array.StructureArray;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static de.linusdev.cvg4j.nat.glfw3.GLFWValues.GLFW_TRUE;
import static de.linusdev.cvg4j.nat.vulkan.utils.VulkanNonInstanceMethods.vkCreateInstance;
import static de.linusdev.lutils.nat.pointer.Pointer64.refL;
import static de.linusdev.lutils.nat.pointer.TypedPointer64.ofArray;
import static de.linusdev.lutils.nat.pointer.TypedPointer64.ref;
import static de.linusdev.lutils.nat.struct.abstracts.Structure.allocate;

public class VulkanEngine<GAME extends Game> implements Engine<GAME>, AsyncManager, UpdateListener {

    private final static @NotNull LogInstance LOG = LLog.getLogInstance();

    private final @NotNull GAME game;
    private final @NotNull VulkanRequirements requirements;

    private final @NotNull RenderThread<GAME, VulkanRasterizationWindow, VulkanRasterizationWindow> renderThread;
    private final @NotNull VulkanRasterizationWindow window;
    private final @NotNull VulkanEngineInfo vulkanInfo;
    private final @NotNull VulkanExtensionList vulkanExtensions = new VulkanExtensionList();

    private final @NotNull VkInstance vkInstance;
    private final @NotNull VkPhysicalDevice vkPhysicalDevice;

    public VulkanEngine(
            @NotNull GAME game,
            @NotNull VulkanRequirements requirements
    ) throws EngineException {
        StaticSetup.checkSetup();
        this.requirements = requirements;
        this.vulkanInfo = new VulkanEngineInfo();
        this.vkInstance = allocate(new VkInstance());
        this.vkPhysicalDevice = allocate(new VkPhysicalDevice());

        // Check if Vulkan is supported
        try {
            GLFW.glfwInit();
        } catch (GLFWException e) {
            throw new EngineException(e);
        }
        if(GLFW.glfwVulkanSupported() != GLFW_TRUE) {
            throw new EngineException("Vulkan is not supported!");
        }

        this.game = game;

        LOG.logDebug("Creating render-thread");
        this.renderThread = new RenderThread<>(
                this,
                rt -> {
                    createVkInstance(rt);

                    return new VulkanRasterizationWindow(null, vkInstance, rt.getStack());
                },
                (rt, win, fut) -> {
                    fut.complete(win, Nothing.INSTANCE, null);
                },
                (rt, win) -> {
                    win.show(this);
                    win.close();
                }
        );

        this.renderThread.getThreadDeathFuture().then((result, secondary, error) -> {
            if(error != null) {
                LOG.log(StandardLogLevel.ERROR, "Render thread died due to an error: ");
                LOG.logThrowable(error.asThrowable());
            } else {
                LOG.log(StandardLogLevel.DEBUG, "Render thread died.");
            }

            // cleanup
            vkInstance.vkDestroyInstance(ref(null));
        });

        // Wait until the render thread is created
        try {
            this.window = this.renderThread.create().getResult();
            LOG.log(StandardLogLevel.DEBUG, "Render thread created.");
        } catch (InterruptedException | ErrorException e) {
            throw new EngineException(e);
        }
    }

    @Override
    public @NotNull GAME getGame() {
        return game;
    }

    public @NotNull VkInstance getVkInstance() {
        return vkInstance;
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

    private void createVkInstance(
            @NotNull RenderThread<GAME, VulkanRasterizationWindow, VulkanRasterizationWindow> rt
    ) throws EngineException {
        LOG.logDebug("Start creating VkInstance.");

        DirectMemoryStack64 stack = rt.getStack();
        assert stack.createSafePoint();

        vulkanInfo.load(stack);

        // Check minRequiredInstanceVersion
        requirements.checkMinRequiredInstanceVersion(vulkanInfo);
        requirements.checkRequiredInstanceExtensions(vulkanInfo);

        // add all required vulkan extensions
        vulkanExtensions.addAll(requirements.getRequiredInstanceExtensions());
        vulkanExtensions.addAll(vulkanInfo.getGlfwRequiredInstanceExtensions());

        // VkApplicationInfo
        NullTerminatedUTF8String appName = stack.pushString(game.name());
        NullTerminatedUTF8String engineName = stack.pushString(Engine.name());

        VkApplicationInfo vkApplicationInfo = stack.push(new VkApplicationInfo());
        vkApplicationInfo.sType.set(VkStructureType.APPLICATION_INFO);
        vkApplicationInfo.pNext.set(0);
        vkApplicationInfo.pApplicationName.set(appName);
        vkApplicationInfo.applicationVersion.set(VulkanVersionUtils.makeVersion(game.version().version()));
        vkApplicationInfo.pEngineName.set(engineName);
        vkApplicationInfo.engineVersion.set(VulkanVersionUtils.makeVersion(Engine.version().version()));
        vkApplicationInfo.apiVersion.set(requirements.getMinRequiredInstanceVersion().getAsInt());
        LOG.log(StandardLogLevel.DATA, "VkApplicationInfo: " + vkApplicationInfo);

        // VkInstanceCreateInfo
        var enabledExtensionsNatArray = vulkanExtensions.toNativeArray(stack::pushArray, stack::pushString);
        @Nullable var enabledLayersNatArray = requirements.getVulkanLayersAsNatArray(stack::pushArray, stack::pushString);

        VkInstanceCreateInfo vkInstanceCreateInfo = stack.push(new VkInstanceCreateInfo());
        vkInstanceCreateInfo.sType.set(VkStructureType.INSTANCE_CREATE_INFO);
        vkInstanceCreateInfo.pNext.set(0);
        vkInstanceCreateInfo.pApplicationInfo.set(vkApplicationInfo);
        vkInstanceCreateInfo.enabledExtensionCount.set(enabledExtensionsNatArray.length());
        vkInstanceCreateInfo.ppEnabledExtensionNames.set(enabledExtensionsNatArray.getPointer());
        vkInstanceCreateInfo.enabledLayerCount.set(enabledLayersNatArray == null ? 0 : enabledLayersNatArray.length());
        vkInstanceCreateInfo.ppEnabledLayerNames.set(refL(enabledLayersNatArray));
        vkInstanceCreateInfo.flags.set(0);
        LOG.log(StandardLogLevel.DATA, "VkInstanceCreateInfo: " + vkInstanceCreateInfo);

        // Create VkInstance
        vkCreateInstance(vkInstanceCreateInfo, null, vkInstance).check();
        vkInstance.initMethodPointers();

        if(enabledLayersNatArray != null) {
            for (int i = 0; i < enabledLayersNatArray.length(); i++)
                stack.pop(); // string in enabledLayersNatArray
            stack.pop(); // enabledLayersNatArray
        }

        for (int i = 0; i < enabledExtensionsNatArray.length(); i++)
            stack.pop(); // string in enabledExtensionsNatArray
        stack.pop(); // enabledExtensionsNatArray

        stack.pop(); // vkInstanceCreateInfo
        stack.pop(); // vkApplicationInfo
        stack.pop(); // engineName
        stack.pop(); // appName
        assert stack.checkSafePoint();

        LOG.logDebug("Finished creating VkInstance.");
    }

    /**
     * requires {@link #createVkInstance(RenderThread)} to be called.
     */
    private void pickGPU(
            @NotNull RenderThread<GAME, VulkanRasterizationWindow, VulkanRasterizationWindow> rt,
            @NotNull VkSurfaceKHR vkSurface,
            @NotNull VulkanGPUSelector selector
    ) throws EngineException {
        LOG.logDebug("Start picking gpu.");
        DirectMemoryStack64 stack = rt.getStack();
        // Pick GPU
        assert stack.createSafePoint();

        BBUInt1 integer = stack.pushUnsignedInt();
        vkInstance.vkEnumeratePhysicalDevices(TypedPointer64.of(integer), TypedPointer64.of(null)).check();

        StructureArray<VkPhysicalDevice> vkPhysicalDevices = stack.pushArray(integer.get(), VkPhysicalDevice.class, VkPhysicalDevice::new);
        vkInstance.vkEnumeratePhysicalDevices(TypedPointer64.of(integer), TypedPointer64.ofArray(vkPhysicalDevices)).check();


        GPUSelectionProgress progress = new GPUSelectionProgress(selector);

        VkPhysicalDeviceProperties props = stack.push(new VkPhysicalDeviceProperties());
        StructureArray<VkExtensionProperties> extensions = stack.pushArray(200, VkExtensionProperties.class, VkExtensionProperties::new);
        VkSurfaceCapabilitiesKHR surfacesCaps = stack.push(new VkSurfaceCapabilitiesKHR());
        StructureArray<VkSurfaceFormatKHR> surfaceFormats = stack.pushArray(100, VkSurfaceFormatKHR.class, VkSurfaceFormatKHR::new);
        StructureArray<NativeEnumValue32<VkPresentModeKHR>> presentModes = stack.pushArray(100, NativeEnumValue32.class, NativeEnumValue32::newUnallocatedT);
        for (VkPhysicalDevice dev : vkPhysicalDevices) {
            // Props
            vkInstance.vkGetPhysicalDeviceProperties(dev, TypedPointer64.of(props));

            // Extensions
            vkInstance.vkEnumerateDeviceExtensionProperties(dev, ref(null), ref(integer), ref(null));
            int extensionCount = integer.get();
            if(extensionCount > extensions.length()) {
                // unlikely, if this happens just allocate one outside the stack
                extensions = StructureArray.newAllocated(extensionCount, VkExtensionProperties.class, VkExtensionProperties::new);
            }
            vkInstance.vkEnumerateDeviceExtensionProperties(dev, ref(null), ref(integer), ofArray(extensions));

            // Surface caps
            vkInstance.vkGetPhysicalDeviceSurfaceCapabilitiesKHR(dev, vkSurface, ref(surfacesCaps));

            // Surface formats
            vkInstance.vkGetPhysicalDeviceSurfaceFormatsKHR(vkPhysicalDevice, vkSurface, ref(integer), ref(null));
            int surfaceFormatCount = integer.get();
            if(surfaceFormatCount > surfaceFormats.length()) {
                // unlikely, if this happens just allocate one outside the stack
                surfaceFormats = StructureArray.newAllocated(surfaceFormatCount, VkSurfaceFormatKHR.class, VkSurfaceFormatKHR::new);
            }
            vkInstance.vkGetPhysicalDeviceSurfaceFormatsKHR(vkPhysicalDevice, vkSurface, ref(integer), ofArray(surfaceFormats));

            // Presentation Modes
            vkInstance.vkGetPhysicalDeviceSurfacePresentModesKHR(vkPhysicalDevice, vkSurface, ref(integer), ref(null));
            int presentModeCount = integer.get();
            if(presentModeCount > presentModes.length()) {
                // unlikely, if this happens just allocate one outside the stack
                presentModes = StructureArray.newAllocated(presentModeCount, NativeEnumValue32.class, NativeEnumValue32::newUnallocatedT);
            }
            vkInstance.vkGetPhysicalDeviceSurfacePresentModesKHR(vkPhysicalDevice, vkSurface, ref(integer), ofArray(presentModes));


            int priority = progress.addGpu(dev,
                    props,
                    extensionCount, extensions,
                    surfacesCaps,
                    surfaceFormatCount,
                    surfaceFormats,
                    presentModeCount,
                    presentModes
            );
            LOG.logDebug("Checking gpu '"+ props.deviceName.get() + "': " + priority);


        }

        VkPhysicalDevice best = progress.getBestGPU();

        if(best == null)
            throw new EngineException("No suitable gpu available.");

        this.vkPhysicalDevice.set(best.get());

        vkInstance.vkGetPhysicalDeviceProperties(best, TypedPointer64.of(props));
        LOG.logDebug("Selected gpu: " + props.deviceName.get());

        stack.pop(); // surfaceFormats
        stack.pop(); // surfacesCaps
        stack.pop(); // extensions
        stack.pop(); // props
        stack.pop(); // vkPhysicalDevices

        stack.pop(); // integer
        assert stack.checkSafePoint();
        LOG.logDebug("Finished picking gpu.");
    }
}
