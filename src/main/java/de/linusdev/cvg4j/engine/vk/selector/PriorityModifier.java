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

package de.linusdev.cvg4j.engine.vk.selector;

import de.linusdev.cvg4j.engine.vk.device.GPUInfo;
import de.linusdev.cvg4j.engine.vk.selector.priority.Priority;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;
import java.util.function.Predicate;

public interface PriorityModifier {


    @NotNull PriorityModifierType type();

    int apply(int current, @NotNull GPUInfo info);



    record Impl(
            @NotNull PriorityModifierType type,
            @NotNull Priority modifier,
            @NotNull Predicate<GPUInfo> tester
    ) implements PriorityModifier {

        @Override
        public int apply(int current, @NotNull GPUInfo info) {
            if(!tester.test(info))
                return current;

            return type().apply(current, modifier().priority());
        }

    }

    class VariableImpl implements PriorityModifier {
        private final @NotNull PriorityModifierType type;
        private final @NotNull Function<GPUInfo, Priority> tester;

        public VariableImpl(@NotNull PriorityModifierType type, @NotNull Function<GPUInfo, Priority> tester) {
            this.type = type;
            this.tester = tester;
        }

        @Override
        public @NotNull PriorityModifierType type() {
            return type;
        }

        @Override
        public int apply(int current, @NotNull GPUInfo info) {
            return type.apply(current, tester.apply(info).priority());
        }
    }

}
