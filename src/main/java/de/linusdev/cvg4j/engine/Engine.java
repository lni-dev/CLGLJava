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

package de.linusdev.cvg4j.engine;

import de.linusdev.cvg4j.api.misc.interfaces.TRunnable;
import de.linusdev.cvg4j.nat.ABISelector;
import de.linusdev.cvg4j.nat.cl.objects.Context;
import de.linusdev.cvg4j.window.CLGLWindow;
import de.linusdev.cvg4j.window.input.InputManagerImpl;
import de.linusdev.cvg4j.window.input.InputManger;
import de.linusdev.cvg4j.window.queue.ReturnRunnable;
import de.linusdev.lutils.async.Future;
import de.linusdev.lutils.async.Nothing;
import de.linusdev.lutils.async.manager.AsyncManager;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public interface Engine<G extends Game> {

    class StaticSetup {
        private static boolean staticSetupDone = false;

        private static void checkSetup() {
            if(!staticSetupDone)
                throw new IllegalStateException("Engine.staticSetup() must be called as first line in main.");
        }
    }


    static void staticSetup() {
        ABISelector.retrieveAndSetDefaultABI();
        StaticSetup.staticSetupDone = true;
    }

    /**
     * Creates a {@link Engine} instance. This will also create the window.
     * Call {@link Engine#loadScene(Scene)} to load your {@link Scene}.
     * @param game your {@link Game}
     * @return {@link Engine}
     * @param <T> your {@link Game}
     */
    static <T extends Game> @NotNull Engine<T> getInstance(@NotNull T game) {
        StaticSetup.checkSetup();
        return new EngineImpl<>(game);
    }

    @NotNull Future<Nothing, Scene<G>> loadScene(@NotNull Scene<G> scene);

    @ApiStatus.Internal
    @NotNull InputManagerImpl createInputManagerForScene(@NotNull Scene<G> scene);

    @NotNull G getGame();

    @NotNull CLGLWindow getWindow();

    /**
     * {@link InputManger} to be used across all scenes.
     * @return {@link InputManger}
     */
    @NotNull InputManger getGlobalInputManager();

    @NotNull Context getClContext();

    @NotNull AsyncManager getAsyncManager();

    @NotNull UIThread<G> getUIThread();

    <R> @NotNull Future<R, Engine<G>> runSupervised(@NotNull ReturnRunnable<R> runnable);

    void runSupervised(@NotNull TRunnable runnable);

}
