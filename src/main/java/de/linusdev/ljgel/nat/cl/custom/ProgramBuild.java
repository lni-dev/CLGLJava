/*
 * Copyright (c) 2023-2025 Linus Andera
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

package de.linusdev.ljgel.nat.cl.custom;

import de.linusdev.ljgel.nat.cl.objects.Device;
import de.linusdev.ljgel.nat.cl.objects.Program;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public class ProgramBuild {

    private final @NotNull Program program;
    private final @NotNull Device @NotNull [] devices;

    public ProgramBuild(@NotNull Program program, @NotNull Device @NotNull [] devices) {
        this.program = program;
        this.devices = devices;
    }

    public @NotNull Device @NotNull [] getDevices() {
        return devices;
    }

    public @NotNull Program getProgram() {
        return program;
    }
}
