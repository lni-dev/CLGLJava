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
import de.linusdev.clgl.nat.glfw3.custom.KeyListener;
import de.linusdev.clgl.nat.glfw3.objects.GLFWWindow;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class InputManagerImpl implements InputManger, KeyListener {

    private static final int SCANCODE_ARRAY_SIZE = 20;
    private static final int MAX_SCANCODE = SCANCODE_ARRAY_SIZE * Integer.SIZE - 1;

    private final int[] scancodes;
    private final @Nullable Key @NotNull [] keys = new Key[MAX_SCANCODE];

    public InputManagerImpl(@NotNull GLFWWindow window) {
        this.scancodes = new int[SCANCODE_ARRAY_SIZE];
        window.addKeyListener(this);
    }

    private void press(int scancode) {
        scancodes[scancode / 32] |= (1 << (scancode & 31));
        keys[scancode].onPress();
    }

    private void release(int scancode) {
        scancodes[scancode / 32] &= ~(1 << (scancode & 31));
        keys[scancode].onRelease();
    }

    @Override
    public boolean isPressed(int scancode) {
        return (scancodes[scancode / 32] & (1 << (scancode & 31))) != 0;
    }

    @Override
    public @NotNull Key getKey(int scancode) {
        Key key = keys[scancode];

        return key == null ? keys[scancode] = new Key(scancode) : key;
    }

    @Override
    public void onKey(int key, int scancode, int action, int mods) {
        if(action == GLFWValues.Actions.GLFW_PRESS)
            press(scancode);
        else if(action == GLFWValues.Actions.GLFW_RELEASE)
            release(scancode);
    }
}
