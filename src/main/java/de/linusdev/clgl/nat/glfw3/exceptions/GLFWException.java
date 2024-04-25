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

package de.linusdev.clgl.nat.glfw3.exceptions;

import de.linusdev.clgl.nat.glfw3.GLFW;

public class GLFWException extends Exception {

    public GLFWError error;

    public static GLFWException readFromGLFWGetError() {
        GLFWError error = GLFW.glfwGetError();
        if(error == null)
            throw new IllegalStateException("glfwGetError returned null.");
        return new GLFWException(error);
    }

    public GLFWException(GLFWError error) {
        this.error = error;
    }

    @Override
    public String getMessage() {
        return "GLFW Error " + error.code() + ": " + error.description();
    }
}
