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

package de.linusdev.ljgel.engine.vk.selector.swapchain;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

public interface SwapChainImageCountSelector {

    public static final @NotNull SwapChainImageCountSelector DEFAULT = (min, max) -> Math.min(min + 1, max);

    /**
     * How many images the swap chain should contain. By default, returns {@code min(min + 1, max)}, because
     * it is good to require an additional image, so we can write to an image while the graphics card driver uses the other
     * images.
     * @param min min image count
     * @param max max image count, may be very high
     * @return value between min and max
     */
    int select(@Range(from = 1, to = Integer.MAX_VALUE) int min, @Range(from = 1, to = Integer.MAX_VALUE) int max);

}
