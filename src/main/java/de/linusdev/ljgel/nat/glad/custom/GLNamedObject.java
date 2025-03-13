/*
 * Copyright (c) 2023-2025 Linus Andera
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

package de.linusdev.ljgel.nat.glad.custom;

import de.linusdev.ljgel.nat.glad.listener.ReCreationListener;
import de.linusdev.lutils.llist.LLinkedList;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;

@SuppressWarnings("unused")
public abstract class GLNamedObject<SELF extends GLNamedObject<SELF>> implements AutoCloseable {

    protected int name = 0;
    protected boolean closed = false;

    protected @NotNull List<ReCreationListener<SELF>> rcListeners = new LLinkedList<>();
    protected @NotNull HashMap<BindingID, Binding<?, ?>> bindings = new HashMap<>();

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

    public boolean isClosed() {
        return closed;
    }

    @ApiStatus.Internal
    public void addBinding(@NotNull Binding<?, ?> binding) {
        bindings.put(binding.getId(), binding);
    }

    @ApiStatus.Internal
    public void removeBinding(@NotNull BindingID id) {
        bindings.remove(id);
    }

    protected void checkAndDeleteExistingBinding(@NotNull Object @NotNull ... target) {
        Binding<?, ?> oldBinding = bindings.get(new BindingID(target));
        if(oldBinding != null) oldBinding.remove();
    }
}
