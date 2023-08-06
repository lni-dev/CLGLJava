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
public interface Float2 extends FloatN {

    default float x() {
        return get(0);
    }

    default float y() {
        return get(1);
    }

    default void x(float f) {
        put(0, f);
    }

    default void y(float f) {
        put(1, f);
    }

    default void xy(float x, float y) {
        put(0, x);
        put(1, y);
    }

    @SuppressWarnings("ClassCanBeRecord")
    class Float2View implements Float2 {

        private final @NotNull FloatN original;
        private final int @NotNull [] mapping;

        public Float2View(@NotNull FloatN original, int @NotNull [] mapping) {

            if(original.isView()) {
                mapping = Vector.recalculateMappingToOriginal(original, mapping);
                original = original.getOriginal();
            }

            this.original = original;
            this.mapping = mapping;
        }

        @Override
        public float get(int index) {
            return original.get(mapping[index]);
        }

        @Override
        public void put(int index, float value) {
            original.put(mapping[index], value);
        }

        @Override
        public int getMemberCount() {
            return 2;
        }

        @Override
        public boolean isArrayBacked() {
            return false;
        }

        @Override
        public boolean isBufferBacked() {
            return false;
        }

        @Override
        public boolean isView() {
            return true;
        }

        @NotNull
        @Override
        public FloatN getOriginal() {
            return original;
        }

        @Override
        public int @NotNull [] getMapping() {
            return mapping;
        }
    }

}
