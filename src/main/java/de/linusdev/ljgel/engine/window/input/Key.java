/*
 * Copyright (c) 2023-2025 Linus Andera
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

package de.linusdev.ljgel.engine.window.input;

import de.linusdev.ljgel.nat.glfw3.GLFWValues;
import org.jetbrains.annotations.NotNull;

import static de.linusdev.ljgel.nat.glfw3.GLFW._glfwGetKeyName;

@SuppressWarnings("unused")
public class Key extends PressableImpl {

    final int scancode;
    final String name;

    Key(
            @NotNull InputManger manger,
            int scancode
    ) {
        super(manger, InputType.KEYBOARD_KEY);
        this.scancode = scancode;
        this.name = _glfwGetKeyName(GLFWValues.Keys_US.GLFW_KEY_UNKNOWN, scancode);
    }

    @Override
    public String toString() {
        return name + "(" + scancode + ")";
    }

    @Override
    public boolean isPressed() {
        return manager.isKeyPressed(scancode);
    }

    @Override
    public @NotNull Serializable toSerializable() {
        return new Serializable(scancode, getType());
    }
}
