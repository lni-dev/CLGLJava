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

package de.linusdev.clgl.api.types;

import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public class VMath {

    public static @NotNull FloatN add(@NotNull FloatN left, @NotNull FloatN right, @NotNull FloatN store) {
        for(int i = 0; i < left.getMemberCount(); i++)
            store.put(i, left.get(i) + right.get(i));

        return store;
    }

    public static @NotNull FloatN subtract(@NotNull FloatN left, @NotNull FloatN right, @NotNull FloatN store) {
        for(int i = 0; i < left.getMemberCount(); i++)
            store.put(i, left.get(i) - right.get(i));

        return store;
    }

    public static @NotNull FloatN multiply(@NotNull FloatN left, @NotNull FloatN right, @NotNull FloatN store) {
        for(int i = 0; i < left.getMemberCount(); i++)
            store.put(i, left.get(i) * right.get(i));

        return store;
    }

    public static @NotNull FloatN divide(@NotNull FloatN left, @NotNull FloatN right, @NotNull FloatN store) {
        for(int i = 0; i < left.getMemberCount(); i++)
            store.put(i, left.get(i) / right.get(i));

        return store;
    }

    public static @NotNull FloatN scale(@NotNull FloatN toScale, float factor, @NotNull FloatN store) {
        for(int i = 0; i < toScale.getMemberCount(); i++)
            store.put(i, toScale.get(i) * factor);

        return store;
    }

    public static float dot(@NotNull FloatN left, @NotNull FloatN right) {
        float dot = 0.0f;
        for(int i = 0; i < left.getMemberCount(); i++)
            dot += left.get(i) * right.get(i);

        return dot;
    }


}
