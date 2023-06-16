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

package de.linusdev.clgl.nat;

import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

public class NativeUtils {

    public static native boolean isNull(long pointer);

    private static final long C_NULL_POINTER = _getNullPointer();
    public static native long _getNullPointer();
    public static long getNullPointer() {
        return C_NULL_POINTER;
    }

    public static final long SIZE_OF_CL_MEM = sf_cl_mem();
    private static native long sf_cl_mem();

    /**
     * If the string ends with '\0', it will be removed.
     * @param pointer pointer of the native data
     * @param capacity capacity of the buffer or 0 if the pointer points to a string ending with 0
     * @return direct {@link ByteBuffer} pointing to the native data
     */
    public static native @NotNull ByteBuffer getBufferFromPointer(long pointer, int capacity);
}
