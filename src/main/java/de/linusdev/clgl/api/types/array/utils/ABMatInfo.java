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

package de.linusdev.clgl.api.types.array.utils;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.util.function.BiFunction;

public class ABMatInfo<M> {

    private final @NotNull Class<M> memberClass;
    private final int memberSize;
    private final @NotNull BiFunction<Object, Integer, M> creator;
    private final int height;

    public ABMatInfo(
            @NotNull Class<M> memberClass,
            int memberSize,
            @NotNull BiFunction<Object, Integer, M> creator,
            int height
    ) {
        this.memberClass = memberClass;
        this.memberSize = memberSize;
        this.creator = creator;
        this.height = height;
    }

    public M[] createArray() {
        //noinspection unchecked
        return (M[]) Array.newInstance(memberClass, height);
    }

    public int getMemberSize() {
        return memberSize;
    }

    public @NotNull M createNew(Object array, int offset) {
        return creator.apply(array, offset);
    }
}

