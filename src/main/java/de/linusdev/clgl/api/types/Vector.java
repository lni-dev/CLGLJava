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
     * Used by {@link #isView() view vectors} to calculate the mapping directly to the original vectors if a view of
     * a view vector is created.
     * @param view the view vector, which the view should be created upon
     * @param mapping the mapping on the view vector
     * @return mapping directly to the original vector
     */
    static int @NotNull [] recalculateMappingToOriginal(@NotNull FloatN view, int @NotNull [] mapping) {
        int[] viewMapping = view.getMapping();
        int[] newMapping = new int[mapping.length];

        for(int i = 0; i < mapping.length; i++)
            newMapping[i] = viewMapping[mapping[i]];

        return newMapping;
    }

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

    /**
     * Whether this vector is only a view onto another vector.
     * @return {@code true} if this vector is a view.
     */
    boolean isView();

    /**
     *
     * @return the original vector this vector views to
     * @throws UnsupportedOperationException if this vector is not {@link #isView() a view on another vector}.
     */
    default @NotNull Vector getOriginal() {
        throw new UnsupportedOperationException("This vector is not buffer backed.");
    }

    /**
     * The returned mapping must always map to a non view vector.
     * @return the mapping to the original vector
     * @throws UnsupportedOperationException if this vector is not {@link #isView() a view on another vector}.
     */
    default int @NotNull [] getMapping() {
        throw new UnsupportedOperationException("This vector is not buffer backed.");
    }


}
