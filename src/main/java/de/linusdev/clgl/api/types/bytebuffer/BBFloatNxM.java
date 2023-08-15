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

package de.linusdev.clgl.api.types.bytebuffer;

import de.linusdev.clgl.api.structs.Structure;
import de.linusdev.clgl.api.structs.StructureInfo;
import de.linusdev.clgl.api.types.bytebuffer.utils.BBMatInfo;
import de.linusdev.clgl.api.types.matrix.FloatNxM;
import de.linusdev.clgl.api.types.matrix.Matrix;
import org.jetbrains.annotations.NotNull;

import java.nio.FloatBuffer;

public abstract class BBFloatNxM<M extends BBFloatN> extends Structure implements FloatNxM {
    protected final @NotNull BBMatInfo<M> info;

    protected FloatBuffer buf;
    protected final M[] lines;

    public BBFloatNxM(@NotNull Structure mostParentStructure, int offset, @NotNull BBMatInfo<M> info) {
        this.info = info;
        this.lines = info.createArray();
        for(int i = 0; i < lines.length; i++)
            lines[i] = info.createNew();

        useBuffer(mostParentStructure, offset);
    }

    public BBFloatNxM(boolean allocateBuffer, @NotNull BBMatInfo<M> info) {
        this.info = info;
        this.lines = info.createArray();
        for(int i = 0; i < lines.length; i++)
            lines[i] = info.createNew();

        if(allocateBuffer)
            allocate();
    }

    @Override
    public void useBuffer(@NotNull Structure mostParentStructure, int offset) {
        super.useBuffer(mostParentStructure, offset);
        buf = byteBuf.asFloatBuffer();

        for(int i = 0; i < lines.length; i++)
            lines[i].useBuffer(this, info.getMemberSize() * i);
    }

    @Override
    public int getHeight() {
        return lines.length;
    }

    protected M getRow(int y) {
        return lines[y];
    }

    @Override
    public float get(int x, int y) {
        return getRow(y).get(x);
    }

    @Override
    public @NotNull String getOpenCLName() {
        return "BBFloat" + getWidth() + "x" + getHeight();
    }

    @Override
    public void put(int x, int y, float value) {
        getRow(y).put(x, value);
    }

    @Override
    public boolean isArrayBacked() {
        return true;
    }

    @Override
    public boolean isBufferBacked() {
        return false;
    }

    @Override
    protected @NotNull StructureInfo getInfo() {
        return info.getInfo();
    }

    @Override
    public String toString() {
        return Matrix.toString("BBFloat", this);
    }
}
