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

package de.linusdev.clgl.api.types;

import de.linusdev.clgl.api.types.bytebuffer.BBFloat3;
import de.linusdev.clgl.api.types.bytebuffer.BBFloat4;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VMathTest {

    @Test
    void add() {
        BBFloat4 a = new BBFloat4(true);
        a.xyzw(1f, 2f, 3f, 4f);

        BBFloat4 b = new BBFloat4(true);
        b.xyzw(1f, 2f, 3f, 4f);

        VMath.add(a, b, a);

        assertEquals(2f, a.x());
        assertEquals(4f, a.y());
        assertEquals(6f, a.z());
        assertEquals(8f, a.w());
    }

    @Test
    void subtract() {
        BBFloat4 a = new BBFloat4(true);
        a.xyzw(2f, 2f, 3f, 1f);

        BBFloat4 b = new BBFloat4(true);
        b.xyzw(1f, 2f, 3f, 4f);

        VMath.subtract(a, b, a);

        assertEquals(1f, a.x());
        assertEquals(0f, a.y());
        assertEquals(0f, a.z());
        assertEquals(-3f, a.w());
    }

    @Test
    void multiply() {
        BBFloat4 a = new BBFloat4(true);
        a.xyzw(2f, 2f, 3f, 1f);

        BBFloat4 b = new BBFloat4(true);
        b.xyzw(1f, 2f, 3f, 4f);

        VMath.multiply(a, b, a);

        assertEquals(2f, a.x());
        assertEquals(4f, a.y());
        assertEquals(9f, a.z());
        assertEquals(4f, a.w());
    }

    @Test
    void divide() {
        BBFloat4 a = new BBFloat4(true);
        a.xyzw(2f, 2f, 3f, 1f);

        BBFloat4 b = new BBFloat4(true);
        b.xyzw(1f, 2f, 3f, 4f);

        VMath.divide(a, b, a);

        assertEquals(2f, a.x());
        assertEquals(1f, a.y());
        assertEquals(1f, a.z());
        assertEquals(1f/4f, a.w());
    }

    @Test
    void scale() {
        BBFloat4 a = new BBFloat4(true);
        a.xyzw(2f, 2f, 3f, 1f);

        VMath.scale(a, 2.f, a);

        assertEquals(4f, a.x());
        assertEquals(4f, a.y());
        assertEquals(6f, a.z());
        assertEquals(2f, a.w());
    }

    @Test
    void dot() {
        BBFloat4 a = new BBFloat4(true);
        a.xyzw(2f, 2f, 3f, 1f);

        BBFloat4 b = new BBFloat4(true);
        b.xyzw(1f, 2f, 3f, 4f);

        assertEquals(2f+4f+9f+4f, VMath.dot(a, b));
    }

    @Test
    void cross() {
        BBFloat3 a = new BBFloat3(true);
        a.xyz(2f, 2f, 3f);

        BBFloat3 b = new BBFloat3(true);
        b.xyz(1f, 2f, 3f);

        VMath.cross(a, b, a);

        assertEquals(0f, a.x());
        assertEquals(-3f, a.y());
        assertEquals(2f, a.z());
    }

    @Test
    void normalize() {
        BBFloat3 a = new BBFloat3(true);
        a.xyz(2f, 2f, 3f);

        VMath.normalize(a, a);

        assertEquals(0.48507127f, a.x());
        assertEquals(0.48507127f, a.y());
        assertEquals(0.7276069f, a.z());
    }
}