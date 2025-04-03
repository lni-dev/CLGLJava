/*
 * Copyright (c) 2023-2025 Linus Andera
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

package de.linusdev.ljgel;

import de.linusdev.ljgel.engine.NativeInteropEngine;
import de.linusdev.ljgel.engine.cl.window.CLGLWindow;
import de.linusdev.ljgel.engine.cl.window.Handler;
import de.linusdev.ljgel.engine.cl.window.args.KernelView;
import de.linusdev.ljgel.engine.gl.OpenGLWindow;
import de.linusdev.ljgel.engine.window.input.InputManger;
import de.linusdev.ljgel.engine.window.input.Key;
import de.linusdev.ljgel.nat.cl.objects.Kernel;
import de.linusdev.ljgel.nat.cl.objects.Program;
import de.linusdev.ljgel.nat.glad.GladInitException;
import de.linusdev.ljgel.nat.glfw3.GLFWValues;
import de.linusdev.ljgel.nat.glfw3.custom.FrameInfo;
import de.linusdev.ljgel.nat.glfw3.exceptions.GLFWException;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static de.linusdev.ljgel.nat.glad.GLConstants.GL_COLOR_BUFFER_BIT;
import static de.linusdev.ljgel.nat.glad.Glad.glClear;

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
        var t = new Thread(() -> {
            try {
                CLGLWindow window = new CLGLWindow(new Handler() {
                    @Override
                    public void setRenderKernelArgs(@NotNull KernelView renderKernel) {

                    }

                    @Override
                    public void setUIKernelArgs(@NotNull KernelView uiKernel) {

                    }

                    @Override
                    public void update(@NotNull FrameInfo frameInfo) {

                    }
                }, 10);

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

                Key key = m.getUSKey(GLFWValues.Keys_US.GLFW_KEY_W);


                m.getUSKey(GLFWValues.Keys_US.GLFW_KEY_E).addReleaseListener(() -> {
                    System.out.println("E released :)");
                });

                window.show();

                window.close();
            } catch (Throwable tt) {
                tt.printStackTrace();
            }

        });

        t.setDaemon(false);
        t.start();

        Thread.sleep(5000);

    }

    @Test
    void test() throws InterruptedException, GLFWException, GladInitException {
        NativeInteropEngine.StaticSetup.setup();
        OpenGLWindow window = new OpenGLWindow(null);

        window.setSize(800, 500);
        window.setTitle("Nice");
        window.eventLoop((frameInfo) -> {
            glClear(GL_COLOR_BUFFER_BIT);
        });
        window.close();
    }
}
