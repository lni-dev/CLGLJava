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

import de.linusdev.clgl.api.types.array.ABFloat4x4;
import de.linusdev.clgl.api.types.bytebuffer.BBFloat3;
import de.linusdev.clgl.api.types.bytebuffer.BBFloat4;
import de.linusdev.clgl.api.types.bytebuffer.BBFloat4x4;
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

    @Test
    void determinant() {
        BBFloat4x4 mat = new BBFloat4x4(true);

        mat.put(0, 0, 1);
        mat.put(1, 0, 5.5f);
        mat.put(2, 0, 3);
        mat.put(3, 0, 7);

        mat.put(0, 1, 8);
        mat.put(1, 1, 2);
        mat.put(2, 1, 1.5f);
        mat.put(3, 1, 1);

        mat.put(0, 2, 3);
        mat.put(1, 2, 4);
        mat.put(2, 2, 7);
        mat.put(3, 2, 5.5f);

        mat.put(0, 3, 3.2f);
        mat.put(1, 3, 2.1f);
        mat.put(2, 3, 5f);
        mat.put(3, 3, 6.6f);

        assertEquals(-751.97499999999999988f, VMath.determinant(mat));
    }

    @Test
    void adjugate() {
        ABFloat4x4 res = new ABFloat4x4();
        BBFloat4x4 mat = new BBFloat4x4(true);

        mat.put(0, 0, 1);
        mat.put(1, 0, 5.5f);
        mat.put(2, 0, 3);
        mat.put(3, 0, 7);

        mat.put(0, 1, 8);
        mat.put(1, 1, 2);
        mat.put(2, 1, 1.5f);
        mat.put(3, 1, 1);

        mat.put(0, 2, 3);
        mat.put(1, 2, 4);
        mat.put(2, 2, 7);
        mat.put(3, 2, 5.5f);

        mat.put(0, 3, 3.2f);
        mat.put(1, 3, 2.1f);
        mat.put(2, 3, 5f);
        mat.put(3, 3, 6.6f);

        //System.out.println(Matrix.toString("Matrix ", mat));

        VMath.adjugate(mat, res);

        //System.out.println(Matrix.toString("Adjugate ", res));

        assertEquals(20.42501f, res.get(0, 0));
        assertEquals(-95.39998f, res.get(1, 0));
        assertEquals(41.60000f, res.get(2, 0));
        assertEquals(-41.87500f, res.get(3, 0));

        assertEquals(-138.89999f, res.get(0, 1));
        assertEquals(-39.70000f, res.get(1, 1));
        assertEquals(-102.50000f, res.get(2, 1));
        assertEquals(238.75000f, res.get(3, 1));

        assertEquals(107.899994f, res.get(0, 2));
        assertEquals(42.749996f, res.get(1, 2));
        assertEquals(-188.90001f, res.get(2, 2));
        assertEquals(36.50000f, res.get(3, 2));

        assertEquals(-47.450012f, res.get(0, 3));
        assertEquals(26.500006f, res.get(1, 3));
        assertEquals(155.54999f, res.get(2, 3));
        assertEquals(-197.25000f, res.get(3, 3));
    }

    @Test
    void inverse() {
        ABFloat4x4 res = new ABFloat4x4();
        BBFloat4x4 mat = new BBFloat4x4(true);

        mat.put(0, 0, 1);
        mat.put(1, 0, 5.5f);
        mat.put(2, 0, 3);
        mat.put(3, 0, 7);

        mat.put(0, 1, 8);
        mat.put(1, 1, 2);
        mat.put(2, 1, 1.5f);
        mat.put(3, 1, 1);

        mat.put(0, 2, 3);
        mat.put(1, 2, 4);
        mat.put(2, 2, 7);
        mat.put(3, 2, 5.5f);

        mat.put(0, 3, 3.2f);
        mat.put(1, 3, 2.1f);
        mat.put(2, 3, 5f);
        mat.put(3, 3, 6.6f);

        //System.out.println(mat);

        VMath.inverse(mat, res);

        //System.out.println(mat);

        assertEquals(-0.027161822f, res.get(0, 0));
        assertEquals(0.1268659f, res.get(1, 0));
        assertEquals(-0.05532098561525345, res.get(2, 0));
        assertEquals(0.055686691711825526106f, res.get(3, 0));

        assertEquals(0.18471357f, res.get(0, 1));
        assertEquals(0.052794308321420259988f, res.get(1, 1));
        assertEquals(0.13630772299611024305f, res.get(2, 1));
        assertEquals(-0.31749725722264702952f, res.get(3, 1));

        assertEquals(-0.143488809466362f, res.get(0, 2));
        assertEquals(-0.056850288f, res.get(1, 2));
        assertEquals(0.25120518f, res.get(2, 2));
        assertEquals(-0.04853884770105389139f, res.get(3, 2));

        assertEquals(0.06310052f, res.get(0, 3));
        assertEquals(-0.035240542f, res.get(1, 3));
        assertEquals(-0.20685527f, res.get(2, 3));
        assertEquals(0.26230925230227068722f, res.get(3, 3));
    }
}