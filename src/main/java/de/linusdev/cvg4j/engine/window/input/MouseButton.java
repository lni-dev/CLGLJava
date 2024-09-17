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

package de.linusdev.cvg4j.engine.window.input;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MouseButton extends PressableImpl {

    final int button;
    final @Nullable StandardMouseButton name;

    public MouseButton(@NotNull InputManger manger, int button) {
        super(manger, InputType.MOUSE_BUTTON);
        this.button = button;
        this.name = StandardMouseButton.translate(button);
    }

    @SuppressWarnings("unused")
    public MouseButton(@NotNull InputManger manger, StandardMouseButton button) {
        super(manger, InputType.MOUSE_BUTTON);
        this.button = button.getValue();
        this.name = button;
    }

    @Override
    public boolean isPressed() {
        return manager.isMouseButtonPressed(this);
    }

    @Override
    public @NotNull Serializable toSerializable() {
        return new Serializable(button, getType());
    }
}
