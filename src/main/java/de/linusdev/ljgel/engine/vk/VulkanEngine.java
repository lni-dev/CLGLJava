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

package de.linusdev.ljgel.engine.vk;

import de.linusdev.ljgel.engine.Engine;
import de.linusdev.ljgel.engine.exception.EngineException;
import de.linusdev.ljgel.engine.queue.TaskQueue;
import de.linusdev.ljgel.engine.scene.Loader;
import de.linusdev.ljgel.engine.scene.State;
import de.linusdev.ljgel.engine.ticker.TickerImpl;
import de.linusdev.ljgel.engine.vk.async.VkAsyncManager;
import de.linusdev.ljgel.engine.vk.command.pool.GraphicsQueueTransientCommandPool;
import de.linusdev.ljgel.engine.vk.device.Device;
import de.linusdev.ljgel.engine.vk.instance.Instance;
import de.linusdev.ljgel.engine.vk.render.RenderThread;
import de.linusdev.ljgel.engine.vk.renderer.rast.RasterizationRenderer;
import de.linusdev.ljgel.engine.vk.renderpass.RenderPassHolder;
import de.linusdev.ljgel.engine.vk.scene.LoadedScene;
import de.linusdev.ljgel.engine.vk.scene.SceneHolder;
import de.linusdev.ljgel.engine.vk.scene.VkScene;
import de.linusdev.ljgel.engine.vk.selector.VulkanEngineInfo;
import de.linusdev.ljgel.engine.vk.selector.swapchain.HasSwapChainSelectors;
import de.linusdev.ljgel.engine.vk.swapchain.SwapChain;
import de.linusdev.ljgel.engine.vk.utils.VkEngineUtils;
import de.linusdev.ljgel.engine.vk.window.VulkanWindow;
import de.linusdev.ljgel.engine.window.WindowThread;
import de.linusdev.ljgel.engine.window.input.InputManagerImpl;
import de.linusdev.ljgel.engine.window.input.InputManger;
import de.linusdev.ljgel.nat.glfw3.GLFW;
import de.linusdev.ljgel.nat.glfw3.custom.WindowCloseListener;
import de.linusdev.ljgel.nat.vulkan.handles.VkInstance;
import de.linusdev.llog.LLog;
import de.linusdev.llog.base.LogInstance;
import de.linusdev.llog.base.impl.StandardLogLevel;
import de.linusdev.lutils.async.Future;
import de.linusdev.lutils.async.Nothing;
import de.linusdev.lutils.async.completeable.CompletableFuture;
import de.linusdev.lutils.async.error.ThrowableAsyncError;
import de.linusdev.lutils.async.manager.AsyncManager;
import de.linusdev.lutils.async.manager.HasAsyncManager;
import de.linusdev.lutils.interfaces.AdvTRunnable;
import de.linusdev.lutils.interfaces.TFunction;
import de.linusdev.lutils.nat.memory.stack.Stack;
import de.linusdev.lutils.nat.memory.stack.StackFactory;
import de.linusdev.lutils.nat.memory.stack.impl.DirectMemoryStack64;
import de.linusdev.lutils.nat.size.ByteUnits;
import de.linusdev.lutils.nat.size.Size;
import de.linusdev.lutils.thread.pool.ThreadWithStackPool;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static de.linusdev.ljgel.nat.glfw3.GLFWValues.GLFW_TRUE;

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
        HasAsyncManager
{

    public final static @NotNull LogInstance LOG = LLog.getLogInstance();

    private static final int LOAD_SCENE_TASK_ID = TaskQueue.getUniqueTaskId("LOAD_SCENE");

    private final @NotNull GAME game;

    private final @NotNull VkAsyncManager asyncManager;

    private final @NotNull Executor executor = Executors.newWorkStealingPool(16);
    private final @NotNull ThreadWithStackPool threadWithStackPool;
    private final @NotNull TickerImpl ticker;
    private final @NotNull InputManger inputManger;

    private final @NotNull WindowThread<VulkanWindow> windowThread;
    private final @NotNull VulkanWindow window;
    private final @NotNull RasterizationRenderer renderer;
    private final @NotNull RenderThread renderThread;

    private final @NotNull VulkanEngineInfo vulkanInfo;

    private final @NotNull Instance instance;
    private final @NotNull Device device;
    private final @NotNull SwapChain swapChain;
    private final @NotNull GraphicsQueueTransientCommandPool transientCommandPool;

    private final @NotNull SceneHolder currentScene;
    private final @NotNull RenderPassHolder currentRenderPass;

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
        this.asyncManager = new VkAsyncManager();
        this.threadWithStackPool = new ThreadWithStackPool(1, 10000, this.asyncManager, Thread::new, StackFactory.DEFAULT);
        this.vulkanInfo = new VulkanEngineInfo();
        this.game = game;


        // Create a small stack for short-lived structures
        DirectMemoryStack64 stack = new DirectMemoryStack64(new Size(100, ByteUnits.KiB));

        // Create Vulkan Instance
        instance = new Instance(stack, game, vulkanInfo);



        // Create window and renderer
        windowThread = new WindowThread<>(this, winThread -> new VulkanWindow(instance, null, winThread));
        window = windowThread.create().getResult();
        LOG.debug("Window and window thread created.");

        inputManger = new InputManagerImpl(window);

        device = VkEngineUtils.selectAndCreateDevice(stack, game, instance, window);
        transientCommandPool = GraphicsQueueTransientCommandPool.create(this, stack, instance, device);
        swapChain = VkEngineUtils.createSwapChain(stack, game, instance, window, device);

        renderer = new RasterizationRenderer(instance, window);
        renderThread = new RenderThread(this, swapChain, renderer, window);
        // Wait until the render thread is created
        renderThread.create().getResult();

        // Load the start scene.
        try {
            VkScene<?> scene = game.startScene(this);
            Loader loader = scene.loader();
            scene.currentState().set(State.LOADING);
            loader.start(stack);
            scene.currentState().set(State.LOADED);
            currentScene = new SceneHolder(scene, instance, swapChain, renderer);
            currentRenderPass = new RenderPassHolder(scene.getRenderPass());
            scene.currentState().set(State.RENDERING);
        } catch (IOException e) {
            throw new EngineException(e);
        }

        renderThread.getThreadDeathFuture().then((win, secondary, error) -> {
            LOG.log(StandardLogLevel.DEBUG, "Render thread died.");

            if(error != null) {
                LOG.log(StandardLogLevel.ERROR, "Render thread died due to an error: ");
                LOG.throwable(error.asThrowable());
            }

            // cleanup
            currentScene.consumeIfNotNull(VkScene::close);
            transientCommandPool.close();
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



        ticker = new TickerImpl(game.getMillisPerTick());
        ticker.start();
        ticker.addTickable(currentScene);

        renderer.init(stack, device, currentRenderPass, swapChain, 2, currentScene);
        // Wait until render thread is in the main render loop
        renderThread.endWarmUp().getResult();
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

    public @NotNull SwapChain getSwapChain() {
        return swapChain;
    }

    public @NotNull VulkanWindow getWindow() {
        return window;
    }

    public @NotNull InputManger getInputManger() {
        return inputManger;
    }

    @Override
    public @NotNull AsyncManager getAsyncManager() {
        return asyncManager;
    }

    public @NotNull RenderThread getRenderThread() {
        return renderThread;
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
    public @NotNull <R> Future<R, Nothing> runSupervised(@NotNull TFunction<Stack, R, ?> runnable) {
        return threadWithStackPool.execute(runnable);
    }

    public <G extends VulkanGame> Future<LoadedScene<VkScene<G>>, Nothing> loadScene(@NotNull VkScene<G> scene) {

        var fut = CompletableFuture.<LoadedScene<VkScene<G>>, Nothing>create(getAsyncManager(), false);
        Loader loader = scene.loader();

       runSupervisedV((stack) -> {
           scene.currentState().set(State.LOADING);
           ticker.addTickable(loader);
           loader.start(stack);
       }).then((result, secondary, error) -> {
            if(error != null) {
                LOG.throwable(error.asThrowable());
                fut.complete(null, Nothing.INSTANCE, error);
                return;
            }

            scene.currentState().set(State.LOADED);

            fut.complete(new LoadedScene<>(scene, s -> renderThread.getTaskQueue().queueForExecution(LOAD_SCENE_TASK_ID, renderThreadStack -> {
                // Wait until the old resources (Frame buffers) are not used anymore
                renderer.waitIdle();

                // Swap current render pass
                currentRenderPass.swap(renderThreadStack, scene.getRenderPass());

                // Swap current scene
                VkScene<?> oldScene = currentScene.get();
                currentScene.set(scene);
                ticker.removeTickable(loader);
                scene.currentState().set(State.RENDERING);

                // Release resources of the old scene async
                Loader releaser = oldScene.releaser();
                runSupervisedV((stack) -> {
                    oldScene.currentState().set(State.RELEASING);
                    ticker.addTickable(releaser);
                    releaser.start(stack);
                    oldScene.close();
                    ticker.removeTickable(releaser);
                    oldScene.currentState().set(State.CLOSED);
                });

                return scene;
            })), Nothing.INSTANCE, null);

        });

        return fut;
    }

    public @NotNull Future<Nothing, VulkanEngine<GAME>> getEngineDeathFuture() {
        var fut = CompletableFuture.<Nothing, VulkanEngine<GAME>>create(asyncManager, false);

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

    public @NotNull HasSwapChainSelectors getCurrentSwapChainSelectors() {
        return game;
    }
}
