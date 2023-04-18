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

package de.linusdev.clgl.api.structs;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PrimitiveTypeArrayTest {

    @Test
    void test() {
        assertThrows(IllegalArgumentException.class, () -> new PrimitiveTypeArray<>(String.class, 20, true));

        PrimitiveTypeArray<Integer> array = new PrimitiveTypeArray<>(Integer.class, 20, true);

        array.set(0, 10);
        array.set(16, 10);
        array.setInt(9, 23);

        assertEquals(10, array.get(0));
        assertEquals(10, array.getInt(0));

        assertEquals(10, array.get(16));
        assertEquals(10, array.getInt(16));

        assertEquals(23, array.get(9));
        assertEquals(23, array.getInt(9));

    }
}