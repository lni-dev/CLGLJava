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

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Marks this class as a {@link Structure} and contains specific information about it.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface StructureSettings {

    /**
     *
     * @return {@code true} if this structures size is only known after instanced.
     */
    boolean isOfVariableSize() default false;

    /**
     * Only important if {@link #isOfVariableSize()} is {@code true}.
     * @return {@code true} if this structure has a<br>
     * {@code public static StructureInfo calculateInfo(Sizeable elementType, int size)}<br>
     * method.
     * @see StructureArray#calculateInfo(Sizeable, int)
     */
    boolean supportsCalculateInfoMethod() default false;

}
