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

package de.linusdev.cvg4j.engine.queue;

import de.linusdev.cvg4j.api.misc.annos.CallFromAnyThread;
import de.linusdev.llog.LLog;
import de.linusdev.llog.base.LogInstance;
import de.linusdev.lutils.async.Nothing;
import de.linusdev.lutils.async.manager.AsyncManager;
import de.linusdev.lutils.nat.memory.stack.Stack;
import org.jetbrains.annotations.*;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.function.Consumer;

public class TaskQueue {

    @SuppressWarnings("unused")
    private final static LogInstance log = LLog.getLogInstance();

    public static final int NO_TASK_ID = -1;
    public static final int MAX_TASK_ID = 256;

    protected static int nextId = 1;
    protected static final @NotNull Object idLock = new Object();
    protected static final @Nullable String @NotNull [] taskNames = new String[MAX_TASK_ID + 1];

    public static int getUniqueTaskId(@NotNull String taskName) {
        synchronized (idLock) {
            if(nextId > MAX_TASK_ID)
                throw new IllegalStateException("No more unique task ids remaining.");
            taskNames[nextId] = taskName;
            return nextId++;
        }
    }


    protected final @NotNull AsyncManager asyncManager;
    /**
     * Consumer, which will be called after a future has been {@link #queue(int, TQFutureImpl) submitted} to the queue.
     * Note, that the future might already be handled when the consumer is called, as it is not synchronized.
     */
    protected final @Nullable Consumer<TQFuture<?>> queuedFutureConsumer;

    protected final @NotNull AtomicReferenceArray<Wrapper<TQFutureImpl<?>>> wrappers;
    protected final @NotNull Queue<Wrapper<TQFutureImpl<?>>> taskQueue;
    protected final long maxQueuedTaskMillisPerFrame;



    public TaskQueue(
            @NotNull AsyncManager asyncManager,
            @Nullable Consumer<TQFuture<?>> queuedFutureConsumer,
            long maxQueuedTaskMillisPerFrame
    ) {
        this.asyncManager = asyncManager;
        this.queuedFutureConsumer = queuedFutureConsumer;
        this.taskQueue = new ConcurrentLinkedQueue<>();
        this.wrappers = new AtomicReferenceArray<>(MAX_TASK_ID + 1);
        this.maxQueuedTaskMillisPerFrame = maxQueuedTaskMillisPerFrame;

        for(int i = 0; i < wrappers.length(); i++)
            wrappers.set(i, new Wrapper<>(i));
    }

    @CallFromAnyThread
    @NonBlocking
    @ApiStatus.Internal
    private void queue(int id, @NotNull TQFutureImpl<?> future) {
        if(id > 0 && id < wrappers.length()) {
            if(!wrappers.get(id).queueIfNull(future, taskQueue)) {
                //the same task has already been queued. So cancel this future.
                future.cancel();
            }
        } else {
            taskQueue.offer(new Wrapper<>(id, future));
        }
        if(queuedFutureConsumer != null)
            queuedFutureConsumer.accept(future);
    }

    @Blocking
    @ApiStatus.Internal
    public void runQueuedTasks(@NotNull Stack stack) {
        final long startTime = System.currentTimeMillis();
        int taskCount = 0;

        Wrapper<TQFutureImpl<?>> wrapper;
        TQFutureImpl<?> future;
        while (
                (System.currentTimeMillis() - startTime) < maxQueuedTaskMillisPerFrame &&
                        (wrapper = taskQueue.poll()) != null
        ) {
            future = wrapper.getItemAndSetToNull();
            if(future != null) {
                future.run(stack);
                taskCount++;
            }
        }

        //TODO: Debug stats, task time, task count, ...

    }

    @NonBlocking
    public <T> @NotNull TQFuture<T> queueForExecution(@Range(from = 0, to = MAX_TASK_ID) int id, @NotNull TQRunnable<T> runnable) {
        TQFutureImpl<T> f = new TQFutureImpl<>(asyncManager, runnable);
        queue(id, f);
        return f;
    }

    @NonBlocking
    public @NotNull TQFuture<Nothing> queueForExecution(@Range(from = 0, to = MAX_TASK_ID) int id, @NotNull TQVoidRunnable runnable) {
        TQFutureImpl<Nothing> f = new TQFutureImpl<>(asyncManager, s -> {
            runnable.run(s);
            return Nothing.INSTANCE;
        });
        queue(id, f);
        return f;
    }

    @NonBlocking
    public <T> @NotNull TQFuture<T> queueForExecution(@NotNull TQRunnable<T> runnable) {
        TQFutureImpl<T> f = new TQFutureImpl<>(asyncManager, runnable);
        queue(NO_TASK_ID, f);
        return f;
    }

    @NonBlocking
    public @NotNull TQFuture<Nothing> queueForExecution(@NotNull TQVoidRunnable runnable) {
        TQFutureImpl<Nothing> f = new TQFutureImpl<>(asyncManager, s -> {
            runnable.run(s);
            return Nothing.INSTANCE;
        });
        queue(NO_TASK_ID, f);
        return f;
    }

}
