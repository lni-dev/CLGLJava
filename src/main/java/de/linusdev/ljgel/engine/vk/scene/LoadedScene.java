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

package de.linusdev.ljgel.engine.vk.scene;

import de.linusdev.ljgel.engine.scene.Scene;
import de.linusdev.lutils.async.Future;
import de.linusdev.lutils.async.Nothing;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class LoadedScene<S extends Scene> {

    private final @NotNull S scene;
    private final @NotNull Function<S, Future<S, Nothing>> activator;

    public LoadedScene(
            @NotNull S scene,
            @NotNull Function<S, Future<S, Nothing>> activator
    ) {
        this.scene = scene;
        this.activator = activator;
    }

    public @NotNull Future<S, Nothing> activate() {
        return activator.apply(scene);
    }
}
