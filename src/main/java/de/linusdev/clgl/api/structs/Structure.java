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

@SuppressWarnings("unused")
public abstract class Structure implements Sizeable, NativeParsable {

    protected Structure mostParentStructure;
    protected ByteBuffer byteBuf;
    protected int offset;
    protected volatile boolean modified;

    /**
     * Set this {@link Structure} to be a child of {@code mostParentStructure}.
     * @param mostParentStructure most parental structure
     * @param offset start of this structure
     */
    public void useBuffer(@NotNull Structure mostParentStructure, int offset) {
        this.mostParentStructure = mostParentStructure;
        this.offset = offset;
        this.byteBuf = offset == 0 ?
                mostParentStructure.getByteBuf().order(ByteOrder.nativeOrder()) :
                mostParentStructure.getByteBuf().slice(offset, getRequiredSize()).order(ByteOrder.nativeOrder());
    }

    /**
     * Will set the {@link ByteOrder} to native order.
     * The most parental structure will be {@code this}.
     * @param buffer {@link ByteBuffer} to claim
     */
    public void claimBuffer(@NotNull ByteBuffer buffer) {
        this.mostParentStructure = this;
        this.offset = 0;
        this.byteBuf = buffer.order(ByteOrder.nativeOrder());
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

    /**
     * Mark {@code size} bytes at {@code offset} as modified.
     * @param offset region start
     * @param size region size
     */
    public void modified(int offset, int size) {
        mostParentStructure.onModification(offset, size);
    }

    /**
     * Mark the whole structure as modified.
     */
    public void modified() {
        modified(offset, getSize());
    }

    /**
     * Called on the most parental structure if on it or any of its children {@link #modified(int, int)} is called.
     * @param offset modified region start
     * @param size modified region size
     */
    @ApiStatus.OverrideOnly
    protected void onModification(int offset, int size) {
        modified = true;
    }

    /**
     * Will be {@code true} after a call to {@link #modified(int, int)}. Will be reset by {@link #unmodified()}.
     * Note that modifications are only tracked on the most parental structure. The return value of this method is undefined,
     * if it is not the most parental structure.
     * @return whether this {@link Structure} has been modified.
     */
    public boolean isModified() {
        return modified;
    }

    /**
     * Marks this structure has not modified. May only be called on the most parental structure.
     */
    @ApiStatus.Internal
    public void unmodified() {
        modified = false;
    }

    /**
     * Will never be {@code null} after either {@link #claimBuffer(ByteBuffer)} or {@link #useBuffer(Structure, int)}
     * has been called.
     * @return the most parental structure.
     */
    @ApiStatus.Internal
    public Structure getMostParentStructure() {
        return mostParentStructure;
    }

}
