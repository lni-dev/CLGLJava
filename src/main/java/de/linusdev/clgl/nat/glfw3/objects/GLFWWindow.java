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
import de.linusdev.clgl.nat.custom.StaticCallbackObject;
import de.linusdev.clgl.nat.custom.StaticCallbackObjects;
import de.linusdev.clgl.nat.glad.custom.DebugMessageCallback;
import de.linusdev.clgl.nat.glad.custom.DebugMessageListener;
import de.linusdev.clgl.nat.glfw3.custom.FrameInfoImpl;
import de.linusdev.clgl.nat.glfw3.custom.FramebufferSizeListener;
import de.linusdev.clgl.nat.glfw3.custom.UpdateListener;
import de.linusdev.clgl.nat.glfw3.custom.WindowSizeListener;
import de.linusdev.lutils.llist.LLinkedList;
import org.jetbrains.annotations.*;

import java.nio.ByteBuffer;
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

    protected final @NotNull List<WindowSizeListener> windowSizeListeners;
    protected final @NotNull List<FramebufferSizeListener> framebufferSizeListeners;

    public GLFWWindow() {
        Load.init();
        glfwInit();

        //Create window
        this.pointer = glfwCreateWindow(500, 500, "Window");
        this.id = windows.add(this);

        //Init variables
        this.windowSizeListeners = new LLinkedList<>();
        this.framebufferSizeListeners = new LLinkedList<>();

        //Set user pointer to window id
        glfwSetWindowUserPointer(pointer, id);

        //Set callbacks
        glfwSetWindowSizeCallback(this.pointer, GLFWWindow.class);
        glfwSetFramebufferSizeCallback(this.pointer, GLFWWindow.class);

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

    @CallOnlyFromUIThread(value = "glfw-thread", creates = true, claims = true)
    @Blocking
    public void show(@NotNull UpdateListener updateListener) {
        glfwShowWindow(pointer);

        long frameStartMillis = System.currentTimeMillis();

        while (!glfwWindowShouldClose(pointer)) {
            //clear screen
            glClear(GL_COLOR_BUFFER_BIT);

            updateListener.update(this, frameInfo);

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

    //Native callbacks

    @Override
    public void message(int source, int type, int id, int severity, ByteBuffer message, long userParam) {
        if(debugMessageListener != null) {
            String msg = BufferUtils.byteBufferToString(message, false);
            debugMessageListener.onMessage(source, type, id, severity, msg, userParam);
        }
    }

    @SuppressWarnings("unused") //called natively only
    public static void window_size_callback(long p_window, int width, int height) {
        long id = glfwGetWindowUserPointer(p_window);
        GLFWWindow window = windows.get(id);

        for(WindowSizeListener listener : window.windowSizeListeners)
            listener.onWindowSizeChanged(window, width, height);

    }

    @SuppressWarnings("unused") //called natively only
    public static void framebuffer_size_callback(long p_window, int width, int height) {
        long id = glfwGetWindowUserPointer(p_window);
        GLFWWindow window = windows.get(id);

        for(FramebufferSizeListener listener : window.framebufferSizeListeners)
            listener.onFramebufferSizeChanged(window, width, height);

    }
}
