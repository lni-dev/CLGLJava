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

package de.linusdev.clgl.api.structs;


import de.linusdev.clgl.api.utils.BufferUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.locks.ReentrantLock;

@SuppressWarnings("unused")
public abstract class Structure implements Sizeable, NativeParsable {

    protected Structure mostParentStructure;
    protected ByteBuffer byteBuf;
    protected int offset;
    protected volatile boolean modified;
    protected ReentrantLock modificationLock;

    public void useBuffer(@NotNull Structure mostParentStructure, int offset) {
        this.mostParentStructure = mostParentStructure;
        this.offset = offset;
        this.byteBuf = offset == 0 ?
                mostParentStructure.getByteBuf().order(ByteOrder.nativeOrder()) :
                mostParentStructure.getByteBuf().slice(offset, getRequiredSize()).order(ByteOrder.nativeOrder());
        this.modificationLock = mostParentStructure.modificationLock;
    }

    public void claimBuffer(@NotNull ByteBuffer buffer) {
        this.mostParentStructure = this;
        this.offset = 0;
        this.byteBuf = buffer.order(ByteOrder.nativeOrder());
        this.modificationLock = new ReentrantLock();
    }

    public void modified(int offset, int size) {
        mostParentStructure.onModification(offset, size);
    }

    @ApiStatus.OverrideOnly
    protected void onModification(int offset, int size) {
        modified = true;
    }

    /**
     * Creates an 8 byte aligned direct byte buffer and calls {@link #useBuffer(Structure, int)}.
     */
    public void allocate() {
        claimBuffer(BufferUtils.createAlignedByteBuffer(getRequiredSize(), 8));
        useBuffer(this, 0);
    }

    /**
     * Size required for this {@link Structure} in bytes.
     * @return required size
     */
    @Override
    public int getRequiredSize() {
        return getInfo().getRequiredSize();
    }

    @Override
    public int getAlignment() {
        return getInfo().getAlignment();
    }

    /**
     * Information about this {@link Structure}.
     * @return {@link StructureInfo}
     */
    protected abstract @NotNull StructureInfo getInfo();


    /**
     * Byte size of this {@link Structure}
     * @return byte size
     */
    public int getSize() {
        return byteBuf.capacity();
    }

    /**
     * Offset in the byte buffer of the most parental structure.
     * @return offset in bytes
     */
    public int getOffset() {
        return offset;
    }

    /**
     * Get the {@link ByteBuffer} backed by this {@link Structure}.
     * @return {@link ByteBuffer} of this {@link Structure}
     */
    public ByteBuffer getByteBuf() {
        return byteBuf;
    }

    @Override
    public @NotNull ByteBuffer getByteBuffer() {
        return byteBuf;
    }

    /**
     *
     * @param name name of this struct
     * @return A nice string describing this structure
     */
    public String toString(@NotNull String name) {
        return name + " " + this;
    }

    public abstract @NotNull String getOpenCLName();

    public @NotNull String getOpenCLName(@Nullable Class<?> elementType, int size, @NotNull String paramName) {
        return getOpenCLName() + " " + paramName;
    }

    public boolean isModified() {
        return modified;
    }

    public void unmodified() {
        modified = false;
    }

    public ModificationInfo getFirstModificationInfo(boolean clear) {
        throw new UnsupportedOperationException();
    }

    public boolean hasModificationsInfo() {
        return false;
    }

    public void acquireModificationLock() {
        modificationLock.lock();
    }

    public void releaseModificationLock() {
        modificationLock.unlock();
    }

    public Structure getMostParentStructure() {
        return mostParentStructure;
    }
}
