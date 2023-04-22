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

package de.linusdev.clgl.nat.glad.custom;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class BindingID {

    private final @NotNull Object @NotNull [] target;


    public BindingID(@NotNull Object @NotNull ... target) {
        this.target = target;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BindingID bindingID = (BindingID) o;

        return Arrays.equals(target, bindingID.target);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(target);
    }
}
