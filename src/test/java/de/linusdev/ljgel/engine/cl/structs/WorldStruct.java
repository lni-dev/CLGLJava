/*
 * Copyright (c) 2023-2025 Linus Andera
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

package de.linusdev.ljgel.engine.cl.structs;

import de.linusdev.lutils.math.vector.buffer.intn.BBInt1;
import de.linusdev.lutils.nat.abi.DefaultABIs;
import de.linusdev.lutils.nat.abi.OverwriteChildABI;
import de.linusdev.lutils.nat.struct.abstracts.ComplexStructure;
import de.linusdev.lutils.nat.struct.annos.StructValue;
import de.linusdev.lutils.nat.struct.annos.StructureLayoutSettings;
import de.linusdev.lutils.nat.struct.array.StructureArray;
import org.jetbrains.annotations.NotNull;

@StructureLayoutSettings(value = DefaultABIs.CVG4J_OPEN_CL, overwriteChildrenABI = OverwriteChildABI.FORCE_OVERWRITE)
public class WorldStruct extends ComplexStructure {

    @StructValue public final @NotNull BBInt1 int1 = BBInt1.newUnallocated();
    @StructValue public final @NotNull BBInt1 int2 = BBInt1.newUnallocated();
    @StructValue public final @NotNull CameraStruct cam = new CameraStruct(false);
    @StructValue(length = 5, elementType = PlayerStruct.class)
    public final @NotNull StructureArray<PlayerStruct> players = StructureArray.newUnallocated(false, () -> new PlayerStruct(false));
    @StructValue public final @NotNull PlayerStruct playerA = new PlayerStruct(false);
    @StructValue public final @NotNull PlayerStruct playerB = new PlayerStruct(false);


    public WorldStruct(boolean allocateBuffer) {
        super(true);
        init(null, allocateBuffer);
        if(allocateBuffer)
            allocate();
    }
}
