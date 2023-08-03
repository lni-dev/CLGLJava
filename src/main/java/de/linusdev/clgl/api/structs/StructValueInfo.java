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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class StructValueInfo {

    private static StructureInfo getInfo(@NotNull Class<?> clazz, @NotNull Class<?> fieldClazz, @NotNull Field field) {

        Field infoField;
        try {
            infoField = clazz.getField("INFO");
        } catch (NoSuchFieldException e) {
            throw (IllegalStructValueException) new IllegalStructValueException(fieldClazz, field,
                    clazz.getCanonicalName() + " is missing 'public static StructureInfo INFO' variable")
                    .initCause(e);
        }

        try {
            return (StructureInfo) infoField.get(null);
        } catch (IllegalAccessException e) {
            throw (IllegalStructValueException) new IllegalStructValueException(fieldClazz, field,
                    "Cannot access 'public static StructureInfo INFO' variable of " + clazz.getCanonicalName())
                    .initCause(e);
        }
    }

    public static StructValueInfo fromField(@NotNull Class<?> clazz, @NotNull Field field) {
        StructValue sv = field.getAnnotation(StructValue.class);
        FixedElementSize fes = field.getAnnotation(FixedElementSize.class);
        if(sv == null) return null;

        Class<?> fieldClass = field.getType();
        if(!Structure.class.isAssignableFrom(fieldClass)) {
            throw new IllegalStructValueException(clazz, field, "Class is not a sub class of Structure.");
        }

        //Settings / Variable size
        StructureSettings settings = fieldClass.getAnnotation(StructureSettings.class);
        if(settings != null && settings.isOfVariableSize()) {

            if(!settings.supportsCalculateInfoMethod())
                throw new IllegalStructValueException(clazz, field,
                        "Structure is of variable size, but does not support the calculateInfo(...) method.");

            if(fes == null)
                throw new IllegalStructValueException(clazz, field,
                        "Structure is of variable size, but field is not annotated with @FixedElementSize.");

            Method calcInfoMethod;
            try {
                calcInfoMethod = fieldClass.getMethod("calculateInfo", Sizeable.class, int.class);
            } catch (NoSuchMethodException e) {
                throw new IllegalStructValueException(clazz, field,
                        "calculateInfo(...) method not found. See @StructureSettings.");
            }

            StructureInfo info;
            try {
                info = (StructureInfo) calcInfoMethod.invoke(null, getInfo(fes.elementType(), fieldClass, field), fes.value());
            } catch (IllegalAccessException | InvocationTargetException | ClassCastException e) {
                throw (IllegalStructValueException) new IllegalStructValueException(clazz, field,
                        "Error invoking calculateInfo(...) method. See @StructureSettings.").initCause(e);
            }

            return new StructValueInfo(field, info, sv, fes);
        }

        //get INFO
        StructureInfo info = getInfo(fieldClass, fieldClass, field);
        return new StructValueInfo(field, info, sv, fes);
    }

    private final @NotNull Field field;
    private final @NotNull StructureInfo info;
    private final @NotNull StructValue structValue;
    private final @Nullable FixedElementSize fes;


    protected StructValueInfo(
            @NotNull Field field,
            @NotNull StructureInfo info,
            @NotNull StructValue structValue,
            @Nullable FixedElementSize fes
    ) {
        this.field = field;
        this.info = info;
        this.structValue = structValue;
        this.fes = fes;
    }

    public @NotNull String getName() {
        return field.getName();
    }


    public @NotNull StructureInfo getInfo() {
        return info;
    }

    public @NotNull StructValue getStructValue() {
        return structValue;
    }

    public @Nullable Class<?> getElementType() {
        return fes == null ? null : fes.elementType();
    }

    public int getArraySize() {
        return fes == null ? 0 : fes.value();
    }



    public Structure get(@NotNull ComplexStructure obj) {
        try {
            return (Structure) field.get(obj);
        } catch (IllegalAccessException e) {
            throw new IllegalStructValueException(field.getDeclaringClass(), field,
                    "Cannot access field value");
        }
    }

    public String toString(@NotNull ComplexStructure self) {
        return get(self).toString(field.getType().getSimpleName() + " " + getName());
    }
}
