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

import de.linusdev.clgl.api.misc.interfaces.TRunnable;
import de.linusdev.clgl.window.CLGLWindow;
import de.linusdev.clgl.window.queue.ReturnRunnable;
import de.linusdev.lutils.async.Future;
import de.linusdev.lutils.async.Nothing;
import de.linusdev.lutils.async.manager.AsyncManager;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public interface Engine<G extends Game> {

    @NotNull Future<Nothing, Scene<G>> loadScene(@NotNull Scene<G> scene);

    @NotNull G getGame();

    @NotNull CLGLWindow getWindow();

    @NotNull AsyncManager getAsyncManager();

    <R> @NotNull Future<R, Engine<G>> runSupervised(@NotNull ReturnRunnable<R> runnable);

    void runSupervised(@NotNull TRunnable runnable);

}
