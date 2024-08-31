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
import de.linusdev.lutils.nat.array.NativeInt32Array;
import de.linusdev.lutils.nat.array.NativeInt64Array;
import de.linusdev.lutils.nat.array.NativeUInt8Array;
import de.linusdev.lutils.nat.integer.NativeInteger;
import de.linusdev.lutils.nat.pointer.BBPointer64;
import de.linusdev.lutils.nat.pointer.BBTypedPointer64;
import de.linusdev.lutils.nat.string.NullTerminatedUTF16String;
import de.linusdev.lutils.nat.string.NullTerminatedUTF8String;
import de.linusdev.lutils.nat.struct.abstracts.Structure;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum CTypes implements Type {
    CHAR("B", BBByte1.class, "char", "jbyte", byte.class, Byte.BYTES * 8, null, null),
    FLOAT("F", BBFloat1.class, "float", "jfloat", float.class, Float.BYTES * 8, null, null),
    DOUBLE("D", BBDouble1.class, "double", "jdouble", double.class, Double.BYTES * 8, null, null),
    INT("NI", NativeInteger.class, "int", "jlong", long.class, -1, null, null),
    INT8("B", BBByte1.class, "int8_t", "jbyte", byte.class, Byte.BYTES * 8, null, null),
    UINT8("UB", BBUByte1.class, "uint8_t", "jbyte", byte.class, Byte.BYTES * 8, null, NativeUInt8Array.class),
    INT16("S", BBShort1.class, "int16_t", "jshort", short.class, Short.BYTES * 8, null, null),
    UINT16("US", BBUShort1.class, "uint16_t", "jshort", short.class, Short.BYTES * 8, null, null),
    UINT32("UI", BBUInt1.class, "uint32_t", "jint", int.class, Integer.BYTES * 8, IntBitfield.class, null),
    UINT64("UL", BBULong1.class, "uint64_t", "jlong", long.class, Long.BYTES * 8, LongBitfield.class, null),
    INT32("I", BBInt1.class, "int32_t", "jint", int.class, Integer.BYTES * 8, IntBitfield.class, NativeInt32Array.class),
    INT64("L", BBLong1.class, "int64_t", "jlong", long.class, Long.BYTES * 8, LongBitfield.class, NativeInt64Array.class),
    SIZE("UL", BBULong1.class, "size_t", "jlong", long.class, Long.BYTES * 8, null, null),
    POINTER("P", BBPointer64.class, "void*", "jlong", long.class, Long.BYTES * 8, null, null),

    VOID("V", null,
            JavaClass.custom("de.linusdev.cvg4j.nat", "UnknownStruct"),
            "void", "void", void.class, -1, null, null
    ),


    STRING_UTF8("P", BBTypedPointer64.class,
            JavaClass.ofClass(BBTypedPointer64.class).withGenerics(
                    JavaClass.ofClass(NullTerminatedUTF8String.class)
            ),
            "char*", "jstring", String.class, -1, null, null
    ),

    STRING_UTF16("P", BBTypedPointer64.class,
                JavaClass.ofClass(BBTypedPointer64.class).withGenerics(
            JavaClass.ofClass(NullTerminatedUTF16String.class)
            ),
                    "wchar_t*", "-", String.class, -1, null, null
            ),
    ;



    public static @NotNull CTypes ofCType(@NotNull String name) {
        for (CTypes type : values()) {
            if(type.cName.equals(name))
                return type;
        }

        throw new IllegalStateException("Unknown type " + name);
    }

    private final @NotNull String shortName;
    private final @Nullable Class<? extends Structure> javaStruct;
    private final @Nullable JavaClass javaStructAsJavaClass;
    private final @NotNull String cName;
    private final @NotNull String jniName;
    private final @NotNull Class<?> javaClass;
    private final int bitWidth;
    private final @Nullable Class<?> bitFieldInterface;
    private final @Nullable Class<?> nativeArrayClass;

    CTypes(
            @NotNull String shortName, @Nullable Class<? extends Structure> javaStruct,
            @Nullable JavaClass javaStructAsJavaClass,
            @NotNull String cName, @NotNull String jniName,
            @NotNull Class<?> javaClass,
            int bitWidth, @Nullable Class<?> bitFieldInterface, @Nullable Class<?> nativeArrayClass
    ) {
        this.shortName = shortName;
        this.javaStruct = javaStruct;
        this.javaStructAsJavaClass = javaStructAsJavaClass;
        this.cName = cName;
        this.jniName = jniName;
        this.javaClass = javaClass;
        this.bitWidth = bitWidth;
        this.bitFieldInterface = bitFieldInterface;
        this.nativeArrayClass = nativeArrayClass;
    }

    CTypes(
            @NotNull String shortName,
            @Nullable Class<? extends Structure> javaStruct,
            @NotNull String cName,
            @NotNull String jniName,
            @NotNull Class<?> javaClass,
            int bitWidth,
            @Nullable Class<?> bitFieldInterface, @Nullable Class<?> nativeArrayClass
    ) {
        this(shortName, javaStruct, (JavaClass) null, cName, jniName, javaClass, bitWidth, bitFieldInterface, nativeArrayClass);
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

    public @NotNull String getJniName() {
        return jniName;
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

    public @Nullable Class<?> getNativeArrayClass() {
        return nativeArrayClass;
    }

    public @Nullable JavaClass getJavaStructAsJavaClass() {
        return javaStructAsJavaClass;
    }

    public @NotNull String getShortName() {
        return shortName;
    }

    @Override
    public void generate(@NotNull RegistryLoader registry, @NotNull SourceGenerator generator) {

    }

    @Override
    public @NotNull JavaClass getJavaClass(@NotNull RegistryLoader registry, @NotNull SourceGenerator generator) {
        if(getJavaStructAsJavaClass() != null)
            return getJavaStructAsJavaClass();
        if(javaStruct == null)
            throw new IllegalStateException("Cannot get JavaClass of " + name());
        return JavaClass.ofClass(javaStruct);
    }
}
