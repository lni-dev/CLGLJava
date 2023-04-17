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

package de.linusdev.clgl.api.types;

import de.linusdev.clgl.api.structs.Structure;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public interface Vector {

    /**
     * Count of components in this vector.
     * @return float count in this vector
     */
    int getMemberCount();

    /**
     * Whether this vector is array backed.
     * @return {@code true} if this vector is array backed.
     */
    boolean isArrayBacked();

    /**
     * Whether this vector is buffer backed. If this method returns {@code true},
     * {@link #getStructure()} will not throw an {@link UnsupportedOperationException}.
     * @return {@code true} if this vector is buffer backed.
     */
    boolean isBufferBacked();

    /**
     *
     * @return this vector as {@link Structure}
     * @throws UnsupportedOperationException if this vector is not {@link #isBufferBacked() buffer backed}.
     */
    default @NotNull Structure getStructure() {
        throw new UnsupportedOperationException("This vector is not buffer backed.");
    }
}
