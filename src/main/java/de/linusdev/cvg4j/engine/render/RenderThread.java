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

package de.linusdev.cvg4j.engine.render;

import de.linusdev.cvg4j.engine.Engine;
import de.linusdev.cvg4j.engine.queue.TaskQueue;
import de.linusdev.lutils.async.Future;
import de.linusdev.lutils.async.Nothing;
import de.linusdev.lutils.async.completeable.CompletableFuture;
import de.linusdev.lutils.async.completeable.CompletableTask;
import de.linusdev.lutils.async.error.ThrowableAsyncError;
import de.linusdev.lutils.nat.memory.stack.impl.DirectMemoryStack64;
import org.jetbrains.annotations.NotNull;

public class RenderThread extends Thread {

    private final @NotNull CompletableFuture<Nothing, RenderThread, CompletableTask<Nothing, RenderThread>> creationFuture;
    private final @NotNull CompletableFuture<Nothing, RenderThread, CompletableTask<Nothing, RenderThread>> threadDeathFuture;

    private final @NotNull DirectMemoryStack64 stack;
    private final @NotNull TaskQueue taskQueue;
    private final @NotNull Renderer renderer;

    private boolean running = true;

    public RenderThread(
            @NotNull Engine<?> engine,
            @NotNull Renderer renderer
    ) {
        super("render-thread");

        this.creationFuture = CompletableFuture.create(engine.getAsyncManager(), false);
        this.threadDeathFuture = CompletableFuture.create(engine.getAsyncManager(), false);
        this.renderer = renderer;

        this.taskQueue = new TaskQueue(engine.getAsyncManager(), 20);
        this.stack = new DirectMemoryStack64();

        // Don't let the jvm shutdown
        setDaemon(false);
    }

    public @NotNull Future<Nothing, RenderThread> create() {
        start();
        return creationFuture;
    }

    @Override
    public void run() {
        try {
            creationFuture.complete(Nothing.INSTANCE, this, null);
        } catch (Throwable t) {
            threadDeathFuture.complete(null, this, new ThrowableAsyncError(t));
            return;
        }


        try {
            while (running) {
                renderer.render(stack);
                taskQueue.runQueuedTasks(stack);
            }

            threadDeathFuture.complete(Nothing.INSTANCE, this, null);
        } catch (Throwable t) {

            threadDeathFuture.complete(null, this, new ThrowableAsyncError(t));
        }
    }

    public @NotNull Future<Nothing, RenderThread> getThreadDeathFuture() {
        return threadDeathFuture;
    }

}
