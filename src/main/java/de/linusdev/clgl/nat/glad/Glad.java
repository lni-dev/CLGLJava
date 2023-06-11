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
import de.linusdev.clgl.nat.glad.custom.DebugMessageCallback;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

import static de.linusdev.clgl.nat.glad.GLConstants.*;

@SuppressWarnings("unused")
public class Glad {

    public static native int gladLoadGL();

    public static native void glClear(
            @MagicConstant(intValues = {GL_COLOR_BUFFER_BIT, GL_DEPTH_BUFFER_BIT, GL_STENCIL_BUFFER_BIT})
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
            @MagicConstant(intValues = {GL_FRAMEBUFFER,  GL_DRAW_FRAMEBUFFER, GL_READ_FRAMEBUFFER})
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

    public static native int glGenFramebuffer();

    public static native int glCreateFramebuffer();

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

    public static native void glDeleteFramebuffer(
            int framebuffer
    );

    public static native void glNamedFramebufferRenderbuffer(
            int framebuffer,
            @MagicConstant(intValues = {GL_COLOR_ATTACHMENT0, GL_COLOR_ATTACHMENT1, GL_COLOR_ATTACHMENT2, GL_COLOR_ATTACHMENT3, GL_COLOR_ATTACHMENT4, GL_COLOR_ATTACHMENT5, GL_COLOR_ATTACHMENT6, GL_COLOR_ATTACHMENT7, GL_COLOR_ATTACHMENT8, GL_COLOR_ATTACHMENT9, GL_COLOR_ATTACHMENT10,  GL_DEPTH_ATTACHMENT, GL_STENCIL_ATTACHMENT,  GL_DEPTH_STENCIL_ATTACHMENT})
            int attachment,
            @MagicConstant(intValues = GL_RENDERBUFFER)
            int renderbuffertarget,
            int renderbuffer
    );

    public static void glGenRenderbuffers(
            @NotNull PrimitiveTypeArray<Integer> renderbuffers
    ) {
        _glGenRenderbuffers(
                renderbuffers.size(),
                renderbuffers.getByteBuf()
        );
    }

    private static native void _glGenRenderbuffers(
            int n,
            @NotNull ByteBuffer p_renderbuffers
    );

    public static native int glGenRenderbuffer();

    public static native int glCreateRenderbuffer();

    public static void glDeleteRenderbuffers(
            @NotNull PrimitiveTypeArray<Integer> renderbuffers
    ) {
        _glDeleteRenderbuffers(
                renderbuffers.size(),
                renderbuffers.getByteBuf()
        );
    }

    private static native void _glDeleteRenderbuffers(
            int n,
            @NotNull ByteBuffer p_renderbuffers
    );

    public static native void glDeleteRenderbuffer(
            int renderbuffer
    );

    /**
     *
     * @param internalformat the internal format to be used for the renderbuffer object's storage and must be a color-renderable, depth-renderable, or stencil-renderable format
     */
    public static native void glNamedRenderbufferStorage(
            int renderbuffer,
            @MagicConstant(intValues = {GL_RGBA32F}, valuesFromClass = GLConstants.class)
            int internalformat,
            int width,
            int height
    );

    public static native void glBlitNamedFramebuffer(
            int readFramebuffer,
            int drawFramebuffer,
            int srcX0,
            int srcY0,
            int srcX1,
            int srcY1,
            int dstX0,
            int dstY0,
            int dstX1,
            int dstY1,
            @MagicConstant(flags = {GL_COLOR_BUFFER_BIT, GL_DEPTH_BUFFER_BIT, GL_STENCIL_BUFFER_BIT})
            int mask,
            @MagicConstant(intValues = {GL_NEAREST, GL_LINEAR})
            int filter
    );

    public static native void glNamedFramebufferReadBuffer(
            int framebuffer,
            @MagicConstant(intValues = {
                    GL_FRONT, GL_LEFT,  GL_FRONT_LEFT, GL_FRONT_RIGHT, GL_RIGHT,GL_BACK,
                    GL_BACK_LEFT, GL_COLOR_ATTACHMENT0, GL_COLOR_ATTACHMENT1, GL_COLOR_ATTACHMENT2,
                    GL_COLOR_ATTACHMENT3, GL_COLOR_ATTACHMENT4, GL_COLOR_ATTACHMENT5, GL_COLOR_ATTACHMENT6,
                    GL_COLOR_ATTACHMENT7, GL_COLOR_ATTACHMENT8, GL_COLOR_ATTACHMENT9, GL_COLOR_ATTACHMENT10
            })
            int mode
    );

    public static native void glNamedFramebufferDrawBuffer(
            int framebuffer,
            @MagicConstant(intValues = {
                    GL_NONE, GL_FRONT, GL_LEFT,  GL_FRONT_LEFT, GL_FRONT_RIGHT, GL_RIGHT,GL_BACK,
                    GL_BACK_LEFT, GL_FRONT_AND_BACK, GL_COLOR_ATTACHMENT0, GL_COLOR_ATTACHMENT1, GL_COLOR_ATTACHMENT2,
                    GL_COLOR_ATTACHMENT3, GL_COLOR_ATTACHMENT4, GL_COLOR_ATTACHMENT5, GL_COLOR_ATTACHMENT6,
                    GL_COLOR_ATTACHMENT7, GL_COLOR_ATTACHMENT8, GL_COLOR_ATTACHMENT9, GL_COLOR_ATTACHMENT10
            })
            int buf
    );

    public static native String glGetString(
            @MagicConstant(flags = {
                    GL_VENDOR, GL_RENDERER, GL_VERSION, GL_SHADING_LANGUAGE_VERSION
            })
            int name
    );

    /**
     * @see <a href="https://registry.khronos.org/OpenGL-Refpages/gl4/html/glEnable.xhtml" target="_TOP">reference page</a>
     * @param cap the capability to enable.
     */
    public static native void glEnable(
            @MagicConstant(flags = {
                    GL_DEBUG_OUTPUT, GL_BLEND, GL_COLOR_LOGIC_OP, GL_CULL_FACE, GL_DEPTH_CLAMP,
                    GL_DEPTH_TEST, GL_DITHER, GL_FRAMEBUFFER_SRGB, GL_LINE_SMOOTH,
                    GL_DEBUG_OUTPUT_SYNCHRONOUS, GL_MULTISAMPLE, GL_POLYGON_OFFSET_FILL,
                    GL_POLYGON_OFFSET_LINE, GL_POLYGON_OFFSET_POINT, GL_POLYGON_SMOOTH,
                    GL_PRIMITIVE_RESTART, GL_PRIMITIVE_RESTART_FIXED_INDEX, GL_RASTERIZER_DISCARD,
                    GL_SAMPLE_ALPHA_TO_COVERAGE, GL_SAMPLE_ALPHA_TO_ONE, GL_SAMPLE_COVERAGE,
                    GL_SAMPLE_SHADING, GL_SAMPLE_MASK, GL_SCISSOR_TEST, GL_STENCIL_TEST,
                    GL_TEXTURE_CUBE_MAP_SEAMLESS, GL_PROGRAM_POINT_SIZE
            })
            int cap
    );

    /**
     * @see <a href="https://registry.khronos.org/OpenGL-Refpages/gl4/html/glEnable.xhtml" target="_TOP">reference page</a>
     * @param cap the capability to enable.
     */
    public static native void glDisable(
            @MagicConstant(flags = {
                    GL_DEBUG_OUTPUT, GL_BLEND, GL_COLOR_LOGIC_OP, GL_CULL_FACE, GL_DEPTH_CLAMP,
                    GL_DEPTH_TEST, GL_DITHER, GL_FRAMEBUFFER_SRGB, GL_LINE_SMOOTH,
                    GL_DEBUG_OUTPUT_SYNCHRONOUS, GL_MULTISAMPLE, GL_POLYGON_OFFSET_FILL,
                    GL_POLYGON_OFFSET_LINE, GL_POLYGON_OFFSET_POINT, GL_POLYGON_SMOOTH,
                    GL_PRIMITIVE_RESTART, GL_PRIMITIVE_RESTART_FIXED_INDEX, GL_RASTERIZER_DISCARD,
                    GL_SAMPLE_ALPHA_TO_COVERAGE, GL_SAMPLE_ALPHA_TO_ONE, GL_SAMPLE_COVERAGE,
                    GL_SAMPLE_SHADING, GL_SAMPLE_MASK, GL_SCISSOR_TEST, GL_STENCIL_TEST,
                    GL_TEXTURE_CUBE_MAP_SEAMLESS, GL_PROGRAM_POINT_SIZE
            })
            int cap
    );

    public static native void glDebugMessageCallback(
            DebugMessageCallback callback,
            long userParam
    );

}
