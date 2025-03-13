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

package de.linusdev.ljgel.engine.vk.memory.manager;

import java.util.Objects;

public final class MemoryRequirementsChange {
    public long oldOffset;
    public long oldRequiredSize;
    public long oldRequiredAlignment;
    public long newRequiredSize;
    public long newRequiredAlignment;

    public MemoryRequirementsChange(
            long oldOffset,
            long oldRequiredSize,
            long oldRequiredAlignment
    ) {
        this.oldOffset = oldOffset;
        this.oldRequiredSize = oldRequiredSize;
        this.oldRequiredAlignment = oldRequiredAlignment;
        this.newRequiredSize = newRequiredSize;
        this.newRequiredAlignment = newRequiredAlignment;
    }

    public long oldOffset() {
        return oldOffset;
    }

    public long oldRequiredSize() {
        return oldRequiredSize;
    }

    public long oldRequiredAlignment() {
        return oldRequiredAlignment;
    }

    public long newRequiredSize() {
        return newRequiredSize;
    }

    public long newRequiredAlignment() {
        return newRequiredAlignment;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (MemoryRequirementsChange) obj;
        return this.oldOffset == that.oldOffset &&
                this.oldRequiredSize == that.oldRequiredSize &&
                this.oldRequiredAlignment == that.oldRequiredAlignment &&
                this.newRequiredSize == that.newRequiredSize &&
                this.newRequiredAlignment == that.newRequiredAlignment;
    }

    @Override
    public int hashCode() {
        return Objects.hash(oldOffset, oldRequiredSize, oldRequiredAlignment, newRequiredSize, newRequiredAlignment);
    }

    @Override
    public String toString() {
        return "MemoryRequirementsChange[" +
                "oldOffset=" + oldOffset + ", " +
                "oldRequiredSize=" + oldRequiredSize + ", " +
                "oldRequiredAlignment=" + oldRequiredAlignment + ", " +
                "newRequiredSize=" + newRequiredSize + ", " +
                "newRequiredAlignment=" + newRequiredAlignment + ']';
    }
}
