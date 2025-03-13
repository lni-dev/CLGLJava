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

import de.linusdev.ljgel.api.misc.annos.CallFromAnyThread;
import de.linusdev.ljgel.nat.glfw3.custom.*;
import org.jetbrains.annotations.NonBlocking;
import org.jetbrains.annotations.NotNull;

public interface AbstractGLFWWindowListeners {

    @CallFromAnyThread
    @NonBlocking
    void addWindowSizeListener(@NotNull WindowSizeListener listener);

    @CallFromAnyThread
    @NonBlocking
    void removeWindowSizeListener(@NotNull WindowSizeListener listener);

    @CallFromAnyThread
    @NonBlocking
    void addFramebufferSizeListener(@NotNull FramebufferSizeListener listener);

    @CallFromAnyThread
    @NonBlocking
    void removeFramebufferSizeListener(@NotNull FramebufferSizeListener listener);

    @CallFromAnyThread
    @NonBlocking
    void addFileDropListener(@NotNull FileDropListener listener);

    @CallFromAnyThread
    @NonBlocking
    void removeFileDropListener(@NotNull FileDropListener listener);

    @CallFromAnyThread
    @NonBlocking
    void addCursorEnterListener(@NotNull CursorEnterListener listener);

    @CallFromAnyThread
    @NonBlocking
    void removeCursorEnterListener(@NotNull CursorEnterListener listener);

    @CallFromAnyThread
    @NonBlocking
    void addCursorPositionListener(@NotNull CursorPositionListener listener);

    @CallFromAnyThread
    @NonBlocking
    void removeCursorPositionListener(@NotNull CursorPositionListener listener);

    @CallFromAnyThread
    @NonBlocking
    void addScrollListener(@NotNull ScrollListener listener);

    @CallFromAnyThread
    @NonBlocking
    void removeScrollListener(@NotNull ScrollListener listener);

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

    @CallFromAnyThread
    @NonBlocking
    void addJoystickEventListener(@NotNull JoystickListener listener);

    @CallFromAnyThread
    @NonBlocking
    void removeJoystickEventListener(@NotNull JoystickListener listener);

    @CallFromAnyThread
    @NonBlocking
    void addWindowRefreshListener(@NotNull WindowRefreshListener listener);

    @CallFromAnyThread
    @NonBlocking
    void removeWindowRefreshListener(@NotNull WindowRefreshListener listener);

    @CallFromAnyThread
    @NonBlocking
    void addWindowIconificationListener(@NotNull WindowIconificationListener listener);

    @CallFromAnyThread
    @NonBlocking
    void removeWindowIconificationListener(@NotNull WindowIconificationListener listener);

    @CallFromAnyThread
    @NonBlocking
    void addWindowCloseListener(@NotNull WindowCloseListener listener);

    @CallFromAnyThread
    @NonBlocking
    void removeWindowCloseListener(@NotNull WindowCloseListener listener);

}
