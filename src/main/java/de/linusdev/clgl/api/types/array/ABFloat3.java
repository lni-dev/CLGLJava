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

package de.linusdev.clgl.api.types.array;

import de.linusdev.clgl.api.types.Float3;

@SuppressWarnings("unused")
public class ABFloat3 extends ABFloatN implements Float3 {

    public ABFloat3(float x, float y, float z) {
        super(new float[]{x, y, z}, 0, 3);
    }

    public ABFloat3(float[] array, int arrayStartIndex) {
        super(array, arrayStartIndex, 3);
    }

    public ABFloat3() {
        super(3);
    }
}
