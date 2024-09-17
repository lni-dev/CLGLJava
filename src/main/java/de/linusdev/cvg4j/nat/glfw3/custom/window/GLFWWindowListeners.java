/*
 * Copyright (c) 2024 Linus Andera
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

package de.linusdev.cvg4j.nat.glfw3.custom.window;

import de.linusdev.cvg4j.api.misc.annos.CallFromAnyThread;
import de.linusdev.cvg4j.nat.custom.StaticCallbackObject;
import de.linusdev.cvg4j.nat.glfw3.custom.*;
import de.linusdev.cvg4j.nat.glfw3.objects.GLFWWindow;
import de.linusdev.lutils.llist.LLinkedList;
import org.jetbrains.annotations.NonBlocking;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.List;

public class GLFWWindowListeners implements
        AbstractGLFWWindowListeners,
        StaticCallbackObject<GLFWWindowListeners>,
        WindowSizeListener,
        FramebufferSizeListener,
        FileDropListener,
        CursorEnterListener,
        CursorPositionListener,
        ScrollListener,
        TextInputListener,
        KeyListener,
        MouseButtonListener,
        JoystickListener,
        WindowRefreshListener,
        WindowIconificationListener,
        WindowCloseListener
{

    private final @NotNull GLFWWindow window;
    private final int id;

    //Array "buffers"
    final char[] charCallbackBuffer = new char[2];

    //Listeners
    protected final @NotNull ListSupplier listenerListSupplier = LLinkedList::new;
    protected final @NotNull List<WindowSizeListener> windowSizeListeners;
    protected final @NotNull List<FramebufferSizeListener> framebufferSizeListeners;
    protected final @NotNull List<FileDropListener> fileDropListeners;
    protected final @NotNull List<CursorEnterListener> cursorEnterListeners;
    protected final @NotNull List<CursorPositionListener> cursorPositionListeners;
    protected final @NotNull List<ScrollListener> scrollListeners;
    protected final @NotNull List<JoystickListener> joystickListeners;
    protected final @NotNull List<TextInputListener> textInputListeners;
    protected final @NotNull List<KeyListener> keyListeners;
    protected final @NotNull List<MouseButtonListener> mouseButtonListeners;
    protected final @NotNull List<WindowRefreshListener> windowRefreshListeners;
    protected final @NotNull List<WindowIconificationListener> windowIconificationListeners;
    protected final @NotNull List<WindowCloseListener> windowCloseListeners;

    public GLFWWindowListeners(@NotNull GLFWWindow window) {
        this.window = window;
        this.id = GLFWNativeCallbacks.windows.add(this);

        //Init variables
        this.windowSizeListeners = listenerListSupplier.supply();
        this.framebufferSizeListeners = listenerListSupplier.supply();
        this.fileDropListeners = listenerListSupplier.supply();
        this.cursorEnterListeners = listenerListSupplier.supply();
        this.cursorPositionListeners = listenerListSupplier.supply();
        this.scrollListeners = listenerListSupplier.supply();
        this.joystickListeners = listenerListSupplier.supply();
        this.textInputListeners = listenerListSupplier.supply();
        this.keyListeners = listenerListSupplier.supply();
        this.mouseButtonListeners = listenerListSupplier.supply();
        this.windowRefreshListeners = listenerListSupplier.supply();
        this.windowIconificationListeners = listenerListSupplier.supply();
        this.windowCloseListeners = listenerListSupplier.supply();
    }

    @Override
    public int getId() {
        return id;
    }

    public @NotNull GLFWWindow getWindow() {
        return window;
    }

    @Override
    public void onWindowSizeChanged(int width, int height) {
        windowSizeListeners.forEach(windowSizeListener -> windowSizeListener.onWindowSizeChanged(width, height));
    }

    @Override
    public void onJoystickEvent(int jid, int event) {
        joystickListeners.forEach(joystickListener -> joystickListener.onJoystickEvent(jid, event));
    }

    @Override
    public void onMouseButton(int button, int action, int mods) {
        mouseButtonListeners.forEach(mouseButtonListener -> mouseButtonListener.onMouseButton(button, action, mods));
    }

    @Override
    public void onCursorEntered(boolean entered) {
        cursorEnterListeners.forEach(cursorEnterListener -> cursorEnterListener.onCursorEntered(entered));
    }

    @Override
    public void onCursorMoved(double xpos, double ypos) {
        cursorPositionListeners.forEach(cursorPositionListener -> cursorPositionListener.onCursorMoved(xpos, ypos));
    }

    @Override
    public void onFilesDropped(@NotNull Path @NotNull [] paths) {
        fileDropListeners.forEach(fileDropListener -> fileDropListener.onFilesDropped(paths));
    }

    @Override
    public void onFramebufferSizeChanged(int width, int height) {
        framebufferSizeListeners.forEach(framebufferSizeListener -> framebufferSizeListener.onFramebufferSizeChanged(width, height));
    }

    @Override
    public void onKey(int key, int scancode, int action, int mods) {
        keyListeners.forEach(keyListener -> keyListener.onKey(key, scancode, action, mods));
    }

    @Override
    public void onScroll(double xOffset, double yOffset) {
        scrollListeners.forEach(scrollListener -> scrollListener.onScroll(xOffset, yOffset));
    }

    @Override
    public void onTextInput(char[] chars, boolean supplementaryChar) {
        textInputListeners.forEach(textInputListener -> textInputListener.onTextInput(chars, supplementaryChar));
    }

    @Override
    public void onWindowRefresh() {
        windowRefreshListeners.forEach(WindowRefreshListener::onWindowRefresh);
    }

    @Override
    public void onWindowIconification(boolean iconified) {
        windowIconificationListeners.forEach(windowIconificationListener -> windowIconificationListener.onWindowIconification(iconified));
    }

    @Override
    public void onClose() {
        windowCloseListeners.forEach(WindowCloseListener::onClose);
    }

    @CallFromAnyThread
    @NonBlocking
    public void addWindowSizeListener(@NotNull WindowSizeListener listener) {
        windowSizeListeners.add(listener);
    }

    @CallFromAnyThread
    @NonBlocking
    public void removeWindowSizeListener(@NotNull WindowSizeListener listener) {
        windowSizeListeners.remove(listener);
    }

    @CallFromAnyThread
    @NonBlocking
    public void addFramebufferSizeListener(@NotNull FramebufferSizeListener listener) {
        framebufferSizeListeners.add(listener);
    }

    @CallFromAnyThread
    @NonBlocking
    public void removeFramebufferSizeListener(@NotNull FramebufferSizeListener listener) {
        framebufferSizeListeners.remove(listener);
    }

    @CallFromAnyThread
    @NonBlocking
    public void addFileDropListener(@NotNull FileDropListener listener) {
        fileDropListeners.add(listener);
    }

    @CallFromAnyThread
    @NonBlocking
    public void removeFileDropListener(@NotNull FileDropListener listener) {
        fileDropListeners.remove(listener);
    }

    @CallFromAnyThread
    @NonBlocking
    public void addCursorEnterListener(@NotNull CursorEnterListener listener) {
        cursorEnterListeners.add(listener);
    }

    @CallFromAnyThread
    @NonBlocking
    public void removeCursorEnterListener(@NotNull CursorEnterListener listener) {
        cursorEnterListeners.remove(listener);
    }

    @CallFromAnyThread
    @NonBlocking
    public void addCursorPositionListener(@NotNull CursorPositionListener listener) {
        cursorPositionListeners.add(listener);
    }

    @CallFromAnyThread
    @NonBlocking
    public void removeCursorPositionListener(@NotNull CursorPositionListener listener) {
        cursorPositionListeners.remove(listener);
    }

    @CallFromAnyThread
    @NonBlocking
    public void addScrollListener(@NotNull ScrollListener listener) {
        scrollListeners.add(listener);
    }

    @CallFromAnyThread
    @NonBlocking
    public void removeScrollListener(@NotNull ScrollListener listener) {
        scrollListeners.remove(listener);
    }

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

    @CallFromAnyThread
    @NonBlocking
    public void addJoystickEventListener(@NotNull JoystickListener listener) {
        joystickListeners.add(listener);
    }

    @CallFromAnyThread
    @NonBlocking
    public void removeJoystickEventListener(@NotNull JoystickListener listener) {
        joystickListeners.remove(listener);
    }

    @CallFromAnyThread
    @NonBlocking
    public void addWindowRefreshListener(@NotNull WindowRefreshListener listener) {
        windowRefreshListeners.add(listener);
    }

    @CallFromAnyThread
    @NonBlocking
    public void removeWindowRefreshListener(@NotNull WindowRefreshListener listener) {
        windowRefreshListeners.remove(listener);
    }

    @Override
    public void addWindowIconificationListener(@NotNull WindowIconificationListener listener) {
        windowIconificationListeners.add(listener);
    }

    @Override
    public void removeWindowIconificationListener(@NotNull WindowIconificationListener listener) {
        windowIconificationListeners.remove(listener);
    }

    @Override
    public void addWindowCloseListener(@NotNull WindowCloseListener listener) {
        windowCloseListeners.add(listener);
    }

    @Override
    public void removeWindowCloseListener(@NotNull WindowCloseListener listener) {
        windowCloseListeners.remove(listener);
    }
}
