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

package de.linusdev.cvg4j.engine.vk.memory.image.sampler;

import de.linusdev.cvg4j.engine.vk.device.Device;
import de.linusdev.cvg4j.engine.vk.memory.buffer.BufferStructInput;
import de.linusdev.cvg4j.engine.vk.memory.image.ImageOutput;
import de.linusdev.cvg4j.nat.vulkan.bitmasks.enums.VkImageAspectFlagBits;
import de.linusdev.cvg4j.nat.vulkan.enums.VkImageLayout;
import de.linusdev.cvg4j.nat.vulkan.handles.VkCommandBuffer;
import de.linusdev.cvg4j.nat.vulkan.handles.VkInstance;
import de.linusdev.cvg4j.nat.vulkan.structs.VkBufferImageCopy;
import de.linusdev.lutils.image.ImageSize;
import de.linusdev.lutils.nat.memory.Stack;
import de.linusdev.lutils.nat.struct.abstracts.Structure;
import org.jetbrains.annotations.NotNull;

import static de.linusdev.lutils.nat.pointer.TypedPointer64.ref;

public class Sampler2D<S extends Structure> {

    private final @NotNull VkInstance vkInstance;
    private final @NotNull Device device;

    private final @NotNull BufferStructInput<S> input;
    private final @NotNull ImageOutput output;

    private final @NotNull ImageSize imageSize;

    public Sampler2D(
            @NotNull VkInstance vkInstance, @NotNull Device device,
            @NotNull BufferStructInput<S> input, @NotNull ImageOutput output,
            @NotNull ImageSize imageSize
    ) {
        this.vkInstance = vkInstance;
        this.device = device;
        this.input = input;
        this.output = output;
        this.imageSize = imageSize;

    }

    public void bufferCopyCommand(@NotNull Stack stack, @NotNull VkCommandBuffer vkCommandBuffer) {

        VkBufferImageCopy region = stack.push(new VkBufferImageCopy());
        region.bufferOffset.set(0);
        region.bufferRowLength.set(0);
        region.bufferImageHeight.set(0);

        region.imageSubresource.aspectMask.set(VkImageAspectFlagBits.VK_IMAGE_ASPECT_COLOR_BIT);
        region.imageSubresource.mipLevel.set(0);
        region.imageSubresource.baseArrayLayer.set(0);
        region.imageSubresource.layerCount.set(1);

        region.imageOffset.x.set(0);
        region.imageOffset.y.set(0);
        region.imageOffset.z.set(0);
        region.imageExtent.width.set(imageSize.getWidth());
        region.imageExtent.height.set(imageSize.getHeight());
        region.imageExtent.depth.set(1);

        vkInstance.vkCmdCopyBufferToImage(
                vkCommandBuffer,
                input.getVkBuffer(),
                output.getVkImage(),
                VkImageLayout.TRANSFER_DST_OPTIMAL,
                1,
                ref(region)
        );

        stack.pop(); // region

    }

    public @NotNull BufferStructInput<S> getInput() {
        return input;
    }

    public @NotNull ImageOutput getOutput() {
        return output;
    }
}
