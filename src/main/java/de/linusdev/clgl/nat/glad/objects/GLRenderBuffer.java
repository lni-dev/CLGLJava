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

import de.linusdev.clgl.nat.glad.GLConstants;
import de.linusdev.clgl.nat.glad.Glad;
import org.intellij.lang.annotations.MagicConstant;

@SuppressWarnings("unused")
public class GLRenderBuffer extends GLNamedObject<GLRenderBuffer> {

    protected @MagicConstant(valuesFromClass = GLConstants.class) int colorFormat;
    protected int width;
    protected int height;

    public GLRenderBuffer(
            @MagicConstant(valuesFromClass = GLConstants.class) int colorFormat,
            int width, int height
    ) {
        this.name = Glad.glGenRenderbuffer();
        this.colorFormat = colorFormat;
        this.width = width;
        this.height = height;
        createStorage();
    }

    protected void createStorage() {
        Glad.glNamedRenderbufferStorage(name, colorFormat, width, height);
    }

    public void reCreate() {
        Glad.glDeleteRenderbuffer(name);
        this.name = 0;
        this.name = Glad.glGenRenderbuffer();
        createStorage();
        callReCreationListener();
    }

    @Override
    public boolean isReCreatable() {
        return true;
    }

    public void changeSize(int width, int height) {
        this.width = width;
        this.height = height;
        reCreate();
    }

    @Override
    public void close() {
        Glad.glDeleteRenderbuffer(name);
    }
}
