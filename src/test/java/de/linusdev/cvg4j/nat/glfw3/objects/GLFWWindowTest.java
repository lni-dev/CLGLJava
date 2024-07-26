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

import de.linusdev.cvg4j.nat.glad.Glad;
import de.linusdev.cvg4j.nat.glad.GladInitException;
import de.linusdev.cvg4j.nat.glfw3.GLFWValues;
import de.linusdev.cvg4j.nat.glfw3.custom.GLFWWindowHints;
import de.linusdev.cvg4j.nat.glfw3.custom.RenderAPI;
import de.linusdev.cvg4j.nat.glfw3.exceptions.GLFWException;
import org.junit.jupiter.api.Test;

import static de.linusdev.cvg4j.nat.glad.GLConstants.GL_COLOR_BUFFER_BIT;
import static de.linusdev.cvg4j.nat.glad.Glad.glClear;

class GLFWWindowTest {

    @Test
    void show() throws GladInitException, GLFWException {
        GLFWWindowHints hints = new GLFWWindowHints();

        hints.setContextVersion(4, 6);

        hints.samples = 4;
        hints.openglProfile = GLFWValues.OpenGlProfiles.GLFW_OPENGL_CORE_PROFILE;

        GLFWWindow window = new GLFWWindow(RenderAPI.OPENGL, hints);
        window.enableGLDebugMessageListener((source, type, id, severity, message, userParam) ->
                System.out.println("OpenGl Debug Message: " + message));

        window.setSize(800, 500);
        window.setTitle("Nice");

        int vertexArrayId = Glad.glGenVertexArray();
        Glad.glBindVertexArray(vertexArrayId);

        window.show((window1, frameInfo) -> {
            glClear(GL_COLOR_BUFFER_BIT);
        });
        window.close();
    }
}