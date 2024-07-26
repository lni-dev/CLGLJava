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

package de.linusdev.cvg4j.engine.structs;


import de.linusdev.lutils.math.vector.buffer.floatn.BBFloat3;
import de.linusdev.lutils.nat.abi.DefaultABIs;
import de.linusdev.lutils.nat.abi.OverwriteChildABI;
import de.linusdev.lutils.nat.struct.abstracts.ComplexStructure;
import de.linusdev.lutils.nat.struct.annos.StructValue;
import de.linusdev.lutils.nat.struct.annos.StructureLayoutSettings;
import org.jetbrains.annotations.NotNull;

@StructureLayoutSettings(value = DefaultABIs.CVG4J_OPEN_CL, overwriteChildrenABI = OverwriteChildABI.FORCE_OVERWRITE)
public class PlayerStruct extends ComplexStructure {

    @StructValue
    public final @NotNull BBFloat3 position = BBFloat3.newUnallocated();
    @StructValue public final @NotNull BBFloat3 color = BBFloat3.newUnallocated();


    public PlayerStruct(boolean allocateBuffer) {
        super(true);
        init(null, allocateBuffer);
        if(allocateBuffer) allocate();
    }

}
