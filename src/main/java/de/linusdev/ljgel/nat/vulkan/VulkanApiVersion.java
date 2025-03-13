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

package de.linusdev.ljgel.nat.vulkan;

import de.linusdev.lutils.version.SimpleVersion;

import static de.linusdev.ljgel.nat.vulkan.utils.VulkanApiVersionUtils.VULKAN_API_VARIANT;
import static de.linusdev.ljgel.nat.vulkan.utils.VulkanApiVersionUtils.makeApiVersion;

/**
 * Vulkan Api Versions:
 * <ul>
 *     <li>{@link #V_1_0_0}</li>
 *     <li>{@link #V_1_1_0}</li>
 *     <li>{@link #V_1_2_0}</li>
 *     <li>{@link #V_1_3_0}</li>
 * </ul>
 */
@SuppressWarnings("unused")
public class VulkanApiVersion implements SimpleVersion {

    public static VulkanApiVersion V_1_0_0 = new VulkanApiVersion(1, 0, 0);
    public static VulkanApiVersion V_1_1_0 = new VulkanApiVersion(1, 1, 0);
    public static VulkanApiVersion V_1_2_0 = new VulkanApiVersion(1, 2, 0);
    public static VulkanApiVersion V_1_3_0 = new VulkanApiVersion(1, 3, 0);

    private final int version;
    private final int major;
    private final int minor;
    private final int patch;

    private VulkanApiVersion(int major, int minor, int patch) {
        this.version = makeApiVersion(VULKAN_API_VARIANT, major, minor, patch);
        this.major = major;
        this.minor = minor;
        this.patch = patch;
    }

    public int getAsInt() {
        return version;
    }

    @Override
    public int major() {
        return major;
    }

    @Override
    public int minor() {
        return minor;
    }

    @Override
    public int patch() {
        return patch;
    }

    @Override
    public String toString() {
        return getAsUserFriendlyString();
    }
}
