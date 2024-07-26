/*
 * Copyright (c) 2024 Linus Andera
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

package de.linusdev.cvg4j.nat.glad.objects;

import de.linusdev.cvg4j.nat.glad.custom.GLNamedObject;
import de.linusdev.lutils.nat.NativeParsable;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.NotNull;

import static de.linusdev.cvg4j.nat.glad.GLConstants.*;
import static de.linusdev.cvg4j.nat.glad.Glad.*;

public class GLBuffer extends GLNamedObject<GLBuffer> {

    public GLBuffer() {
        this.name = glGenBuffer();
    }

    @Override
    public void reCreate() {
        throw new UnsupportedOperationException();
    }

    public void glBufferData(
            @NotNull NativeParsable data,
            @MagicConstant(intValues = {
                    GL_STREAM_DRAW, GL_STREAM_READ, GL_STREAM_COPY, GL_STATIC_DRAW,
                    GL_STATIC_READ, GL_STATIC_COPY, GL_DYNAMIC_DRAW, GL_DYNAMIC_READ,
                    GL_DYNAMIC_COPY
            })
            int usage
    ) {
        glNamedBufferData(this, data, usage);
    }

    @Override
    public void close() throws Exception {
        glDeleteBuffer(name);
        closed = true;
    }
}
