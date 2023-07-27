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

import de.linusdev.clgl.engine.kernel.source.KernelSourceInfo;
import de.linusdev.clgl.engine.ticker.Tickable;
import de.linusdev.clgl.engine.ticker.Ticker;
import de.linusdev.clgl.nat.cl.objects.Kernel;
import de.linusdev.clgl.nat.glfw3.custom.FrameInfo;
import de.linusdev.clgl.window.CLGLWindow;
import de.linusdev.clgl.window.Handler;
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

@SuppressWarnings("unused")
public class EngineImpl<GAME extends Game> implements Engine<GAME>, Handler, Tickable {

    private final static @NotNull LogInstance log = LLog.getLogInstance();

    private static final int LOAD_SCENE_TASK_ID = UITaskQueue.getUniqueTaskId("LOAD_SCENE");

    private final @NotNull GAME game;
    private final CLGLWindow window;
    private final @NotNull Executor executor = Executors.newWorkStealingPool(4);
    private final @NotNull Ticker ticker;


    private final @NotNull SyncVar<@Nullable Scene<GAME>> currentScene;
    private final @NotNull SyncVar<@Nullable CompletableFuture<Void, Scene<GAME>, ?>> sceneLoaded;

    public EngineImpl(@NotNull GAME game) {
        this.game = game;
        this.window = new CLGLWindow(this, 5);
        this.ticker = new Ticker(this, game.getMillisPerTick());
        if(game.getMillisPerTick() >= 0L)
            ticker.start();

        this.currentScene = SyncVar.createSyncVar();
        this.sceneLoaded = SyncVar.createSyncVar();
    }

    /*
    Functions
     */

    @Override
    public void loadScene(@NotNull Scene<GAME> scene) {
        sceneLoaded.doSynchronised(sceneLoaded -> {
            var oldLoadFut = sceneLoaded.get();

            if(oldLoadFut != null && !oldLoadFut.isDone())
                throw new IllegalStateException("A scene is already loading.");

            var loadFut = CompletableFuture.<Void, Scene<GAME>>create(this.getAsyncManager());
            sceneLoaded.set(loadFut);


            KernelSourceInfo info;
            Future<Kernel, Nothing> renderKernelFuture, uiKernelFuture;

            info = scene.getRenderKernelInfo();
            renderKernelFuture = info != null ? info.loadKernel(this) : null;

            info = scene.getUIKernelInfo();
            uiKernelFuture = info != null ? info.loadKernel(this) : null;


            runSupervised(() -> {
                try {
                    Kernel renderKernel, uiKernel;
                    if(renderKernelFuture != null) {
                        var res = renderKernelFuture.get();
                        if(res.hasError()) {
                            loadFut.complete(null, scene, res.getError());
                            return;
                        }
                        renderKernel = res.getResult();
                    } else renderKernel = null;

                    if(uiKernelFuture != null) {
                        var res = uiKernelFuture.get();
                        if(res.hasError()) {
                            loadFut.complete(null, scene, res.getError());
                            return;
                        }
                        uiKernel = res.getResult();
                    } else uiKernel = null;

                    window.getUiTaskQueue().queueForExecution(LOAD_SCENE_TASK_ID, () -> {
                        //Set/Reset render kernel
                        window.setRenderKernel(renderKernel);

                        //Set/Reset ui kernel
                        window.setUiKernel(uiKernel);

                        //switch scene and unload old scene
                        currentScene.doSynchronised(v -> {
                            var oldScene = v.get();
                            v.set(scene);

                            if(oldScene != null)
                                oldScene.unload0();
                        });

                        // Load only after the kernels have been set.
                        scene.load0().then(loadFut::complete).then((result, secondary, error) -> {
                            scene.start();
                            scene.loaded.set(true);
                        });

                        return null;
                    });
                } catch (InterruptedException ignored) {
                }
            });

        });
    }

    /*
    Listener
     */

    @Override
    @NonBlocking
    public void tick() {
        Scene<GAME> scene = currentScene.get();
        if(scene != null && scene.loaded.get())
            scene.tick();

    }

    @Override
    public void update(@NotNull CLGLWindow window, @NotNull FrameInfo frameInfo) {
        Scene<GAME> scene = currentScene.get();
        if(scene != null) {
            scene.update0(this, frameInfo);
        }

    }

    @Override
    public void setRenderKernelArgs(@NotNull Kernel renderKernel) {
        Scene<GAME> scene = currentScene.get();
        if(scene != null) {
            scene.setRenderKernelArgs(renderKernel);
        }
    }

    @Override
    public void setUIKernelArgs(@NotNull Kernel uiKernel) {
        Scene<GAME> scene = currentScene.get();
        if(scene != null) {
            scene.setUIKernelArgs(uiKernel);
        }
    }

    @Override
    public @NotNull <R> Future<R, Engine<GAME>> runSupervised(@NotNull ReturnRunnable<R> runnable) {
        var future = CompletableFuture.<R, Engine<GAME>>create(getAsyncManager());
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
    public void runSupervised(@NotNull Runnable runnable) {
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
    public @NotNull GAME getGame() {
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
