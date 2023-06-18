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

package de.linusdev.clgl.window.input;

import de.linusdev.lutils.bitfield.IntBitFieldValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static de.linusdev.clgl.nat.glfw3.GLFWValues.Buttons.*;

@SuppressWarnings("unused")
public enum StandardMouseButton implements IntBitFieldValue {

    MOUSE_BUTTON_1(GLFW_MOUSE_BUTTON_1),
    MOUSE_BUTTON_2(GLFW_MOUSE_BUTTON_2),
    MOUSE_BUTTON_3(GLFW_MOUSE_BUTTON_3),
    MOUSE_BUTTON_4(GLFW_MOUSE_BUTTON_4),
    MOUSE_BUTTON_5(GLFW_MOUSE_BUTTON_5),
    MOUSE_BUTTON_6(GLFW_MOUSE_BUTTON_6),
    MOUSE_BUTTON_7(GLFW_MOUSE_BUTTON_7),
    MOUSE_BUTTON_8(GLFW_MOUSE_BUTTON_8),

    ;

    private static final @NotNull StandardMouseButton @NotNull [] translateArray = new StandardMouseButton[] {
            MOUSE_BUTTON_1, MOUSE_BUTTON_2, MOUSE_BUTTON_3, MOUSE_BUTTON_4,
            MOUSE_BUTTON_5, MOUSE_BUTTON_6, MOUSE_BUTTON_7, MOUSE_BUTTON_8,
    };

    public static @Nullable StandardMouseButton translate(int button) {
        if(button > translateArray.length) return null;
        return translateArray[button];
    }

    public static final StandardMouseButton MOUSE_BUTTON_LEFT = MOUSE_BUTTON_1;
    public static final StandardMouseButton MOUSE_BUTTON_RIGHT = MOUSE_BUTTON_2;
    public static final StandardMouseButton MOUSE_BUTTON_MIDDLE = MOUSE_BUTTON_3;

    private final int value;

    StandardMouseButton(int value) {
        this.value = value;
    }

    @Override
    public int getValue() {
        return value;
    }
}
