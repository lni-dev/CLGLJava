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

package de.linusdev.clgl.window;

import de.linusdev.clgl.nat.cl.objects.Kernel;
import de.linusdev.clgl.nat.glfw3.custom.UpdateListener;
import de.linusdev.clgl.window.args.KernelView;
import org.jetbrains.annotations.NotNull;

public interface Handler extends UpdateListener<CLGLWindow> {

    /**
     * Called if the render kernel args must be set (again). <br>
     * Argument 0, 1 and 2 are already set.
     * @param renderKernel the render kernel
     * @see CLGLWindow#setRenderKernel(Kernel) 
     */
    void setRenderKernelArgs(@NotNull KernelView renderKernel);

    /**
     * Called if the ui kernel args must be set (again). <br>
     * Argument 0 and 1 are already set.
     * @param uiKernel the ui kernel
     * @see CLGLWindow#setUiKernel(Kernel)
     */
    void setUIKernelArgs(@NotNull KernelView uiKernel);
    
}
