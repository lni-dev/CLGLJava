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

import de.linusdev.clgl.api.types.bytebuffer.BBInt2;
import de.linusdev.clgl.api.utils.BufferUtils;
import de.linusdev.clgl.nat.Load;
import de.linusdev.clgl.nat.custom.StaticCallbackObject;
import de.linusdev.clgl.nat.custom.StaticCallbackObjects;
import de.linusdev.clgl.nat.glad.custom.DebugMessageCallback;
import de.linusdev.clgl.nat.glad.custom.DebugMessageListener;
import de.linusdev.clgl.nat.glfw3.custom.FrameInfoImpl;
import de.linusdev.clgl.nat.glfw3.custom.UpdateListener;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;

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

    public GLFWWindow() {
        Load.init();
        glfwInit();
        this.pointer = glfwCreateWindow(500, 500, "Window");
        this.id = windows.add(this);
        glfwSetWindowUserPointer(pointer, id);

        makeGLContextCurrent();
        gladLoadGL();
    }

    public void makeGLContextCurrent() {
        glfwMakeContextCurrent(pointer);
    }

    public void enableDebugMessageListener(@NotNull DebugMessageListener listener) {
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

    @Override
    public void message(int source, int type, int id, int severity, ByteBuffer message, long userParam) {
        if(debugMessageListener != null) {
            String msg = BufferUtils.byteBufferToString(message, false);
            debugMessageListener.onMessage(source, type, id, severity, msg, userParam);
        }
    }
}
