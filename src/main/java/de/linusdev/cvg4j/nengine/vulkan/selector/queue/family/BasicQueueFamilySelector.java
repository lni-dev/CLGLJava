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

package de.linusdev.cvg4j.nengine.vulkan.selector.queue.family;

import de.linusdev.cvg4j.nengine.vulkan.selector.priority.Priority;
import de.linusdev.lutils.result.BiResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

class BasicQueueFamilySelector implements QueueFamilySelector {

    private final @NotNull Function<QueueFamilyInfo, Priority> graphicsQueueEvaluator;
    private final @NotNull Function<QueueFamilyInfo, Priority> presentationQueueEvaluator;

    BasicQueueFamilySelector(
            @NotNull Function<QueueFamilyInfo, Priority> graphicsQueueEvaluator,
            @NotNull Function<QueueFamilyInfo, Priority> presentationQueueEvaluator
    ) {
        this.graphicsQueueEvaluator = graphicsQueueEvaluator;
        this.presentationQueueEvaluator = presentationQueueEvaluator;
    }


    @Override
    public @NotNull BiResult<QueueFamilyInfo, Priority> selectGraphicsQueue(@NotNull List<QueueFamilyInfo> queueFamilyInfoList) {
        int bestPriority = NOTHING_SELECTED_PRIORITY;
        QueueFamilyInfo best = null;

        for (QueueFamilyInfo info : queueFamilyInfoList) {
            int priority = graphicsQueueEvaluator.apply(info).priority();
            if(priority > bestPriority) {
                bestPriority = priority;
                best = info;
            }
        }

        return new BiResult<>(best, Priority.of(bestPriority));
    }

    @Override
    public @NotNull BiResult<QueueFamilyInfo, Priority> selectPresentationQueue(@NotNull List<QueueFamilyInfo> queueFamilyInfoList) {
        int bestPriority = NOTHING_SELECTED_PRIORITY;
        QueueFamilyInfo best = null;

        for (QueueFamilyInfo info : queueFamilyInfoList) {
            int priority = presentationQueueEvaluator.apply(info).priority();
            if(priority > bestPriority) {
                bestPriority = priority;
                best = info;
            }
        }

        return new BiResult<>(best, Priority.of(bestPriority));
    }

    public static class Builder {
        private @Nullable Function<QueueFamilyInfo, Priority> graphicsQueueEvaluator;
        private @Nullable Function<QueueFamilyInfo, Priority> presentationQueueEvaluator;

        Builder(){ }

        public Builder setGraphicsQueueEvaluator(@Nullable Function<QueueFamilyInfo, Priority> graphicsQueueEvaluator) {
            this.graphicsQueueEvaluator = graphicsQueueEvaluator;
            return this;
        }

        public Builder setPresentationQueueEvaluator(@Nullable Function<QueueFamilyInfo, Priority> presentationQueueEvaluator) {
            this.presentationQueueEvaluator = presentationQueueEvaluator;
            return this;
        }

        public @NotNull QueueFamilySelector build() {
            return new BasicQueueFamilySelector(
                    Objects.requireNonNull(graphicsQueueEvaluator),
                    Objects.requireNonNull(presentationQueueEvaluator)
            );
        }
    }
}
