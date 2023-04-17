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

import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public class StructureInfo implements Sizeable {

    /**
     * Returns the size of the biggest {@link Structure} in given array.
     * The size will be at least 4, even if every struct is smaller than 4 bytes.
     * @param structures array of {@link Structure}
     * @return max(4, biggestStruct.getRequiredSize())
     */
    public static int getBiggestStructSize(@NotNull Sizeable @NotNull ... structures) {
        int biggest = 4;
        for(Sizeable structure : structures)
            biggest = Math.max(biggest, structure.getRequiredSize());
        return biggest;
    }

    private final int alignment;
    private final boolean compressed;

    /**
     * array of item sizes. Always alternating between padding and item.
     * First and last element are always a padding.<br><br>
     * Layout: <br>
     * [0]: pad, [1]: size, [2]: pad, [3]: size, [4]: pad, [5]: size, ...
     */
    private final int[] sizes;

    private final int size;

    /**
     * Calculate {@link Structure} size. Alignment will be calculated
     * using {@link #getBiggestStructSize(Sizeable...)}.
     * @param structures array of {@link Sizeable} to align
     */
    public StructureInfo(@NotNull Sizeable @NotNull ... structures) {
        this(getBiggestStructSize(structures), false, structures);
    }

    /**
     *
     * @param alignment alignment of the structure
     * @param compress whether the structure should be compressed, see {@link #isCompressed()}
     * @param structures array of {@link Sizeable} to align
     */
    public StructureInfo(final int alignment, final boolean compress, @NotNull Sizeable @NotNull ... structures) {
        this.alignment = alignment;
        this.compressed = compress;
        this.sizes = new int[structures.length * 2 + 1];

        int padding = 0;
        int position = 0;

        for(int i = 0; i < structures.length; ) {
            if(position % alignment == 0 || alignment - (position % alignment) >= structures[i].getRequiredSize()) {

                int itemSize = structures[i].getRequiredSize();
                if(!compress && (position % itemSize) != 0) {
                    int offset = (itemSize - (position % itemSize));
                    position += offset;
                    padding += offset;
                    continue;
                }

                this.sizes[i * 2] = padding;
                this.sizes[i * 2 + 1] = itemSize;
                position += itemSize;
                padding = 0;
                i++;
            } else {
                int offset = (alignment - (position % alignment));
                position += offset;
                padding += offset;
            }
        }

        if(position % alignment != 0) {
            sizes[sizes.length - 1] = (alignment - (position % alignment));
            position += sizes[sizes.length - 1];
        }
        else sizes[sizes.length - 1] = 0;
        this.size = position;
    }

    public StructureInfo(int alignment, boolean compressed, int prePadding, int size, int postPadding) {
        this.alignment = alignment;
        this.compressed = compressed;
        this.sizes = new int[]{prePadding, size, postPadding};
        this.size = prePadding + size + postPadding;
    }

    /**
     * Alignment used to create this {@link StructureInfo}
     * @return alignment
     */
    public int getAlignment() {
        return alignment;
    }

    /**
     * The calculated size.
     * @return size
     */
    public int getRequiredSize() {
        return size;
    }

    /**
     * Whether this {@link StructureInfo} is compressed. Compressed means, it should ignore
     * the size of the individual elements when aligning the structure.
     * @return {@code true} if it is compressed.
     */
    public boolean isCompressed() {
        return compressed;
    }

    /**
     * Padding and item sizes.
     * @return Padding and item sizes.
     * @see #sizes
     */
    public int[] getSizes() {
        return sizes;
    }

    public String toString(@NotNull String name, @NotNull Structure @NotNull ... structures) {
        StringBuilder sb = new StringBuilder(name);
        sb.append("(size: ").append(size)
                .append(", alignment: ").append(alignment)
                .append(", compressed: ").append(compressed)
                .append(") {\n");

        for(int i = 0; i < sizes.length; i++) {
            if(sizes[i] == 0) continue;
            String text = sizes[i] + " : " + (((i-1) % 2) == 0 ? structures[(i - 1)/2] : "padding");
            sb.append(text.indent(4));
        }

        sb.append("}");

        return sb.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("StructureInfo");
        sb.append("(size: ").append(size)
                .append(", alignment: ").append(alignment)
                .append(", compressed: ").append(compressed)
                .append(") {\n");

        for(int i = 0; i < sizes.length; i++) {
            if(sizes[i] == 0) continue;
            String text = sizes[i] + " : " + (((i-1) % 2) == 0 ? "item" : "padding");
            sb.append(text.indent(4));
        }

        sb.append("}");

        return sb.toString();
    }
}
