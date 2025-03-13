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

package de.linusdev.ljgel.engine.vk.renderpass;

import de.linusdev.lutils.llist.LLinkedList;
import de.linusdev.lutils.nat.memory.stack.Stack;
import de.linusdev.lutils.thread.var.SyncVarImpl;
import org.jetbrains.annotations.NotNull;

public class RenderPassHolder extends SyncVarImpl<@NotNull RenderPass> {

    public @NotNull LLinkedList<RenderPassChangedListener> listeners = new LLinkedList<>();

    public RenderPassHolder(@NotNull RenderPass var) {
        super(var);
    }

    public void swap(@NotNull Stack stack, @NotNull RenderPass newRenderPass) {
        RenderPass oldRenderPass = get();
        set(newRenderPass);
        listeners.forEach(listener -> listener.renderPassChanged(stack));
    }

    public void addChangeListener(@NotNull RenderPassChangedListener listener) {
        listeners.add(listener);
    }

    public void removeChangeListener(@NotNull RenderPassChangedListener listener) {
        listeners.remove(listener);
    }
}
