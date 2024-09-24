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

package de.linusdev.cvg4j.engine;

import de.linusdev.cvg4j.de.linusdev.cvg4j.GeneratedConstants;
import de.linusdev.cvg4j.engine.info.Game;
import de.linusdev.cvg4j.nat.Load;
import de.linusdev.cvg4j.nat.NativeUtils;
import de.linusdev.cvg4j.nat.abi.ABISelector;
import de.linusdev.lutils.async.Future;
import de.linusdev.lutils.async.Nothing;
import de.linusdev.lutils.async.manager.HasAsyncManager;
import de.linusdev.lutils.interfaces.AdvTRunnable;
import de.linusdev.lutils.interfaces.TRunnable;
import de.linusdev.lutils.nat.struct.utils.BufferUtils;
import de.linusdev.lutils.version.Version;
import org.jetbrains.annotations.NotNull;

public interface Engine<GAME extends Game> extends HasAsyncManager {

    class StaticSetup {

        static {
            setup();
        }

        private static boolean staticSetupDone = false;

        public static synchronized void checkSetup() {
            if(!staticSetupDone)
                throw new IllegalStateException("CLEngine.StaticSetup.setup() must be called as first line in main.");
        }

        public static synchronized void setup() {
            if(staticSetupDone)
                return;
            ABISelector.retrieveAndSetDefaultABI();
            Load.init();
            BufferUtils.setByteBufferFromPointerMethod(NativeUtils::getBufferFromPointer);
            staticSetupDone = true;
        }
    }

    static @NotNull Version version() {
        return GeneratedConstants.ENGINE_VERSION;
    }

    static @NotNull String name() {
        return "CVG4J";
    }

    @NotNull GAME getGame();

    <R> @NotNull Future<R, ? extends Engine<GAME>> runSupervised(@NotNull AdvTRunnable<R, ?> runnable);

    default @NotNull Future<Nothing, ? extends Engine<GAME>> runSupervisedV(@NotNull TRunnable<?> runnable) {
        return runSupervised(() -> {
            runnable.run();
            return Nothing.INSTANCE;
        });
    }
}
