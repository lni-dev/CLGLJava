package de.linusdev.clgl.api.types.array;

import de.linusdev.clgl.api.types.array.utils.ABMatInfo;
import de.linusdev.clgl.api.types.matrix.Float4x4;
import org.jetbrains.annotations.NotNull;

public class ABFloat4x4 extends ABFloatNxM<ABFloat4> implements Float4x4 {

    public static final @NotNull ABMatInfo<ABFloat4> MAT_INFO = new ABMatInfo<>(
      ABFloat4.class,
      4,
      (array, length) -> new ABFloat4((float[]) array, length),
      4
    );

    public ABFloat4x4() {
        super(MAT_INFO);
    }
}
