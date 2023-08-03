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

package de.linusdev.clgl.api.structs;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@StructureSettings(isOfVariableSize = true, supportsCalculateInfoMethod = true)
@SuppressWarnings("unused")
public class StructureArray<T extends Structure> extends ComplexStructure implements NativeArray<T> {

    public static @NotNull StructureInfo calculateInfo(@NotNull Sizeable elementType, int size) {
        return new StructureInfo(
                elementType.getAlignment(),
                false,
                0,
                elementType.getRequiredSize() * size,
                0
        );
    }

    private final @NotNull Sizeable type;
    private final @NotNull StructureInfo info;
    private final @NotNull ElementCreator<T> creator;

    private final @Nullable Structure @NotNull [] items;
    private final int size;

    public StructureArray(
            boolean allocateBuffer,
            boolean trackModifications,
            @NotNull Sizeable type,
            int size,
            @NotNull ElementCreator<T> creator
    ) {
        super(trackModifications);
        this.type = type;
        this.size = size;
        super.items = null; //Set this to null, so useBuffer will not do weird stuff
        this.items = new Structure[size];
        this.creator = creator;
        this.info = calculateInfo(type, size);

        if(allocateBuffer)
            allocate();

    }

    public void set(int index, @NotNull T struct) {
        items[index] = struct;

        struct.useBuffer(this.mostParentStructure, this.offset + (type.getRequiredSize() * index));
    }

    @Override
    public void useBuffer(@NotNull Structure mostParentStructure, int offset) {
        super.useBuffer(mostParentStructure, offset);
    }

    @Override
    public int length() {
        return size;
    }

    @SuppressWarnings("unchecked")
    public T get(int index) {
        if(items[index] == null) {
            items[index] = creator.create();
            items[index].useBuffer(this.mostParentStructure, this.offset + (type.getRequiredSize() * index));
        }

        return (T) items[index];
    }

    @Override
    protected @NotNull StructureInfo getInfo() {
        return info;
    }

    @Override
    public @NotNull String getOpenCLName(@Nullable Class<?> elementType, int size, @NotNull String paramName) {
        return "StructureArray " + paramName + "[" + size + "]";
    }

    @FunctionalInterface
    public interface ElementCreator<T extends Structure> {
        @Nullable T create();
    }
}
