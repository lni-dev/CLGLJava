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

package de.linusdev.clgl.window.args;

import de.linusdev.clgl.api.structs.Structure;
import de.linusdev.clgl.nat.cl.objects.Kernel;
import de.linusdev.clgl.nat.cl.objects.MemoryObject;
import de.linusdev.clgl.window.CLGLWindow;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public class KernelView {

    private final @NotNull Kernel kernel;
    private final @NotNull CLGLWindow window;
    private final @NotNull AutoUpdateArgManager manager;

    public KernelView(
            @NotNull Kernel kernel,
            @NotNull CLGLWindow window,
            @NotNull AutoUpdateArgManager manager
    ) {
        this.kernel = kernel;
        this.window = window;
        this.manager = manager;
    }

    public void setKernelArg(int index, @NotNull Structure structure) {
        kernel.setKernelArg(index, structure);
    }

    public void setKernelArg(int index, @NotNull MemoryObject memoryObject) {
        kernel.setKernelArg(index, memoryObject);
    }

    public void setKernelArg(int index, @NotNull AutoUpdateArgument argument) {
        manager.addArgument(argument);
        argument.setArgumentInfo(new ArgumentInfo(index, kernel, window));
        argument.applyToKernel(kernel, index);

    }

}
