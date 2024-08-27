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

package de.linusdev.cvg4j.nengine;

import de.linusdev.cvg4j.api.misc.annos.CallOnlyFromUIThread;
import de.linusdev.cvg4j.nengine.info.Game;
import de.linusdev.lutils.async.Future;
import de.linusdev.lutils.async.Nothing;
import de.linusdev.lutils.async.completeable.CompletableFuture;
import de.linusdev.lutils.async.completeable.CompletableTask;
import de.linusdev.lutils.async.error.ThrowableAsyncError;
import de.linusdev.lutils.interfaces.TBiConsumer;
import de.linusdev.lutils.interfaces.TFunction;
import de.linusdev.lutils.interfaces.TriConsumer;
import de.linusdev.lutils.nat.memory.DirectMemoryStack64;
import org.jetbrains.annotations.NotNull;

public class RenderThread<GAME extends Game, CR, WINDOW> extends Thread {


    private final @NotNull CompletableFuture<CR, Nothing, CompletableTask<CR, Nothing>> creationFuture;
    private final @NotNull CompletableFuture<WINDOW, RenderThread<GAME, CR, WINDOW>, CompletableTask<WINDOW, RenderThread<GAME, CR, WINDOW>>> threadDeathFuture;
    private final @NotNull TFunction<RenderThread<GAME, CR, WINDOW>, WINDOW, Throwable> windowSupplier;
    private final @NotNull TriConsumer<RenderThread<GAME, CR, WINDOW>, WINDOW, CompletableFuture<CR, Nothing, CompletableTask<CR, Nothing>>> completeCreationFuture;
    private final @NotNull TBiConsumer<RenderThread<GAME, CR, WINDOW>, WINDOW, Throwable> loop;

    private final @NotNull Engine<GAME> engine;
    private final @NotNull DirectMemoryStack64 stack;

    public RenderThread(
            @NotNull Engine<GAME> engine,
            @NotNull TFunction<RenderThread<GAME, CR, WINDOW>, WINDOW, Throwable> windowSupplier,
            @NotNull TriConsumer<RenderThread<GAME, CR, WINDOW>, WINDOW, CompletableFuture<CR, Nothing, CompletableTask<CR, Nothing>>> completeCreationFuture,
            @NotNull TBiConsumer<RenderThread<GAME, CR, WINDOW>, WINDOW, Throwable> loop
    ) {
        this.engine = engine;
        this.windowSupplier = windowSupplier;
        this.loop = loop;
        this.completeCreationFuture = completeCreationFuture;
        this.creationFuture = CompletableFuture.create(engine.getAsyncManager(), false);
        this.threadDeathFuture = CompletableFuture.create(engine.getAsyncManager(), false);
        this.stack = new DirectMemoryStack64();

        // Don't let the jvm shutdown
        setDaemon(false);
    }

    public @NotNull Future<CR, Nothing> create() {
        start();
        return creationFuture;
    }

    @Override
    public void run() {
        try {
            WINDOW window = windowSupplier.apply(this);

            completeCreationFuture.consume(this, window, creationFuture);

            loop.consume(this, window);

            threadDeathFuture.complete(window, this, null);
        } catch (Throwable t) {
            creationFuture.complete(null, Nothing.INSTANCE, new ThrowableAsyncError(t));
            threadDeathFuture.complete(null, this, new ThrowableAsyncError(t));
        }
    }

    public @NotNull Future<WINDOW, RenderThread<GAME, CR, WINDOW>> getThreadDeathFuture() {
        return threadDeathFuture;
    }

    @CallOnlyFromUIThread("render-thread")
    public @NotNull DirectMemoryStack64 getStack() {
        assert Thread.currentThread() == this;
        return stack;
    }
}
