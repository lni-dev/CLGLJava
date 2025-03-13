/*
 * Copyright (c) 2024-2025 Linus Andera
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

package de.linusdev.ljgel.nat.glfw3.custom.window;

import de.linusdev.ljgel.nat.NativeUtils;
import de.linusdev.ljgel.nat.custom.StaticCallbackObjects;
import de.linusdev.ljgel.nat.glfw3.custom.JoystickListener;
import de.linusdev.lutils.llist.LLinkedList;
import de.linusdev.lutils.nat.struct.utils.BufferUtils;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.LongBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static de.linusdev.ljgel.nat.glfw3.GLFW.glfwGetWindowUserPointer;

@SuppressWarnings("unused") // called natively
public class GLFWNativeCallbacks {

    public final static @NotNull List<JoystickListener> joystickListeners = new LLinkedList<>();

    private static void joystick_callback(int jid, int event) {
        joystickListeners.forEach(joystickListener -> joystickListener.onJoystickEvent(jid, event));
    }

    public static final @NotNull StaticCallbackObjects<GLFWWindowListeners> windows = new StaticCallbackObjects<>();

    @SuppressWarnings("unused") //called natively only
    private static void window_size_callback(long p_window, int width, int height) {
        long id = glfwGetWindowUserPointer(p_window);
        var window = windows.get(id);
        window.onWindowSizeChanged(width, height);
    }

    @SuppressWarnings("unused") //called natively only
    private static void framebuffer_size_callback(long p_window, int width, int height) {
        long id = glfwGetWindowUserPointer(p_window);
        var window = windows.get(id);
        window.onFramebufferSizeChanged(width, height);
    }

    private static void key_callback(long p_window, int key, int scancode, int action, int mods) {
        long id = glfwGetWindowUserPointer(p_window);
        var window = windows.get(id);
        window.onKey(key, scancode, action, mods);
    }

    private static void cursor_position_callback(long p_window, double xpos, double ypos) {
        long id = glfwGetWindowUserPointer(p_window);
        var window = windows.get(id);
        window.onCursorMoved(xpos, ypos);
    }

    private static void cursor_enter_callback(long p_window, boolean entered) {
        long id = glfwGetWindowUserPointer(p_window);
        var window = windows.get(id);
        window.onCursorEntered(entered);
    }

    private static void mouse_button_callback(long p_window, int button, int action, int mods) {
        long id = glfwGetWindowUserPointer(p_window);
        var window = windows.get(id);
        window.onMouseButton(button, action, mods);
    }

    private static void character_callback(long p_window, int codepoint) {
        long id = glfwGetWindowUserPointer(p_window);
        var window = windows.get(id);

        int ret = Character.toChars(codepoint, window.charCallbackBuffer, 0);
        window.onTextInput(window.charCallbackBuffer, ret == 2);
    }

    private static void scroll_callback(long p_window, double xoffset, double yoffset) {
        long id = glfwGetWindowUserPointer(p_window);
        var window = windows.get(id);
        window.onScroll(xoffset, yoffset);
    }

    private static void drop_callback(long p_window, int count, @NotNull ByteBuffer paths) {
        long id = glfwGetWindowUserPointer(p_window);
        var window = windows.get(id);

        paths.order(ByteOrder.nativeOrder());
        LongBuffer b = paths.asLongBuffer();

        Path[] pathArray = new Path[b.capacity()];

        for(int i = 0; i < b.capacity(); i++) {
            long pointer = b.get(i);
            pathArray[i] = Paths.get(BufferUtils.readString(
                    NativeUtils.getBufferFromPointer(pointer, 0), false));
        }

        window.onFilesDropped(pathArray);
    }

    private static void window_refresh_callback(long p_window) {
        long id = glfwGetWindowUserPointer(p_window);
        var window = windows.get(id);
        window.onWindowRefresh();
    }

    private static void window_iconified(long p_window, boolean iconified) {
        long id = glfwGetWindowUserPointer(p_window);
        var window = windows.get(id);
        window.onWindowIconification(iconified);
    }

}
