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

package de.linusdev.clgl.nat.glad.objects;

import de.linusdev.clgl.nat.glad.Glad;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.NotNull;

import static de.linusdev.clgl.nat.glad.GLConstants.*;
import static de.linusdev.clgl.nat.glad.Glad.glGenFramebuffer;
import static de.linusdev.clgl.nat.glad.Glad.glNamedFramebufferRenderbuffer;

@SuppressWarnings("unused")
public class GLFrameBuffer extends GLNamedObject<GLFrameBuffer> {

    public GLFrameBuffer() {
        this.name = glGenFramebuffer();
    }

    @Override
    public void reCreate() {
        Glad.glDeleteFramebuffer(name);
        this.name = 0;
        this.name = glGenFramebuffer();
        callReCreationListener();
    }

    public void addRenderBuffer(
            @NotNull GLRenderBuffer buffer,
            @MagicConstant(intValues = {GL_COLOR_ATTACHMENT0, GL_COLOR_ATTACHMENT1, GL_COLOR_ATTACHMENT2, GL_COLOR_ATTACHMENT3, GL_COLOR_ATTACHMENT4, GL_COLOR_ATTACHMENT5, GL_COLOR_ATTACHMENT6, GL_COLOR_ATTACHMENT7, GL_COLOR_ATTACHMENT8, GL_COLOR_ATTACHMENT9, GL_COLOR_ATTACHMENT10,  GL_DEPTH_ATTACHMENT, GL_STENCIL_ATTACHMENT,  GL_DEPTH_STENCIL_ATTACHMENT})
            int attachment,
            @MagicConstant(intValues = GL_RENDERBUFFER)
            int target
    ) {
        //todo store info + add listener
        glNamedFramebufferRenderbuffer(
                name,
                attachment,
                target,
                buffer.getName()
        );
    }

    @Override
    public boolean isReCreatable() {
        return true;
    }

    @Override
    public void close() {
        Glad.glDeleteFramebuffer(name);
    }
}
