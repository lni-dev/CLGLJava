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

package de.linusdev.cvg4j.engine.vk.device;

import de.linusdev.cvg4j.nat.vulkan.structs.VkExtent2D;
import de.linusdev.lutils.math.vector.buffer.intn.BBUInt2;
import org.jetbrains.annotations.NotNull;

public class Extend2D extends BBUInt2 {

    private final @NotNull VkExtent2D backingExtend;

    public Extend2D(@NotNull VkExtent2D backingExtend) {
        super(true, null);
        this.backingExtend = backingExtend;
        unionWith(this, backingExtend);
    }

    public @NotNull VkExtent2D geVkExtend2D() {
        return backingExtend;
    }

    public int width() {
        return x();
    }

    public int height() {
        return y();
    }
}
