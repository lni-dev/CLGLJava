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

import java.lang.reflect.Field;

public class IllegalStructValueException extends RuntimeException {

    private final @NotNull Class<?> structClass;
    private final @NotNull Field field;
    private final @NotNull String message;

    public IllegalStructValueException(@NotNull Class<?> structClass, @NotNull Field field, @NotNull String message) {
        this.structClass = structClass;
        this.field = field;
        this.message = message;
    }

    @Override
    public String getMessage() {
        return "Field '" + field.getType().getSimpleName() +
                field.getName() + "' in structure " + structClass.getCanonicalName() + " is not a valid struct value: " + message;
    }
}
