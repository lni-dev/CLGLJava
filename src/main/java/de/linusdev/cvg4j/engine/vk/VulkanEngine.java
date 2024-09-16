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
import de.linusdev.cvg4j.engine.exception.EngineException;
import de.linusdev.cvg4j.engine.queue.ReturnRunnable;
import de.linusdev.cvg4j.engine.queue.TQFutureImpl;
import de.linusdev.cvg4j.engine.queue.TaskQueue;
import de.linusdev.cvg4j.engine.render.RenderThread;
import de.linusdev.cvg4j.engine.ticker.Tickable;
import de.linusdev.cvg4j.engine.ticker.Ticker;
import de.linusdev.cvg4j.engine.vk.command.pool.GraphicsQueueTransientCommandPool;
import de.linusdev.cvg4j.engine.vk.device.Device;
import de.linusdev.cvg4j.engine.vk.device.Extend2D;
import de.linusdev.cvg4j.engine.vk.device.SwapChainBuilder;
import de.linusdev.cvg4j.engine.vk.engine.VulkanWindow;
import de.linusdev.cvg4j.engine.vk.infos.GpuInfo;
import de.linusdev.cvg4j.engine.vk.infos.SurfaceInfo;
import de.linusdev.cvg4j.engine.vk.instance.Instance;
import de.linusdev.cvg4j.engine.vk.pipeline.RasterizationPipeline;
import de.linusdev.cvg4j.engine.vk.renderer.rast.RasterizationRenderer;
import de.linusdev.cvg4j.engine.vk.renderpass.RenderPass;
import de.linusdev.cvg4j.engine.vk.selector.VulkanEngineInfo;
import de.linusdev.cvg4j.engine.vk.selector.gpu.GPUSelectionProgress;
import de.linusdev.cvg4j.engine.vk.swapchain.SwapChain;
import de.linusdev.cvg4j.engine.window.WindowThread;
import de.linusdev.cvg4j.nat.glfw3.GLFW;
import de.linusdev.cvg4j.nat.glfw3.custom.FrameInfo;
import de.linusdev.cvg4j.nat.glfw3.custom.UpdateListener;
import de.linusdev.cvg4j.nat.vulkan.bool.VkBool32;
import de.linusdev.cvg4j.nat.vulkan.enums.VkPresentModeKHR;
import de.linusdev.cvg4j.nat.vulkan.handles.VkCommandBuffer;
import de.linusdev.cvg4j.nat.vulkan.handles.VkInstance;
import de.linusdev.cvg4j.nat.vulkan.handles.VkPhysicalDevice;
import de.linusdev.cvg4j.nat.vulkan.structs.*;
import de.linusdev.cvg4j.window.input.InputManagerImpl;
import de.linusdev.cvg4j.window.input.InputManger;
import de.linusdev.llog.LLog;
import de.linusdev.llog.base.LogInstance;
import de.linusdev.llog.base.impl.StandardLogLevel;
import de.linusdev.lutils.async.Future;
import de.linusdev.lutils.async.Nothing;
import de.linusdev.lutils.async.Task;
import de.linusdev.lutils.async.completeable.CompletableFuture;
import de.linusdev.lutils.async.error.ThrowableAsyncError;
import de.linusdev.lutils.async.exception.ErrorException;
import de.linusdev.lutils.async.exception.NonBlockingThreadException;
import de.linusdev.lutils.async.manager.AsyncManager;
import de.linusdev.lutils.math.VMath;
import de.linusdev.lutils.math.vector.buffer.intn.BBInt2;
import de.linusdev.lutils.math.vector.buffer.intn.BBUInt1;
import de.linusdev.lutils.math.vector.buffer.intn.BBUInt2;
import de.linusdev.lutils.nat.enums.NativeEnumValue32;
import de.linusdev.lutils.nat.memory.stack.Stack;
import de.linusdev.lutils.nat.memory.stack.impl.DirectMemoryStack64;
import de.linusdev.lutils.nat.size.ByteUnits;
import de.linusdev.lutils.nat.size.Size;
import de.linusdev.lutils.nat.struct.array.StructureArray;
import de.linusdev.lutils.thread.var.SyncVar;
import de.linusdev.lutils.thread.var.SyncVarImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static de.linusdev.cvg4j.nat.glfw3.GLFWValues.GLFW_TRUE;
import static de.linusdev.lutils.nat.pointer.TypedPointer64.ofArray;
import static de.linusdev.lutils.nat.pointer.TypedPointer64.ref;
import static de.linusdev.lutils.nat.struct.abstracts.Structure.unionWith;

public class VulkanEngine<GAME extends VulkanGame> implements
        Engine<GAME>,
        AsyncManager,
        UpdateListener,
        VulkanRasterizationWindow.RenderCommandsFunction,
        Tickable
{

    public final static @NotNull LogInstance LOG = LLog.getLogInstance();

    private static final int LOAD_SCENE_TASK_ID = TaskQueue.getUniqueTaskId("LOAD_SCENE");

    private final @NotNull GAME game;

    private final @NotNull Executor executor = Executors.newWorkStealingPool(16);
    private final @NotNull Ticker ticker;
    private final @NotNull InputManger inputManger;


    private final @NotNull WindowThread<VulkanWindow> windowThread;
    private final @NotNull VulkanWindow window;
    private final @NotNull RasterizationRenderer renderer;
    private final @NotNull RenderThread renderThread;

    private final @NotNull VulkanEngineInfo vulkanInfo;
    private final @NotNull Instance instance;

    /**
     * Created in {@link #pickGPU(RenderThread, VulkanRasterizationWindow)}
     */
    private SwapChain swapChain;
    /**
     * Created in {@link #pickGPU(RenderThread, VulkanRasterizationWindow)}
     */
    private Device device;

    private RenderPass renderPass;

    private GraphicsQueueTransientCommandPool transientCommandPool;

    private final @NotNull SyncVar<VkScene<GAME>> currentScene = new SyncVarImpl<>(null);

    public VulkanEngine(
            @NotNull GAME game
    ) throws EngineException, InterruptedException {
        // Check if StaticSetup was called!
        StaticSetup.checkSetup();

        // Check if Vulkan is supported
        if(GLFW.glfwVulkanSupported() != GLFW_TRUE) {
            throw new EngineException("Vulkan is not supported!");
        }

        // Init variables
        this.vulkanInfo = new VulkanEngineInfo();
        this.game = game;
        this.ticker = new Ticker(this, game.getMillisPerTick());


        // Create a small stack for short-lived structures
        DirectMemoryStack64 stack = new DirectMemoryStack64(new Size(10, ByteUnits.KiB));

        // Create Vulkan Instance
        instance = new Instance(stack, game, vulkanInfo);

        // Start ticker
        ticker.start();

        // Create window and renderer
        windowThread = new WindowThread<>(this, t -> new VulkanWindow(instance, null), stack);
        window = windowThread.create().getResult();

        renderer = new RasterizationRenderer(instance, window);
        renderThread = new RenderThread(this, renderer);

        //TODO: pickGPU(rt, win);
        //                    renderPass = RenderPass.create(rt.getStack(), vkInstance, device, swapChain);
        //                    transientCommandPool = GraphicsQueueTransientCommandPool.create(this, rt.getStack(), vkInstance, device);



        this.renderThread.getThreadDeathFuture().then((win, secondary, error) -> {
            if(error != null) {
                LOG.log(StandardLogLevel.ERROR, "Render thread died due to an error: ");
                LOG.throwable(error.asThrowable());
            } else {
                LOG.log(StandardLogLevel.DEBUG, "Render thread died.");
            }

            // cleanup
            currentScene.consumeIfNotNull(VkScene::close);
            transientCommandPool.close();
            renderPass.close();
            swapChain.close();
            win.close();
            device.close();
            instance.close();

        });

        // Wait until the render thread is created
        try {
            this.window = this.renderThread.create().getResult();
            this.inputManger = new InputManagerImpl(window);
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
        return instance.getVkInstance();
    }

    public Device getDevice() {
        return device;
    }

    public @NotNull InputManger getInputManger() {
        return inputManger;
    }

    @Override
    public @NotNull AsyncManager getAsyncManager() {
        return this;
    }

    @Override
    public @NotNull <R> Future<R, VulkanEngine<GAME>> runSupervised(@NotNull ReturnRunnable<R> runnable) {
        var future = CompletableFuture.<R, VulkanEngine<GAME>>create(getAsyncManager(), false);
        executor.execute(() -> {
            try {
                future.complete(runnable.run(), this, null);
            } catch (Throwable t) {
                LOG.throwable(t);
                future.complete(null, this, new ThrowableAsyncError(t));
            }
        });
        return future;
    }

    @Override
    public void checkThread() throws NonBlockingThreadException {

    }

    @Override
    public void onExceptionInListener(@NotNull Future<?, ?> future, @Nullable Task<?, ?> task, @NotNull Throwable throwable) {

    }

    @Override
    public boolean available() {
        //TODO: not thread safe if currentScene turns from non-null to null
        return currentScene.get() != null;
    }

    @Override
    public void render(
            int currentFrameBufferImageIndex,
            int currentFrame,
            @NotNull VkCommandBuffer commandBuffer
    ) {
        VkScene<?> scene = currentScene.get();
        if(scene != null) {
            scene.render(renderThread.getStack(), instance.getVkInstance(), swapChain.getExtend(), currentFrameBufferImageIndex, currentFrame, commandBuffer, window.getFrameBuffers().getFrameBuffer(currentFrameBufferImageIndex));
        }
    }

    @Override
    public void recreateSwapChain(@NotNull Stack stack) {
        try {
            createSwapChain(stack, device, window, null, true);
        } catch (EngineException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(@NotNull FrameInfo frameInfo) {
        renderThreadTaskQueue.runQueuedTasks();
        VkScene<?> scene = currentScene.get();
        if(scene != null) {
            scene.update(frameInfo);
        }
    }

    public TQFutureImpl<VkScene<GAME>> loadScene(@NotNull VkScene<GAME> scene) {
        var fut = renderThreadTaskQueue.queueForExecution(LOAD_SCENE_TASK_ID, () -> {
            DirectMemoryStack64 stack = renderThread.getStack();
            try(var ignored = stack.safePoint()) {
                scene.onLoad0(stack, window, swapChain);
                RasterizationPipeline pipeLine = RasterizationPipeline.create(stack, instance.getVkInstance(), device, swapChain, renderPass, scene.pipeline(stack));
                window.createFrameBuffers(renderPass);
                scene.setPipeLine(pipeLine);
            }

            return scene;
        });

        fut.then((loadedScene, secondary, error) -> {
            if(error != null){
                LOG.throwable(error.asThrowable());
                return;
            }
            currentScene.set(loadedScene);
            LOG.debug("Scene loaded.");
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

    public GraphicsQueueTransientCommandPool getTransientCommandPool() {
        return transientCommandPool;
    }


    private void pickGPU(
            @NotNull RenderThread<GAME, VulkanRasterizationWindow, VulkanRasterizationWindow> rt,
            @NotNull VulkanRasterizationWindow window
    ) throws EngineException {
        LOG.debug("Start picking gpu.");
        VkInstance vkInstance = instance.getVkInstance();
        DirectMemoryStack64 stack = rt.getStack();
        // Pick GPU
        try(var ignored = stack.safePoint()) {
            BBUInt1 integer = stack.pushUnsignedInt();
            vkInstance.vkEnumeratePhysicalDevices(ref(integer), ref(null)).check();

            StructureArray<VkPhysicalDevice> vkPhysicalDevices = stack.pushArray(integer.get(), VkPhysicalDevice.class, VkPhysicalDevice::new);
            vkInstance.vkEnumeratePhysicalDevices(ref(integer), ofArray(vkPhysicalDevices)).check();


            GPUSelectionProgress progress = game.gpuSelector().startSelection();

            VkPhysicalDevice lastChecked = null;
            GpuInfo info = null;
            VkPhysicalDeviceProperties props = stack.push(new VkPhysicalDeviceProperties());
            StructureArray<VkExtensionProperties> extensions = stack.pushArray(200, VkExtensionProperties.class, VkExtensionProperties::new);
            StructureArray<VkQueueFamilyProperties> queueFamilies = stack.pushArray(100, VkQueueFamilyProperties.class, VkQueueFamilyProperties::new);
            VkBool32 queueFamilySupportsSurface = stack.push(new VkBool32());

            VkSurfaceCapabilitiesKHR surfacesCaps = stack.push(new VkSurfaceCapabilitiesKHR());
            StructureArray<VkSurfaceFormatKHR> surfaceFormats = stack.pushArray(100, VkSurfaceFormatKHR.class, VkSurfaceFormatKHR::new);
            StructureArray<NativeEnumValue32<VkPresentModeKHR>> presentModes = stack.pushArray(100, NativeEnumValue32.class, NativeEnumValue32::newUnallocatedT);

            for (VkPhysicalDevice dev : vkPhysicalDevices) {
                if(progress.canSelectionStop()) break;
                lastChecked = dev;

                info = GpuInfo.ofPhysicalDevice(vkInstance, window.getVkSurface(), dev,
                        integer, props, extensions, queueFamilies, queueFamilySupportsSurface,
                        SurfaceInfo.ofVkSurface(vkInstance, window.getVkSurface(), dev,
                                integer, surfacesCaps, surfaceFormats, presentModes
                        )
                );

                // calculate gpu priority
                int priority = progress.addGpu(dev, info);
                LOG.debug("Checking gpu '"+ props.deviceName.get() + "': " + priority);

            }

            VkPhysicalDevice best = progress.getBestGPU();

            if(best == null)
                throw new EngineException("No suitable gpu available.");

            // get the gpu information again (if required)...
            if(lastChecked != best) {
                info = GpuInfo.ofPhysicalDevice(vkInstance, window.getVkSurface(), best,
                        integer, props, extensions,
                        queueFamilies, queueFamilySupportsSurface,
                        SurfaceInfo.ofVkSurface(vkInstance, window.getVkSurface(), best,
                                integer, surfacesCaps, surfaceFormats, presentModes
                        )
                );
            }

            // Chose surface format and present mode
            LOG.debug("Selected gpu: " + info.props().deviceName.get());

            try(var ignored2 = stack.safePoint()) {
                createDevice(stack, info);
            }

            try(var ignored2 = stack.safePoint()) {
                createSwapChain(stack, device, window, info.surfaceInfo(), false);
            }

            stack.pop(); // presentModes
            stack.pop(); // surfaceFormats
            stack.pop(); // surfacesCaps

            stack.pop(); // queueFamilySupportsSurface
            stack.pop(); // queueFamilies
            stack.pop(); // extensions
            stack.pop(); // props
            stack.pop(); // vkPhysicalDevices

            stack.pop(); // integer
        }

        LOG.debug("Finished picking gpu.");
    }

    private void createDevice(
            @NotNull Stack stack,
            @NotNull GpuInfo info
    ) {
        // Create device
        device = Device.create(
                stack,
                instance.getVkInstance(),
                info.vkPhysicalDevice(),
                game.queueFamilySelector().selectGraphicsQueue(info.queueFamilyInfoList()).result1().index(),
                game.queueFamilySelector().selectPresentationQueue(info.queueFamilyInfoList()).result1().index(),
                game.requiredDeviceExtensions(),
                game.activatedVulkanLayers()
        );

        LOG.debug("Device created");
    }

    private void createSwapChain(
            @NotNull Stack stack,
            @NotNull Device device,
            @NotNull VulkanRasterizationWindow window,
            @Nullable SurfaceInfo info,
            boolean recreate
    ) throws EngineException {
        boolean pop = false;
        if(info == null) {
            pop = true;

            BBUInt1 integer = stack.pushUnsignedInt();
            VkSurfaceCapabilitiesKHR surfacesCaps = stack.push(new VkSurfaceCapabilitiesKHR());
            StructureArray<VkSurfaceFormatKHR> surfaceFormats = stack.pushArray(100, VkSurfaceFormatKHR.class, VkSurfaceFormatKHR::new);
            StructureArray<NativeEnumValue32<VkPresentModeKHR>> presentModes = stack.pushArray(100, NativeEnumValue32.class, NativeEnumValue32::newUnallocatedT);

            info = SurfaceInfo.ofVkSurface(instance.getVkInstance(), window.getVkSurface(), device.getVkPhysicalDevice(),
                    integer, surfacesCaps, surfaceFormats, presentModes
            );
        }

        SwapChainBuilder builder = new SwapChainBuilder()
                .setSurfaceFormat(
                        game.surfaceFormatSelector().select(info.surfaceFormatCount(), info.surfaceFormats()).result1()
                ).setPresentMode(
                        game.presentModeSelector().select(info.presentModeCount(), info.presentModes()).result1()
                );


        // Calculate swap extend
        LOG.debug("Calculate swap extend");
        Extend2D swapChainExtend = new Extend2D(stack.push(new VkExtent2D()));
        if(info.surfacesCaps().currentExtent.width.get() != 0xFFFFFFFF) {
            LOG.debug("Swap extend is fixed");
            swapChainExtend.xy(
                    info.surfacesCaps().currentExtent.width.get(),
                    info.surfacesCaps().currentExtent.height.get()
            );
        } else {
            LOG.debug("Swap extend is not fixed, select it based on frame buffer size.");
            BBInt2 size = unionWith(BBInt2.newAllocatable(null), swapChainExtend);
            window.getFrameBufferSize(size);

            BBUInt2 maxImageExtend = unionWith(BBUInt2.newAllocatable(null), info.surfacesCaps().maxImageExtent);
            BBUInt2 minImageExtend = unionWith(BBUInt2.newAllocatable(null), info.surfacesCaps().minImageExtent);

            VMath.clamp(size, minImageExtend, maxImageExtend, size);
        }

        builder.setSwapExtend(swapChainExtend);

        // Swap chain image count
        int max = info.surfacesCaps().maxImageCount.get();
        int min = info.surfacesCaps().minImageCount.get();
        builder.setSwapChainImageCount(game.swapChainImageCount(min, max == 0 ? Integer.MAX_VALUE : max));

        // Set surface transform (current is fine)
        builder.setSurfaceTransform(info.surfacesCaps().currentTransform);

        if(recreate) {
            builder.recreateSwapChain(stack, swapChain);
            LOG.debug("SwapChain recreated");
        } else {
            swapChain = builder.buildSwapChain(stack, window, instance.getVkInstance(), device);
            LOG.debug("SwapChain created");
        }

        stack.pop(); // swapChainExtend
        if(pop) {
            stack.pop(); // presentModes
            stack.pop(); // surfaceFormats
            stack.pop(); // surfacesCaps
            stack.pop(); // integer
        }
    }


    @Override
    public void tick() {
        VkScene<?> scene = currentScene.get();
        if(scene != null) {
            scene.tick();
        }
    }
}
