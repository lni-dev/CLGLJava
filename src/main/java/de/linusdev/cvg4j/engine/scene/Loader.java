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

package de.linusdev.cvg4j.engine.scene;

import de.linusdev.cvg4j.engine.exception.EngineException;
import de.linusdev.cvg4j.engine.ticker.Tickable;
import de.linusdev.lutils.nat.memory.stack.Stack;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public interface Loader extends Tickable {

    /**
     * Starts and executes the loading progress on the current thread. This method should return only after
     * the loading is finished.
     * @param stack {@link Stack}, which can be used for short term allocations
     * @throws EngineException can be thrown, will be logged and fail loading
     * @throws IOException can be thrown, will be logged and fail loading
     * @throws InterruptedException can be thrown, will be logged and fail loading
     */
    @Blocking
    void start(@NotNull Stack stack) throws EngineException, IOException, InterruptedException;

    /**
     * Current loading progress between {@code 0.0} and {@code 1.0}. If the progress is
     * unknown a negative number should be returned.
     */
    double progress();

}
