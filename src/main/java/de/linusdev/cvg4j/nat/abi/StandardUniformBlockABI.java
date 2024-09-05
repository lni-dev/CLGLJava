/*
 * Copyright (c) 2024 Linus Andera
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

package de.linusdev.cvg4j.nat.abi;

import de.linusdev.lutils.nat.abi.ABI;
import de.linusdev.lutils.nat.abi.OverwriteChildABI;
import de.linusdev.lutils.nat.struct.annos.StructureLayoutSettings;
import org.jetbrains.annotations.NotNull;

@StructureLayoutSettings(
        selectorMethodClass = StandardUniformBlockABI.class,
        selectorMethodName = "get",
        overwriteChildrenABI = OverwriteChildABI.FORCE_OVERWRITE)
public class StandardUniformBlockABI {

    public static @NotNull ABI get() {
        return OpenGLABIs.STANDARD_UNIFORM_BLOCK_LAYOUT;
    }

}