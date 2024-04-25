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

package de.linusdev.clgl.engine.obj.collider;

import de.linusdev.lutils.math.VMath;
import de.linusdev.lutils.math.vector.abstracts.floatn.Float1;
import de.linusdev.lutils.math.vector.abstracts.floatn.Float3;
import de.linusdev.lutils.math.vector.array.floatn.ABFloat3;
import org.jetbrains.annotations.NotNull;

public class SphereCollider implements Collider {

    private final int colliderGroup;

    private final @NotNull Float3 center;
    private final @NotNull Float1 radius;

    public SphereCollider(
            int colliderGroup,
            @NotNull Float3 center,
            @NotNull Float1 radius
    ) {
        this.colliderGroup = colliderGroup;
        this.center = center;
        this.radius = radius;
        throw new UnsupportedOperationException("Not Implemented");
    }

    @Override
    public @NotNull Float3 getCenter() {
        return center;
    }

    public @NotNull Float1 getRadius() {
        return radius;
    }

    @Override
    public float getDiagonal() {
        return radius.get() * 2f;
    }

    @Override
    public float bestEffortDistanceTo(@NotNull SphereCollider other) {
        return exactDistanceTo(other);
    }

    @Override
    public float exactDistanceTo(@NotNull SphereCollider other) {
        return VMath.length(VMath.subtract(this.center, other.center, new ABFloat3()))
                        - this.radius.get()
                        - other.radius.get();
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
