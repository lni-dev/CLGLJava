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

package de.linusdev.clgl.api.types.bytebuffer;

import de.linusdev.clgl.api.structs.Structure;
import de.linusdev.clgl.api.structs.StructureInfo;
import de.linusdev.clgl.api.types.bytebuffer.utils.BBMatInfo;
import de.linusdev.clgl.api.types.matrix.Float3x3;
import org.jetbrains.annotations.NotNull;

public class BBFloat4x4 extends BBFloatNxM<BBFloat4> implements Float3x3 {

    public static final BBMatInfo<BBFloat4> MAT_INFO = new BBMatInfo<>(
            BBFloat4.class,
            BBFloat4.INFO,
            () -> new BBFloat4(false),
            4
    );

    public static final @NotNull StructureInfo INFO = MAT_INFO.getInfo();

    public BBFloat4x4(@NotNull Structure mostParentStructure, int offset) {
        super(mostParentStructure, offset, MAT_INFO);
    }

    public BBFloat4x4(boolean allocateBuffer) {
        super(allocateBuffer, MAT_INFO);
    }
}
