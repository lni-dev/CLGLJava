/*
 * Copyright (c) 2023 Linus Andera
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

package de.linusdev.clgl.engine;

import de.linusdev.clgl.window.CLGLWindow;
import de.linusdev.clgl.window.Handler;
import de.linusdev.llog.LLog;
import de.linusdev.llog.base.LogInstance;
import de.linusdev.lutils.async.Future;
import de.linusdev.lutils.async.completeable.CompletableFuture;
import de.linusdev.lutils.async.completeable.CompletableTask;
import de.linusdev.lutils.async.error.ThrowableAsyncError;
import org.jetbrains.annotations.NotNull;

public class UIThread<G extends Game> extends Thread implements HasEngine<G> {

    private final static @NotNull LogInstance log = LLog.getLogInstance();

    private final @NotNull Engine<G> engine;
    private final @NotNull Handler handler;
    private final CompletableFuture<CLGLWindow, UIThread<G>, CompletableTask<CLGLWindow, UIThread<G>>> future;

    public UIThread(@NotNull Engine<G> engine, @NotNull Handler handler) {
        super("glfw-ui-thread");
        setDaemon(false);
        this.engine = engine;
        this.handler = handler;
        this.future = CompletableFuture.create(engine.getAsyncManager());
    }

    public @NotNull Future<CLGLWindow, UIThread<G>> create() {
        start();
        return future;
    }

    @Override
    public void run() {
        CLGLWindow window;
        try {
            window = new CLGLWindow(handler, 5);
            future.complete(window, this, null);
        } catch (Throwable t) {
            future.complete(null, this, new ThrowableAsyncError(t));
            return;
        }

        try {
            window.show();
            window.close();
        } catch (Throwable t) {
            log.logThrowable(t);
        }
    }

    @Override
    public @NotNull Engine<G> getEngine() {
        return engine;
    }
}
