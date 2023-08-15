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

import de.linusdev.clgl.api.types.Float4;

@SuppressWarnings("unused")
public class ABFloat4 extends ABFloatN implements Float4 {

    public ABFloat4(float x, float y, float z, float w) {
        super(new float[]{x, y, z, w}, 0, 4);
    }

    public ABFloat4(float[] array, int arrayStartIndex) {
        super(array, arrayStartIndex, 4);
    }

    public ABFloat4() {
        super(4);
    }
}
