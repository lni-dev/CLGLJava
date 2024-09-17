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

package de.linusdev.cvg4j.engine.window.input;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public abstract class PressableImpl implements Pressable {

    protected final @NotNull InputManger manager;
    protected final @NotNull InputType type;

    //TODO: removing a listener while iterating over the list (removing it in a onPress call) would lead
    //to a ConcurrentModificationException
    private @Nullable List<PressListener> pressListeners;
    private @Nullable List<ReleaseListener> releaseListeners;
    private final @NotNull ReentrantReadWriteLock listenersLock = new ReentrantReadWriteLock(false);
    private final @NotNull ReentrantReadWriteLock.ReadLock listenersReadLock = listenersLock.readLock();

    public PressableImpl(@NotNull InputManger inputManger, @NotNull InputType type) {
        this.manager = inputManger;
        this.type = type;
    }

    @Override
    public @NotNull InputManger getInputManager() {
        return manager;
    }

    @Override
    public @NotNull InputType getType() {
        return type;
    }

    @Override
    public void addPressListener(@NotNull PressListener listener) {
        ReentrantReadWriteLock.WriteLock lock = listenersLock.writeLock();
        lock.lock();

        if(pressListeners == null)
            pressListeners = new ArrayList<>(1);
        pressListeners.add(listener);

        lock.unlock();
    }

    @Override
    public void removePressListener(@NotNull PressListener listener) {
        ReentrantReadWriteLock.WriteLock lock = listenersLock.writeLock();
        lock.lock();

        if(pressListeners == null) return;
        pressListeners.remove(listener);

        lock.unlock();
    }

    @Override
    public void addReleaseListener(@NotNull ReleaseListener listener) {
        ReentrantReadWriteLock.WriteLock lock = listenersLock.writeLock();
        lock.lock();

        if(releaseListeners == null)
            releaseListeners = new ArrayList<>(1);
        releaseListeners.add(listener);

        lock.unlock();
    }

    @Override
    public void removeReleaseListener(@NotNull ReleaseListener listener) {
        ReentrantReadWriteLock.WriteLock lock = listenersLock.writeLock();
        lock.lock();

        if(releaseListeners == null) return;
        releaseListeners.remove(listener);

        lock.unlock();
    }

    void onPress() {
        if(pressListeners == null) return;
        listenersReadLock.lock();
        pressListeners.forEach(PressListener::onPress);
        listenersReadLock.unlock();
    }

    void onRelease() {
        if(releaseListeners == null) return;
        listenersReadLock.lock();
        releaseListeners.forEach(ReleaseListener::onRelease);
        listenersReadLock.unlock();
    }
}
