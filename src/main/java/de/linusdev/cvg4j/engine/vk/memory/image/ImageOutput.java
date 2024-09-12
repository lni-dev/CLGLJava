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

package de.linusdev.cvg4j.engine.vk.memory.image;

import de.linusdev.cvg4j.engine.vk.memory.manager.objects.image.VulkanSamplerImage;
import de.linusdev.cvg4j.nat.vulkan.handles.VkImage;
import org.jetbrains.annotations.NotNull;

public class ImageOutput {

    private final @NotNull VulkanSamplerImage image;

    public ImageOutput(@NotNull VulkanSamplerImage image) {
        this.image = image;
    }

    public @NotNull VulkanSamplerImage getImage() {
        return image;
    }

    public @NotNull VkImage getVkImage() {
        return image.getVkImage();
    }

}
