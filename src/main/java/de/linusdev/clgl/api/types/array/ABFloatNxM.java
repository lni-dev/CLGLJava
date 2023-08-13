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

import de.linusdev.clgl.api.types.FloatN;
import de.linusdev.clgl.api.types.FloatNxM;

public abstract class ABFloatNxM implements FloatNxM {

    abstract protected FloatN getRow(int y);

    @Override
    public float get(int x, int y) {
        return getRow(y).get(x);
    }

    @Override
    public void put(int x, int y, float value) {
        getRow(y).put(x, value);
    }

    @Override
    public boolean isArrayBacked() {
        return true;
    }

    @Override
    public boolean isBufferBacked() {
        return false;
    }
}
