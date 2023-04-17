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

import org.jetbrains.annotations.NotNull;

public abstract class ComplexStructure extends Structure {

    protected @NotNull Structure [] items;

    protected final boolean trackModifications;
    protected final int modificationSplitOffset = 128;
    protected ModificationInfo modInfo = null;

    public ComplexStructure(boolean trackModifications) {
        this.trackModifications = trackModifications;
    }

    public void init(boolean allocateBuffer, @NotNull Structure @NotNull ... items) {
        this.items = items;
        if(allocateBuffer)
            allocate();
    }

    @Override
    protected void onModification(int offset, int size) {
        super.onModification(offset, size);

        if(trackModifications) {
            modificationLock.lock();

            int offsetEnd = offset + size;

            if(modInfo == null) {
               modInfo = new ModificationInfo(offset, offsetEnd);
            } else {
                modInfo.add(offset, offsetEnd, modificationSplitOffset, null);
                if(modInfo.previous != null)
                    modInfo = modInfo.previous;
            }

            modificationLock.unlock();
        }
    }

    @Override
    public ModificationInfo getFirstModificationInfo(boolean clear) {
        ModificationInfo ret = modInfo;
        modInfo = null;
        return ret;
    }

    @Override
    public void useBuffer(@NotNull Structure mostParentStructure, int offset) {
        super.useBuffer(mostParentStructure, offset);
        StructureInfo info = getInfo();

        int[] sizes = info.getSizes();

        int position = 0;
        for(int i = 0; i < items.length ; i++) {
            position += sizes[i * 2];
            items[i].useBuffer(mostParentStructure, offset + position);
            position += sizes[i * 2 + 1];
        }

    }

    public String toString(@NotNull Structure @NotNull ... items) {
        return getInfo().toString(this.getClass().getSimpleName(), items);
    }
}
