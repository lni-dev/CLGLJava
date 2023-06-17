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

import de.linusdev.clgl.window.input.InputManger;
import de.linusdev.clgl.nat.cl.objects.Kernel;
import de.linusdev.clgl.nat.cl.objects.Program;
import de.linusdev.clgl.nat.glfw3.GLFWValues;
import de.linusdev.clgl.nat.glfw3.objects.GLFWWindow;
import de.linusdev.clgl.window.CLGLWindow;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

@SuppressWarnings("RedundantThrows")
public class MainTest {

    static @NotNull String readFromResourceFile(@NotNull String file) throws IOException {
        InputStream in =  MainTest.class.getClassLoader().getResourceAsStream(file);
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));

        StringBuilder text = new StringBuilder();
        String line;
        while((line = reader.readLine()) != null) {
            text.append(line).append("\n");
        }

        return text.toString();
    }

    @Test
    void testCLGLWindow() throws IOException, InterruptedException {
        CLGLWindow window = new CLGLWindow(10);

        {
            Program program = new Program(window.getClContext(), readFromResourceFile("test.cl"));
            var fut = program.build("-cl-std=CL2.0", true, window.getClDevice());
            fut.getResult();
            System.out.println("Build finished: " + program.getBuildLog(window.getClDevice()));
            Kernel kernel = new Kernel(program, "render");
            window.setRenderKernel(kernel);
        }

        {
            Program program = new Program(window.getClContext(), readFromResourceFile("ui.cl"));
            var fut = program.build("-cl-std=CL2.0", true, window.getClDevice());
            fut.getResult();
            System.out.println("Build finished (UI): " + program.getBuildLog(window.getClDevice()));
            Kernel kernel = new Kernel(program, "render");
            window.setUiKernel(kernel);
        }

        InputManger m = window.getInputManger();

        m.getUSKey(GLFWValues.Keys_US.GLFW_KEY_E).addReleaseListener(() -> {
            System.out.println("E released :)");
        });

        window.show();

        window.close();
    }

    @Test
    void test() throws InterruptedException {
        GLFWWindow window = new GLFWWindow();

        window.setSize(800, 500);
        window.setTitle("Nice");
        window.show((window1, frameInfo) -> {

        });
        window.close();
    }
}
