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
import de.linusdev.clgl.nat.glfw3.custom.ListSupplier;
import de.linusdev.clgl.nat.glfw3.custom.MouseButtonListener;
import de.linusdev.clgl.nat.glfw3.custom.TextInputListener;
import de.linusdev.clgl.nat.glfw3.objects.GLFWWindow;
import de.linusdev.lutils.llist.LLinkedList;
import org.jetbrains.annotations.NonBlocking;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class InputManagerImpl implements InputManger, KeyListener, MouseButtonListener, TextInputListener {

    private static final int SCANCODE_ARRAY_SIZE = 20;
    private static final int MAX_SCANCODE = SCANCODE_ARRAY_SIZE * Integer.SIZE - 1;

    protected final @NotNull ListSupplier listenerListSupplier = LLinkedList::new;

    private final int[] scancodes;
    private final @Nullable Key @NotNull [] keys = new Key[MAX_SCANCODE];
    protected final @NotNull List<KeyListener> keyListeners;

    private int mouseButtonCodes = 0;
    private final @Nullable MouseButton @NotNull [] mouseButtons = new MouseButton[Integer.SIZE];
    protected final @NotNull List<MouseButtonListener> mouseButtonListeners;

    protected final @NotNull List<TextInputListener> textInputListeners;

    /**
     * Every {@link GLFWWindow} may only have a single {@link InputManagerImpl}.
     * @param window window to listen to. If {@code null}, {@link #onKey(int, int, int, int)} and {@link #onMouseButton(int, int, int)}
     *               must be called to transmit events.
     */
    public InputManagerImpl(@Nullable GLFWWindow window) {
        this.scancodes = new int[SCANCODE_ARRAY_SIZE];
        if(window != null) {
            window.setKeyListener(this);
            window.setMouseButtonListener(this);
            window.setTextInputListener(this);
        }
        this.keyListeners = listenerListSupplier.supply();
        this.mouseButtonListeners = listenerListSupplier.supply();
        this.textInputListeners = listenerListSupplier.supply();
    }

    private void press(int scancode) {
        scancodes[scancode / 32] |= (1 << (scancode & 31));
        if(keys[scancode]  != null)
            keys[scancode].onPress();
    }

    private void release(int scancode) {
        scancodes[scancode / 32] &= ~(1 << (scancode & 31));
        if(keys[scancode]  != null)
            keys[scancode].onRelease();
    }

    private void pressMouseButton(int mouseButtonCode) {
        mouseButtonCodes |= (1 << (mouseButtonCode));
        if(mouseButtons[mouseButtonCode] != null)
            mouseButtons[mouseButtonCode].onPress();
    }

    private void releaseMouseButton(int mouseButtonCode) {
        mouseButtonCodes &= ~(1 << (mouseButtonCode));
        if(mouseButtons[mouseButtonCode] != null)
            mouseButtons[mouseButtonCode].onRelease();
    }

    @Override
    public boolean isKeyPressed(int scancode) {
        return (scancodes[scancode / 32] & (1 << (scancode & 31))) != 0;
    }

    @Override
    public boolean isMouseButtonPressed(int button) {
        return (mouseButtonCodes & (1 << (button))) != 0;
    }

    @Override
    public @NotNull Key getKey(int scancode) {
        Key key = keys[scancode];

        return key == null ? keys[scancode] = new Key(this, scancode) : key;
    }

    @Override
    public @NotNull MouseButton getMouseButton(int button) {
        MouseButton mouseButton = mouseButtons[button];
        return mouseButton == null ? mouseButtons[button] = new MouseButton(this, button) : mouseButton;
    }

    /*
    Add/Remove listeners
     */

    @CallFromAnyThread
    @NonBlocking
    public void addTextInputListener(@NotNull TextInputListener listener) {
        textInputListeners.add(listener);
    }

    @CallFromAnyThread
    @NonBlocking
    public void removeTextInputListener(@NotNull TextInputListener listener) {
        textInputListeners.remove(listener);
    }

    @CallFromAnyThread
    @NonBlocking
    public void addKeyListener(@NotNull KeyListener listener) {
        keyListeners.add(listener);
    }

    @CallFromAnyThread
    @NonBlocking
    public void removeKeyListener(@NotNull KeyListener listener) {
        keyListeners.remove(listener);
    }

    @CallFromAnyThread
    @NonBlocking
    public void addMouseButtonListener(@NotNull MouseButtonListener listener) {
        mouseButtonListeners.add(listener);
    }

    @CallFromAnyThread
    @NonBlocking
    public void removeMouseButtonListener(@NotNull MouseButtonListener listener) {
        mouseButtonListeners.remove(listener);
    }

    /*
    Listeners
     */

    @Override
    public void onKey(int key, int scancode, int action, int mods) {
        if(action == GLFWValues.Actions.GLFW_PRESS)
            press(scancode);
        else if(action == GLFWValues.Actions.GLFW_RELEASE)
            release(scancode);

        this.keyListeners.forEach(keyListener -> keyListener.onKey(key, scancode, action, mods));
    }

    @Override
    public void onMouseButton(int button, int action, int mods) {
        if(action == GLFWValues.Actions.GLFW_PRESS)
            pressMouseButton(button);
        else if(action == GLFWValues.Actions.GLFW_RELEASE)
            releaseMouseButton(button);

        this.mouseButtonListeners.forEach(mbl -> mbl.onMouseButton(button, action, mods));
    }

    @Override
    public void onTextInput(char[] chars, boolean supplementaryChar) {
        this.textInputListeners.forEach(textInputListener -> textInputListener.onTextInput(chars, supplementaryChar));
    }
}
