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

package de.linusdev.cvg4j.engine.window;

import de.linusdev.cvg4j.engine.Engine;
import de.linusdev.cvg4j.engine.queue.TaskQueue;
import de.linusdev.cvg4j.nat.glfw3.GLFW;
import de.linusdev.cvg4j.nat.glfw3.objects.GLFWWindow;
import de.linusdev.lutils.async.Future;
import de.linusdev.lutils.async.completeable.CompletableFuture;
import de.linusdev.lutils.async.completeable.CompletableTask;
import de.linusdev.lutils.async.error.ThrowableAsyncError;
import de.linusdev.lutils.interfaces.TFunction;
import de.linusdev.lutils.nat.memory.stack.Stack;
import de.linusdev.lutils.nat.memory.stack.impl.DirectMemoryStack64;
import org.jetbrains.annotations.NotNull;

public class WindowThread<W extends GLFWWindow> extends Thread {

    private final @NotNull TFunction<WindowThread<W>, W, Throwable> windowSupplier;

    private final @NotNull CompletableFuture<W, WindowThread<W>, CompletableTask<W, WindowThread<W>>> creationFuture;
    private final @NotNull CompletableFuture<W, WindowThread<W>, CompletableTask<W, WindowThread<W>>> threadDeathFuture;

    private final @NotNull TaskQueue taskQueue;
    private final @NotNull Stack stack;

    public WindowThread(
            @NotNull Engine<?> engine,
            @NotNull TFunction<WindowThread<W>, W, Throwable> windowSupplier
    ){
        super("glfw-window-thread");
        this.windowSupplier = windowSupplier;
        this.stack = new DirectMemoryStack64();

        this.creationFuture = CompletableFuture.create(engine.getAsyncManager(), false);
        this.threadDeathFuture = CompletableFuture.create(engine.getAsyncManager(), false);

        this.taskQueue = new TaskQueue(engine.getAsyncManager(), fut -> GLFW.glfwPostEmptyEvent(), 20);
    }

    public @NotNull Future<W, WindowThread<W>> create() {
        start();
        return creationFuture;
    }

    public @NotNull Future<W, WindowThread<W>> getThreadDeathFuture() {
        return threadDeathFuture;
    }

    public @NotNull Stack getStack() {
        return stack;
    }

    public @NotNull TaskQueue getTaskQueue() {
        return taskQueue;
    }

    @Override
    public void run() {
        try {
            W window = windowSupplier.apply(this);
            creationFuture.complete(window, this, null);

            window.listeners().addWindowRefreshListener(() -> taskQueue.runQueuedTasks(stack));
            window.eventLoop(frameInfo -> taskQueue.runQueuedTasks(stack));
            threadDeathFuture.complete(window, this, null);
        } catch (Throwable t) {
            creationFuture.complete(null, this, new ThrowableAsyncError(t));
            threadDeathFuture.complete(null, this, new ThrowableAsyncError(t));
        }
    }

}
