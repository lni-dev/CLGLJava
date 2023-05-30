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

package de.linusdev.clgl.window.queue;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Queue;

public class Wrapper<T> {

    private volatile @Nullable T item;
    private final int id;

    public Wrapper(int id) {
        this.id = id;
    }

    public Wrapper(int id, @Nullable T item) {
        this.id = id;
        this.item = item;
    }

    @SuppressWarnings("unused")
    public int getId() {
        return id;
    }

    public synchronized T getItemAndSetToNull() {
        T temp = item;
        item = null;
        return temp;
    }

    public synchronized void setItem(@Nullable T item) {
        this.item = item;
    }

    public synchronized void queueIfNull(@NotNull T item, @NotNull Queue<Wrapper<T>> queue) {
        if(this.item == null) {
            setItem(item);
            queue.offer(this);
        }
    }
}
