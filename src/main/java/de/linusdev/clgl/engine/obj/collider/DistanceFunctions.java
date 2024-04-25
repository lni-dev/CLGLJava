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

import de.linusdev.lutils.math.VMath;
import de.linusdev.lutils.math.vector.abstracts.floatn.Float3;
import de.linusdev.lutils.math.vector.array.floatn.ABFloat3;

public class DistanceFunctions {

    public static float sdf(SphereCollider col1, AlignedBoxCollider col2) {
        throw new UnsupportedOperationException("Not Implemented");
    }

    public static float sdf(SphereCollider col1, SphereCollider col2) {
        return VMath.length(VMath.subtract(col1.getCenter(), col2.getCenter(), new ABFloat3()))
                - col1.getRadius().get()
                - col2.getRadius().get();
    }

    public static float sdf(AlignedBoxCollider col1, AlignedBoxCollider col2) {

        Float3 distanceToCenter =  new ABFloat3();
        VMath.subtract(col1.getCenter(), col2.getCenter(),distanceToCenter);
        VMath.absolute(distanceToCenter, distanceToCenter);

        Float3 dif = new ABFloat3();
        VMath.subtract(distanceToCenter, col1.getSize(), dif);
        VMath.subtract(dif, col2.getSize(), dif);

        throw new UnsupportedOperationException("Not Implemented");
    }

}
