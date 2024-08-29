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

package de.linusdev.cvg4j.engine.cl;

import de.linusdev.cvg4j.api.misc.annos.CallOnlyFromUIThread;
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

public abstract class CLScene<GAME extends CLGame> implements
        HasCLEngine<GAME>,
        UpdateListener,
        Tickable
{

    @SuppressWarnings("unused")
    private final static LogInstance log = LLog.getLogInstance();

    public static final KernelSourceInfo LOADING_UI_KERNEL_INFO = KernelSourceInfo.ofUTF8StringResource(KernelSourceInfo.class,
            "kernels/loading-ui.cl", "render");

    public static final KernelSourceInfo LOADING_RENDER_KERNEL_INFO = KernelSourceInfo.ofUTF8StringResource(KernelSourceInfo.class,
            "kernels/loading-render.cl", "render");

    protected final @NotNull CLEngine<GAME> engine;
    private final @NotNull InputManagerImpl inputManger;

    protected final @NotNull AtomicReference<CLSceneState> state;
    protected final @NotNull BBFloat1 loadingPercent = BBFloat1.newAllocated(null);

    protected CLScene(@NotNull CLEngine<GAME> engine) {
        this.engine = engine;
        this.state = new AtomicReference<>(CLSceneState.CREATED);
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
     * CLScene is about to end. Unload resources. The scene may already switch to the new
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
     * @param engine {@link CLEngine}
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
    @NotNull Future<Nothing, CLScene<GAME>> load0() {
        this.state.set(CLSceneState.LOADING);
        var future = CompletableFuture.<Nothing, CLScene<GAME>>create(engine.getAsyncManager(), false);

        engine.runSupervised(() -> {
            //noinspection BlockingMethodInNonBlockingContext: run in new thread
            load();
            this.state.set(CLSceneState.UNSTARTED);
            future.complete(Nothing.INSTANCE, this, null);
        });

        return future;
    }

    @SuppressWarnings("UnusedReturnValue")
    @ApiStatus.Internal
    @NonBlocking
    @NotNull Future<Nothing, CLScene<GAME>> unload0() {
        this.state.set(CLSceneState.UNLOADING);
        var future = CompletableFuture.<Nothing, CLScene<GAME>>create(engine.getAsyncManager(), false);

        engine.runSupervised(() -> {
            //noinspection BlockingMethodInNonBlockingContext: run in new thread
            unload();
            this.state.set(CLSceneState.DEAD);
            future.complete(Nothing.INSTANCE, this, null);
        });

        return future;
    }

    @ApiStatus.Internal
    @NonBlocking
    void start0() {
        start();
        this.state.set(CLSceneState.STARTED);
    }

    @ApiStatus.Internal
    @NonBlocking
    void tick0() {
        tick();
    }

    @Override
    public void update0(@NotNull FrameInfo frameInfo) {

        UpdateListener.super.update0(frameInfo);
    }

    @NotNull
    @Override
    public CLEngine<GAME> getEngine() {
        return engine;
    }

    public @NotNull CLSceneState getState() {
        return state.get();
    }

    /**
     *
     * @return {@link InputManger} for this {@link CLScene}
     */
    public @NotNull InputManger getInputManger() {
        return inputManger;
    }
}
