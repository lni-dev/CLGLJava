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

package de.linusdev.clgl.engine.obj.manager;

import de.linusdev.clgl.engine.obj.GameObject;
import de.linusdev.clgl.engine.ticker.Tickable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

public class GameObjectManager<T extends GameObject<T>> implements
        Tickable,
        HasGameObjectManager<T>
{

    private final AtomicInteger nextId = new AtomicInteger(0);
    private final ConcurrentMap<Integer, GameObject<T>> gameObjects = new ConcurrentHashMap<>();

    private final List<CollisionManager<T>> collisionManagers;

    public GameObjectManager(int collisionGroupCount) {
        this.collisionManagers = new ArrayList<>();

        for(int i=0; i < collisionGroupCount; i++) {
            this.collisionManagers.add(new CollisionManager<>());
        }
    }

    public int registerObject(@NotNull GameObject<T> object) {
        int id;
        synchronized (nextId) {
            id = nextId.getAndIncrement();
            gameObjects.put(id, object);
        }

        if(object.hasCollider()) {
            //noinspection DataFlowIssue: checked above
            collisionManagers.get(object.getCollider().getColliderGroup()).addGameObject(object);
        }

        return id;
    }

    public void unregisterObject(int id) {
        GameObject<T> object = gameObjects.remove(id);
        if(object != null && object.hasCollider())
            //noinspection DataFlowIssue: checked above
            collisionManagers.get(object.getCollider().getColliderGroup()).removeGameObject(id);
    }

    @Override
    public void tick() {
        for(CollisionManager<T> manager : collisionManagers)
            manager.tick();

        for(GameObject<T> obj : gameObjects.values())
            obj.tick();
    }

    @Override
    public @NotNull GameObjectManager<T> getGameObjectManager() {
        return this;
    }
}
