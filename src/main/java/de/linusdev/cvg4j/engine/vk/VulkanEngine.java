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
import de.linusdev.cvg4j.engine.queue.TQFuture;
import de.linusdev.cvg4j.engine.queue.TaskQueue;
import de.linusdev.cvg4j.engine.render.RenderThread;
import de.linusdev.cvg4j.engine.ticker.Tickable;
import de.linusdev.cvg4j.engine.ticker.Ticker;
import de.linusdev.cvg4j.engine.vk.command.pool.GraphicsQueueTransientCommandPool;
import de.linusdev.cvg4j.engine.vk.device.Device;
import de.linusdev.cvg4j.engine.vk.instance.Instance;
import de.linusdev.cvg4j.engine.vk.pipeline.RasterizationPipeline;
import de.linusdev.cvg4j.engine.vk.renderer.rast.RasterizationRenderer;
import de.linusdev.cvg4j.engine.vk.renderer.rast.RenderCommandsFunction;
import de.linusdev.cvg4j.engine.vk.renderpass.RenderPass;
import de.linusdev.cvg4j.engine.vk.selector.VulkanEngineInfo;
import de.linusdev.cvg4j.engine.vk.swapchain.SwapChain;
import de.linusdev.cvg4j.engine.vk.utils.VkEngineUtils;
import de.linusdev.cvg4j.engine.vk.window.VulkanWindow;
import de.linusdev.cvg4j.engine.window.WindowThread;
import de.linusdev.cvg4j.engine.window.input.InputManagerImpl;
import de.linusdev.cvg4j.engine.window.input.InputManger;
import de.linusdev.cvg4j.nat.glfw3.GLFW;
import de.linusdev.cvg4j.nat.glfw3.custom.FrameInfo;
import de.linusdev.cvg4j.nat.glfw3.custom.UpdateListener;
import de.linusdev.cvg4j.nat.glfw3.custom.WindowCloseListener;
import de.linusdev.cvg4j.nat.vulkan.handles.VkCommandBuffer;
import de.linusdev.cvg4j.nat.vulkan.handles.VkInstance;
import de.linusdev.llog.LLog;
import de.linusdev.llog.base.LogInstance;
import de.linusdev.llog.base.impl.StandardLogLevel;
import de.linusdev.lutils.async.Future;
import de.linusdev.lutils.async.Nothing;
import de.linusdev.lutils.async.Task;
import de.linusdev.lutils.async.completeable.CompletableFuture;
import de.linusdev.lutils.async.error.ThrowableAsyncError;
import de.linusdev.lutils.async.exception.NonBlockingThreadException;
import de.linusdev.lutils.async.manager.AsyncManager;
import de.linusdev.lutils.interfaces.AdvTRunnable;
import de.linusdev.lutils.nat.memory.stack.Stack;
import de.linusdev.lutils.nat.memory.stack.impl.DirectMemoryStack64;
import de.linusdev.lutils.nat.size.ByteUnits;
import de.linusdev.lutils.nat.size.Size;
import de.linusdev.lutils.thread.var.SyncVar;
import de.linusdev.lutils.thread.var.SyncVarImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static de.linusdev.cvg4j.nat.glfw3.GLFWValues.GLFW_TRUE;

/**
 * <h1>Implementation</h1>
 * <h2>Threads</h2>
 * <p>The engine has two main threads, an {@link #windowThread} for glfw related event polling
 * and an {@link #renderThread} for the actual rendering.</p>
 * <h2>Engine Death</h2>
 * <p>When the engine is supposed to die (e.g. user quits), the {@link #windowThread} will transmit
 * a {@link WindowCloseListener#onClose() close} event. The {@link #renderThread} will listen to this
 * event and delay the window close, until the already submitted render operations are completed
 * and the {@link #renderer} has successfully closed. The engine will wait until the render thread
 * has died and then close it's resources. During this close operation, the engine will also wait the render
 * thread until the window thread has died, if it did not already die. Thus the engine death is the same
 * as the death of the render thread.</p>
 */
public class VulkanEngine<GAME extends VulkanGame> implements
        Engine<GAME>,
        AsyncManager,
        UpdateListener,
        Tickable,
        RenderCommandsFunction
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
    private final @NotNull Device device;
    private final @NotNull SwapChain swapChain;
    private final @NotNull RenderPass renderPass;
    private final @NotNull GraphicsQueueTransientCommandPool transientCommandPool;

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
        DirectMemoryStack64 stack = new DirectMemoryStack64(new Size(100, ByteUnits.KiB));

        // Create Vulkan Instance
        instance = new Instance(stack, game, vulkanInfo);

        // Start ticker
        ticker.start();

        // Create window and renderer
        windowThread = new WindowThread<>(this, winThread -> new VulkanWindow(instance, null, winThread));
        window = windowThread.create().getResult();
        LOG.debug("Window and window thread created.");

        inputManger = new InputManagerImpl(window);

        renderer = new RasterizationRenderer(instance, window);
        renderThread = new RenderThread(this, renderer, window);

        device = VkEngineUtils.selectAndCreateDevice(stack, game, instance, window);
        swapChain = VkEngineUtils.createSwapChain(stack,
                game.surfaceFormatSelector(), game.presentModeSelector(), game.swapChainImageCountSelector(),
                instance, window, device, null
        );
        renderPass = RenderPass.create(stack, instance, device, swapChain);
        transientCommandPool = GraphicsQueueTransientCommandPool.create(this, stack, instance, device);

        renderThread.getThreadDeathFuture().then((win, secondary, error) -> {
            LOG.log(StandardLogLevel.DEBUG, "Render thread died.");

            if(error != null) {
                LOG.log(StandardLogLevel.ERROR, "Render thread died due to an error: ");
                LOG.throwable(error.asThrowable());
            }

            // cleanup
            currentScene.consumeIfNotNull(VkScene::close);
            transientCommandPool.close();
            renderPass.close();
            swapChain.close();

            // Wait for the window thread to die before closing the window.
            try {
                var res = windowThread.getThreadDeathFuture().get();
                LOG.log(StandardLogLevel.DEBUG, "Window thread died.");
                if(res.hasError()) {
                    LOG.log(StandardLogLevel.ERROR, "Render thread died due to an error: ");
                    LOG.throwable(res.getError().asThrowable());
                }
            } catch (InterruptedException e) {
                LOG.throwable(e);
            }
            // TODO: window.close() will call glfwDestroyWindow(), which should only be called on the window thread.
            // If this makes problems move it to the window thread.
            window.close();
            device.close();
            instance.close();

        });

        renderer.init(stack, device, renderPass, swapChain, 2, this);
        // Wait until the render thread is created
        this.renderThread.create().getResult();
        LOG.debug("Render thread created.");

    }

    @Override
    public @NotNull GAME getGame() {
        return game;
    }

    public @NotNull VkInstance getVkInstance() {
        return instance.getVkInstance();
    }

    public @NotNull Device getDevice() {
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
    public @NotNull <R> Future<R, VulkanEngine<GAME>> runSupervised(@NotNull AdvTRunnable<R, ?> runnable) {
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
        LOG.error("Exception in async listener:");
        LOG.throwable(throwable);
    }

    @Override
    public boolean available() {
        //TODO: not thread safe if currentScene turns from non-null to null
        return currentScene.get() != null;
    }

    @Override
    public void render(
            @NotNull Stack stack,
            int currentFrameBufferImageIndex,
            int currentFrame,
            @NotNull VkCommandBuffer commandBuffer
    ) {
        VkScene<?> scene = currentScene.get();
        if(scene != null) {
            scene.render(stack, instance.getVkInstance(), swapChain.getExtend(), currentFrameBufferImageIndex, currentFrame, commandBuffer, renderer.getFrameBuffers().getFrameBuffer(currentFrameBufferImageIndex));
        }
    }

    @Override
    public void recreateSwapChain(@NotNull Stack stack) {
        try {
            VkEngineUtils.createSwapChain(stack,
                    game.surfaceFormatSelector(), game.presentModeSelector(), game.swapChainImageCountSelector(),
                    instance, window, device, swapChain
            );
        } catch (EngineException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(@NotNull FrameInfo frameInfo) {
        VkScene<?> scene = currentScene.get();
        if(scene != null) {
            scene.update(frameInfo);
        }
    }

    public TQFuture<VkScene<GAME>> loadScene(@NotNull VkScene<GAME> scene) {
        var fut = renderThread.getTaskQueue().queueForExecution(LOAD_SCENE_TASK_ID, (stack) -> {
            try(var ignored = stack.safePoint()) {
                scene.onLoad0(stack, window, swapChain);
                RasterizationPipeline pipeLine = RasterizationPipeline.create(stack, instance.getVkInstance(), device, swapChain, renderPass, scene.pipeline(stack));
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

    public @NotNull GraphicsQueueTransientCommandPool getTransientCommandPool() {
        return transientCommandPool;
    }

    @Override
    public void tick() {
        VkScene<?> scene = currentScene.get();
        if(scene != null) {
            scene.tick();
        }
    }
}
