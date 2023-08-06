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
import de.linusdev.clgl.api.types.LongN;
import org.jetbrains.annotations.NotNull;

import java.nio.LongBuffer;

@SuppressWarnings("unused")
public abstract class BBLongN extends Structure implements LongN {

    protected LongBuffer buf;
    protected final int memberCount;

    public BBLongN(@NotNull Structure mostParentStructure, int offset, int count) {
        this.memberCount = count;
        useBuffer(mostParentStructure, offset);
    }

    public BBLongN(int count, boolean allocateBuffer) {
        this.memberCount = count;
        if(allocateBuffer)
            allocate();
    }

    @Override
    public void useBuffer(@NotNull Structure mostParentStructure, int offset) {
        super.useBuffer(mostParentStructure, offset);
        buf = byteBuf.asLongBuffer();
    }

    @Override
    public int getMemberCount() {
        return memberCount;
    }

    @Override
    public long get(int index) {
        return buf.get(index);
    }

    @Override
    public void put(int index, long value) {
        buf.put(index, value);
    }

    @Override
    public boolean isArrayBacked() {
        return false;
    }

    @Override
    public boolean isBufferBacked() {
        return true;
    }

    @Override
    public @NotNull Structure getStructure() {
        return this;
    }

    @Override
    public boolean isView() {
        return false;
    }

    @Override
    public @NotNull String getOpenCLName() {
        return memberCount == 1 ? "long" : "long" + memberCount;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        boolean first = true;
        for(int i = 0; i < memberCount; i++) {
            if(first) first = false;
            else sb.append(", ");
            sb.append(buf.get(i));
        }

        return String.format("%s%d(%s)", "long", memberCount, sb);
    }
}
