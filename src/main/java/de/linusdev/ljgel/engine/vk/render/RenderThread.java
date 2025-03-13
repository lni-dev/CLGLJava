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

package de.linusdev.ljgel.engine.vk.render;

import de.linusdev.ljgel.engine.exception.EngineException;
import de.linusdev.ljgel.engine.queue.TaskQueue;
import de.linusdev.ljgel.engine.vk.VulkanEngine;
import de.linusdev.ljgel.engine.vk.device.Device;
import de.linusdev.ljgel.engine.vk.swapchain.SwapChain;
import de.linusdev.ljgel.engine.vk.swapchain.SwapChainRecreationReturn;
import de.linusdev.ljgel.engine.vk.window.VulkanWindow;
import de.linusdev.ljgel.nat.vulkan.handles.VkInstance;
import de.linusdev.llog.LLog;
import de.linusdev.llog.base.LogInstance;
import de.linusdev.lutils.async.Future;
import de.linusdev.lutils.async.Nothing;
import de.linusdev.lutils.async.completeable.CompletableFuture;
import de.linusdev.lutils.async.completeable.CompletableTask;
import de.linusdev.lutils.async.error.ThrowableAsyncError;
import de.linusdev.lutils.nat.memory.stack.impl.DirectMemoryStack64;
import de.linusdev.lutils.thread.var.SyncVar;
import de.linusdev.lutils.thread.var.SyncVarImpl;
import org.jetbrains.annotations.NotNull;

import static de.linusdev.ljgel.engine.vk.utils.VkEngineUtils.ignoreInterrupts;

public class RenderThread extends Thread {

    public final @NotNull LogInstance LOG = LLog.getLogInstance();

    private final @NotNull CompletableFuture<Nothing, RenderThread, CompletableTask<Nothing, RenderThread>> creationFuture;
    private final @NotNull CompletableFuture<Nothing, RenderThread, CompletableTask<Nothing, RenderThread>> warmUpEndedFuture;
    private final @NotNull CompletableFuture<Nothing, RenderThread, CompletableTask<Nothing, RenderThread>> threadDeathFuture;

    private final @NotNull VulkanEngine<?> engine;
    private final @NotNull DirectMemoryStack64 stack;
    private final @NotNull TaskQueue taskQueue;
    private final @NotNull VkInstance vkInstance;
    private final @NotNull Device device;
    private final @NotNull SwapChain swapChain;
    private final @NotNull Renderer renderer;

    private volatile boolean warmUp = true;
    private final @NotNull Waiter taskQueueWaiter = new Waiter();
    private boolean shouldStop = false;

    public RenderThread(
            @NotNull VulkanEngine<?> engine,
            @NotNull SwapChain swapChain,
            @NotNull Renderer renderer,
            @NotNull VulkanWindow window
    ) {
        super("render-thread");
        this.engine = engine;
        this.vkInstance = engine.getVkInstance();
        this.device = engine.getDevice();
        this.swapChain = swapChain;
        this.renderer = renderer;

        this.creationFuture = CompletableFuture.create(engine.getAsyncManager(), false);
        this.threadDeathFuture = CompletableFuture.create(engine.getAsyncManager(), false);
        this.warmUpEndedFuture = CompletableFuture.create(engine.getAsyncManager(), false);

        this.taskQueue = new TaskQueue(engine.getAsyncManager(), fut -> taskQueueWaiter.signal(),20);

        this.stack = new DirectMemoryStack64();

        // Don't let the jvm shutdown
        setDaemon(false);

        window.listeners().addWindowCloseListener(() -> {
            ignoreInterrupts(() -> {
                taskQueue.queueForExecution(stack -> {
                    shouldStop = true;
                    renderer.waitIdle();
                    renderer.close();
                }).getResult();
            });
        });

        window.listeners().addWindowRefreshListener(() -> {
            // This is called during event polling on the window thread, if the window is currently resized
            // In order to avoid stuttering we have to wait the event polling until the next frame, with
            // the resized swap chain, is submitted
            ignoreInterrupts(() -> {
                taskQueue.queueForExecution(stack -> {
                    renderer.render(stack);
                }).get();
            });
        });

        window.listeners().addFramebufferSizeListener((width, height) -> {
            recreateSwapChain.set(true);
            if(width != 0 && height != 0) {
                minimized.set(false);
                taskQueueWaiter.signal();
            }
        });
        window.listeners().addWindowIconificationListener(minimized::set);
    }

    public @NotNull Future<Nothing, RenderThread> create() {
        start();
        return creationFuture;
    }

    public @NotNull Future<Nothing, RenderThread> endWarmUp() {
        warmUp = false;
        taskQueueWaiter.signal();
        return warmUpEndedFuture;
    }

    /*
     * Event related booleans
     */
    private final @NotNull SyncVar<@NotNull Boolean> recreateSwapChain = new SyncVarImpl<>(false);
    private final @NotNull SyncVar<@NotNull Boolean> minimized = new SyncVarImpl<>(false);

    @Override
    public void run() {
        try {
            creationFuture.complete(Nothing.INSTANCE, this, null);
        } catch (Throwable t) {
            threadDeathFuture.complete(null, this, new ThrowableAsyncError(t));
            return;
        }

        try {
            while (warmUp) {
                taskQueue.runQueuedTasks(stack);
                taskQueueWaiter.await();
            }
            warmUpEndedFuture.complete(Nothing.INSTANCE, this, null);
        }  catch (Throwable t) {
            warmUpEndedFuture.complete(null, this, new ThrowableAsyncError(t));
            threadDeathFuture.complete(null, this, new ThrowableAsyncError(t));
            return;
        }


        try {
            while (!shouldStop) {

                taskQueue.runQueuedTasks(stack);

                if(shouldStop)
                    break;

                // Check if window is minimized or the swapChain needs to be recreated ...
                taskQueueWaiter.awaitIf(
                        // TODO: This code really isn't beautiful, make it beautiful.
                        () -> minimized.computeSynchronised(ignored -> {
                            recreateSwapChain.doSynchronised(ignored2 -> {
                                if (recreateSwapChain.get() && !minimized.get()) {
                                    recreateSwapChain.set(false);
                                    vkInstance.vkDeviceWaitIdle(device.getVkDevice());

                                    try {
                                        if (swapChain.recreate(stack, engine.getCurrentSwapChainSelectors()) == SwapChainRecreationReturn.ERROR_ZERO_AREA) {
                                            minimized.set(true);
                                        }
                                    } catch (EngineException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                            });

                            return minimized.get();
                        })
                );

                renderer.render(stack);
            }

            threadDeathFuture.complete(Nothing.INSTANCE, this, null);
        } catch (Throwable t) {
            threadDeathFuture.complete(null, this, new ThrowableAsyncError(t));
        }
    }

    public @NotNull TaskQueue getTaskQueue() {
        return taskQueue;
    }

    public @NotNull Future<Nothing, RenderThread> getThreadDeathFuture() {
        return threadDeathFuture;
    }

}
