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
import de.linusdev.ljgel.nat.glfw3.custom.KeyListener;
import de.linusdev.ljgel.nat.glfw3.custom.TextInputListener;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class TextInput implements TextInputListener, KeyListener {

    private final @NotNull InputManger inputManger;
    private final @NotNull StringBuffer string;

    private final @NotNull Listener listener;

    public TextInput(@NotNull InputManger inputManger, @NotNull Listener listener) {
        this.inputManger = inputManger;
        this.listener = listener;
        this.string = new StringBuffer();
    }
    public void start() {
        inputManger.addTextInputListener(this);
        inputManger.addKeyListener(this);
    }

    public void stop() {
        inputManger.removeTextInputListener(this);
        inputManger.removeKeyListener(this);
    }

    public @NotNull StringBuffer getString() {
        return string;
    }

    @Override
    public void onTextInput(char[] chars, boolean supplementaryChar) {
        System.out.println("onTextInput: " + Arrays.toString(chars));
        string.append(chars);
        listener.onAdd(this, string, chars);
    }

    @Override
    public void onKey(int key, int scancode, int action, int mods) {
        if(key == GLFWValues.Keys_US.GLFW_KEY_BACKSPACE && !string.isEmpty()) {
            char removed = string.charAt(string.length()-1);
            string.setLength(string.length()-1);
            listener.onRemove(this, string, removed);
        } else if(key == GLFWValues.Keys_US.GLFW_KEY_ENTER) {
            listener.onEnter(this, string);
        }
    }

    public interface Listener {
        void onAdd(@NotNull TextInput input, @NotNull StringBuffer current,  char @NotNull[] added);

        void onRemove(@NotNull TextInput input, @NotNull StringBuffer current, char removed);

        void onEnter(@NotNull TextInput input, @NotNull StringBuffer current);
    }
}
