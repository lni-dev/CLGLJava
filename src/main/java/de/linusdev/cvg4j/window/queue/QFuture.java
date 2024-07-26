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

package de.linusdev.cvg4j.window.queue;

import de.linusdev.cvg4j.window.CLGLWindow;
import de.linusdev.lutils.async.completeable.CompletableFuture;
import de.linusdev.lutils.async.completeable.CompletableTask;
import de.linusdev.lutils.async.error.ThrowableAsyncError;
import de.linusdev.lutils.async.manager.AsyncManager;
import org.jetbrains.annotations.NotNull;

public class QFuture<T> extends CompletableFuture<T, CLGLWindow, CompletableTask<T, CLGLWindow>> {

    protected final @NotNull ReturnRunnable<T> runnable;

    public QFuture(@NotNull AsyncManager asyncManager, @NotNull ReturnRunnable<T> runnable) {
        super(asyncManager, false);
        this.runnable = runnable;
    }

    public void run(@NotNull CLGLWindow window) {
        try {
            T ret = runnable.run();
            complete(ret, window, null);
        } catch (Throwable t) {
            complete(null, window, new ThrowableAsyncError(t));
        }
    }
}
