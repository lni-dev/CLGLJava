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

package de.linusdev.cvg4j.engine.vk.memory.buffer.vertex;

import de.linusdev.lutils.math.vector.buffer.floatn.BBFloat2;
import de.linusdev.lutils.math.vector.buffer.floatn.BBFloat3;
import de.linusdev.lutils.nat.struct.abstracts.ComplexStructure;
import de.linusdev.lutils.nat.struct.annos.StructValue;
import org.jetbrains.annotations.NotNull;

public class SimpleVertex extends ComplexStructure {

    @StructValue(0)
    public final @NotNull BBFloat3 position = BBFloat3.newUnallocated();

    @StructValue(1)
    public final @NotNull BBFloat3 color = BBFloat3.newUnallocated();

    @StructValue(2)
    public final @NotNull BBFloat2 texCoord = BBFloat2.newUnallocated();

    public SimpleVertex() {
        super(false);
        init(null, true, position, color, texCoord);
    }
}
