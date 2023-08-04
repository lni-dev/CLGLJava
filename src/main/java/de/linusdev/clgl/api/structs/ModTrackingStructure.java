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

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.util.concurrent.locks.ReentrantLock;

public abstract class ModTrackingStructure extends Structure {

    protected ReentrantLock modificationLock;

    @Override
    public void useBuffer(@NotNull Structure mostParentStructure, int offset) {
        super.useBuffer(mostParentStructure, offset);
    }

    @Override
    public void claimBuffer(@NotNull ByteBuffer buffer) {
        super.claimBuffer(buffer);
        this.modificationLock = new ReentrantLock();
    }

    /**
     * Get the first {@link ModificationInfo}. Only available if this structure was {@link #isModified() modified}
     * and {@link #tracksModifications() tracks modifications}.
     * @param clear {@code true} if the modifications should be cleared.
     * @return {@link ModificationInfo} or {@code null}
     */
    @ApiStatus.Internal
    abstract public @Nullable ModificationInfo getFirstModificationInfo(boolean clear);

    /**
     *
     * @return {@code true} if this structure tracks modifications.
     */
    @ApiStatus.Internal
    abstract public boolean tracksModifications();

    @ApiStatus.Internal
    public void acquireModificationLock() {
        modificationLock.lock();
    }

    @ApiStatus.Internal
    public void releaseModificationLock() {
        modificationLock.unlock();
    }
}
