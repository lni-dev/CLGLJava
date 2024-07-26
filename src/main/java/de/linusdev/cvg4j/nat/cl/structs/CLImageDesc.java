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

package de.linusdev.cvg4j.nat.cl.structs;

import de.linusdev.cvg4j.nat.NativeUtils;
import de.linusdev.cvg4j.nat.cl.CL;
import de.linusdev.cvg4j.nat.cl.objects.MemoryObject;
import de.linusdev.lutils.math.vector.buffer.intn.BBInt1;
import de.linusdev.lutils.math.vector.buffer.longn.BBLong1;
import de.linusdev.lutils.nat.struct.abstracts.ComplexStructure;
import de.linusdev.lutils.nat.struct.annos.StructValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CLImageDesc extends ComplexStructure {

    @StructValue(0) public final @NotNull BBInt1 image_type = BBInt1.newUnallocated();
    @StructValue(1) public final @NotNull BBLong1 image_width = BBLong1.newUnallocated();
    @StructValue(2) public final @NotNull BBLong1 image_height = BBLong1.newUnallocated();
    @StructValue(3) public final @NotNull BBLong1 image_depth = BBLong1.newUnallocated();
    @StructValue(4) public final @NotNull BBLong1 image_array_size = BBLong1.newUnallocated();
    @StructValue(5) public final @NotNull BBLong1 image_row_pitch = BBLong1.newUnallocated();
    @StructValue(6) public final @NotNull BBLong1 image_slice_pitch = BBLong1.newUnallocated();
    @StructValue(7) public final @NotNull BBInt1 num_mip_levels = BBInt1.newUnallocated();
    @StructValue(8) public final @NotNull BBInt1 num_samples = BBInt1.newUnallocated();
    @StructValue(9) public final @NotNull MemoryObject buffer = MemoryObject.newUnallocated();

    public CLImageDesc(
            @NotNull CL.CLMemoryObjectType type,
            long width,
            long height,
            long depth,
            long array_size,
            long row_pitch,
            long slice_pitch,
            int num_mip_levels,
            int num_samples,
            @Nullable MemoryObject buffer
    ) {
        super(false);
        init(null, true,
                this.image_type,
                this.image_width,
                this.image_height,
                this.image_depth,
                this.image_array_size,
                this.image_row_pitch,
                this.image_slice_pitch,
                this.num_mip_levels,
                this.num_samples,
                this.buffer
        );
        allocate();

        setImageType(type);
        this.image_width.set(width);
        this.image_height.set(height);
        this.image_depth.set(depth);
        this.image_array_size.set(array_size);
        this.image_row_pitch.set(row_pitch);
        this.image_slice_pitch.set(slice_pitch);
        this.num_mip_levels.set(num_mip_levels);
        this.num_samples.set(num_samples);
        setBuffer(buffer);
    }

    public void setImageType(@NotNull CL.CLMemoryObjectType type) {
        image_type.set(type.getValue());
    }

    public void setBuffer(@Nullable MemoryObject memoryObject) {
        buffer.set(memoryObject == null ? NativeUtils.getNullPointer() : memoryObject.get());
    }
}
