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

package de.linusdev.clgl.nat.glfw3.objects;

import de.linusdev.clgl.nat.glad.Glad;
import de.linusdev.clgl.nat.glad.GladInitException;
import de.linusdev.clgl.nat.glfw3.GLFWValues;
import de.linusdev.clgl.nat.glfw3.custom.GLFWWindowHints;
import de.linusdev.clgl.nat.glfw3.exceptions.GLFWException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GLFWWindowTest {

    @Test
    void show() throws GladInitException, GLFWException {
        GLFWWindowHints hints = new GLFWWindowHints();

        hints.setContextVersion(4, 6);

        hints.samples = 4;
        hints.openglProfile = GLFWValues.OpenGlProfiles.GLFW_OPENGL_CORE_PROFILE;

        GLFWWindow window = new GLFWWindow(hints);
        window.enableGLDebugMessageListener((source, type, id, severity, message, userParam) ->
                System.out.println("OpenGl Debug Message: " + message));

        window.setSize(800, 500);
        window.setTitle("Nice");

        int vertexArrayId = Glad.glGenVertexArray();
        Glad.glBindVertexArray(vertexArrayId);

        window.show((window1, frameInfo) -> {

        });
        window.close();
    }
}