/*
 * Copyright (c) 2023-2025 Linus Andera
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

package de.linusdev.ljgel.engine.cl;

import de.linusdev.ljgel.engine.cl.window.CLGLWindow;
import de.linusdev.ljgel.engine.cl.window.Handler;
import de.linusdev.llog.LLog;
import de.linusdev.llog.base.LogInstance;
import de.linusdev.lutils.async.Future;
import de.linusdev.lutils.async.Nothing;
import de.linusdev.lutils.async.completeable.CompletableFuture;
import de.linusdev.lutils.async.completeable.CompletableTask;
import de.linusdev.lutils.async.error.ThrowableAsyncError;
import org.jetbrains.annotations.NotNull;

public class UIThread<G extends CLGame> extends Thread implements HasCLEngine<G> {

    private final static @NotNull LogInstance log = LLog.getLogInstance();

    private final @NotNull CLEngine<G> engine;
    private final @NotNull Handler handler;
    private final CompletableFuture<CLGLWindow, UIThread<G>, CompletableTask<CLGLWindow, UIThread<G>>> createWindowFuture;
    private final CompletableFuture<Nothing, UIThread<G>, CompletableTask<Nothing, UIThread<G>>> windowClosedFuture;

    public UIThread(@NotNull CLEngine<G> engine, @NotNull Handler handler) {
        super("glfw-ui-thread");
        setDaemon(false);
        this.engine = engine;
        this.handler = handler;
        this.createWindowFuture = CompletableFuture.create(engine.getAsyncManager(), false);
        this.windowClosedFuture = CompletableFuture.create(engine.getAsyncManager(), false);
    }

    public @NotNull Future<CLGLWindow, UIThread<G>> create() {
        start();
        return createWindowFuture;
    }

    public @NotNull Future<Nothing, UIThread<G>> getWindowClosedFuture() {
        return windowClosedFuture;
    }

    @Override
    public void run() {
        CLGLWindow window;
        try {
            window = new CLGLWindow(handler, 5);
            createWindowFuture.complete(window, this, null);
        } catch (Throwable t) {
            createWindowFuture.complete(null, this, new ThrowableAsyncError(t));
            return;
        }

        try {
            window.show();
            window.close();
            windowClosedFuture.complete(Nothing.INSTANCE, this, null);
        } catch (Throwable t) {
            log.logThrowable(t);
            windowClosedFuture.complete(null, this, new ThrowableAsyncError(t));
        }
    }

    @Override
    public @NotNull CLEngine<G> getEngine() {
        return engine;
    }
}
