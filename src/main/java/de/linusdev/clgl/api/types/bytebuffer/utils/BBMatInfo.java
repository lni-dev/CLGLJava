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

package de.linusdev.clgl.api.types.bytebuffer.utils;

import de.linusdev.clgl.api.structs.StructureInfo;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.util.function.Supplier;

public class BBMatInfo<M> {

    private final @NotNull Class<M> memberClass;
    private final @NotNull StructureInfo memberInfo;
    private final @NotNull Supplier<@NotNull M> supplier;
    private final int height;

    private final @NotNull StructureInfo info;

    public BBMatInfo(
            @NotNull Class<M> memberClass,
            @NotNull StructureInfo memberInfo,
            @NotNull Supplier<@NotNull M> supplier,
            int height
    ) {
        this.memberClass = memberClass;
        this.memberInfo = memberInfo;
        this.supplier = supplier;
        this.height = height;
        this.info = new StructureInfo(getMemberAlignment(), false,
                0, getMemberSize() * height, 0);
    }

    public M[] createArray() {
        //noinspection unchecked
        return (M[]) Array.newInstance(memberClass, height);
    }

    public int getMemberSize() {
        return memberInfo.getRequiredSize();
    }

    public int getMemberAlignment() {
        return memberInfo.getAlignment();
    }

    public @NotNull M createNew() {
        return supplier.get();
    }

    public @NotNull StructureInfo getInfo() {
        return info;
    }
}
