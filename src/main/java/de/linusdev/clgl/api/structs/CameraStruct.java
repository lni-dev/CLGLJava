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


import de.linusdev.clgl.api.types.bytebuffer.BBFloat1;
import de.linusdev.clgl.api.types.bytebuffer.BBFloat3;
import org.jetbrains.annotations.NotNull;

public class CameraStruct extends ComplexStructure {

    public static final StructureInfo INFO = new StructureInfo(
            BBFloat3.INFO,
            BBFloat3.INFO,
            BBFloat1.INFO);

    public final @NotNull BBFloat3 position = new BBFloat3(false);
    public final @NotNull BBFloat3 lookAtVector = new BBFloat3(false);
    public final @NotNull BBFloat1 distanceToScreen = new BBFloat1(false);

    public CameraStruct(boolean allocateBuffer) {
        super(true);
        init(allocateBuffer, position, lookAtVector, distanceToScreen);
    }

    @Override
    protected @NotNull StructureInfo getInfo() {
        return INFO;
    }

    @Override
    public String toString() {
        return toString(position, lookAtVector, distanceToScreen);
    }
}
