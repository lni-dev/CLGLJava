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

package de.linusdev.clgl.nat.glfw3.objects;

import de.linusdev.clgl.api.misc.annos.CallFromAnyThread;
import de.linusdev.clgl.api.misc.annos.CallOnlyFromUIThread;
import de.linusdev.clgl.api.types.bytebuffer.BBInt2;
import de.linusdev.clgl.api.utils.BufferUtils;
import de.linusdev.clgl.nat.Load;
import de.linusdev.clgl.nat.NativeUtils;
import de.linusdev.clgl.nat.custom.StaticCallbackObject;
import de.linusdev.clgl.nat.custom.StaticCallbackObjects;
import de.linusdev.clgl.nat.glad.custom.DebugMessageCallback;
import de.linusdev.clgl.nat.glad.custom.DebugMessageListener;
import de.linusdev.clgl.nat.glfw3.custom.*;
import de.linusdev.lutils.llist.LLinkedList;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.NonBlocking;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.LongBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static de.linusdev.clgl.nat.glad.GLConstants.*;
import static de.linusdev.clgl.nat.glad.Glad.*;
import static de.linusdev.clgl.nat.glfw3.GLFW.*;

@SuppressWarnings("unused")
public class GLFWWindow implements AutoCloseable, StaticCallbackObject<GLFWWindow>, DebugMessageCallback {

    private static final @NotNull StaticCallbackObjects<GLFWWindow> windows = new StaticCallbackObjects<>();

    protected final long pointer;
    protected boolean closed = false;
    protected final int id;

    protected final @NotNull FrameInfoImpl frameInfo = new FrameInfoImpl(100);
    protected @Nullable DebugMessageListener debugMessageListener;


    //Listeners
    protected final @NotNull ListSupplier listenerListSupplier = LLinkedList::new;
    protected final @NotNull List<WindowSizeListener> windowSizeListeners;
    protected final @NotNull List<FramebufferSizeListener> framebufferSizeListeners;
    protected final @NotNull List<FileDropListener> fileDropListeners;
    protected final @NotNull List<CursorEnterListener> cursorEnterListeners;
    protected final @NotNull List<CursorPositionListener> cursorPositionListeners;
    protected final @NotNull List<ScrollListener> scrollListeners;
    protected final @NotNull List<TextInputListener> textInputListeners;
    protected final @NotNull List<KeyListener> keyListeners;
    protected final @NotNull List<MouseButtonListener> mouseButtonListeners;
    protected final static @NotNull List<JoystickListener> joystickListeners = new LLinkedList<>();

    //Array "buffers"
    protected final char[] charCallbackBuffer = new char[2];

    public GLFWWindow() {
        Load.init();
        glfwInit();

        //Create window
        this.pointer = glfwCreateWindow(500, 500, "Window");
        this.id = windows.add(this);

        //Init variables
        this.windowSizeListeners = listenerListSupplier.supply();
        this.framebufferSizeListeners = listenerListSupplier.supply();
        this.fileDropListeners = listenerListSupplier.supply();
        this.cursorEnterListeners = listenerListSupplier.supply();
        this.cursorPositionListeners = listenerListSupplier.supply();
        this.scrollListeners = listenerListSupplier.supply();
        this.textInputListeners = listenerListSupplier.supply();
        this.keyListeners = listenerListSupplier.supply();
        this.mouseButtonListeners = listenerListSupplier.supply();

        //Set user pointer to window id
        glfwSetWindowUserPointer(pointer, id);

        //Set callbacks
        glfwSetWindowSizeCallback(this.pointer);
        glfwSetFramebufferSizeCallback(this.pointer);
        glfwSetKeyCallback(this.pointer);
        glfwSetCursorPosCallback(this.pointer);
        glfwSetCursorEnterCallback(this.pointer);
        glfwSetMouseButtonCallback(this.pointer);
        glfwSetCharCallback(this.pointer);
        glfwSetScrollCallback(this.pointer);
        glfwSetDropCallback(this.pointer);

        //OpenGL
        makeGLContextCurrent();
        gladLoadGL();
    }

    public void makeGLContextCurrent() {
        glfwMakeContextCurrent(pointer);
    }

    public void enableGLDebugMessageListener(@NotNull DebugMessageListener listener) {
        glEnable(GL_DEBUG_OUTPUT);
        glEnable(GL_DEBUG_OUTPUT_SYNCHRONOUS);
        this.debugMessageListener = listener;
        glDebugMessageCallback(this, 0);
    }

    public void setSize(int width, int height) {
        glfwSetWindowSize(pointer, width, height);
    }

    public void setTitle(@NotNull String title) {
        glfwSetWindowTitle(pointer, title);
    }

    public @NotNull BBInt2 getFrameBufferSize(@Nullable BBInt2 size) {
        if(size == null)
            size = new BBInt2(true);
        glfwGetFramebufferSize(pointer, size);
        return size;
    }

    @CallOnlyFromUIThread(value = "glfw", creates = true, claims = true)
    @Blocking
    public void show(@NotNull UpdateListener<GLFWWindow> updateListener) {
        glfwShowWindow(pointer);

        long frameStartMillis = System.currentTimeMillis();

        while (!glfwWindowShouldClose(pointer)) {
            //clear screen
            glClear(GL_COLOR_BUFFER_BIT);

            updateListener.update0(this, frameInfo);

            //swap buffers and poll for events
            glfwSwapBuffers(pointer);
            glfwPollEvents();

            //submit frame time
            frameInfo.submitFrame(System.currentTimeMillis() - frameStartMillis);
            frameStartMillis = System.currentTimeMillis();
        }
    }

    @Override
    public void close() {
        closed = true;
        glfwDestroyWindow(pointer);
    }

    public boolean isClosed() {
        return closed;
    }

    @Override
    public int getId() {
        return id;
    }

    //Java listeners
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
    public static void addJoystickEventListener(@NotNull JoystickListener listener) {
        joystickListeners.add(listener);
    }

    @CallFromAnyThread
    @NonBlocking
    public static void removeJoystickEventListener(@NotNull JoystickListener listener) {
        joystickListeners.remove(listener);
    }

    //Native callbacks

    @Override
    public void message(int source, int type, int id, int severity, ByteBuffer message, long userParam) {
        if(debugMessageListener != null) {
            String msg = BufferUtils.byteBufferToString(message, false);
            debugMessageListener.onMessage(source, type, id, severity, msg, userParam);
        }
    }

    @SuppressWarnings("unused") //called natively only
    private static void window_size_callback(long p_window, int width, int height) {
        long id = glfwGetWindowUserPointer(p_window);
        GLFWWindow window = windows.get(id);

        for(WindowSizeListener listener : window.windowSizeListeners)
            listener.onWindowSizeChanged(window, width, height);

    }

    @SuppressWarnings("unused") //called natively only
    private static void framebuffer_size_callback(long p_window, int width, int height) {
        long id = glfwGetWindowUserPointer(p_window);
        GLFWWindow window = windows.get(id);

        for(FramebufferSizeListener listener : window.framebufferSizeListeners)
            listener.onFramebufferSizeChanged(window, width, height);

    }

    private static void key_callback(long p_window, int key, int scancode, int action, int mods) {
        long id = glfwGetWindowUserPointer(p_window);
        GLFWWindow window = windows.get(id);

        window.keyListeners.forEach(keyListener -> keyListener.onKey(key, scancode, action, mods));
    }

    private static void cursor_position_callback(long p_window, double xpos, double ypos) {
        long id = glfwGetWindowUserPointer(p_window);
        GLFWWindow window = windows.get(id);

        window.cursorPositionListeners.forEach(cursorPositionListener -> cursorPositionListener.onCursorMoved(xpos, ypos));
    }

    private static void cursor_enter_callback(long p_window, boolean entered) {
        long id = glfwGetWindowUserPointer(p_window);
        GLFWWindow window = windows.get(id);

        window.cursorEnterListeners.forEach(cursorEnterListener -> cursorEnterListener.onCursorEntered(entered));
    }

    private static void mouse_button_callback(long p_window, int button, int action, int mods) {
        long id = glfwGetWindowUserPointer(p_window);
        GLFWWindow window = windows.get(id);

        window.mouseButtonListeners.forEach(mbl -> mbl.onMouseButton(button, action, mods));
    }

    private static void character_callback(long p_window, int codepoint) {
        long id = glfwGetWindowUserPointer(p_window);
        GLFWWindow window = windows.get(id);

        int ret = Character.toChars(codepoint, window.charCallbackBuffer, 0);
        char[] chars = Character.toChars(codepoint);

        window.textInputListeners.forEach(textInputListener ->
                textInputListener.onTextInput(window.charCallbackBuffer, ret == 2));
    }

    private static void scroll_callback(long p_window, double xoffset, double yoffset) {
        long id = glfwGetWindowUserPointer(p_window);
        GLFWWindow window = windows.get(id);

        window.scrollListeners.forEach(scrollListener -> scrollListener.onScroll(xoffset, yoffset));
    }

    private static void drop_callback(long p_window, int count, @NotNull ByteBuffer paths) {
        long id = glfwGetWindowUserPointer(p_window);
        GLFWWindow window = windows.get(id);

        paths.order(ByteOrder.nativeOrder());
        LongBuffer b = paths.asLongBuffer();

        Path[] pathArray = new Path[b.capacity()];

        for(int i = 0; i < b.capacity(); i++) {
            long pointer = b.get(i);
            pathArray[i] = Paths.get(BufferUtils.byteBufferToString(
                    NativeUtils.getBufferFromPointer(pointer, 0), false));
        }

        window.fileDropListeners.forEach(fileDropListener -> fileDropListener.onFilesDropped(pathArray));
    }

    private static void joystick_callback(int jid, int event) {
        joystickListeners.forEach(joystickListener -> joystickListener.onJoystickEvent(jid, event));
    }

}
