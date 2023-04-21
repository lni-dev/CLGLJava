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

package de.linusdev.clgl.nat.glad.objects;

import de.linusdev.clgl.nat.glad.listener.ReCreationListener;
import de.linusdev.lutils.llist.LLinkedList;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@SuppressWarnings("unused")
public abstract class GLNamedObject<SELF extends GLNamedObject<SELF>> implements AutoCloseable {

    protected int name = 0;
    protected @NotNull List<ReCreationListener<SELF>> rcListeners = new LLinkedList<>();

    public int getName() {
        return name;
    }

    public boolean isReCreatable() {
        return false;
    }

    public abstract void reCreate();

    protected void callReCreationListener() {
        for(ReCreationListener<SELF> listener : rcListeners)
            //noinspection unchecked
            listener.afterReCreation((SELF) this);
    }

    public void addReCreationListener(@NotNull ReCreationListener<SELF> listener) {
        rcListeners.add(listener);
    }

    public void removeReCreationListener(@NotNull ReCreationListener<SELF> listener) {
        rcListeners.remove(listener);
    }
}
