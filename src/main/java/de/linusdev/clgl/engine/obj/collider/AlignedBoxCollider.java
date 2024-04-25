/*
 * Copyright (c) 2024 Linus Andera
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

import de.linusdev.lutils.math.vector.abstracts.floatn.Float3;
import org.jetbrains.annotations.NotNull;

public class AlignedBoxCollider implements Collider {

    private final @NotNull Float3 center;
    private final @NotNull Float3 size;

    public AlignedBoxCollider(@NotNull Float3 center, @NotNull Float3 size) {
        this.center = center;
        this.size = size;
        throw new UnsupportedOperationException("Not Implemented");
    }


    @Override
    public @NotNull Float3 getCenter() {
        return center;
    }

    @Override
    public float getDiagonal() {
        return 0;
    }

    @Override
    public float bestEffortDistanceTo(@NotNull SphereCollider other) {
        return 0;
    }

    @Override
    public float exactDistanceTo(@NotNull SphereCollider other) {
        return 0;
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
        return 0;
    }

    public @NotNull Float3 getSize() {
        return size;
    }
}
