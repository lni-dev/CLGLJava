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

package de.linusdev.cvg4j.nat.glad;

import de.linusdev.lutils.bitfield.IntBitFieldValue;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public enum GLError implements IntBitFieldValue {

    GL_NO_ERROR(0),
    GL_INVALID_ENUM(0x0500),
    GL_INVALID_VALUE(0x0501),
    GL_INVALID_OPERATION(0x0502),
    GL_STACK_OVERFLOW(0x0503),
    GL_STACK_UNDERFLOW(0x0504),
    GL_OUT_OF_MEMORY(0x0505),

    GL_ERROR_UNKNOWN(-1),
    ;

    private final int value;

    GLError(int value) {
        this.value = value;
    }

    public static @NotNull GLError fromInt(int code) {
        for(GLError c : values()) {
            if(c.value == code) return c;
        }

        return GL_ERROR_UNKNOWN;
    }

    public static void check(int code) {
        if(code != GL_NO_ERROR.value)
            throw new GLException(code);
    }

    @Override
    public String toString() {
        return name() + " (" + value + ")";
    }

    @Override
    public int getValue() {
        return value;
    }
}
