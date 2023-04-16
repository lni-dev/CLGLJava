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

package de.linusdev.clgl.nat.glfw3;

import org.jetbrains.annotations.NotNull;

public class GLFWWindow {

    public static native int _glfwInit();
    public static native void _glfwWindowHint(int hint, int value);
    public static native void _glfwSetErrorCallback(@NotNull ErrorCallback callback);
    public static native long _glfwCreateWindow(int width, int height, String title);
    public static native void _glfwTerminate();
    public static native void _glfwPollEvents();

    public static native void _glfwMakeContextCurrent(long pointer);
    public static native void _glfwDestroyWindow(long pointer);
    public static native void _glfwShowWindow(long pointer);
    public static native void _glfwSwapBuffers(long pointer);
    public static native void _glfwSetWindowUserPointer(long pointer, long userPointer);
    public static native void _glfwSetInputMode(long pointer, int mode, int value);
    public static native void _glfwSetWindowSize(long pointer, int width, int height);
    public static native void _glfwSetWindowTitle(long pointer, String title);
    public static native void _glfwSetWindowAttrib(long pointer, int attribute, int value);
    public static native long _glfwGetWindowUserPointer(long pointer);
    public static native int _glfwWindowShouldClose(long pointer);
    //TODO: glfwGetFramebufferSize

}
