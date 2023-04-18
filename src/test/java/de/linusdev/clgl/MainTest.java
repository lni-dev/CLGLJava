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

package de.linusdev.clgl;

import de.linusdev.clgl.nat.Load;
import de.linusdev.clgl.nat.cl.CL;
import de.linusdev.clgl.nat.glfw3.GLFWWindow;
import org.junit.jupiter.api.Test;

public class MainTest {

    @Test
    void test() throws InterruptedException {
        Load._init();


        for(Long l : CL.getPlatformIDs()) {
            System.out.println(l);
            System.out.println(CL.getPlatformInfoString(l, CL.PlatformInfo.CL_PLATFORM_NAME));
            System.out.println(CL.getPlatformInfoString(l, CL.PlatformInfo.CL_PLATFORM_VERSION));
        }


        GLFWWindow._glfwInit();
        GLFWWindow._glfwSetErrorCallback((error, description) -> {
            System.out.println("GLFW ERROR: " + description);
        });

        long pointer = GLFWWindow._glfwCreateWindow(500, 500, "hey");
        GLFWWindow._glfwShowWindow(pointer);

        while (GLFWWindow._glfwWindowShouldClose(pointer) == 0) {
            GLFWWindow._glfwPollEvents();
        }

        Load._close();
    }
}
