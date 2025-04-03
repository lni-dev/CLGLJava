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

package de.linusdev.ljgel.engine;

import de.linusdev.ljgel.GeneratedConstants;
import de.linusdev.ljgel.engine.info.Game;
import de.linusdev.ljgel.engine.info.HasGame;
import de.linusdev.llog.LLog;
import de.linusdev.llog.base.LogInstance;
import de.linusdev.lutils.async.Future;
import de.linusdev.lutils.async.Nothing;
import de.linusdev.lutils.async.manager.HasAsyncManager;
import de.linusdev.lutils.interfaces.AdvTRunnable;
import de.linusdev.lutils.interfaces.TRunnable;
import de.linusdev.lutils.version.Version;
import org.jetbrains.annotations.NotNull;

public interface Engine<GAME extends Game> extends HasAsyncManager, HasGame<GAME> {

    @NotNull LogInstance LOG = LLog.getLogInstance();

    <R> @NotNull Future<R, Nothing> runSupervised(@NotNull AdvTRunnable<R, ?> runnable);

    default @NotNull Future<Nothing, Nothing> runSupervisedV(@NotNull TRunnable<?> runnable) {
        return runSupervised(() -> {
            runnable.run();
            return Nothing.INSTANCE;
        });
    }

    /* ================================================================================================= *\
    |                                                                                                     |
    |                                             Engine Infos                                            |
    |                                                                                                     |
    \* ================================================================================================= */

    static @NotNull Version getVersion() {
        return GeneratedConstants.ENGINE_VERSION;
    }

    static @NotNull String getName() {
        return GeneratedConstants.ENGINE_NAME;
    }
}
