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

package de.linusdev.clgl.engine.ticker;

import de.linusdev.llog.LLog;
import de.linusdev.llog.base.LogInstance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("unused")
public class Ticker implements Runnable {

    private final static @NotNull LogInstance log = LLog.getLogInstance();

    private final @NotNull Tickable tickable;
    private long millisPerTick;

    private final @NotNull ScheduledExecutorService executor;
    private @Nullable ScheduledFuture<?> future;

    private long lastTickStart = 0L;
    private double deltaTime = 0d;

    public Ticker(@NotNull Tickable tickable, long millisPerTick) {
        this.tickable = tickable;
        this.millisPerTick = millisPerTick;
        this.executor = Executors.newSingleThreadScheduledExecutor();
    }

    public synchronized void start() {
        lastTickStart = System.currentTimeMillis() - millisPerTick;
        if(future == null || future.isDone())
            future = executor.scheduleAtFixedRate(this, 0, millisPerTick, TimeUnit.MILLISECONDS);
    }

    public synchronized void stop() {
        if(future != null)
            future.cancel(false);
    }

    public synchronized void changeMillisPerTick(long millisPerTick) {
        executor.shutdown();
        try {
            //noinspection ResultOfMethodCallIgnored
            executor.awaitTermination(this.millisPerTick, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ignored) {}
        this.millisPerTick = millisPerTick;
        start();
    }


    @Override
    public void run() {
        try {
            long delta = System.currentTimeMillis() - lastTickStart;
            deltaTime = ((double) delta / 1000d);
            //TODO: make delta time easier accessible
            tickable.tick();
        } catch (Throwable t) {
            log.logThrowable(t);
        }
    }

    public double getLastDeltaTime() {
        return deltaTime;
    }
}
