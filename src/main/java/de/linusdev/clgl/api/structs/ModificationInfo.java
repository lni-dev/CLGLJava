/*
 * Copyright (c) 2023 Linus Andera
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

package de.linusdev.clgl.api.structs;

import org.jetbrains.annotations.Nullable;

public class ModificationInfo {
    public volatile int startOffset;
    public volatile int endOffset;

    public volatile ModificationInfo next = null;
    public volatile ModificationInfo previous = null;

    public ModificationInfo(int startOffset, int endOffset) {
        this.startOffset = startOffset;
        this.endOffset = endOffset;
    }

    @SuppressWarnings("NonAtomicOperationOnVolatileField") //may only be called on the parent strucutes lock
    public @Nullable ModificationInfo add(int startOffset, int endOffset, int split, @Nullable ModificationInfo checked) {
        if(endOffset + split  < this.startOffset) { // new < this
            if(previous == null) {
                previous = new ModificationInfo(startOffset, endOffset);
                return previous;
            } else {
                if(previous == checked) {
                    assert checked != null; // previous != null && previous == check -> checked != null mister compiler
                    this.previous = new ModificationInfo(startOffset, endOffset);
                    checked.next = previous;
                    previous.next = this;
                    previous.previous = checked;
                    return previous;
                }

                return previous.add(startOffset, endOffset, split, this);
            }
        } else if(startOffset - split > this.endOffset) { //this < new
            if(next == null) {
                next = new ModificationInfo(startOffset, endOffset);
                return next;
            } else {
                if(next == checked) {
                    assert checked != null; // next != null && next == check -> checked != null mister compiler
                    this.next  = new ModificationInfo(startOffset, endOffset);
                    checked.previous = this.next;
                    next.previous = this;
                    next.next = checked;
                    return next;
                }

                return next.add(startOffset, endOffset, split, this);
            }
        } else { //this + new
            //Replace self
            this.startOffset = Math.min(startOffset, this.startOffset);
            this.endOffset = Math.max(endOffset, this.endOffset);

            //check if this can be combined with prev or next
            if(previous != null && previous.endOffset + split < this.startOffset) {
                this.startOffset = previous.startOffset;
                this.previous = previous.previous;
                this.previous.next = this;
            }

            if(next != null && this.endOffset + split > next.startOffset) {
                this.endOffset = next.endOffset;
                this.next = next.next;
                this.next.previous = this;
            }

            return null;
        }
    }

    @Override
    public String toString() {
        return "ModificationInfo[startOffset=" + startOffset + " endOffset=" + endOffset + " hasNext=" + (next != null) + "]";
    }
}
