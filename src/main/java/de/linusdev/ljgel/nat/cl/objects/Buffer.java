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

package de.linusdev.ljgel.nat.cl.objects;

import de.linusdev.ljgel.nat.cl.CL;
import de.linusdev.ljgel.nat.cl.custom.EventWaitList;
import de.linusdev.lutils.bitfield.LongBitfield;
import de.linusdev.lutils.nat.struct.abstracts.Structure;
import de.linusdev.lutils.nat.struct.utils.BufferUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;

import static de.linusdev.ljgel.nat.cl.CL.*;

@SuppressWarnings("unused")
public class Buffer extends MemoryObject {


    public Buffer(
            @NotNull Context context,
            @NotNull LongBitfield<CL.CLMemFlag> clMemFlags,
            @NotNull Structure hostPtr
    ) {
        super(true, null);
        allocate();
        set(clCreateBuffer(context.getPointer(), clMemFlags, hostPtr));
    }

    public Buffer(
            @NotNull Context context,
            @NotNull LongBitfield<CL.CLMemFlag> clMemFlags,
            long size
    ) {
        super(true, null);
        set(clCreateBuffer(context.getPointer(), clMemFlags, size));
    }

    @SuppressWarnings("UnusedReturnValue")
    @Contract("_, true, _, _, _, _, _, -> null; _, false, _, _, _, _, _, -> !null")
    public @Nullable Event enqueueWriteBuffer(
            @NotNull CommandQueue queue,
            boolean blocking,
            long offset,
            boolean offsetData,
            long size,
            @NotNull ByteBuffer data,
            @Nullable EventWaitList eventWaitList
            ) {

        Event event = blocking ? null : Event.newAllocated(null);

        clEnqueueWriteBuffer(
                queue.getPointer(),
                get(),
                blocking,
                offset,
                size,
                offsetData ? BufferUtils.getHeapAddress(data) + offset : BufferUtils.getHeapAddress(data),
                eventWaitList,
                event
        );

        return event;
    }

    @SuppressWarnings("UnusedReturnValue")
    @Contract("_, true, _, _, _, _, _, -> null; _, false, _, _, _, _, _, -> !null")
    public @Nullable Event enqueueReadBuffer(
            @NotNull CommandQueue queue,
            boolean blocking,
            long offset,
            boolean offsetData,
            long size,
            @NotNull ByteBuffer data,
            @Nullable EventWaitList eventWaitList
    ) {
        Event event = blocking ? null : Event.newAllocated(null);

        clEnqueueReadBuffer(
                queue.getPointer(),
                get(),
                blocking,
                offset,
                size,
                offsetData ? BufferUtils.getHeapAddress(data) + offset : BufferUtils.getHeapAddress(data),
                eventWaitList,
                event
        );

        return event;
    }
}
