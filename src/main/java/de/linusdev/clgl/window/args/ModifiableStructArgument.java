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
import org.jetbrains.annotations.NotNull;

public class ModifiableStructArgument implements AutoUpdateArgument{

    private final @NotNull Structure structure;
    private ArgumentInfo argumentInfo;

    public ModifiableStructArgument(@NotNull Structure structure) {
        this.structure = structure;
    }

    @Override
    public void check() {
        if(structure.isModified())
            argumentInfo.getKernel().setKernelArg(argumentInfo.getIndex(), structure);
    }

    @Override
    public void setArgumentInfo(@NotNull ArgumentInfo info) {
        this.argumentInfo = info;
    }

    @Override
    public void applyToKernel(@NotNull Kernel kernel, int index) {
        kernel.setKernelArg(index, structure);
    }
}
