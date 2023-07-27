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

import de.linusdev.clgl.api.misc.annos.CallOnlyFromUIThread;
import de.linusdev.clgl.engine.kernel.source.KernelSourceInfo;
import de.linusdev.clgl.engine.ticker.Tickable;
import de.linusdev.clgl.nat.cl.objects.Kernel;
import de.linusdev.clgl.nat.glfw3.custom.FrameInfo;
import de.linusdev.clgl.nat.glfw3.custom.UpdateListener;
import de.linusdev.clgl.window.Handler;
import de.linusdev.lutils.async.Future;
import de.linusdev.lutils.async.completeable.CompletableFuture;
import org.jetbrains.annotations.*;

import java.util.concurrent.atomic.AtomicBoolean;

public abstract class Scene<GAME extends Game> implements
        HasEngine<GAME>,
        UpdateListener<Engine<GAME>>,
        Tickable
{

    private final @NotNull Engine<GAME> engine;

    protected final @NotNull AtomicBoolean loaded = new AtomicBoolean(false);

    protected Scene(@NotNull Engine<GAME> engine) {
        this.engine = engine;
    }




    abstract @Nullable KernelSourceInfo getUIKernelInfo();

    abstract @Nullable KernelSourceInfo getRenderKernelInfo();

    /**
     * @see Handler#setRenderKernelArgs(Kernel)
     */
    abstract void setRenderKernelArgs(@NotNull Kernel renderKernel);

    /**
     * @see Handler#setUIKernelArgs(Kernel) (Kernel)
     */
    abstract void setUIKernelArgs(@NotNull Kernel uiKernel);

    /**
     * Load required resources, textures, etc for the current scene
     */
    @ApiStatus.OverrideOnly
    @Blocking
    abstract protected void load();

    /**
     * Scene is about to end. Unload resources. The scene may already switch to the new
     * scene during unloading.
     */
    @ApiStatus.OverrideOnly
    @Blocking
    abstract protected void unload();

    /**
     * Called after the scene has finished {@link #load() loading} but before the first {@link #tick() tick}.
     */
    @ApiStatus.OverrideOnly
    @NonBlocking
    public abstract void start();

    @ApiStatus.OverrideOnly
    @NonBlocking
    @Override
    abstract public void tick();

    /**
     * Called once per frame. Runs on the UI thread!
     * @param engine {@link Engine}
     * @param frameInfo {@link FrameInfo}
     */
    @CallOnlyFromUIThread("glfw")
    @ApiStatus.OverrideOnly
    @Override
    public abstract void update(@NotNull Engine<GAME> engine, @NotNull FrameInfo frameInfo);





    @ApiStatus.Internal
    @NonBlocking
    @NotNull Future<Void, Scene<GAME>> load0() {
        var future = CompletableFuture.<Void, Scene<GAME>>create(engine.getAsyncManager());

        engine.runSupervised(() -> {
            //noinspection BlockingMethodInNonBlockingContext: run in new thread
            load();
            future.complete(null, this, null);
        });

        return future;
    }

    @SuppressWarnings("UnusedReturnValue")
    @ApiStatus.Internal
    @NonBlocking
    @NotNull Future<Void, Scene<GAME>> unload0() {
        var future = CompletableFuture.<Void, Scene<GAME>>create(engine.getAsyncManager());

        engine.runSupervised(() -> {
            //noinspection BlockingMethodInNonBlockingContext: run in new thread
            unload();
            future.complete(null, this, null);
        });

        return future;
    }

    @NotNull
    @Override
    public Engine<GAME> getEngine() {
        return engine;
    }
}
