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

package de.linusdev.cvg4j.build.vkregistry.types;

import de.linusdev.cvg4j.build.vkregistry.RegistryLoader;
import de.linusdev.cvg4j.build.vkregistry.types.abstracts.Type;
import de.linusdev.cvg4j.build.vkregistry.types.abstracts.TypeType;
import de.linusdev.lutils.bitfield.IntBitfield;
import de.linusdev.lutils.bitfield.LongBitfield;
import de.linusdev.lutils.codegen.SourceGenerator;
import de.linusdev.lutils.codegen.java.JavaClass;
import de.linusdev.lutils.math.vector.buffer.byten.BBByte1;
import de.linusdev.lutils.math.vector.buffer.byten.BBUByte1;
import de.linusdev.lutils.math.vector.buffer.doublen.BBDouble1;
import de.linusdev.lutils.math.vector.buffer.floatn.BBFloat1;
import de.linusdev.lutils.math.vector.buffer.intn.BBInt1;
import de.linusdev.lutils.math.vector.buffer.intn.BBUInt1;
import de.linusdev.lutils.math.vector.buffer.longn.BBLong1;
import de.linusdev.lutils.math.vector.buffer.longn.BBULong1;
import de.linusdev.lutils.math.vector.buffer.shortn.BBShort1;
import de.linusdev.lutils.math.vector.buffer.shortn.BBUShort1;
import de.linusdev.lutils.nat.pointer.Pointer64;
import de.linusdev.lutils.nat.struct.abstracts.Structure;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum CTypes implements Type {
    VOID(null, "void", void.class, -1, null),
    CHAR(BBByte1.class, "char", byte.class, Byte.BYTES * 8, null),
    FLOAT(BBFloat1.class, "float", float.class, Float.BYTES * 8, null),
    DOUBLE(BBDouble1.class, "double", double.class, Double.BYTES * 8, null),
    INT8(BBByte1.class, "int8_t", byte.class, Byte.BYTES * 8, null),
    UINT8(BBUByte1.class, "uint8_t", byte.class, Byte.BYTES * 8, null),
    INT16(BBShort1.class, "int16_t", short.class, Short.BYTES * 8, null),
    UINT16(BBUShort1.class, "uint16_t", short.class, Short.BYTES * 8, null),
    UINT32(BBUInt1.class, "uint32_t", int.class, Integer.BYTES * 8, IntBitfield.class),
    UINT64(BBULong1.class, "uint64_t", long.class, Long.BYTES * 8, LongBitfield.class),
    INT32(BBInt1.class, "int32_t", int.class, Integer.BYTES * 8, IntBitfield.class),
    INT64(BBLong1.class, "int64_t", long.class, Long.BYTES * 8, LongBitfield.class),
    SIZE(BBULong1.class, "size_t", long.class, Long.BYTES * 8, null),
    POINTER(Pointer64.class, "*", long.class, Long.BYTES * 8, null),
    STRING(null, "char*", String.class, -1, null)
    ;

    public static @NotNull CTypes ofCType(@NotNull String name) {
        for (CTypes type : values()) {
            if(type.cName.equals(name))
                return type;
        }

        throw new IllegalStateException("Unknown type " + name);
    }

    private final @Nullable Class<? extends Structure> javaStruct;
    private final @NotNull String cName;
    private final @NotNull Class<?> javaClass;
    private final int bitWidth;
    private final @Nullable Class<?> bitFieldInterface;

    CTypes(
            @Nullable Class<? extends Structure> javaStruct,
            @NotNull String cName,
            @NotNull Class<?> javaClass,
            int bitWidth, @Nullable Class<?> bitFieldInterface
    ) {
        this.javaStruct = javaStruct;
        this.cName = cName;
        this.javaClass = javaClass;
        this.bitWidth = bitWidth;
        this.bitFieldInterface = bitFieldInterface;
    }

    public @Nullable Class<? extends Structure> getJavaStruct() {
        return javaStruct;
    }

    public String getCName() {
        return cName;
    }

    @Override
    public @NotNull String getName() {
        return getCName();
    }

    @Override
    public @NotNull TypeType getType() {
        return TypeType.BASIC;
    }

    @Override
    public @NotNull CTypes getAsBaseType() {
        return this;
    }

    public @NotNull Class<?> getJavaClass() {
        return javaClass;
    }

    public int getBitWidth() {
        return bitWidth;
    }

    public @Nullable Class<?> getBitFieldInterface() {
        return bitFieldInterface;
    }

    @Override
    public void generate(@NotNull RegistryLoader registry, @NotNull SourceGenerator generator) {

    }

    @Override
    public @NotNull JavaClass getJavaClass(@NotNull RegistryLoader registry, @NotNull SourceGenerator generator) {
        if(javaStruct == null)
            throw new IllegalStateException("Cannot get JavaClass of " + name());
        return JavaClass.ofClass(javaStruct);
    }
}
