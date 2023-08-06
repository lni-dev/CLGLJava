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

import de.linusdev.clgl.api.types.bytebuffer.BBInt2;
import de.linusdev.clgl.nat.glfw3.custom.ErrorCallback;
import de.linusdev.clgl.nat.glfw3.objects.GLFWWindow;
import de.linusdev.llog.LLog;
import de.linusdev.lutils.llist.LLinkedList;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;

@SuppressWarnings("unused")
public class GLFW {

    protected static boolean init = false;

    protected static final @NotNull LLinkedList<ErrorCallback> errorCallbacks = new LLinkedList<>();
    protected static final @NotNull ErrorCallback staticErrorCallback = (error, description) -> {
        LLog.getLogInstance().logError(description);
        for(ErrorCallback callback : errorCallbacks)
            callback.onError(error, description);
    };

    public synchronized static void glfwInit() {
        if(!init) {
            _glfwInit();

            Runtime.getRuntime().addShutdownHook(new Thread(GLFW::_glfwTerminate));

            setJavaGLFWWindowClass(GLFWWindow.class);

            //Callbacks
            _glfwSetErrorCallback(staticErrorCallback);
            glfwSetJoystickCallback();

            init = true;
        }
    }

    protected static native int _glfwInit();

    public static native void glfwSwapInterval(int interval);

    public static void glfwWindowHint(int hint, int value) {
        _glfwWindowHint(hint, value);
    }
    protected static native void _glfwWindowHint(int hint, int value);



    protected static native void _glfwSetErrorCallback(@NotNull ErrorCallback callback);



    public static long glfwCreateWindow(int width, int height, String title) {
        return _glfwCreateWindow(width, height, title);
    }
    protected static native long _glfwCreateWindow(int width, int height, String title);



    protected static native void _glfwTerminate();



    public static void glfwPollEvents() {
        _glfwPollEvents();
    }

    protected static native void _glfwPollEvents();



    public static void glfwMakeContextCurrent(long pointer) {
        _glfwMakeContextCurrent(pointer);
    }

    protected static native void _glfwMakeContextCurrent(long pointer);



    public static void glfwDestroyWindow(long pointer) {
        _glfwShowWindow(pointer);
    }

    protected static native void _glfwDestroyWindow(long pointer);



    public static void glfwShowWindow(long pointer) {
        _glfwShowWindow(pointer);
    }

    protected static native void _glfwShowWindow(long pointer);



    public static void glfwSwapBuffers(long pointer) {
        _glfwSwapBuffers(pointer);
    }

    protected static native void _glfwSwapBuffers(long pointer);



    public static void glfwSetWindowUserPointer(long pointer, long userPointer) {
        _glfwSetWindowUserPointer(pointer, userPointer);
    }

    protected static native void _glfwSetWindowUserPointer(long pointer, long userPointer);



    public static void glfwSetInputMode(long pointer, int mode, int value) {
        _glfwSetInputMode(pointer, mode, value);
    }

    protected static native void _glfwSetInputMode(long pointer, int mode, int value);



    public static void glfwSetWindowSize(long pointer, int width, int height) {
        _glfwSetWindowSize(pointer, width, height);
    }

    protected static native void _glfwSetWindowSize(long pointer, int width, int height);



    public static void glfwSetWindowTitle(long pointer, String title) {
        _glfwSetWindowTitle(pointer, title);
    }

    protected static native void _glfwSetWindowTitle(long pointer, String title);



    public static void glfwSetWindowAttrib(long pointer, int attribute, int value) {
        _glfwSetWindowAttrib(pointer, attribute, value);
    }

    protected static native void _glfwSetWindowAttrib(long pointer, int attribute, int value);



    public static long glfwGetWindowUserPointer(long pointer) {
        return _glfwGetWindowUserPointer(pointer);
    }

    protected static native long _glfwGetWindowUserPointer(long pointer);



    public static boolean glfwWindowShouldClose(long pointer) {
        return _glfwWindowShouldClose(pointer) != 0;
    }

    protected static native int _glfwWindowShouldClose(long pointer);



    public static void glfwGetFramebufferSize(long pointer, @NotNull BBInt2 widthHeight) {
        _glfwGetFramebufferSize(pointer, widthHeight.getByteBuf());
    }
    protected static native void _glfwGetFramebufferSize(
            long pointer,
            @NotNull ByteBuffer p_width_height
    );

    public static native void setJavaGLFWWindowClass(
            Class<GLFWWindow> clazz
    );

    public static native void glfwSetWindowSizeCallback(
            long pointer
    );

    public static native void glfwSetFramebufferSizeCallback(
            long pointer
    );

    public static native void glfwSetKeyCallback(
            long pointer
    );

    public static native void glfwSetCharCallback(
            long pointer
    );

    public static native void glfwSetCursorPosCallback(
            long pointer
    );

    public static native void glfwSetCursorEnterCallback(
            long pointer
    );

    public static native void glfwSetMouseButtonCallback(
            long pointer
    );

    public static native void glfwSetScrollCallback(
            long pointer
    );

    public static native void glfwSetJoystickCallback();

    public static native void glfwSetDropCallback(
            long pointer
    );

    /**
     * @see <a href="https://www.glfw.org/docs/3.3/group__input.html#gaeaed62e69c3bd62b7ff8f7b19913ce4f">glfw doc</a>
     */
    public static native @Nullable String _glfwGetKeyName(
            @MagicConstant(valuesFromClass = GLFWValues.Keys_US.class) int key,
            int scancode
    );

    public static native int glfwGetKeyScancode(
            @MagicConstant(valuesFromClass = GLFWValues.Keys_US.class) int key
    );

}
