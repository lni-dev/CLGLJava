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

package de.linusdev.cvg4j.nat.glfw3.objects;

import de.linusdev.cvg4j.nat.glad.GladInitException;
import de.linusdev.cvg4j.nat.glad.custom.DebugMessageCallback;
import de.linusdev.cvg4j.nat.glad.custom.DebugMessageListener;
import de.linusdev.cvg4j.nat.glfw3.custom.GLFWWindowHints;
import de.linusdev.cvg4j.nat.glfw3.custom.RenderAPI;
import de.linusdev.cvg4j.nat.glfw3.exceptions.GLFWException;
import de.linusdev.lutils.nat.struct.utils.BufferUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;

import static de.linusdev.cvg4j.nat.glad.GLConstants.GL_DEBUG_OUTPUT;
import static de.linusdev.cvg4j.nat.glad.GLConstants.GL_DEBUG_OUTPUT_SYNCHRONOUS;
import static de.linusdev.cvg4j.nat.glad.Glad.*;
import static de.linusdev.cvg4j.nat.glfw3.GLFW.*;

public class OpenGLWindow extends GLFWWindow implements DebugMessageCallback {
    public OpenGLWindow(@Nullable GLFWWindowHints hints) throws GLFWException, GladInitException {
        super(RenderAPI.OPENGL, hints);

        makeGLContextCurrent();
        glfwSwapInterval(0);
        gladLoadGL();
    }

    @Override
    protected void perFrameOperations() {
        // swap buffers
        glfwSwapBuffers(pointer);
        super.perFrameOperations();
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

    @Override
    public void message(int source, int type, int id, int severity, ByteBuffer message, long userParam) {
        if(debugMessageListener != null) {
            String msg = BufferUtils.readString(message, false);
            debugMessageListener.onMessage(source, type, id, severity, msg, userParam);
        }
    }
}
