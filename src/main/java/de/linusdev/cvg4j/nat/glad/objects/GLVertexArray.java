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

import static de.linusdev.cvg4j.nat.glad.Glad.*;

public class GLVertexArray extends GLNamedObject<GLVertexArray> {

    public GLVertexArray() {
        this.name = glGenVertexArray();
    }

    @Override
    public void reCreate() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() throws Exception {
        glDeleteVertexArray(name);
        closed = true;
    }
}
