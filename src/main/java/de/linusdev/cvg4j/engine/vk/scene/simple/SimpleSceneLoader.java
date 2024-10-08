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

package de.linusdev.cvg4j.engine.vk.scene.simple;

import de.linusdev.cvg4j.engine.exception.EngineException;
import de.linusdev.cvg4j.engine.scene.Loader;
import de.linusdev.cvg4j.engine.ticker.Ticker;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class SimpleSceneLoader implements Loader {

    private final @NotNull SimpleScene<?> scene;

    public SimpleSceneLoader(@NotNull SimpleScene<?> scene) {
        this.scene = scene;
    }

    @Override
    public void start() throws EngineException, IOException, InterruptedException {
        scene.load0();
    }

    @Override
    public double progress() {
        return -1.0;
    }

    @Override
    public void tick(@NotNull Ticker ticker) {

    }
}
