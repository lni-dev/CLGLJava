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

package de.linusdev.clgl.api.types.matrix;

import org.jetbrains.annotations.NotNull;

public interface Matrix {

    static @NotNull String toString(@NotNull String name, @NotNull FloatNxM matrix) {
        StringBuilder sb = new StringBuilder();

        sb
                .append(name)
                .append(matrix.getWidth())
                .append("x")
                .append(matrix.getHeight())
                .append(":\n[\n");

        for(int y = 0; y < matrix.getHeight(); y++) {
            for(int x = 0; x < matrix.getWidth(); x++) {
                sb.append(String.format(" % 10.2f ", matrix.get(x, y)));
            }
            if(y != matrix.getHeight()-1)
                sb.append("\n");
        }

        sb.append("\n]");

        return sb.toString();
    }

    /**
     * For a NxM Matrix, return N.
     * @return width of the matrix
     */
    int getWidth();

    /**
     * For a NxM Matrix, return M.
     * @return height of the matrix
     */
    int getHeight();

    /**
     * Whether this matrix is array backed.
     * @return {@code true} if this matrix is array backed.
     */
    boolean isArrayBacked();

    /**
     * Whether this vector is buffer backed.
     * @return {@code true} if this vector is buffer backed.
     */
    boolean isBufferBacked();

}
