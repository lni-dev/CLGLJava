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

package de.linusdev.ljgel.engine.obj;

import de.linusdev.ljgel.engine.obj.collider.Collider;
import de.linusdev.ljgel.engine.obj.manager.GameObjectManager;
import de.linusdev.ljgel.engine.obj.manager.HasGameObjectManager;
import de.linusdev.ljgel.engine.ticker.Tickable;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class GameObject<T extends GameObject<T>> implements Tickable {

    protected final int id;
    private final GameObjectManager<T> manager;

    private final @Nullable Collider collider;

    public GameObject(
            HasGameObjectManager<T> manager,
            @Nullable Collider collider
    ) {
        this.manager = manager.getGameObjectManager();
        this.id = this.manager.registerObject(this);

        this.collider = collider;
    }

    @ApiStatus.OverrideOnly
    public abstract void onCollision(@NotNull GameObject<T> other);

    public @Nullable Collider getCollider() {
        return collider;
    }

    public boolean hasCollider() {
        return collider != null;
    }

    public void unregister() {
        this.manager.unregisterObject(id);
    }
}
