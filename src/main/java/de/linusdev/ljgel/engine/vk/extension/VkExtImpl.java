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

package de.linusdev.ljgel.engine.vk.extension;

import org.jetbrains.annotations.NotNull;

public class VkExtImpl implements VulkanExtension {

    private final @NotNull String name;
    private final int version;

    public VkExtImpl(@NotNull String name, int version) {
        this.name = name;
        this.version = version;
    }

    @Override
    public @NotNull String extensionName() {
        return name;
    }

    @Override
    public int version() {
        return version;
    }

    @Override
    public String toString() {
        return VulkanExtension.toString(this);
    }
}
