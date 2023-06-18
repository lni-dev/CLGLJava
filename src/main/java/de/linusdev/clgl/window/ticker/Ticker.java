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

package de.linusdev.clgl.window.ticker;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("unused")
public class Ticker implements Runnable {

    private final @NotNull Tickable tickable;
    private final long millisPerTick;

    private final @NotNull ScheduledExecutorService executor;
    private @Nullable ScheduledFuture<?> future;

    public Ticker(@NotNull Tickable tickable, long millisPerTick) {
        this.tickable = tickable;
        this.millisPerTick = millisPerTick;
        this.executor = Executors.newSingleThreadScheduledExecutor();
    }

    public void start() {
        if(future == null || future.isDone())
            future = executor.scheduleAtFixedRate(this, 0, millisPerTick, TimeUnit.MILLISECONDS);

    }

    public void stop() {
        if(future != null)
            future.cancel(false);
    }


    @Override
    public void run() {
        try {
            tickable.tick();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
