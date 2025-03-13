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

package de.linusdev.ljgel.engine.vk.selector.priority;

public enum Priorities implements Priority {

    HIGH(50),
    MEDIUM(30),
    LOW(10),
    VERY_LOW(2),
    DO_NOT_CARE(0),
    UNSUPPORTED(-1)

    ;

    private final int priority;

    Priorities(int priority) {
        this.priority = priority;
    }

    @Override
    public int priority() {
        return priority;
    }
}
