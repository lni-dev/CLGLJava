/*
 * Copyright (c) 2023-2025 Linus Andera
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

package de.linusdev.ljgel.engine.obj.manager;

import de.linusdev.ljgel.engine.obj.GameObject;
import de.linusdev.ljgel.engine.ticker.Tickable;
import de.linusdev.ljgel.engine.ticker.Ticker;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class CollisionManager<T extends GameObject<T>> implements Tickable {

    private final @NotNull ConcurrentMap<Integer, GameObjectInfo<T>> gameObjects;

    public CollisionManager() {
        this.gameObjects = new ConcurrentHashMap<>();
    }

    public void addGameObject(@NotNull GameObject<T> gameObject) {
        assert gameObject.getCollider() != null;
    }

    public void removeGameObject(int id) {

    }

    @Override
    public void tick(@NotNull Ticker ticker) {
            //TODO: use a hierarchy volume
    }


    private static class GameObjectInfo<T extends GameObject<T>> {
        public final @NotNull GameObject<T> gameObject;

        private GameObjectInfo(@NotNull GameObject<T> gameObject) {
            this.gameObject = gameObject;
        }
    }
}
