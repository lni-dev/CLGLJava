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

package de.linusdev.cvg4j.nat.abi;

import de.linusdev.cvg4j.engine.obj.ModelViewProjection;
import de.linusdev.lutils.math.matrix.buffer.floatn.BBFloat4x4;
import de.linusdev.lutils.math.vector.buffer.floatn.BBFloat1;
import de.linusdev.lutils.math.vector.buffer.floatn.BBFloat3;
import de.linusdev.lutils.math.vector.buffer.intn.BBInt1;
import de.linusdev.lutils.nat.struct.abstracts.ComplexStructure;
import de.linusdev.lutils.nat.struct.annos.SVWrapper;
import de.linusdev.lutils.nat.struct.annos.StructValue;
import de.linusdev.lutils.nat.struct.array.StructureArray;
import org.junit.jupiter.api.Test;

import static de.linusdev.lutils.nat.struct.abstracts.Structure.allocate;
import static org.junit.jupiter.api.Assertions.assertEquals;

class StandardUniformBlockABITest {

    public static class ExampleStruct extends ComplexStructure {

        @StructValue(0)
        public final BBFloat1 value = BBFloat1.newUnallocated();

        @StructValue(1)
        public final BBFloat3 vector = BBFloat3.newUnallocated();

        @StructValue(2)
        public final BBFloat4x4 matrix = BBFloat4x4.newUnallocated();

        @StructValue(value = 3, length = 3, elementType = BBFloat1.class)
        public final StructureArray<BBFloat1> values = StructureArray.newUnallocated(false, BBFloat1::newUnallocated);

        @StructValue(4)
        public final BBInt1 bool = BBInt1.newUnallocated();

        @StructValue(5)
        public final BBInt1 integer = BBInt1.newUnallocated();

        public ExampleStruct() {
            super(false);
            init(SVWrapper.overwriteLayout(StandardUniformBlockABI.class), true, value, vector, matrix, values, bool, integer);
        }
    }

    @Test
    void test() {
        ModelViewProjection mvp = ModelViewProjection.newUnAllocatedForOpenGLUniform();
        allocate(mvp);

        assertEquals("ModelViewProjection (alignment=16, size=192, offsetStart=0, offsetEnd=192) {\n" +
                "        model: float4x4 (alignment=16, size=64, offsetStart=0, offsetEnd=64) {\n" +
                "            float4x4:\n" +
                "            [\n" +
                "                   0.00        0.00        0.00        0.00 \n" +
                "                   0.00        0.00        0.00        0.00 \n" +
                "                   0.00        0.00        0.00        0.00 \n" +
                "                   0.00        0.00        0.00        0.00 \n" +
                "            ]\n" +
                "        }\n" +
                "        view: float4x4 (alignment=16, size=64, offsetStart=64, offsetEnd=128) {\n" +
                "            float4x4:\n" +
                "            [\n" +
                "                   0.00        0.00        0.00        0.00 \n" +
                "                   0.00        0.00        0.00        0.00 \n" +
                "                   0.00        0.00        0.00        0.00 \n" +
                "                   0.00        0.00        0.00        0.00 \n" +
                "            ]\n" +
                "        }\n" +
                "        projection: float4x4 (alignment=16, size=64, offsetStart=128, offsetEnd=192) {\n" +
                "            float4x4:\n" +
                "            [\n" +
                "                   0.00        0.00        0.00        0.00 \n" +
                "                   0.00        0.00        0.00        0.00 \n" +
                "                   0.00        0.00        0.00        0.00 \n" +
                "                   0.00        0.00        0.00        0.00 \n" +
                "            ]\n" +
                "        }\n" +
                "}", mvp.toString());
        System.out.println(mvp);
        System.out.println(mvp.getInfo());

    }

    @Test
    void test2() {
        ExampleStruct exampleStruct = allocate(new ExampleStruct());

        assertEquals("Example (alignment=16, size=160, offsetStart=0, offsetEnd=160) {\n" +
                "        value: float1 (alignment=4, size=4, offsetStart=0, offsetEnd=4) {\n" +
                "            float1(0.0)\n" +
                "        }\n" +
                "        padding: { size=12 }\n" +
                "        vector: float3 (alignment=16, size=16, offsetStart=16, offsetEnd=32) {\n" +
                "            float3(0.0, 0.0, 0.0)\n" +
                "        }\n" +
                "        matrix: float4x4 (alignment=16, size=64, offsetStart=32, offsetEnd=96) {\n" +
                "            float4x4:\n" +
                "            [\n" +
                "                   0.00        0.00        0.00        0.00 \n" +
                "                   0.00        0.00        0.00        0.00 \n" +
                "                   0.00        0.00        0.00        0.00 \n" +
                "                   0.00        0.00        0.00        0.00 \n" +
                "            ]\n" +
                "        }\n" +
                "        values: StructureArray<BBFloat1> (alignment=16, size=48, offsetStart=96, offsetEnd=144) {\n" +
                "            length=3\n" +
                "            stride=16\n" +
                "            items={\n" +
                "                0 (offsetStart=96): null,\n" +
                "                1 (offsetStart=112): null,\n" +
                "                2 (offsetStart=128): null,\n" +
                "            }\n" +
                "        }\n" +
                "        bool: int1 (alignment=4, size=4, offsetStart=144, offsetEnd=148) {\n" +
                "            int1(0)\n" +
                "        }\n" +
                "        integer: int1 (alignment=4, size=4, offsetStart=148, offsetEnd=152) {\n" +
                "            int1(0)\n" +
                "        }\n" +
                "        padding: { size=8 }\n" +
                "}", exampleStruct.toString());
        System.out.println(exampleStruct);
        System.out.println(exampleStruct.getInfo());
    }

}