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

import de.linusdev.lutils.math.vector.abstracts.floatn.Float3;
import org.jetbrains.annotations.NotNull;

public interface Collider {

    /**
     * Central position of this collider
     */
    @NotNull Float3 getCenter();

    /**
     *
     * @return The longest diagonal (going through the center) of this collider.
     */
    float getDiagonal();

    /**
     * Best effort smallest-distance to given sphere-collider. Must always be smaller or equal to
     * the {@link #exactDistanceTo(SphereCollider) exact smallest distance}.
     * @param other sphere-collider to which the distance shall be calculated.
     * @return distance as described above
     */
    float bestEffortDistanceTo(@NotNull SphereCollider other);

    /**
     * Exact smallest-distance to given sphere-collider.
     * @param other sphere-collider to which the distance shall be calculated.
     * @return distance as described above
     */
    float exactDistanceTo(@NotNull SphereCollider other);

    /**
     * Best effort smallest-distance to given aligned-box-collider. Must always be smaller or equal to
     * the {@link #exactDistanceTo(AlignedBoxCollider) exact smallest distance}.
     * @param other aligned-box-collider to which the distance shall be calculated.
     * @return distance as described above
     */
    float bestEffortDistanceTo(@NotNull AlignedBoxCollider other);

    /**
     * Exact smallest-distance to given aligned-box-collider.
     * @param other aligned-box-collider to which the distance shall be calculated.
     * @return distance as described above
     */
    float exactDistanceTo(@NotNull AlignedBoxCollider other);

    float bestEffortDistanceTo(@NotNull Collider other);

    float exactDistanceTo(@NotNull Collider other);

    int getColliderGroup();

}
