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
import org.jetbrains.annotations.NotNull;

import static de.linusdev.clgl.nat.cl.CL.*;

@SuppressWarnings("unused")
public class CommandQueue implements AutoCloseable {

    protected long pointer;
    protected boolean closed = false;

    public CommandQueue(@NotNull Context context, @NotNull Device device) {
        pointer = clCreateCommandQueueWithProperties(context.getPointer(), device.getPointer(), null);
    }

    public void flush() {
        clFlush(pointer);
    }

    public void finish() {
        clFinish(pointer);
    }

    @Override
    public void close() {
        try {
            clReleaseCommandQueue(pointer);
        } finally {
            closed = true;
            pointer = NativeUtils.getNullPointer();
        }

    }
}
