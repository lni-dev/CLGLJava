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

package de.linusdev.clgl.window.args.impl;

import de.linusdev.clgl.nat.cl.objects.Buffer;
import de.linusdev.clgl.nat.cl.objects.Kernel;
import de.linusdev.clgl.window.CLGLWindow;
import de.linusdev.clgl.window.args.ArgumentInfo;
import de.linusdev.clgl.window.args.AutoUpdateArgument;
import de.linusdev.lutils.struct.mod.ModTrackingStructure;
import de.linusdev.lutils.struct.mod.ModificationInfo;
import org.jetbrains.annotations.NotNull;

public class AutoUpdateBuffer implements AutoUpdateArgument {

    private final @NotNull ModTrackingStructure structure;
    private final @NotNull Buffer buffer;

    private CLGLWindow window;

    public AutoUpdateBuffer(@NotNull ModTrackingStructure structure, @NotNull Buffer buffer) {
        this.structure = structure;
        this.buffer = buffer;
    }

    @Override
    public void check() {
        if (structure.isModified()) {
            //set structure to unmodified first. During copying there may be coming in new modifications,
            //that must be copied in the next check()...
            structure.unmodified();

            if (!structure.tracksModifications()) {
                //No info about the modifications given. Copy the complete buffer.
                buffer.enqueueWriteBuffer(
                        window.getClQueue(),
                        false,
                        0, false, structure.getRequiredSize(),
                        structure.getByteBuffer(),
                        null
                );
                return;
            }

            //Acquire lock for this structure's modifications info.
            structure.acquireModificationLock();
            try {
                ModificationInfo first = structure.getFirstModificationInfo(true);

                while (first != null) {
                    buffer.enqueueWriteBuffer(
                            window.getClQueue(),
                            false,
                            first.startOffset, true,
                            first.endOffset - first.startOffset,
                            structure.getByteBuffer(),
                            null
                    );
                    first = first.next;
                }
            } finally {
                structure.releaseModificationLock();
            }

        }
    }

    @Override
    public void setArgumentInfo(@NotNull ArgumentInfo info) {
        this.window = info.getWindow();
    }

    @Override
    public void applyToKernel(@NotNull Kernel kernel, int index) {
        kernel.setKernelArg(index, buffer);
    }
}
