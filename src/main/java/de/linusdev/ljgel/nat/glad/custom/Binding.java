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
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

public abstract class Binding<P extends GLNamedObject<P>, T extends GLNamedObject<T>> implements ReCreationListener<T> {

    protected final P parent;
    protected final T component;

    protected final @NotNull BindingID id;

    public Binding(@NotNull P parent, @NotNull T component, @NotNull Object @NotNull ... target) {
        this.parent = parent;
        this.component = component;
        this.id = new BindingID(target);

        component.addReCreationListener(this);
        parent.addBinding(this);
    }

    @ApiStatus.OverrideOnly
    protected abstract void _remove(@NotNull P parent, @NotNull T component, @NotNull BindingID idToRemove);

    @ApiStatus.NonExtendable
    public void remove() {
        if(!parent.isClosed()) {
            _remove(parent, component, id);
            parent.removeBinding(id);
        }

    }

    @ApiStatus.OverrideOnly
    protected abstract void onObjectRecreated(@NotNull P parent, @NotNull T component);

    @ApiStatus.NonExtendable
    @Override
    public void afterReCreation(@NotNull T obj) {
        if(!parent.isClosed())
            onObjectRecreated(parent, component);
        else {
            component.removeReCreationListener(this);
        }
    }

    public @NotNull BindingID getId() {
        return id;
    }
}
