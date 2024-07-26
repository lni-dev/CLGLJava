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

package de.linusdev.cvg4j.window.args.impl;

import de.linusdev.cvg4j.nat.cl.objects.Buffer;
import de.linusdev.cvg4j.nat.cl.objects.Kernel;
import de.linusdev.cvg4j.window.CLGLWindow;
import de.linusdev.cvg4j.window.args.ArgumentInfo;
import de.linusdev.cvg4j.window.args.AutoUpdateArgument;
import de.linusdev.llog.LLog;
import de.linusdev.llog.base.LogInstance;
import de.linusdev.lutils.nat.struct.mod.ModTrackingStructure;
import org.jetbrains.annotations.NotNull;

public class AutoUpdateBuffer implements AutoUpdateArgument {

    @SuppressWarnings("unused")
    private final static @NotNull LogInstance log = LLog.getLogInstance();

    private final @NotNull ModTrackingStructure structure;
    private final @NotNull Buffer buffer;

    private CLGLWindow window;

    public AutoUpdateBuffer(@NotNull ModTrackingStructure structure, @NotNull Buffer buffer) {
        this.structure = structure;
        this.buffer = buffer;
    }

    @Override
    public void check() {
        structure.handleModifications(
                modInfo -> buffer.enqueueWriteBuffer(
                        window.getClQueue(),
                        false,
                        modInfo.startOffset,
                        true,
                        modInfo.endOffset - modInfo.startOffset,
                        structure.getByteBuffer(),
                        null
                )
        );
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
