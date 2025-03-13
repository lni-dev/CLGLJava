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

package de.linusdev.ljgel.engine.vk.selector.queue.family;

import de.linusdev.ljgel.engine.vk.selector.Selector;
import de.linusdev.ljgel.engine.vk.selector.priority.Priority;
import de.linusdev.lutils.result.BiResult;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface QueueFamilySelector extends Selector {

    public static @NotNull BasicQueueFamilySelector.Builder builder() {
        return new BasicQueueFamilySelector.Builder();
    }

    @NotNull BiResult<QueueFamilyInfo, Priority> selectGraphicsQueue(
            @NotNull List<QueueFamilyInfo> queueFamilyInfoList
    );

    @NotNull BiResult<QueueFamilyInfo, Priority> selectPresentationQueue(
            @NotNull List<QueueFamilyInfo> queueFamilyInfoList
    );

}
