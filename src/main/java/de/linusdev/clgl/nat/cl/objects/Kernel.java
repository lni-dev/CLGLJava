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

import de.linusdev.clgl.api.structs.Structure;
import org.jetbrains.annotations.NotNull;

import static de.linusdev.clgl.nat.cl.CL.*;

public class Kernel implements AutoCloseable {

    protected final long pointer;

    public Kernel(@NotNull Program program, @NotNull String kernelName) {
        this.pointer = clCreateKernel(program.getPointer(), kernelName);
    }

    public void setKernelArg(int index, @NotNull Structure structure) {
        clSetKernelArg(pointer, index, structure);
    }

    public void setKernelArg(int index, @NotNull MemoryObject memoryObject) {
        clSetKernelArg(pointer, index, memoryObject);
    }

    public long getPointer() {
        return pointer;
    }

    @Override
    public void close() {
        clReleaseKernel(pointer);
    }
}
