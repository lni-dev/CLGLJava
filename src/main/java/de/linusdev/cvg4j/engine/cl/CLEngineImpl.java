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

import de.linusdev.cvg4j.api.misc.interfaces.TRunnable;
import de.linusdev.cvg4j.engine.queue.ReturnRunnable;
import de.linusdev.cvg4j.engine.queue.TaskQueue;
import de.linusdev.cvg4j.engine.ticker.Tickable;
import de.linusdev.cvg4j.engine.ticker.Ticker;
import de.linusdev.cvg4j.nat.cl.objects.Context;
import de.linusdev.cvg4j.nat.glfw3.custom.FrameInfo;
import de.linusdev.cvg4j.nat.glfw3.custom.KeyListener;
import de.linusdev.cvg4j.nat.glfw3.custom.MouseButtonListener;
import de.linusdev.cvg4j.nat.glfw3.custom.TextInputListener;
import de.linusdev.cvg4j.window.CLGLWindow;
import de.linusdev.cvg4j.window.Handler;
import de.linusdev.cvg4j.window.args.KernelView;
import de.linusdev.cvg4j.window.input.InputManagerImpl;
import de.linusdev.cvg4j.window.input.InputManger;
import de.linusdev.llog.LLog;
import de.linusdev.llog.base.LogInstance;
import de.linusdev.lutils.async.Future;
import de.linusdev.lutils.async.Nothing;
import de.linusdev.lutils.async.completeable.CompletableFuture;
import de.linusdev.lutils.async.error.ThrowableAsyncError;
import de.linusdev.lutils.async.manager.AsyncManager;
import de.linusdev.lutils.thread.var.SyncVar;
import org.jetbrains.annotations.NonBlocking;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@SuppressWarnings({"unused", "FieldCanBeLocal"})
public class CLEngineImpl<G extends CLGame> implements CLEngine<G>, Handler, Tickable, KeyListener, MouseButtonListener, TextInputListener {

    private final static @NotNull LogInstance log = LLog.getLogInstance();

    private static final int LOAD_SCENE_TASK_ID = TaskQueue.getUniqueTaskId("LOAD_SCENE");

    private final @NotNull G game;
    private final @NotNull UIThread<G> uiThread;
    private final @NotNull CLGLWindow window;
    private final @NotNull Executor executor = Executors.newWorkStealingPool(4);
    private final @NotNull Ticker ticker;


    private final @NotNull SyncVar<@Nullable CLScene<G>> currentScene;
    private final @NotNull SyncVar<@Nullable CompletableFuture<Nothing, CLScene<G>, ?>> sceneLoaded;

    public CLEngineImpl(@NotNull G game) {
        this.game = game;

        this.currentScene = SyncVar.createSyncVar();
        this.sceneLoaded = SyncVar.createSyncVar();

        this.uiThread = new UIThread<>(this, this);

        try {
            this.window = this.uiThread.create().getResult();
            this.window.getGlfwWindow().setTitle(game.getTitle());
            this.window.getInputManger().addKeyListener(this);
            this.window.getInputManger().addMouseButtonListener(this);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        this.ticker = new Ticker(this, game.getMillisPerTick());
        if(game.getMillisPerTick() >= 0L)
            ticker.start();
    }

    /*
    Functions
     */

    @Override
    public @NotNull InputManagerImpl createInputManagerForScene(@NotNull CLScene<G> scene) {
        return new InputManagerImpl(null);
    }

    @Override
    public @NotNull Future<Nothing, CLScene<G>> loadScene(@NotNull CLScene<G> scene) {
        sceneLoaded.doSynchronised(sceneLoaded -> {

            var oldLoadFut = sceneLoaded.get();

            if(oldLoadFut != null && !oldLoadFut.isDone())
                throw new IllegalStateException("A scene is already loading.");

            var loadFut = CompletableFuture.<Nothing, CLScene<G>>create(this.getAsyncManager(), false);
            sceneLoaded.set(loadFut);
        });

        var loadFut = sceneLoaded.get();
        assert loadFut != null;

        loadFut.then((result, secondary, error) -> {
            if(error != null)
                log.logThrowable(error.asThrowable());
        });

        CLEngineUtils.loadKernels(this, scene.getLoadingRenderKernelInfo(), scene.getLoadingUIKernelInfo()).then(
                (loadingKernels, secondary, error) -> {

                    if (error != null && !game.onKernelLoadError(error)) {
                        loadFut.complete(null, scene, error);
                        return;
                    }

                    var uiTaskFut = window.getUiTaskQueue().queueForExecution(LOAD_SCENE_TASK_ID, (stack) -> {
                        //switch scene and unload old scene
                        currentScene.doSynchronised(v -> {
                            var oldScene = v.get();
                            v.set(scene);

                            if (oldScene != null)
                                oldScene.unload0();
                        });

                        //Set/Reset loading kernels
                        window.clearKernels();
                        window.setRenderKernel(loadingKernels == null ? null : loadingKernels.renderKernel);
                        window.setUiKernel(loadingKernels == null ? null : loadingKernels.uiKernel);

                        //start loading after setting loading kernels
                        return scene.load0();
                    });

                    // Start loading the normal kernels
                    CLEngineUtils.loadKernels(this, scene.getRenderKernelInfo(), scene.getUIKernelInfo())
                            .then((normalKernels, secondary1, error1) -> {

                                if (error1 != null && !game.onKernelLoadError(error1)) {
                                    loadFut.complete(null, scene, error1);
                                    return;
                                }

                                runSupervised(() -> {
                                    var uiTaskResult = uiTaskFut.get(); //make sure loading kernels are set

                                    if(uiTaskResult.hasError()) {
                                        loadFut.complete(null, scene, uiTaskResult.getError());
                                        return;
                                    }

                                    uiTaskResult.getResult().then((result2, secondary2, error2) -> {
                                        if (error2 != null) {
                                            loadFut.complete(null, scene, error2);
                                            return;
                                        }

                                        window.getUiTaskQueue().queueForExecution(LOAD_SCENE_TASK_ID, () -> {

                                            //Set/Reset normal kernels
                                            window.clearKernels();
                                            window.setRenderKernel(normalKernels == null ? null : normalKernels.renderKernel);
                                            window.setUiKernel(normalKernels == null ? null : normalKernels.uiKernel);

                                            scene.start0();
                                            loadFut.complete(Nothing.INSTANCE, scene, null);

                                            return null;
                                        });

                                    });
                                });
                            });

                });

        return loadFut;
    }

    @Override
    public @NotNull <R> Future<R, CLEngine<G>> runSupervised(@NotNull ReturnRunnable<R> runnable) {
        var future = CompletableFuture.<R, CLEngine<G>>create(getAsyncManager(), false);
        executor.execute(() -> {
            try {
                future.complete(runnable.run(), this, null);
            } catch (Throwable t) {
                log.logThrowable(t);
                future.complete(null, this, new ThrowableAsyncError(t));
            }
        });
        return future;
    }

    @Override
    public void runSupervised(@NotNull TRunnable runnable) {
        executor.execute(() -> {
            try {
                runnable.run();
            } catch (Throwable t) {
                log.logThrowable(t);

            }
        });
    }

    /*
    Listener
     */

    @Override
    public void onKey(int key, int scancode, int action, int mods) {
        CLScene<G> scene = currentScene.get();
        if(scene == null)
            return;
        scene.getState().onKey(scene, key, scancode, action, mods);
    }

    @Override
    public void onMouseButton(int button, int action, int mods) {
        CLScene<G> scene = currentScene.get();
        if(scene == null)
            return;
        scene.getState().onMouseButton(scene, button, action, mods);
    }

    @Override
    public void onTextInput(char[] chars, boolean supplementaryChar) {
        CLScene<G> scene = currentScene.get();
        if(scene == null)
            return;
        scene.getState().onTextInput(scene, chars, supplementaryChar);
    }

    @Override
    @NonBlocking
    public void tick() {
        CLScene<G> scene = currentScene.get();
        if(scene == null) return;

        scene.getState().tick(scene);
    }

    @Override
    public void update(@NotNull FrameInfo frameInfo) {
        CLScene<G> scene = currentScene.get();
        if(scene == null) return;

        scene.getState().update(scene, this, frameInfo);
    }

    @Override
    public void setRenderKernelArgs(@NotNull KernelView renderKernel) {
        CLScene<G> scene = currentScene.get();
        if(scene == null) return;

        scene.getState().setRenderKernelArgs(scene, renderKernel);

    }

    @Override
    public void setUIKernelArgs(@NotNull KernelView uiKernel) {
        CLScene<G> scene = currentScene.get();
        if(scene == null) return;

        scene.getState().setUIKernelArgs(scene, uiKernel);

    }

    /*
    Getter
     */

    @Override
    public @NotNull G getGame() {
        return game;
    }

    @Override
    public @NotNull AsyncManager getAsyncManager() {
        return window;
    }

    @Override
    public @NotNull UIThread<G> getUIThread() {
        return uiThread;
    }

    @Override
    public @NotNull CLGLWindow getWindow() {
        return window;
    }

    @Override
    public @NotNull InputManger getGlobalInputManager() {
        return window.getInputManger();
    }

    @Override
    public @NotNull Context getClContext() {
        return window.getClContext();
    }
}
