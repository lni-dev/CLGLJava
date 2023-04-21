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

package de.linusdev.clgl.nat.glad;

import de.linusdev.clgl.api.structs.PrimitiveTypeArray;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

@SuppressWarnings("unused")
public class Glad {

    public static native int gladLoadGL();

    public static native void glClear(
            int mask
    );

    public static native void glClearColor(
            float r,
            float g,
            float b,
            float a
    );

    public static native void glFinish();

    public static native void glBindFramebuffer(
            int target,
            int framebuffer
    );

    public static void glGenFramebuffers(
            @NotNull PrimitiveTypeArray<Integer> ids
    ) {
        _glGenFramebuffers(
                ids.size(),
                ids.getByteBuf()
        );
    }

    private static native void _glGenFramebuffers(
            int n,
            @NotNull ByteBuffer p_ids
    );

    public static void glDeleteFramebuffers(
            @NotNull PrimitiveTypeArray<Integer> framebuffers
    ) {
        _glDeleteFramebuffers(
                framebuffers.size(),
                framebuffers.getByteBuf()
        );
    }

    private static native void _glDeleteFramebuffers(
            int n,
            @NotNull ByteBuffer p_framebuffers
    );

    private static native void _glNamedFramebufferRenderbuffer(
            int framebuffer,
            int attachment,
            int renderbuffertarget,
            int renderbuffer
    );

}
