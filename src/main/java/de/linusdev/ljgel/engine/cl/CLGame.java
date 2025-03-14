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

import de.linusdev.ljgel.engine.info.Game;
import de.linusdev.lutils.async.error.AsyncError;
import org.jetbrains.annotations.NotNull;

public interface CLGame extends Game {

    /**
     *
     * @param error the error caused while loading the kernels
     * @return whether the loading should continue without kernels. {@code false} will fail the loading of the {@link CLScene}.
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    boolean onKernelLoadError(@NotNull AsyncError error);

    /**
     * Window title
     * @return {@link String} title
     */
    @NotNull String getTitle();

}
