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

package de.linusdev.clgl.window.input;

import de.linusdev.clgl.nat.glfw3.GLFWValues;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static de.linusdev.clgl.nat.glfw3.GLFW._glfwGetKeyName;

@SuppressWarnings("unused")
public class Key  {

    final int scancode;
    final String name;

    private @Nullable List<KeyPressListener> pressListeners;
    private @Nullable List<KeyReleaseListener> releaseListeners;
    private final @NotNull ReentrantReadWriteLock listenersLock = new ReentrantReadWriteLock(false);
    private final @NotNull ReentrantReadWriteLock.ReadLock listenersReadLock = listenersLock.readLock();

    Key(
            int scancode
    ) {
        this.scancode = scancode;
        this.name = _glfwGetKeyName(GLFWValues.Keys_US.GLFW_KEY_UNKNOWN, scancode);
    }

    @Override
    public String toString() {
        return name + "(" + scancode + ")";
    }

    public void addPressListener(@NotNull KeyPressListener listener) {
        ReentrantReadWriteLock.WriteLock lock = listenersLock.writeLock();
        lock.lock();

        if(pressListeners == null)
            pressListeners = new ArrayList<>(1);
        pressListeners.add(listener);

        lock.unlock();
    }

    public void removePressListener(@NotNull KeyPressListener listener) {
        ReentrantReadWriteLock.WriteLock lock = listenersLock.writeLock();
        lock.lock();

        if(pressListeners == null) return;
        pressListeners.remove(listener);

        lock.unlock();
    }

    public void addReleaseListener(@NotNull KeyReleaseListener listener) {
        ReentrantReadWriteLock.WriteLock lock = listenersLock.writeLock();
        lock.lock();

        if(releaseListeners == null)
            releaseListeners = new ArrayList<>(1);
        releaseListeners.add(listener);

        lock.unlock();
    }

    public void removeReleaseListener(@NotNull KeyReleaseListener listener) {
        ReentrantReadWriteLock.WriteLock lock = listenersLock.writeLock();
        lock.lock();

        if(releaseListeners == null) return;
        releaseListeners.remove(listener);

        lock.unlock();
    }

    void onPress() {
        if(pressListeners == null) return;
        listenersReadLock.lock();
        pressListeners.forEach(KeyPressListener::onPress);
        listenersReadLock.unlock();
    }

    void onRelease() {
        if(releaseListeners == null) return;
        listenersReadLock.lock();
        releaseListeners.forEach(KeyReleaseListener::onRelease);
        listenersReadLock.unlock();
    }
}
