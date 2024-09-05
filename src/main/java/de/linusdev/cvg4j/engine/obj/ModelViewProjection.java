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

package de.linusdev.cvg4j.engine.obj;

import de.linusdev.cvg4j.nat.abi.StandardUniformBlockABI;
import de.linusdev.lutils.math.matrix.buffer.floatn.BBFloat4x4;
import de.linusdev.lutils.nat.struct.abstracts.ComplexStructure;
import de.linusdev.lutils.nat.struct.annos.SVWrapper;
import de.linusdev.lutils.nat.struct.annos.StructValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ModelViewProjection extends ComplexStructure {

    public static ModelViewProjection newUnAllocatedForOpenGLUniform() {
        return new ModelViewProjection(SVWrapper.overwriteLayout(StandardUniformBlockABI.class));
    }

    @StructValue(0)
    public final @NotNull BBFloat4x4 model = BBFloat4x4.newUnallocated();
    @StructValue(1)
    public final @NotNull BBFloat4x4 view = BBFloat4x4.newUnallocated();
    @StructValue(2)
    public final @NotNull BBFloat4x4 projection = BBFloat4x4.newUnallocated();

    public ModelViewProjection(@Nullable StructValue structValue) {
        super(false);
        init(structValue, true, model, view, projection);


    }
}
