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

import de.linusdev.clgl.api.misc.interfaces.TRunnable;
import de.linusdev.clgl.engine.ticker.Tickable;
import de.linusdev.clgl.engine.ticker.Ticker;
import de.linusdev.clgl.nat.glfw3.custom.FrameInfo;
import de.linusdev.clgl.window.CLGLWindow;
import de.linusdev.clgl.window.Handler;
import de.linusdev.clgl.window.args.KernelView;
import de.linusdev.clgl.window.queue.ReturnRunnable;
import de.linusdev.clgl.window.queue.UITaskQueue;
import de.linusdev.llog.LLog;
import de.linusdev.llog.base.LogInstance;
import de.linusdev.lutils.async.Future;
import de.linusdev.lutils.async.Nothing;
import de.linusdev.lutils.async.completeable.CompletableFuture;
import de.linusdev.lutils.async.error.ThrowableAsyncError;
import de.linusdev.lutils.async.manager.AsyncManager;
import de.linusdev.lutils.thready.var.SyncVar;
import org.jetbrains.annotations.NonBlocking;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@SuppressWarnings({"unused", "FieldCanBeLocal"})
public class EngineImpl<G extends Game> implements Engine<G>, Handler, Tickable {

    private final static @NotNull LogInstance log = LLog.getLogInstance();

    private static final int LOAD_SCENE_TASK_ID = UITaskQueue.getUniqueTaskId("LOAD_SCENE");

    private final @NotNull G game;
    private final @NotNull UIThread<G> uiThread;
    private final @NotNull CLGLWindow window;
    private final @NotNull Executor executor = Executors.newWorkStealingPool(4);
    private final @NotNull Ticker ticker;


    private final @NotNull SyncVar<@Nullable Scene<G>> currentScene;
    private final @NotNull SyncVar<@Nullable CompletableFuture<Nothing, Scene<G>, ?>> sceneLoaded;

    public EngineImpl(@NotNull G game) {
        this.game = game;

        this.currentScene = SyncVar.createSyncVar();
        this.sceneLoaded = SyncVar.createSyncVar();

        this.uiThread = new UIThread<>(this, this);

        try {
            this.window = this.uiThread.create().getResult();
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
    public @NotNull Future<Nothing, Scene<G>> loadScene(@NotNull Scene<G> scene) {
        sceneLoaded.doSynchronised(sceneLoaded -> {

            var oldLoadFut = sceneLoaded.get();

            if(oldLoadFut != null && !oldLoadFut.isDone())
                throw new IllegalStateException("A scene is already loading.");

            var loadFut = CompletableFuture.<Nothing, Scene<G>>create(this.getAsyncManager(), false);
            sceneLoaded.set(loadFut);
        });

        var loadFut = sceneLoaded.get();
        assert loadFut != null;

        loadFut.then((result, secondary, error) -> {
            if(error != null)
                log.logThrowable(error.asThrowable());
        });

        Helper.loadKernels(this, scene.getLoadingRenderKernelInfo(), scene.getLoadingUIKernelInfo()).then(
                (loadingKernels, secondary, error) -> {

                    if (error != null) {
                        loadFut.complete(null, scene, error);
                        return;
                    }

                    var uiTaskFut = window.getUiTaskQueue().queueForExecution(LOAD_SCENE_TASK_ID, () -> {
                        //switch scene and unload old scene
                        currentScene.doSynchronised(v -> {
                            var oldScene = v.get();
                            v.set(scene);

                            if (oldScene != null)
                                oldScene.unload0();
                        });

                        //Set/Reset loading kernels
                        window.clearKernels();
                        window.setRenderKernel(loadingKernels.renderKernel);
                        window.setUiKernel(loadingKernels.uiKernel);

                        return null;
                    });

                    var sceneLoadFut = scene.load0();

                    // Start loading the normal kernels
                    Helper.loadKernels(this, scene.getRenderKernelInfo(), scene.getUIKernelInfo())
                            .then((normalKernels, secondary1, error1) -> {

                                if (error1 != null) {
                                    loadFut.complete(null, scene, error1);
                                    return;
                                }

                                runSupervised(() -> {
                                    uiTaskFut.get(); //make sure loading kernels are set

                                    sceneLoadFut.then((result2, secondary2, error2) -> {
                                        if (error2 != null) {
                                            loadFut.complete(null, scene, error2);
                                            return;
                                        }

                                        window.getUiTaskQueue().queueForExecution(LOAD_SCENE_TASK_ID, () -> {

                                            //Set/Reset normal kernels
                                            window.clearKernels();
                                            window.setRenderKernel(normalKernels.renderKernel);
                                            window.setUiKernel(normalKernels.uiKernel);

                                            scene.start();
                                            scene.loaded.set(true);
                                            loadFut.complete(Nothing.INSTANCE, scene, null);

                                            return null;
                                        });

                                    });
                                });
                            });

                });

        return loadFut;
    }

    /*
    Listener
     */

    @Override
    @NonBlocking
    public void tick() {
        Scene<G> scene = currentScene.get();
        if(scene != null && scene.loaded.get())
            scene.tick();

    }

    @Override
    public void update(@NotNull CLGLWindow window, @NotNull FrameInfo frameInfo) {
        Scene<G> scene = currentScene.get();
        if(scene != null) {
            scene.update0(this, frameInfo);
        }

    }

    @Override
    public void setRenderKernelArgs(@NotNull KernelView renderKernel) {
        Scene<G> scene = currentScene.get();
        if(scene == null) return;

        if(scene.loaded.get())
            scene.setRenderKernelArgs(renderKernel);
        else
            scene.setLoadingRenderKernelArgs(renderKernel);

    }

    @Override
    public void setUIKernelArgs(@NotNull KernelView uiKernel) {
        Scene<G> scene = currentScene.get();
        if(scene == null) return;

        if(scene.loaded.get())
            scene.setUIKernelArgs(uiKernel);
        else
            scene.setLoadingUIKernelArgs(uiKernel);

    }

    @Override
    public @NotNull <R> Future<R, Engine<G>> runSupervised(@NotNull ReturnRunnable<R> runnable) {
        var future = CompletableFuture.<R, Engine<G>>create(getAsyncManager(), false);
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
    public @NotNull CLGLWindow getWindow() {
        return window;
    }
}
