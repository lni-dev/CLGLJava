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

package de.linusdev.cvg4j.nat.glfw3;

import de.linusdev.cvg4j.nat.NativeUtils;
import de.linusdev.cvg4j.nat.glfw3.custom.ErrorCallback;
import de.linusdev.cvg4j.nat.glfw3.custom.GLFWWindowHints;
import de.linusdev.cvg4j.nat.glfw3.custom.NativeErrorCallback;
import de.linusdev.cvg4j.nat.glfw3.custom.window.GLFWNativeCallbacks;
import de.linusdev.cvg4j.nat.glfw3.exceptions.GLFWError;
import de.linusdev.cvg4j.nat.glfw3.exceptions.GLFWException;
import de.linusdev.llog.LLog;
import de.linusdev.lutils.llist.LLinkedList;
import de.linusdev.lutils.math.vector.buffer.intn.BBInt1;
import de.linusdev.lutils.math.vector.buffer.intn.BBInt2;
import de.linusdev.lutils.math.vector.buffer.longn.BBLong1;
import de.linusdev.lutils.nat.pointer.BBTypedPointer64;
import de.linusdev.lutils.nat.string.NullTerminatedUTF8String;
import de.linusdev.lutils.nat.struct.array.StructureArray;
import de.linusdev.lutils.nat.struct.utils.BufferUtils;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;

@SuppressWarnings("unused")
public class GLFW {

    protected static boolean init = false;

    protected static final @NotNull LLinkedList<ErrorCallback> errorCallbacks = new LLinkedList<>();
    protected static final @NotNull NativeErrorCallback staticErrorCallback = (code, description) -> {
        GLFWError error = new GLFWError(code, description);

        LLog.getLogInstance().logError(
                error.description() == null ?
                        "Error " + error.code() + ": " + "No description provided." :
                        "Error " + error.code() + ": " + error.description()
        );

        for(ErrorCallback callback : errorCallbacks)
            callback.onError(error);
    };

    public synchronized static void glfwInit() throws GLFWException {
        if(!init) {
            if(_glfwInit() == GLFWValues.GLFW_FALSE)
                throw GLFWException.readFromGLFWGetError();

            Runtime.getRuntime().addShutdownHook(new Thread(GLFW::_glfwTerminate));

            setJavaGLFWWindowClass(GLFWNativeCallbacks.class);

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



    protected static native void _glfwSetErrorCallback(@NotNull NativeErrorCallback callback);


    /**
     * @param resetHints {@code true} to reset the window hints before creating this window
     * @param hints custom window hints to set or {@code null}.
     * @param width width of the window
     * @param height height of the window
     * @param title title of the window
     * @return window pointer
     * @throws GLFWException if the window cannot be created
     */
    public static long glfwCreateWindow(
            boolean resetHints, @Nullable GLFWWindowHints hints,
            int width, int height, String title
    ) throws GLFWException {

        if(resetHints)
            glfwDefaultWindowHints();

        if(hints != null)
            hints.adjustWindowHints();

        long pointer = _glfwCreateWindow(width, height, title);

        if(NativeUtils.isNull(pointer))
            throw GLFWException.readFromGLFWGetError();

        return pointer;
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
        _glfwGetFramebufferSize(pointer, widthHeight.getByteBuffer());
    }
    protected static native void _glfwGetFramebufferSize(
            long pointer,
            @NotNull ByteBuffer p_width_height
    );

    /**
     *
     * @param clazz usually class of {@link GLFWNativeCallbacks}
     */
    public static native void setJavaGLFWWindowClass(
            Class<?> clazz
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

    public static native void glfwSetWindowRefreshCallback(
            long pointer
    );

    public static native void glfwSetWindowIconifyCallback(
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

    protected static native int _glfwGetError(long pointer);

    public static @Nullable GLFWError glfwGetError() {
        BBLong1 pointer = BBLong1.newAllocated(null);

        int code = _glfwGetError(pointer.getPointer());

        if(code == GLFWValues.ErrorCodes.GLFW_NO_ERROR)
            return null;

        String description = NativeUtils.isNull(pointer.get()) ? null :
                BufferUtils.readString(NativeUtils.getBufferFromPointer(pointer.get(), 0), false);

        return new GLFWError(code, description);
    }

    public static native void glfwDefaultWindowHints();

    public static native int glfwVulkanSupported();

    public static native long glfwGetInstanceProcAddress(long p_instance, String procname);

    protected static native long glfwGetRequiredInstanceExtensions(long pCount);

    @NotNull
    public static StructureArray<BBTypedPointer64<NullTerminatedUTF8String>> glfwGetRequiredInstanceExtensions(
            @NotNull BBInt1 count
    ) {
        long pointer = glfwGetRequiredInstanceExtensions(count.getPointer());

        return StructureArray.ofPointer(false, BBTypedPointer64.class, count.get(), pointer, BBTypedPointer64::newUnallocated1);
    }

    public static native int glfwCreateWindowSurface(long vkInstance, long pGlfwWindow, long pAllocator, long pVkSurfaceKHR);

    public static native void glfwWaitEvents();

    public static native void glfwSetWindowSizeLimits(long pointer, int minWidth, int minHeight, int maxWidth, int maxHeight);

    public static native void glfwSetWindowAspectRatio(long pointer, int numerator, int denominator);

}
