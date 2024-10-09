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
import de.linusdev.cvg4j.engine.queue.TQFuture;
import de.linusdev.cvg4j.engine.scene.Loader;
import de.linusdev.cvg4j.engine.scene.loaders.NoOpLoader;
import de.linusdev.cvg4j.engine.vk.VulkanEngine;
import de.linusdev.cvg4j.engine.vk.VulkanGame;
import de.linusdev.cvg4j.engine.vk.scene.VkScene;
import de.linusdev.lutils.async.Nothing;
import de.linusdev.lutils.nat.memory.stack.Stack;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.NonBlocking;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public abstract class SimpleScene<GAME extends VulkanGame> extends VkScene<GAME> {
    protected SimpleScene(@NotNull VulkanEngine<GAME> engine) {
        super(engine);
    }

    @Blocking
    void load0(@NotNull Stack stack) throws InterruptedException, IOException, EngineException {
        TQFuture<Nothing> fut = window.getWindowThread().getTaskQueue().queueForExecution(this::setupWindow);
        load(stack, fut);
        fut.getResult();
    }

    @NonBlocking
    abstract protected void setupWindow(@NotNull Stack stack);

    @Blocking
    abstract protected void load(@NotNull Stack stack, @NotNull TQFuture<Nothing> windowSetupFuture) throws InterruptedException, IOException, EngineException ;

    @Override
    public @NotNull Loader loader() {
        return new SimpleSceneLoader(this);
    }

    @Override
    public @NotNull Loader releaser() {
        return new NoOpLoader();
    }
}
