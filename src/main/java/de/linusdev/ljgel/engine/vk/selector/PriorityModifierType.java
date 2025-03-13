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

package de.linusdev.ljgel.engine.vk.selector;

public enum PriorityModifierType {

    ADD {
        @Override
        public int apply(int current, int arg) {
            return current + arg;
        }
    },
    SUBTRACT {
        @Override
        public int apply(int current, int arg) {
            return current - arg;
        }
    },
    MULTIPLY {
        @Override
        public int apply(int current, int arg) {
            return current * arg;
        }
    },
    MIN {
        @Override
        public int apply(int current, int arg) {
            return Math.max(current, arg);
        }
    },

    ;

    public abstract int apply(int current, int arg);
}
