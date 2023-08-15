package de.linusdev.clgl.api.types.array;

import de.linusdev.clgl.api.types.array.utils.ABMatInfo;
import de.linusdev.clgl.api.types.matrix.Float3x3;
import org.jetbrains.annotations.NotNull;

public class ABFloat3x3 extends ABFloatNxM<ABFloat3> implements Float3x3 {

    public static final @NotNull ABMatInfo<ABFloat3> MAT_INFO = new ABMatInfo<>(
            ABFloat3.class,
            3,
            (array, length) -> new ABFloat3((float[]) array, length),
            3
    );

    public ABFloat3x3() {
        super(MAT_INFO);
    }
}
