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

import de.linusdev.clgl.api.misc.annos.CallFromAnyThread;
import de.linusdev.clgl.nat.glfw3.GLFWValues;
import de.linusdev.clgl.nat.glfw3.custom.KeyListener;
import de.linusdev.clgl.nat.glfw3.custom.MouseButtonListener;
import de.linusdev.clgl.nat.glfw3.custom.TextInputListener;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.NonBlocking;
import org.jetbrains.annotations.NotNull;

import static de.linusdev.clgl.nat.glfw3.GLFW.glfwGetKeyScancode;

@SuppressWarnings("unused")
public interface InputManger {

    boolean isKeyPressed(int scancode);

    default boolean isKeyPressed(@NotNull Key key) {
        return isKeyPressed(key.scancode);
    }

    boolean isMouseButtonPressed(int button);

    default boolean isMouseButtonPressed(@NotNull MouseButton button) {
        return isMouseButtonPressed(button.button);
    }

    @NotNull Key getKey(int scancode);

    default @NotNull Key getUSKey(@MagicConstant(valuesFromClass = GLFWValues.Keys_US.class) int key) {
        return getKey(glfwGetKeyScancode(key));
    }

    default @NotNull MouseButton getMouseButton(@NotNull StandardMouseButton button) {
        return getMouseButton(button.getValue());
    }

    @NotNull MouseButton getMouseButton(int button);

    default @NotNull Pressable ofSerialized(@NotNull Pressable.Serializable serialized) {
        return switch (serialized.type()) {
            case KEYBOARD_KEY -> getKey(serialized.value());
            case MOUSE_BUTTON -> getMouseButton(serialized.value());
        };
    }

    @CallFromAnyThread
    @NonBlocking
    void addTextInputListener(@NotNull TextInputListener listener);

    @CallFromAnyThread
    @NonBlocking
    void removeTextInputListener(@NotNull TextInputListener listener);

    @CallFromAnyThread
    @NonBlocking
    void addKeyListener(@NotNull KeyListener listener);

    @CallFromAnyThread
    @NonBlocking
    void removeKeyListener(@NotNull KeyListener listener);

    @CallFromAnyThread
    @NonBlocking
    void addMouseButtonListener(@NotNull MouseButtonListener listener);

    @CallFromAnyThread
    @NonBlocking
    void removeMouseButtonListener(@NotNull MouseButtonListener listener);

}
