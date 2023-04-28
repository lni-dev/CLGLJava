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

package de.linusdev.clgl.nat.cl.objects;

import de.linusdev.clgl.nat.NativeUtils;
import de.linusdev.clgl.nat.cl.CL;
import de.linusdev.clgl.nat.glad.objects.GLRenderBuffer;
import de.linusdev.lutils.bitfield.LongBitfield;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public class MemoryObject implements AutoCloseable {

    protected long pointer;
    protected boolean closed = false;

    protected MemoryObject(long pointer) {
        this.pointer = pointer;
    }

    public static @NotNull MemoryObject fromGLRenderBuffer(
            @NotNull Context context,
            @NotNull LongBitfield<CL.CLMemFlag> memFlags,
            @NotNull GLRenderBuffer renderBuffer
    ) {
        return new MemoryObject(CL.clCreateFromGLRenderbuffer(
                context.getPointer(),
                memFlags,
                renderBuffer.getName()
        ));
    }

    @Override
    public void close() {
        try {
            CL.clReleaseMemObject(pointer);
        } finally {
            closed = true;
            pointer = NativeUtils.getNullPointer();
        }
    }

    public long getPointer() {
        return pointer;
    }

    public boolean isClosed() {
        return closed;
    }
}
