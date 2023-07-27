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

package de.linusdev.clgl.nat.glfw3.custom;

import de.linusdev.clgl.api.misc.annos.CallOnlyFromUIThread;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.NonBlocking;
import org.jetbrains.annotations.NotNull;

public interface UpdateListener<W> {

    @CallOnlyFromUIThread("glfw")
    @ApiStatus.Internal
    @MustBeInvokedByOverriders
    @NonBlocking
    default void update0(@NotNull W window, @NotNull FrameInfo frameInfo) {
        update(window, frameInfo);
    }

    @CallOnlyFromUIThread("glfw")
    @ApiStatus.OverrideOnly
    @NonBlocking
    void update(@NotNull W window, @NotNull FrameInfo frameInfo);

}
