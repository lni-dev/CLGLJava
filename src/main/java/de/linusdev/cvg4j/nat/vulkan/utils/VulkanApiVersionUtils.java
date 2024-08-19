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

package de.linusdev.cvg4j.nat.vulkan.utils;

import de.linusdev.cvg4j.nat.vulkan.VulkanApiVersion;
import de.linusdev.lutils.version.SimpleVersion;
import org.jetbrains.annotations.NotNull;

/**
 * For common vulkan api versions, see {@link VulkanApiVersion}
 */
public interface VulkanApiVersionUtils {

    static int makeApiVersion(int variant, int major, int minor, int patch) {
        return (variant << 29) | (major << 22) | (minor << 12) | patch;
    }

    @SuppressWarnings("unused")
    static int getVariantOfApiVersion(int version) {
        return version >>> 29;
    }

    static int getMajorOfApiVersion(int version) {
        return (version >>> 22) & 0x7F;
    }

    static int getMinorOfApiVersion(int version) {
        return (version >>> 12) & 0x3FF;
    }

    static int getPatchOfApiVersion(int version) {
        return version & 0xFFF;
    }

    /**
     * {@link #getVariantOfApiVersion(int) variant} will be discarded.
     * @param apiVersion vulkan api version as int
     * @return {@link SimpleVersion} representing given {@code apiVersion} as described above.
     */
    static @NotNull SimpleVersion toSimpleVersion(int apiVersion) {
        return SimpleVersion.of(
                getMajorOfApiVersion(apiVersion),
                getMinorOfApiVersion(apiVersion),
                getPatchOfApiVersion(apiVersion)
        );
    }

    int VULKAN_API_VARIANT = 0;

}
