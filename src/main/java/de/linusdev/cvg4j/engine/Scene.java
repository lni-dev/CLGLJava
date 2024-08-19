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

import de.linusdev.cvg4j.api.misc.annos.CallOnlyFromUIThread;
import de.linusdev.cvg4j.engine.kernel.source.KernelSourceInfo;
import de.linusdev.cvg4j.engine.ticker.Tickable;
import de.linusdev.cvg4j.nat.glfw3.custom.FrameInfo;
import de.linusdev.cvg4j.nat.glfw3.custom.UpdateListener;
import de.linusdev.cvg4j.window.Handler;
import de.linusdev.cvg4j.window.args.KernelView;
import de.linusdev.cvg4j.window.args.impl.ModifiableStructArgument;
import de.linusdev.cvg4j.window.input.InputManagerImpl;
import de.linusdev.cvg4j.window.input.InputManger;
import de.linusdev.llog.LLog;
import de.linusdev.llog.base.LogInstance;
import de.linusdev.lutils.async.Future;
import de.linusdev.lutils.async.Nothing;
import de.linusdev.lutils.async.completeable.CompletableFuture;
import de.linusdev.lutils.math.vector.buffer.floatn.BBFloat1;
import org.jetbrains.annotations.*;

import java.util.concurrent.atomic.AtomicReference;

public abstract class Scene<GAME extends Game> implements
        HasEngine<GAME>,
        UpdateListener,
        Tickable
{

    @SuppressWarnings("unused")
    private final static LogInstance log = LLog.getLogInstance();

    public static final KernelSourceInfo LOADING_UI_KERNEL_INFO = KernelSourceInfo.ofUTF8StringResource(KernelSourceInfo.class,
            "kernels/loading-ui.cl", "render");

    public static final KernelSourceInfo LOADING_RENDER_KERNEL_INFO = KernelSourceInfo.ofUTF8StringResource(KernelSourceInfo.class,
            "kernels/loading-render.cl", "render");

    protected final @NotNull Engine<GAME> engine;
    private final @NotNull InputManagerImpl inputManger;

    protected final @NotNull AtomicReference<SceneState> state;
    protected final @NotNull BBFloat1 loadingPercent = BBFloat1.newAllocated(null);

    protected Scene(@NotNull Engine<GAME> engine) {
        this.engine = engine;
        this.state = new AtomicReference<>(SceneState.CREATED);
        this.loadingPercent.set(0.0f);
        this.inputManger = engine.createInputManagerForScene(this);
    }




    @ApiStatus.OverrideOnly
    protected abstract @Nullable KernelSourceInfo getUIKernelInfo();

    @ApiStatus.OverrideOnly
    protected abstract @Nullable KernelSourceInfo getRenderKernelInfo();

    @ApiStatus.OverrideOnly
    protected @NotNull KernelSourceInfo getLoadingUIKernelInfo() {
        return LOADING_UI_KERNEL_INFO;
    }

    @ApiStatus.OverrideOnly
    protected @NotNull KernelSourceInfo getLoadingRenderKernelInfo() {
        return LOADING_RENDER_KERNEL_INFO;
    }

    /**
     * @see Handler#setRenderKernelArgs(KernelView)
     */
    @ApiStatus.OverrideOnly
    protected abstract void setRenderKernelArgs(@NotNull KernelView renderKernel);

    /**
     * @see Handler#setUIKernelArgs(KernelView)
     */
    @ApiStatus.OverrideOnly
    protected abstract void setUIKernelArgs(@NotNull KernelView uiKernel);

    /**
     * @see Handler#setRenderKernelArgs(KernelView)
     */
    @ApiStatus.OverrideOnly
    @SuppressWarnings("unused")
    protected void setLoadingRenderKernelArgs(@NotNull KernelView renderKernel) {

    }

    /**
     * @see Handler#setUIKernelArgs(KernelView) (Kernel)
     */
    @ApiStatus.OverrideOnly
    protected void setLoadingUIKernelArgs(@NotNull KernelView uiKernel) {
        uiKernel.setKernelArg(2, new ModifiableStructArgument(loadingPercent));
    }

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
    public abstract void update(@NotNull FrameInfo frameInfo);

    @ApiStatus.Internal
    @NonBlocking
    void onKey0(int key, int scancode, int action, int mods) {
        inputManger.onKey(key, scancode, action, mods);
    }

    @ApiStatus.Internal
    @NonBlocking
    void onMouseButton0(int button, int action, int mods) {
        inputManger.onMouseButton(button, action, mods);
    }

    @ApiStatus.Internal
    @NonBlocking
    void onTextInput0(char[] chars, boolean supplementaryChar) {
        inputManger.onTextInput(chars, supplementaryChar);
    }


    @ApiStatus.Internal
    @NonBlocking
    @NotNull Future<Nothing, Scene<GAME>> load0() {
        this.state.set(SceneState.LOADING);
        var future = CompletableFuture.<Nothing, Scene<GAME>>create(engine.getAsyncManager(), false);

        engine.runSupervised(() -> {
            //noinspection BlockingMethodInNonBlockingContext: run in new thread
            load();
            this.state.set(SceneState.UNSTARTED);
            future.complete(Nothing.INSTANCE, this, null);
        });

        return future;
    }

    @SuppressWarnings("UnusedReturnValue")
    @ApiStatus.Internal
    @NonBlocking
    @NotNull Future<Nothing, Scene<GAME>> unload0() {
        this.state.set(SceneState.UNLOADING);
        var future = CompletableFuture.<Nothing, Scene<GAME>>create(engine.getAsyncManager(), false);

        engine.runSupervised(() -> {
            //noinspection BlockingMethodInNonBlockingContext: run in new thread
            unload();
            this.state.set(SceneState.DEAD);
            future.complete(Nothing.INSTANCE, this, null);
        });

        return future;
    }

    @ApiStatus.Internal
    @NonBlocking
    void start0() {
        start();
        this.state.set(SceneState.STARTED);
    }

    @ApiStatus.Internal
    @NonBlocking
    void tick0() {
        tick();
    }

    @Override
    public void update0(@NotNull Engine<GAME> engine, @NotNull FrameInfo frameInfo) {

        UpdateListener.super.update0(engine, frameInfo);
    }

    @NotNull
    @Override
    public Engine<GAME> getEngine() {
        return engine;
    }

    public @NotNull SceneState getState() {
        return state.get();
    }

    /**
     *
     * @return {@link InputManger} for this {@link Scene}
     */
    public @NotNull InputManger getInputManger() {
        return inputManger;
    }
}
