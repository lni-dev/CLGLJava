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
import de.linusdev.ljgel.nat.Load;
import de.linusdev.ljgel.nat.NativeUtils;
import de.linusdev.ljgel.nat.abi.ABISelector;
import de.linusdev.lutils.async.Future;
import de.linusdev.lutils.async.Nothing;
import de.linusdev.lutils.async.manager.HasAsyncManager;
import de.linusdev.lutils.interfaces.AdvTRunnable;
import de.linusdev.lutils.interfaces.TConsumer;
import de.linusdev.lutils.interfaces.TFunction;
import de.linusdev.lutils.interfaces.TRunnable;
import de.linusdev.lutils.nat.memory.stack.Stack;
import de.linusdev.lutils.nat.struct.utils.BufferUtils;
import de.linusdev.lutils.version.Version;
import org.jetbrains.annotations.NotNull;

public interface Engine<GAME extends Game> extends HasAsyncManager, HasGame<GAME> {

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
        return GeneratedConstants.ENGINE_NAME;
    }

    @NotNull GAME getGame();

    <R> @NotNull Future<R, ? extends Engine<GAME>> runSupervised(@NotNull AdvTRunnable<R, ?> runnable);

    default @NotNull Future<Nothing, ? extends Engine<GAME>> runSupervisedV(@NotNull TRunnable<?> runnable) {
        return runSupervised(() -> {
            runnable.run();
            return Nothing.INSTANCE;
        });
    }

    @NotNull <R> Future<R, Nothing> runSupervised(@NotNull TFunction<Stack, R, ?> runnable);

    default @NotNull Future<Nothing,Nothing> runSupervisedV(@NotNull TConsumer<Stack, ?> runnable) {
        return runSupervised((stack) -> {
            runnable.consume(stack);
            return Nothing.INSTANCE;
        });
    }
}
