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

package de.linusdev.ljgel.engine.obj.collider;

import de.linusdev.lutils.math.vector.abstracts.floatn.Float3;
import org.jetbrains.annotations.NotNull;

public class ParentCollider implements Collider {

    private final int colliderGroup;
    private final @NotNull SphereCollider container;
    private final @NotNull SphereCollider @NotNull [] colliders;

    public ParentCollider(
            int colliderGroup,
            @NotNull SphereCollider container,
            @NotNull SphereCollider @NotNull [] colliders
    ) {
        this.colliderGroup = colliderGroup;
        this.container = container;
        this.colliders = colliders;
        throw new UnsupportedOperationException("Not Implemented");
    }

    @Override
    public float bestEffortDistanceTo(@NotNull SphereCollider other) {
        return other.exactDistanceTo(container);
    }

    @Override
    public float getDiagonal() {
        return container.getRadius().get();
    }

    @Override
    public @NotNull Float3 getCenter() {
        return container.getCenter();
    }

    @Override
    public float exactDistanceTo(@NotNull SphereCollider other) {
        float min = 1000f;

        for(SphereCollider collider : colliders)
            min = Math.min(min, other.exactDistanceTo(collider));

        return min;
    }

    @Override
    public float bestEffortDistanceTo(@NotNull AlignedBoxCollider other) {
        return 0;
    }

    @Override
    public float exactDistanceTo(@NotNull AlignedBoxCollider other) {
        return 0;
    }

    @Override
    public float bestEffortDistanceTo(@NotNull Collider other) {
        return 0;
    }

    @Override
    public float exactDistanceTo(@NotNull Collider other) {
        return 0;
    }

    @Override
    public int getColliderGroup() {
        return colliderGroup;
    }


}
