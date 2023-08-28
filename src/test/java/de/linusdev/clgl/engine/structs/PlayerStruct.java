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


import de.linusdev.lutils.math.vector.buffer.floatn.BBFloat3;
import de.linusdev.lutils.struct.abstracts.ComplexStructure;
import de.linusdev.lutils.struct.annos.StructValue;
import org.jetbrains.annotations.NotNull;

public class PlayerStruct extends ComplexStructure {

    @StructValue
    public final @NotNull BBFloat3 position = new BBFloat3(false);
    @StructValue public final @NotNull BBFloat3 color = new BBFloat3(false);


    public PlayerStruct(boolean allocateBuffer) {
        super(true);
        init(allocateBuffer);
    }

}
