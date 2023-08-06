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
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;

public class StructureInfo implements Sizeable {

    private static final @NotNull StackWalker STACK_WALKER = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);

    /**
     * Returns the size of the biggest {@link Structure} in given array.
     * The size will be at least {@code min} and at most {@code max}.
     * @param min minimum size
     * @param max maximum size
     * @param structures array of {@link Structure}
     * @return clamp(min, max, biggestStruct.getRequiredSize())
     */
    public static int getBiggestStructAlignment(int min, int max, @NotNull StructValueInfo @NotNull ... structures) {
        int biggest = min;
        for(StructValueInfo structure : structures)
            biggest = Math.max(biggest, structure.getInfo().getAlignment());
        return Math.min(max, biggest);
    }

    private static StructValueInfo[] getInfoFromStructVars(Class<?> clazz) {
        Field[] fields = clazz.getFields();

        StructValueInfo[] infos = new StructValueInfo[fields.length];
        int index = 0;
        int size = 0;

        for(Field field : fields) {
            StructValueInfo info = StructValueInfo.fromField(clazz, field);

            if(info == null) continue;

            //get INFO
            if(info.getStructValue().value() == -1) infos[index++] = info;
            else infos[info.getStructValue().value()] = info;
            size++;
        }

        return Arrays.copyOf(infos, size);
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
    private final @NotNull StructValueInfo @Nullable [] infos;

    private final int size;

    /**
     * Automatically reads the {@link StructValue}s of the caller class.
     * For more information on aligment and compression see {@link StructureInfo#StructureInfo(StructValueInfo...)}.
     */
    public StructureInfo() {
        this(getInfoFromStructVars(STACK_WALKER.getCallerClass()));
    }

    /**
     * Automatically reads the {@link StructValue}s of the caller class.
     * @see StructureInfo#StructureInfo(int, boolean, StructValueInfo...) 
     */
    public StructureInfo(int alignment, boolean compress) {
        this(alignment, compress, getInfoFromStructVars(STACK_WALKER.getCallerClass()));
    }

    /**
     * Calculate {@link Structure} size. Alignment will be calculated
     * using {@link #getBiggestStructAlignment(int, int, StructValueInfo...) getBiggestStructAlignment()} with {@code min=4}
     * and {@code max=16}.
     * @param structures array of {@link Sizeable} to align
     */
    private StructureInfo(@NotNull StructValueInfo @NotNull ... structures) {
        this(getBiggestStructAlignment(4, 16, structures), false, structures);
    }

    /**
     *
     * @param alignment alignment of the structure
     * @param compress whether the structure should be compressed, see {@link #isCompressed()}
     * @param structures array of {@link Sizeable} to align
     */
    private StructureInfo(final int alignment, final boolean compress, @NotNull StructValueInfo @NotNull ... structures) {
        this.alignment = alignment;
        this.compressed = compress;
        this.sizes = new int[structures.length * 2 + 1];
        this.infos = structures;

        int padding = 0;
        int position = 0;

        for(int i = 0; i < structures.length; ) {
            StructureInfo structure = structures[i].getInfo();
            if((position % alignment) == 0 || alignment - (position % alignment) >= structure.getRequiredSize()) {

                int itemSize = structure.getRequiredSize();
                int itemAlignment = structure.getAlignment();
                if(!compress && (position % itemAlignment) != 0) {
                    int offset = (itemAlignment - (position % itemAlignment));
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

    /**
     * Manually create {@link StructureInfo}.
     * @param alignment alignment of the structure
     * @param compressed whether the structure is compressed, see {@link #isCompressed()}
     * @param prePadding padding before the {@link Structure}
     * @param size size of the actual {@link Structure} (without any padding)
     * @param postPadding padding after the {@link Structure}
     */
    public StructureInfo(int alignment, boolean compressed, int prePadding, int size, int postPadding) {
        this.alignment = alignment;
        this.compressed = compressed;
        this.sizes = new int[]{prePadding, size, postPadding};
        this.size = prePadding + size + postPadding;
        this.infos = null;
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
    @SuppressWarnings("unused")
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

    /**
     * Gets all children through reflection. This should be used sparsely. But it is required if this {@link StructureInfo}
     * was automatically generated (see {@link StructureInfo#StructureInfo()}) with no {@link StructValue#value() element order} specified.
     * @param self instance of the {@link ComplexStructure} this info belongs to
     * @return children {@link Structure} array
     */
    public @NotNull Structure @NotNull [] getChildren(@NotNull ComplexStructure self) {
        if(infos == null)
            throw new UnsupportedOperationException("Only works with auto generated StructureInfos");

        Structure[] structures = new Structure[infos.length];

        for(int i = 0; i < structures.length; i++) {
            structures[i] = infos[i].get(self);
        }

        return structures;
    }


    /*
     *
     *
     * Convenient methods
     *
     *
     */

    private String toStringUseInfos(@NotNull ComplexStructure self, @NotNull String name) {
        assert infos != null;
        StringBuilder sb = new StringBuilder(name);
        sb.append("(size: ").append(size)
                .append(", alignment: ").append(alignment)
                .append(", compressed: ").append(compressed)
                .append(") {\n");

        for(int i = 0; i < sizes.length; i++) {
            if(sizes[i] == 0) continue;
            String text = sizes[i] + " : " + (((i-1) % 2) == 0 ? infos[(i - 1)/2].toString(self) : "padding");
            sb.append(text.indent(4));
        }

        sb.append("}");

        return sb.toString();
    }

    public String toString(@Nullable ComplexStructure self, @NotNull String name) {
        if(infos != null && self != null) return toStringUseInfos(self, name);

        StringBuilder sb = new StringBuilder(name);
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

    @Override
    public String toString() {
        return toString(null, "StructureInfo");
    }

    private static final int[] suppPaddings = {1, 2, 4, 8, 16};
    private static int addPadding(StringBuilder sb, int padding, int index) {
        switch (padding) {
            case 0 -> {
                return index;
            }
            case 1 -> sb.append("byte padding").append(index).append(";\n");
            case 2 -> sb.append("char padding").append(index).append(";\n");
            case 4 -> sb.append("int padding").append(index).append(";\n");
            case 8 -> sb.append("int2 padding").append(index).append(";\n");
            case 16 -> sb.append("int4 padding").append(index).append(";\n");
            default -> {
                for(int i = suppPaddings.length-1; i >= 0; i--) {
                    int pad = suppPaddings[i];
                    if(padding - pad > 0) {
                        padding -= pad;
                        index = addPadding(sb, pad, index);
                        return addPadding(sb, padding, index);
                    }
                }
            }
        }
        return index + 1;
    }

    /**
     * Not fully working! Arrays are not yet supported.
     */
    @SuppressWarnings("unused")
    public @NotNull String toOpenCLStructCode(@NotNull ComplexStructure self) {
        return toOpenCLStructCode(self, false, null);
    }

    /**
     * Not fully working! Arrays are not yet supported.
     */
    public @NotNull String toOpenCLStructCode(@NotNull ComplexStructure self, boolean addChildren, @Nullable ArrayList<String> added) {
        if(infos == null)
            throw new UnsupportedOperationException("Only works with auto generated StructureInfos");

        if(addChildren && added == null)
            added = new ArrayList<>();

        final String name = self.getOpenCLName();
        final StringBuilder sb = new StringBuilder();

        if(addChildren) {
            for(StructValueInfo valueInfo : infos) {
                Structure instance = valueInfo.get(self);
                if(instance instanceof ComplexStructure complexStructure && complexStructure.canGenerateOpenCLCode()) {
                    if(added.contains(complexStructure.getOpenCLName()))
                        continue;
                    added.add(complexStructure.getOpenCLName());
                    sb
                            .append(valueInfo.getInfo().toOpenCLStructCode(complexStructure, true, added))
                            .append("\n\n");
                }

            }
        }

        int paddingIndex = 0;

        sb.append("typedef struct __attribute__((packed)) {\n");

        for(int i = 0; i < sizes.length; i++) {
            if(sizes[i] == 0) continue;
            String text;
            if((((i-1) % 2) == 0)) {
                StructValueInfo info = infos[(i - 1)/2];
                text = info.get(self).getOpenCLName(info.getElementType(), info.getArraySize(), info.getName()) + ";\n";
            } else {
                StringBuilder pad = new StringBuilder();
                paddingIndex = addPadding(pad, sizes[i], paddingIndex);
                text = pad.toString();
            }

            sb.append(text.indent(4));
        }


        sb.append("} ").append(name).append(";");

        return sb.toString();
    }
}
