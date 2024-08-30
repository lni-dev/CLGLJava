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

import de.linusdev.cvg4j.engine.Engine;
import de.linusdev.cvg4j.engine.RenderThread;
import de.linusdev.cvg4j.engine.exception.EngineException;
import de.linusdev.cvg4j.engine.queue.TQFuture;
import de.linusdev.cvg4j.engine.queue.TaskQueue;
import de.linusdev.cvg4j.engine.vk.device.Extend2D;
import de.linusdev.cvg4j.engine.vk.device.VkDeviceAndSwapChainBuilder;
import de.linusdev.cvg4j.engine.vk.extension.VulkanExtensionList;
import de.linusdev.cvg4j.engine.vk.pipeline.RasterizationPipeLine;
import de.linusdev.cvg4j.engine.vk.selector.GpuInfo;
import de.linusdev.cvg4j.engine.vk.selector.VulkanEngineInfo;
import de.linusdev.cvg4j.engine.vk.selector.gpu.GPUSelectionProgress;
import de.linusdev.cvg4j.engine.vk.swapchain.SwapChain;
import de.linusdev.cvg4j.nat.glfw3.GLFW;
import de.linusdev.cvg4j.nat.glfw3.custom.FrameInfo;
import de.linusdev.cvg4j.nat.glfw3.custom.UpdateListener;
import de.linusdev.cvg4j.nat.glfw3.exceptions.GLFWException;
import de.linusdev.cvg4j.nat.vulkan.VkBool32;
import de.linusdev.cvg4j.nat.vulkan.enums.VkPresentModeKHR;
import de.linusdev.cvg4j.nat.vulkan.enums.VkStructureType;
import de.linusdev.cvg4j.nat.vulkan.handles.VkDevice;
import de.linusdev.cvg4j.nat.vulkan.handles.VkInstance;
import de.linusdev.cvg4j.nat.vulkan.handles.VkPhysicalDevice;
import de.linusdev.cvg4j.nat.vulkan.handles.VkQueue;
import de.linusdev.cvg4j.nat.vulkan.structs.*;
import de.linusdev.cvg4j.nat.vulkan.utils.VulkanVersionUtils;
import de.linusdev.llog.LLog;
import de.linusdev.llog.base.LogInstance;
import de.linusdev.llog.base.impl.StandardLogLevel;
import de.linusdev.lutils.async.Future;
import de.linusdev.lutils.async.Nothing;
import de.linusdev.lutils.async.Task;
import de.linusdev.lutils.async.completeable.CompletableFuture;
import de.linusdev.lutils.async.exception.ErrorException;
import de.linusdev.lutils.async.exception.NonBlockingThreadException;
import de.linusdev.lutils.async.manager.AsyncManager;
import de.linusdev.lutils.math.VMath;
import de.linusdev.lutils.math.vector.buffer.intn.BBInt2;
import de.linusdev.lutils.math.vector.buffer.intn.BBUInt1;
import de.linusdev.lutils.math.vector.buffer.intn.BBUInt2;
import de.linusdev.lutils.nat.enums.NativeEnumValue32;
import de.linusdev.lutils.nat.memory.DirectMemoryStack64;
import de.linusdev.lutils.nat.pointer.BBTypedPointer64;
import de.linusdev.lutils.nat.string.NullTerminatedUTF8String;
import de.linusdev.lutils.nat.struct.array.StructureArray;
import de.linusdev.lutils.thread.var.SyncVar;
import de.linusdev.lutils.thread.var.SyncVarImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static de.linusdev.cvg4j.nat.glfw3.GLFWValues.GLFW_TRUE;
import static de.linusdev.cvg4j.nat.vulkan.utils.VulkanNonInstanceMethods.vkCreateInstance;
import static de.linusdev.lutils.nat.pointer.Pointer64.refL;
import static de.linusdev.lutils.nat.pointer.TypedPointer64.ofArray;
import static de.linusdev.lutils.nat.pointer.TypedPointer64.ref;
import static de.linusdev.lutils.nat.struct.abstracts.Structure.allocate;
import static de.linusdev.lutils.nat.struct.abstracts.Structure.unionWith;

public class VulkanEngine<GAME extends VulkanGame> implements Engine<GAME>, AsyncManager, UpdateListener, VulkanRasterizationWindow.RenderCommandsFunction {

    private final static @NotNull LogInstance LOG = LLog.getLogInstance();

    private static final int LOAD_SCENE_TASK_ID = TaskQueue.getUniqueTaskId("LOAD_SCENE");

    private final @NotNull GAME game;

    private final @NotNull RenderThread<GAME, VulkanRasterizationWindow, VulkanRasterizationWindow> renderThread;
    private final @NotNull TaskQueue renderThreadTaskQueue;

    private final @NotNull VulkanRasterizationWindow window;
    private final @NotNull VulkanEngineInfo vulkanInfo;
    private final @NotNull VulkanExtensionList vulkanExtensions = new VulkanExtensionList();

    private final @NotNull VkInstance vkInstance;
    private final @NotNull VkPhysicalDevice vkPhysicalDevice;
    private final @NotNull VkDevice vkDevice;
    private final @NotNull VkQueue graphicsQueue;
    private final @NotNull VkQueue presentationQueue;

    private Extend2D swapChainExtend;
    /**
     * Created in {@link #pickGPU(RenderThread, VulkanRasterizationWindow)}
     */
    private SwapChain swapChain;
    private CommandPool commandPool;

    private final @NotNull SyncVar<VkScene<GAME>> currentScene = new SyncVarImpl<>(null);

    public VulkanEngine(
            @NotNull GAME game
    ) throws EngineException {
        StaticSetup.checkSetup();
        this.vulkanInfo = new VulkanEngineInfo();

        this.vkInstance = allocate(new VkInstance());
        this.vkPhysicalDevice = allocate(new VkPhysicalDevice());
        this.vkDevice = allocate(new VkDevice());
        this.graphicsQueue = allocate(new VkQueue());
        this.presentationQueue = allocate(new VkQueue());
        this.swapChainExtend = new Extend2D(allocate(new VkExtent2D()));

        this.renderThreadTaskQueue = new TaskQueue(this, 100);

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
                    VulkanRasterizationWindow win = new VulkanRasterizationWindow(null, vkInstance, rt.getStack());
                    pickGPU(rt, win);
                    commandPool = CommandPool.create(rt.getStack(), vkInstance, vkDevice, swapChain);
                    return win;
                },
                (rt, win, fut) -> {
                    fut.complete(win, Nothing.INSTANCE, null);
                },
                (rt, win) -> {
                    win.init(rt.getStack(), vkDevice, commandPool, swapChain, graphicsQueue, presentationQueue, this);
                    win.show(this);
                }
        );

        this.renderThread.getThreadDeathFuture().then((win, secondary, error) -> {
            if(error != null) {
                LOG.log(StandardLogLevel.ERROR, "Render thread died due to an error: ");
                LOG.logThrowable(error.asThrowable());
            } else {
                LOG.log(StandardLogLevel.DEBUG, "Render thread died.");
            }

            // cleanup
            commandPool.close();
            currentScene.consumeIfNotNull(VkScene::close);
            swapChain.close();
            win.close();
            vkInstance.vkDestroyDevice(vkDevice, ref(null));
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

    public @NotNull VkDevice getVkDevice() {
        return vkDevice;
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
    public boolean available() {
        return currentScene.get() != null;
    }

    @Override
    public void render(int currentFrameBufferImageIndex) {
        VkScene<?> scene = currentScene.get();
        if(scene != null) {
            scene.render(renderThread.getStack(), vkInstance, swapChainExtend, currentFrameBufferImageIndex, commandPool);
        }
    }

    @Override
    public void update(@NotNull FrameInfo frameInfo) {
        renderThreadTaskQueue.runQueuedTasks();
    }

    public TQFuture<VkScene<GAME>> loadScene(@NotNull VkScene<GAME> scene) {
        var fut = renderThreadTaskQueue.queueForExecution(LOAD_SCENE_TASK_ID, () -> {
            DirectMemoryStack64 stack = renderThread.getStack();
            assert stack.createSafePoint();
            RasterizationPipeLine pipeLine = RasterizationPipeLine.create(stack, vkInstance, vkDevice, swapChain, swapChainExtend, scene.pipeline(stack));
            scene.setPipeLine(pipeLine);
            assert stack.checkSafePoint();
            return scene;
        });

        fut.then((loadedScene, secondary, error) -> {
            if(error != null){
                LOG.logThrowable(error.asThrowable());
                return;
            }
            currentScene.set(loadedScene);
            LOG.logDebug("Scene loaded.");
        });

       return fut;
    }

    public @NotNull Future<Nothing, VulkanEngine<GAME>> getEngineDeathFuture() {
        var fut = CompletableFuture.<Nothing, VulkanEngine<GAME>>create(this, false);

        renderThread.getThreadDeathFuture().then((result, secondary, error) -> {
            if(error != null)
                fut.complete(null, this, error);
            else
                fut.complete(Nothing.INSTANCE, this, null);
        });

        return fut;
    }

    private void createVkInstance(
            @NotNull RenderThread<GAME, VulkanRasterizationWindow, VulkanRasterizationWindow> rt
    ) throws EngineException {
        LOG.logDebug("Start creating VkInstance.");

        DirectMemoryStack64 stack = rt.getStack();
        assert stack.createSafePoint();

        vulkanInfo.load(stack);

        // Check minRequiredInstanceVersion
        vulkanInfo.isVulkanApiVersionAvailable(game.minRequiredInstanceVersion());
        vulkanInfo.areInstanceExtensionsAvailable(game.requiredInstanceExtensions());

        // add all required vulkan extensions
        vulkanExtensions.addAll(game.requiredInstanceExtensions());
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
        vkApplicationInfo.apiVersion.set(game.minRequiredInstanceVersion().getAsInt());
        LOG.log(StandardLogLevel.DATA, "VkApplicationInfo: " + vkApplicationInfo);

        // VkInstanceCreateInfo
        StructureArray<BBTypedPointer64<NullTerminatedUTF8String>> enabledExtensionsNatArray = vulkanExtensions.toNativeArray(stack::pushArray, stack::pushString);
        @Nullable StructureArray<BBTypedPointer64<NullTerminatedUTF8String>> enabledLayersNatArray = null;

        if(!game.activatedVulkanLayers().isEmpty()) {
            enabledLayersNatArray = stack.pushArray(game.activatedVulkanLayers().size(), BBTypedPointer64.class, BBTypedPointer64::newUnallocated1);
            int i = 0;
            for (String ext : game.activatedVulkanLayers())
                enabledLayersNatArray.getOrCreate(i++).set(stack.pushString(ext));
        }


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
            @NotNull VulkanRasterizationWindow window
    ) throws EngineException {
        LOG.logDebug("Start picking gpu.");
        DirectMemoryStack64 stack = rt.getStack();
        // Pick GPU
        assert stack.createSafePoint();

        BBUInt1 integer = stack.pushUnsignedInt();
        vkInstance.vkEnumeratePhysicalDevices(ref(integer), ref(null)).check();

        StructureArray<VkPhysicalDevice> vkPhysicalDevices = stack.pushArray(integer.get(), VkPhysicalDevice.class, VkPhysicalDevice::new);
        vkInstance.vkEnumeratePhysicalDevices(ref(integer), ofArray(vkPhysicalDevices)).check();


        GPUSelectionProgress progress = game.gpuSelector().startSelection();

        VkPhysicalDevice lastChecked = null;
        GpuInfo info = null;
        VkPhysicalDeviceProperties props = stack.push(new VkPhysicalDeviceProperties());
        StructureArray<VkExtensionProperties> extensions = stack.pushArray(200, VkExtensionProperties.class, VkExtensionProperties::new);
        VkSurfaceCapabilitiesKHR surfacesCaps = stack.push(new VkSurfaceCapabilitiesKHR());
        StructureArray<VkSurfaceFormatKHR> surfaceFormats = stack.pushArray(100, VkSurfaceFormatKHR.class, VkSurfaceFormatKHR::new);
        StructureArray<NativeEnumValue32<VkPresentModeKHR>> presentModes = stack.pushArray(100, NativeEnumValue32.class, NativeEnumValue32::newUnallocatedT);
        StructureArray<VkQueueFamilyProperties> queueFamilies = stack.pushArray(100, VkQueueFamilyProperties.class, VkQueueFamilyProperties::new);
        VkBool32 queueFamilySupportsSurface = stack.push(new VkBool32());
        for (VkPhysicalDevice dev : vkPhysicalDevices) {
            if(progress.canSelectionStop()) break;
            lastChecked = dev;

            info = GpuInfo.ofPhysicalDevice(vkInstance, window.getVkSurface(), dev,
                    integer, props, extensions, surfacesCaps, surfaceFormats,
                    presentModes, queueFamilies, queueFamilySupportsSurface
            );

            // calculate gpu priority
            int priority = progress.addGpu(dev, info);
            LOG.logDebug("Checking gpu '"+ props.deviceName.get() + "': " + priority);

        }

        VkPhysicalDevice best = progress.getBestGPU();

        if(best == null)
            throw new EngineException("No suitable gpu available.");

        this.vkPhysicalDevice.set(best.get());

        // get the gpu information again (if required)...
        if(lastChecked != best) {
            info = GpuInfo.ofPhysicalDevice(vkInstance, window.getVkSurface(), best,
                    integer, props, extensions, surfacesCaps, surfaceFormats,
                    presentModes, queueFamilies, queueFamilySupportsSurface
            );
        }

        // Chose surface format and present mode
        LOG.logDebug("Selected gpu: " + info.props().deviceName.get());

        // Next create the actual vulkan device from the picked gpu
        VkDeviceAndSwapChainBuilder builder = new VkDeviceAndSwapChainBuilder()
                .setSurfaceFormat(
                        game.surfaceFormatSelector().select(info.surfaceFormatCount(), info.surfaceFormats()).result1()
                ).setPresentMode(
                        game.presentModeSelector().select(info.presentModeCount(), info.presentModes()).result1()
                );


        // Calculate swap extend
        LOG.logDebug("Calculate swap extend");
        if(info.surfacesCaps().currentExtent.width.get() != 0xFFFFFFFF) {
            LOG.logDebug("Swap extend is fixed");
            swapChainExtend.xy(
                    info.surfacesCaps().currentExtent.width.get(),
                    info.surfacesCaps().currentExtent.height.get()
            );
        } else {
            LOG.logDebug("Swap extend is not fixed, select it based on frame buffer size.");
            BBInt2 size = unionWith(BBInt2.newAllocatable(null), swapChainExtend);
            window.getFrameBufferSize(size);

            BBUInt2 maxImageExtend = unionWith(BBUInt2.newAllocatable(null), info.surfacesCaps().maxImageExtent);
            BBUInt2 minImageExtend = unionWith(BBUInt2.newAllocatable(null), info.surfacesCaps().minImageExtent);

            VMath.clamp(size, minImageExtend, maxImageExtend, size);
        }

        builder.setSwapExtend(swapChainExtend.geVkExtend2D());

        // Swap chain image count
        int max = info.surfacesCaps().maxImageCount.get();
        int min = info.surfacesCaps().minImageCount.get();
        builder.setSwapChainImageCount(game.swapChainImageCount(min, max == 0 ? Integer.MAX_VALUE : max));

        // Set surface transform (current is fine)
        builder.setSurfaceTransform(info.surfacesCaps().currentTransform.get());

        // Queue families
        builder.setGraphicsQueueIndex(game.queueFamilySelector().selectGraphicsQueue(info.queueFamilyInfoList()).result1().index());
        builder.setPresentationQueueIndex(game.queueFamilySelector().selectPresentationQueue(info.queueFamilyInfoList()).result1().index());

        swapChain = builder.build(stack, game, window, vkInstance, best, vkDevice, graphicsQueue, presentationQueue);

        stack.pop(); // queueFamilySupportsSurface
        stack.pop(); // queueFamilies
        stack.pop(); // presentModes
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
