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

import de.linusdev.clgl.api.structs.NativeParsable;
import de.linusdev.clgl.api.types.bytebuffer.BBLong1;
import de.linusdev.clgl.nat.NativeUtils;
import de.linusdev.clgl.nat.cl.CL;
import de.linusdev.clgl.nat.cl.structs.CLImageDesc;
import de.linusdev.clgl.nat.cl.structs.CLImageFormat;
import de.linusdev.clgl.nat.glad.objects.GLRenderBuffer;
import de.linusdev.lutils.bitfield.LongBitfield;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public class MemoryObject extends BBLong1 implements AutoCloseable {


    protected boolean closed = false;

    public MemoryObject(boolean allocateBuffer) {
        super(allocateBuffer);
    }

    public void fromGLRenderBuffer(
            @NotNull Context context,
            @NotNull LongBitfield<CL.CLMemFlag> memFlags,
            @NotNull GLRenderBuffer renderBuffer
    ) {
        set(CL.clCreateFromGLRenderbuffer(
                context.getPointer(),
                memFlags,
                renderBuffer.getName()
        ));
    }

    public void newCLImage(
            @NotNull Context context,
            @NotNull LongBitfield<CL.CLMemFlag> memFlags,
            @NotNull CLImageFormat imageFormat,
            @NotNull CLImageDesc imageDesc,
            @Nullable NativeParsable imageData
    ) {
        set(CL.clCreateImage(
           context.getPointer(),
           memFlags,
           imageFormat,
           imageDesc,
           imageData == null ? NativeUtils.getNullPointer() : imageData.getPointer()
        ));
    }

    @Override
    public void close() {
        try {
            CL.clReleaseMemObject(getPointer());
        } finally {
            closed = true;
            set(NativeUtils.getNullPointer());
        }
    }

    public long getPointer() {
        return get();
    }

    public boolean isClosed() {
        return closed;
    }
}
