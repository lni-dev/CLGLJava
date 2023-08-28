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

package de.linusdev.clgl.nat.cl.structs;

import de.linusdev.clgl.nat.NativeUtils;
import de.linusdev.clgl.nat.cl.CL;
import de.linusdev.clgl.nat.cl.objects.MemoryObject;
import de.linusdev.lutils.math.vector.buffer.intn.BBInt1;
import de.linusdev.lutils.math.vector.buffer.longn.BBLong1;
import de.linusdev.lutils.struct.abstracts.ComplexStructure;
import de.linusdev.lutils.struct.annos.StructValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CLImageDesc extends ComplexStructure {

    @StructValue(0) public final @NotNull BBInt1 image_type = new BBInt1(false);
    @StructValue(1) public final @NotNull BBLong1 image_width = new BBLong1(false);
    @StructValue(2) public final @NotNull BBLong1 image_height = new BBLong1(false);
    @StructValue(3) public final @NotNull BBLong1 image_depth = new BBLong1(false);
    @StructValue(4) public final @NotNull BBLong1 image_array_size = new BBLong1(false);
    @StructValue(5) public final @NotNull BBLong1 image_row_pitch = new BBLong1(false);
    @StructValue(6) public final @NotNull BBLong1 image_slice_pitch = new BBLong1(false);
    @StructValue(7) public final @NotNull BBInt1 num_mip_levels = new BBInt1(false);
    @StructValue(8) public final @NotNull BBInt1 num_samples = new BBInt1(false);
    @StructValue(9) public final @NotNull MemoryObject buffer = new MemoryObject(false);

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
        init(true,
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
