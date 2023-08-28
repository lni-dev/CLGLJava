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

package de.linusdev.clgl.engine.structs;

import de.linusdev.lutils.math.vector.buffer.intn.BBInt1;
import de.linusdev.lutils.struct.abstracts.ComplexStructure;
import de.linusdev.lutils.struct.annos.FixedLength;
import de.linusdev.lutils.struct.annos.StructValue;
import de.linusdev.lutils.struct.array.StructureArray;
import org.jetbrains.annotations.NotNull;

public class WorldStruct extends ComplexStructure {

    @StructValue public final @NotNull BBInt1 int1 = new BBInt1(false);
    @StructValue public final @NotNull BBInt1 int2 = new BBInt1(false);
    @StructValue public final @NotNull CameraStruct cam = new CameraStruct(false);
    @StructValue @FixedLength(value = 5, elementTypes = PlayerStruct.class) public final @NotNull StructureArray<PlayerStruct> players = new StructureArray<>(false, true, PlayerStruct.class, 5, () -> new PlayerStruct(false));
    @StructValue public final @NotNull PlayerStruct playerA = new PlayerStruct(false);
    @StructValue public final @NotNull PlayerStruct playerB = new PlayerStruct(false);


    public WorldStruct(boolean allocateBuffer) {
        super(true);
        init(allocateBuffer);
    }
}
