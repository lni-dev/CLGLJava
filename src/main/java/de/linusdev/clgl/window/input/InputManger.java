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

import de.linusdev.clgl.nat.glfw3.GLFWValues;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.NotNull;

import static de.linusdev.clgl.nat.glfw3.GLFW.glfwGetKeyScancode;

@SuppressWarnings("unused")
public interface InputManger {

    boolean isPressed(int scancode);

    default boolean isPressed(@NotNull Key key) {
        return isPressed(key.scancode);
    }

    @NotNull Key getKey(int scancode);

    default @NotNull Key getUSKey(@MagicConstant(valuesFromClass = GLFWValues.Keys_US.class) int key) {
        return getKey(glfwGetKeyScancode(key));
    }

}
