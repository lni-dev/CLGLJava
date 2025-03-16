/*
 * Copyright (c) 2024-2025 Linus Andera
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

package de.linusdev.ljgel.build;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum CPPBuildType {
    MSVC_DEBUG(
            "msvc",
            "build/cmake/msvc",
            true, "Debug",
            "Debug",
            "", "dll"
    ),
    MSVC_RELEASE(
            "msvc",
            "build/cmake/msvc",
            true, "Release",
            "Release",
            "", "dll"
    ),
    GCC_DEBUG(
            "gcc-debug",
            "build/cmake/gcc-debug",
            false, null,
            "",
            "lib", "so"
    ),
    GCC_RELEASE(
            "gcc-release",
            "build/cmake/gcc-release",
            false, null,
            "",
            "lib", "so"
    ),
    ;

    /**
     * The name of the cmake preset in 'CMakePresets.json'.
     */
    public final @NotNull String cmakePresetName;
    /**
     * Build-directory relative to repository-root.
     */
    public final @NotNull String buildDir;
    /**
     * Whether the 'cmake --build' requires a '--config' param to switch between debug and release mode.
     */
    public final boolean requiresConfigParam;
    /**
     * The '--config' param or {@code null} if {@link #requiresConfigParam} is false.
     */
    public final @Nullable String configParam;
    /**
     * Directory that contains the library binaries (e.g. dll or so files) relative to
     * {@link #buildDir}. Empty string ("") if it is the same as {@link #buildDir}.
     */
    public final @NotNull String libFileDir;
    /**
     * File prefix of the library binaries (e.g. "lib" or "").
     */
    public final @NotNull String libFilePrefix;
    /**
     * File ending of the library binaries (e.g. dll or so).
     */
    public final @NotNull String libFileEnding;


    CPPBuildType(
            @NotNull String cmakePresetName, @NotNull String buildDir,
            boolean requiresConfigParam,
            @Nullable String configParam,
            @NotNull String libFileDir,
            @NotNull String libFilePrefix,
            @NotNull String libFileEnding
    ) {
        this.cmakePresetName = cmakePresetName;
        this.buildDir = buildDir;
        this.requiresConfigParam = requiresConfigParam;
        this.configParam = configParam;
        this.libFileDir = libFileDir;
        this.libFilePrefix = libFilePrefix;
        this.libFileEnding = libFileEnding;
    }
}
