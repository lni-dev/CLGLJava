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

import de.linusdev.clgl.api.types.array.ABFloat4x4;
import de.linusdev.clgl.api.types.matrix.Float3x3;
import de.linusdev.clgl.api.types.matrix.Float4x4;
import de.linusdev.clgl.api.types.matrix.FloatNxM;
import de.linusdev.clgl.api.types.matrix.Matrix;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@SuppressWarnings({"unused", "UnusedReturnValue", "ForLoopReplaceableByForEach"})
public class VMath {

    /**
     * Checks if all {@link Vector}s in {@code vectors} have the same dimension.
     * @param vectors {@link Vector} array
     * @return {@code true} if the {@link Vector#getMemberCount()} is the same for all given {@code vectors}
     */
    private static boolean matchingDimensions(@NotNull Vector @NotNull ... vectors) {
        int dim = vectors[0].getMemberCount();

        for(int i = 1; i < vectors.length; i++) {
            if(vectors[i].getMemberCount() != dim) return false;
        }

        return true;
    }

    /**
     * Checks if all {@link Matrix matrices} in {@code vectors} have the same {@link Matrix#getWidth() width}
     * and {@link Matrix#getHeight() height}.
     * @param matrices {@link Matrix} array
     * @return {@code true} if the {@link Matrix#getWidth() width} and {@link Matrix#getHeight() height}
     * is the same for all given {@code vectors}
     */
    private static boolean matchingDimensions(@NotNull Matrix @NotNull ... matrices) {
        int width = matrices[0].getWidth();
        int height = matrices[0].getHeight();

        for(int i = 1; i < matrices.length; i++) {
            if(matrices[i].getWidth() != width || matrices[i].getHeight() != height)
                return false;
        }

        return true;
    }

    /**
     * Checks if no {@link Vector} in {@code vectors} is the same as {@code unique}.
     * Meaning, that no vector in {@code vectors} is the same as or a {@link Vector#isView() view} on {@code unique}.
     * @param vectors array of {@link Vector vectors}
     * @param unique vector which should be checked if it is unique
     * @return {@code true} if no vector in {@code vectors} is the same as or a {@link Vector#isView() view} on {@code unique}
     */
    private static boolean uniqueVector(@NotNull Vector unique, @NotNull Vector @NotNull ... vectors) {
        Vector original = unique.isView() ? unique.getOriginal() : unique;

        for(int i = 0; i < vectors.length; i++) {
            if(original == (vectors[i].isView() ? vectors[i].getOriginal() : vectors[i]))
                return false;
        }

        return true;
    }

    /**
     * Checks if no {@link Vector} in {@code vectors} is a {@link Vector#isView() view} on {@code unique}.
     * @param vectors array of {@link Vector vectors}
     * @param unique vector which should be checked if it is unique (view wise)
     * @return {@code true} if no vector in {@code vectors} is a {@link Vector#isView() view} on {@code unique}
     */
    private static boolean uniqueViewVector(@NotNull Vector unique, @NotNull Vector @NotNull ... vectors) {
        Vector original = unique.isView() ? unique.getOriginal() : unique;

        for(int i = 0; i < vectors.length; i++) {
            if(vectors[i].isView() && original == vectors[i].getOriginal())
                return false;
        }

        return true;
    }

    /**
     * Checks if no {@link Matrix} in {@code matrices} is the same as {@code unique}.
     * Meaning, that no matrix in {@code matrices} is the same as {@code unique}.
     * @param matrices array of {@link Matrix matrices}
     * @param unique matrix which should be checked if it is unique
     * @return {@code true} if no matrix in {@code matrices} is the same as {@code unique}
     */
    private static boolean uniqueMatrix(@NotNull Matrix unique, @NotNull Matrix @NotNull ... matrices) {
        for(int i = 0; i < matrices.length; i++) {
            if(unique == matrices[i])
                return false;
        }

        return true;
    }

    /**
     * Marks that no other parameter may be the same or a {@link Vector#isView() view} on this parameter.
     */
    @Documented
    @Retention(RetentionPolicy.CLASS)
    public @interface Unique {
    }

    /**
     * Marks that no other parameter may be a {@link Vector#isView() view} on this parameter. But they may
     * be the same parameter
     */
    @Documented
    @Retention(RetentionPolicy.CLASS)
    public @interface UniqueView {
    }

    public static <V extends FloatN> @NotNull V add(@NotNull V left, @NotNull V right, @UniqueView @NotNull V store) {
        assert matchingDimensions(left, right, store);
        assert uniqueViewVector(store, left, right);

        for(int i = 0; i < left.getMemberCount(); i++)
            store.put(i, left.get(i) + right.get(i));

        return store;
    }

    public static <V extends FloatN> @NotNull V subtract(@NotNull V left, @NotNull V right, @UniqueView @NotNull V store) {
        assert matchingDimensions(left, right, store);
        assert uniqueViewVector(store, left, right);

        for(int i = 0; i < left.getMemberCount(); i++)
            store.put(i, left.get(i) - right.get(i));

        return store;
    }

    public static <V extends FloatN> @NotNull V multiply(@NotNull V left, @NotNull V right, @UniqueView @NotNull V store) {
        assert matchingDimensions(left, right, store);
        assert uniqueViewVector(store, left, right);

        for(int i = 0; i < left.getMemberCount(); i++)
            store.put(i, left.get(i) * right.get(i));

        return store;
    }

    public static @NotNull Float4 multiply(@NotNull Float4 left, @NotNull Float4x4 right, @NotNull Float4 store) {
        float storeX, storeY, storeZ, storeW;

        storeX = left.get(0) * right.get(0, 0)
                + left.get(1) * right.get(1, 0)
                + left.get(2) * right.get(2, 0)
                + left.get(3) * right.get(3, 0);

        storeY = left.get(0) * right.get(0, 1)
                + left.get(1) * right.get(1, 1)
                + left.get(2) * right.get(2, 1)
                + left.get(3) * right.get(3, 1);

        storeZ = left.get(0) * right.get(0, 2)
                + left.get(1) * right.get(1, 2)
                + left.get(2) * right.get(2, 2)
                + left.get(3) * right.get(3, 2);

        storeW = left.get(0) * right.get(0, 3)
                + left.get(1) * right.get(1, 3)
                + left.get(2) * right.get(2, 3)
                + left.get(3) * right.get(3, 3);

        store.xyzw(storeX, storeY, storeZ, storeW);

        return store;
    }

    public static @NotNull Float3 multiply(@NotNull Float3 left, @NotNull Float3x3 right, @NotNull Float3 store) {
        float storeX, storeY, storeZ;

        storeX = left.get(0) * right.get(0, 0)
                + left.get(1) * right.get(1, 0)
                + left.get(2) * right.get(2, 0);

        storeY = left.get(0) * right.get(0, 1)
                + left.get(1) * right.get(1, 1)
                + left.get(2) * right.get(2, 1);

        storeZ = left.get(0) * right.get(0, 2)
                + left.get(1) * right.get(1, 2)
                + left.get(2) * right.get(2, 2);

        store.xyz(storeX, storeY, storeZ);

        return store;
    }

    public static <V extends FloatN> @NotNull V divide(@NotNull V left, @NotNull V right, @UniqueView @NotNull V store) {
        assert matchingDimensions(left, right, store);
        assert uniqueViewVector(store, left, right);

        for(int i = 0; i < left.getMemberCount(); i++)
            store.put(i, left.get(i) / right.get(i));

        return store;
    }

    public static <V extends FloatN> @NotNull V scale(@NotNull V toScale, float factor, @UniqueView @NotNull V store) {
        assert matchingDimensions(toScale, store);
        assert uniqueViewVector(store, toScale);

        for(int i = 0; i < toScale.getMemberCount(); i++)
            store.put(i, toScale.get(i) * factor);

        return store;
    }

    public static <M extends FloatNxM> @NotNull M scale(@NotNull M toScale, float factor, @NotNull M store) {
        assert matchingDimensions(toScale, store);

        for(int x = 0; x < toScale.getWidth(); x++) {
            for(int y = 0; y < toScale.getHeight(); y++) {
                store.put(x, y, toScale.get(x, y) * factor);
            }
        }

        return store;
    }

    public static float dot(@NotNull FloatN left, @NotNull FloatN right) {
        assert matchingDimensions(left, right);

        float dot = 0.0f;
        for(int i = 0; i < left.getMemberCount(); i++)
            dot += left.get(i) * right.get(i);

        return dot;
    }

    public static float determinant(@NotNull Float4x4 mat) {
        return 0
                + mat.get(0,0) * mat.get(1, 1) * mat.get(2, 2) * mat.get(3, 3)
                + mat.get(0, 0) * mat.get(1, 2) * mat.get(2, 3) * mat.get(3, 1)
                + mat.get(0, 0) * mat.get(1, 3) * mat.get(2, 1) * mat.get(3, 2) // end line 1
                - mat.get(0, 0) * mat.get(1, 3) * mat.get(2, 2) * mat.get(3, 1)
                - mat.get(0, 0) * mat.get(1, 2) * mat.get(2, 1) * mat.get(3, 3)
                - mat.get(0, 0) * mat.get(1, 1) * mat.get(2, 3) * mat.get(3, 2) // end line 2
                - mat.get(0, 1) * mat.get(1, 0) * mat.get(2, 2) * mat.get(3, 3)
                - mat.get(0, 2) * mat.get(1, 0) * mat.get(2, 3) * mat.get(3, 1)
                - mat.get(0, 3) * mat.get(1, 0) * mat.get(2, 1) * mat.get(3, 2) // end line 3
                + mat.get(0, 3) * mat.get(1, 0) * mat.get(2, 2) * mat.get(3, 1)
                + mat.get(0, 2) * mat.get(1, 0) * mat.get(2, 1) * mat.get(3, 3)
                + mat.get(0, 1) * mat.get(1, 0) * mat.get(2, 3) * mat.get(3, 2) // end line 4
                + mat.get(0, 1) * mat.get(1, 2) * mat.get(2, 0) * mat.get(3, 3)
                + mat.get(0, 2) * mat.get(1, 3) * mat.get(2, 0) * mat.get(3, 1)
                + mat.get(0, 3) * mat.get(1, 1) * mat.get(2, 0) * mat.get(3, 2) // end line 5
                - mat.get(0, 3) * mat.get(1, 2) * mat.get(2, 0) * mat.get(3, 1)
                - mat.get(0, 2) * mat.get(1, 1) * mat.get(2, 0) * mat.get(3, 3)
                - mat.get(0, 1) * mat.get(1, 3) * mat.get(2, 0) * mat.get(3, 2) // end line 6
                - mat.get(0, 1) * mat.get(1, 2) * mat.get(2, 3) * mat.get(3, 0)
                - mat.get(0, 2) * mat.get(1, 3) * mat.get(2, 1) * mat.get(3, 0)
                - mat.get(0, 3) * mat.get(1, 1) * mat.get(2, 2) * mat.get(3, 0) // end line 7
                + mat.get(0, 3) * mat.get(1, 2) * mat.get(2, 1) * mat.get(3, 0)
                + mat.get(0, 2) * mat.get(1, 1) * mat.get(2, 3) * mat.get(3, 0)
                + mat.get(0, 1) * mat.get(1, 3) * mat.get(2, 2) * mat.get(3, 0) // end line 8
                ;
    }

    public static @NotNull Float4x4 adjugate(@NotNull Float4x4 mat, @Unique @NotNull Float4x4 store) {
        assert uniqueMatrix(store, mat);
        // x = 0:

        store.put(0, 0, 0
                + mat.get(1, 1) * mat.get(2, 2) * mat.get(3, 3)
                + mat.get(1, 2) * mat.get(2, 3) * mat.get(3, 1)
                + mat.get(1, 3) * mat.get(2, 1) * mat.get(3, 2)
                - mat.get(1, 3) * mat.get(2, 2) * mat.get(3, 1)
                - mat.get(1, 2) * mat.get(2, 1) * mat.get(3, 3)
                - mat.get(1, 1) * mat.get(2, 3) * mat.get(3, 2)
        );

        store.put(1, 0, 0
                - mat.get(1, 0) * mat.get(2, 2) * mat.get(3, 3)
                - mat.get(1, 2) * mat.get(2, 3) * mat.get(3, 0)
                - mat.get(1, 3) * mat.get(2, 0) * mat.get(3, 2)
                + mat.get(1, 3) * mat.get(2, 2) * mat.get(3, 0)
                + mat.get(1, 2) * mat.get(2, 0) * mat.get(3, 3)
                + mat.get(1, 0) * mat.get(2, 3) * mat.get(3, 2)
        );

        store.put(2, 0, 0
                + mat.get(1, 0) * mat.get(2, 1) * mat.get(3, 3)
                + mat.get(1, 1) * mat.get(2, 3) * mat.get(3, 0)
                + mat.get(1, 3) * mat.get(2, 0) * mat.get(3, 1)
                - mat.get(1, 3) * mat.get(2, 1) * mat.get(3, 0)
                - mat.get(1, 1) * mat.get(2, 0) * mat.get(3, 3)
                - mat.get(1, 0) * mat.get(2, 3) * mat.get(3, 1)
        );

        store.put(3, 0, 0
                - mat.get(1, 0) * mat.get(2, 1) * mat.get(3, 2)
                - mat.get(1, 1) * mat.get(2, 2) * mat.get(3, 0)
                - mat.get(1, 2) * mat.get(2, 0) * mat.get(3, 1)
                + mat.get(1, 2) * mat.get(2, 1) * mat.get(3, 0)
                + mat.get(1, 1) * mat.get(2, 0) * mat.get(3, 2)
                + mat.get(1, 0) * mat.get(2, 2) * mat.get(3, 1)
        );

        // x = 1:

        store.put(0, 1, 0
                - mat.get(0, 1) * mat.get(2, 2) * mat.get(3, 3)
                - mat.get(0, 2) * mat.get(2, 3) * mat.get(3, 1)
                - mat.get(0, 3) * mat.get(2, 1) * mat.get(3, 2)
                + mat.get(0, 3) * mat.get(2, 2) * mat.get(3, 1)
                + mat.get(0, 2) * mat.get(2, 1) * mat.get(3, 3)
                + mat.get(0, 1) * mat.get(2, 3) * mat.get(3, 2)
        );

        store.put(1, 1, 0
                + mat.get(0, 0) * mat.get(2, 2) * mat.get(3, 3)
                + mat.get(0, 2) * mat.get(2, 3) * mat.get(3, 0)
                + mat.get(0, 3) * mat.get(2, 0) * mat.get(3, 2)
                - mat.get(0, 3) * mat.get(2, 2) * mat.get(3, 0)
                - mat.get(0, 2) * mat.get(2, 0) * mat.get(3, 3)
                - mat.get(0, 0) * mat.get(2, 3) * mat.get(3, 2)
        );

        store.put(2, 1, 0
                - mat.get(0, 0) * mat.get(2, 1) * mat.get(3, 3)
                - mat.get(0, 1) * mat.get(2, 3) * mat.get(3, 0)
                - mat.get(0, 3) * mat.get(2, 0) * mat.get(3, 1)
                + mat.get(0, 3) * mat.get(2, 1) * mat.get(3, 0)
                + mat.get(0, 1) * mat.get(2, 0) * mat.get(3, 3)
                + mat.get(0, 0) * mat.get(2, 3) * mat.get(3, 1)
        );

        store.put(3, 1, 0
                + mat.get(0, 0) * mat.get(2, 1) * mat.get(3, 2)
                + mat.get(0, 1) * mat.get(2, 2) * mat.get(3, 0)
                + mat.get(0, 2) * mat.get(2, 0) * mat.get(3, 1)
                - mat.get(0, 2) * mat.get(2, 1) * mat.get(3, 0)
                - mat.get(0, 1) * mat.get(2, 0) * mat.get(3, 2)
                - mat.get(0, 0) * mat.get(2, 2) * mat.get(3, 1)
        );

        // x = 2:

        store.put(0, 2, 0
                + mat.get(0, 1) * mat.get(1, 2) * mat.get(3, 3)
                + mat.get(0, 2) * mat.get(1, 3) * mat.get(3, 1)
                + mat.get(0, 3) * mat.get(1, 1) * mat.get(3, 2)
                - mat.get(0, 3) * mat.get(1, 2) * mat.get(3, 1)
                - mat.get(0, 2) * mat.get(1, 1) * mat.get(3, 3)
                - mat.get(0, 1) * mat.get(1, 3) * mat.get(3, 2)
        );

        store.put(1, 2, 0
                - mat.get(0, 0) * mat.get(1, 2) * mat.get(3, 3)
                - mat.get(0, 2) * mat.get(1, 3) * mat.get(3, 0)
                - mat.get(0, 3) * mat.get(1, 0) * mat.get(3, 2)
                + mat.get(0, 3) * mat.get(1, 2) * mat.get(3, 0)
                + mat.get(0, 2) * mat.get(1, 0) * mat.get(3, 3)
                + mat.get(0, 0) * mat.get(1, 3) * mat.get(3, 2)
        );

        store.put(2, 2, 0
                + mat.get(0, 0) * mat.get(1, 1) * mat.get(3, 3)
                + mat.get(0, 1) * mat.get(1, 3) * mat.get(3, 0)
                + mat.get(0, 3) * mat.get(1, 0) * mat.get(3, 1)
                - mat.get(0, 3) * mat.get(1, 1) * mat.get(3, 0)
                - mat.get(0, 1) * mat.get(1, 0) * mat.get(3, 3)
                - mat.get(0, 0) * mat.get(1, 3) * mat.get(3, 1)
        );

        store.put(3, 2, 0
                - mat.get(0, 0) * mat.get(1, 1) * mat.get(3, 2)
                - mat.get(0, 1) * mat.get(1, 2) * mat.get(3, 0)
                - mat.get(0, 2) * mat.get(1, 0) * mat.get(3, 1)
                + mat.get(0, 2) * mat.get(1, 1) * mat.get(3, 0)
                + mat.get(0, 1) * mat.get(1, 0) * mat.get(3, 2)
                + mat.get(0, 0) * mat.get(1, 2) * mat.get(3, 1)
        );

        // x = 3:

        store.put(0, 3, 0
                - mat.get(0, 1) * mat.get(1, 2) * mat.get(2, 3)
                - mat.get(0, 2) * mat.get(1, 3) * mat.get(2, 1)
                - mat.get(0, 3) * mat.get(1, 1) * mat.get(2, 2)
                + mat.get(0, 3) * mat.get(1, 2) * mat.get(2, 1)
                + mat.get(0, 2) * mat.get(1, 1) * mat.get(2, 3)
                + mat.get(0, 1) * mat.get(1, 3) * mat.get(2, 2)
        );

        store.put(1, 3, 0
                + mat.get(0, 0) * mat.get(1, 2) * mat.get(2, 3)
                + mat.get(0, 2) * mat.get(1, 3) * mat.get(2, 0)
                + mat.get(0, 3) * mat.get(1, 0) * mat.get(2, 2)
                - mat.get(0, 3) * mat.get(1, 2) * mat.get(2, 0)
                - mat.get(0, 2) * mat.get(1, 0) * mat.get(2, 3)
                - mat.get(0, 0) * mat.get(1, 3) * mat.get(2, 2)
        );

        store.put(2, 3, 0
                - mat.get(0, 0) * mat.get(1, 1) * mat.get(2, 3)
                - mat.get(0, 1) * mat.get(1, 3) * mat.get(2, 0)
                - mat.get(0, 3) * mat.get(1, 0) * mat.get(2, 1)
                + mat.get(0, 3) * mat.get(1, 1) * mat.get(2, 0)
                + mat.get(0, 1) * mat.get(1, 0) * mat.get(2, 3)
                + mat.get(0, 0) * mat.get(1, 3) * mat.get(2, 1)
        );

        store.put(3, 3, 0
                + mat.get(0, 0) * mat.get(1, 1) * mat.get(2, 2)
                + mat.get(0, 1) * mat.get(1, 2) * mat.get(2, 0)
                + mat.get(0, 2) * mat.get(1, 0) * mat.get(2, 1)
                - mat.get(0, 2) * mat.get(1, 1) * mat.get(2, 0)
                - mat.get(0, 1) * mat.get(1, 0) * mat.get(2, 2)
                - mat.get(0, 0) * mat.get(1, 2) * mat.get(2, 1)
        );

        return store;
    }

    public static @NotNull Float4x4 inverse(@NotNull Float4x4 mat, @NotNull Float4x4 store) {
        Float4x4 buf = VMath.adjugate(mat, new ABFloat4x4());

        return VMath.scale(buf, 1 / VMath.determinant(mat), store);
    }

    public static @NotNull Float3 cross(@NotNull Float3 left, @NotNull Float3 right, @NotNull Float3 store) {
        float storeX, storeY, storeZ;

        storeX = (left.get(1) * right.get(2)) - (left.get(2) * right.get(1));
        storeY = (left.get(2) * right.get(0)) - (left.get(0) * right.get(2));
        storeZ = (left.get(0) * right.get(1)) - (left.get(1) * right.get(0));

        store.xyz(storeX, storeY, storeZ);
        return store;
    }

    public static <V extends FloatN> @NotNull V normalize(@NotNull V toNormalize, @UniqueView @NotNull V store) {
        assert matchingDimensions(toNormalize, store);
        assert uniqueViewVector(store, toNormalize);

        float length = 0;
        for(int i = 0; i < toNormalize.getMemberCount(); i++)
            length += toNormalize.get(i) * toNormalize.get(i);

        length = (float) Math.sqrt(length);

        for(int i = 0; i < toNormalize.getMemberCount(); i++)
            store.put(i, toNormalize.get(i) / length);

        return store;
    }


}
