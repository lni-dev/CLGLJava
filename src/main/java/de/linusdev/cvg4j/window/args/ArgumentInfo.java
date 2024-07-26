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

package de.linusdev.cvg4j.window.args;

import de.linusdev.cvg4j.nat.cl.objects.Kernel;
import de.linusdev.cvg4j.window.CLGLWindow;
import org.jetbrains.annotations.NotNull;

public class ArgumentInfo {

    private final int index;
    private final @NotNull Kernel kernel;
    private final @NotNull CLGLWindow window;

    protected ArgumentInfo(int index, @NotNull Kernel kernel, @NotNull CLGLWindow window) {
        this.index = index;
        this.kernel = kernel;
        this.window = window;
    }

    public int getIndex() {
        return index;
    }

    public @NotNull Kernel getKernel() {
        return kernel;
    }

    public @NotNull CLGLWindow getWindow() {
        return window;
    }
}
