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

package de.linusdev.clgl.engine;

import de.linusdev.clgl.nat.glfw3.custom.FrameInfo;
import de.linusdev.clgl.window.args.KernelView;
import org.jetbrains.annotations.NonBlocking;
import org.jetbrains.annotations.NotNull;

public enum SceneState {

    /**
     * Starting State
     */
    CREATED {

        @Override
        <G extends Game> void tick(@NotNull Scene<G> scene) {
            
        }

        @Override
        <G extends Game> void update(@NotNull Scene<G> scene, @NotNull Engine<G> engine, @NotNull FrameInfo info) {
            
        }

        @Override
        <G extends Game> void setRenderKernelArgs(@NotNull Scene<G> scene, @NotNull KernelView kernel) {
            scene.setLoadingRenderKernelArgs(kernel);
        }

        @Override
        <G extends Game> void setUIKernelArgs(@NotNull Scene<G> scene, @NotNull KernelView kernel) {
            scene.setLoadingUIKernelArgs(kernel);
        }
    },

    /**
     * Scene is currently loading.
     */
    LOADING {
        @Override
        <G extends Game> void onKey(@NotNull Scene<G> scene, int key, int scancode, int action, int mods) {
            scene.onKey0(key, scancode, action, mods);
        }

        @Override
        <G extends Game> void onMouseButton(@NotNull Scene<G> scene, int button, int action, int mods) {
            scene.onMouseButton0(button, action, mods);
        }

        @Override
        <G extends Game> void onTextInput(@NotNull Scene<G> scene, char[] chars, boolean supplementaryChar) {
            scene.onTextInput0(chars, supplementaryChar);
        }

        @Override
        <G extends Game> void tick(@NotNull Scene<G> scene) {
            
        }

        @Override
        <G extends Game> void update(@NotNull Scene<G> scene, @NotNull Engine<G> engine, @NotNull FrameInfo info) {
            scene.update0(engine, info);
        }

        @Override
        <G extends Game> void setRenderKernelArgs(@NotNull Scene<G> scene, @NotNull KernelView kernel) {
            throw new UnsupportedOperationException();
        }

        @Override
        <G extends Game> void setUIKernelArgs(@NotNull Scene<G> scene, @NotNull KernelView kernel) {
            throw new UnsupportedOperationException();
        }
    },

    /**
     * Scene has finished loading, but {@link Scene#start() start} has not yet been called (The state will change after
     * the call to {@link Scene#start() start} has returned).
     * The scene's render and ui kernels will be set during this state.
     */
    UNSTARTED {
        @Override
        <G extends Game> void onKey(@NotNull Scene<G> scene, int key, int scancode, int action, int mods) {
            scene.onKey0(key, scancode, action, mods);
        }

        @Override
        <G extends Game> void onMouseButton(@NotNull Scene<G> scene, int button, int action, int mods) {
            scene.onMouseButton0(button, action, mods);
        }

        @Override
        <G extends Game> void onTextInput(@NotNull Scene<G> scene, char[] chars, boolean supplementaryChar) {
            scene.onTextInput0(chars, supplementaryChar);
        }

        @Override
        <G extends Game> void tick(@NotNull Scene<G> scene) {

        }

        @Override
        <G extends Game> void update(@NotNull Scene<G> scene, @NotNull Engine<G> engine, @NotNull FrameInfo info) {
            scene.update0(engine, info);
        }

        @Override
        <G extends Game> void setRenderKernelArgs(@NotNull Scene<G> scene, @NotNull KernelView kernel) {
            scene.setRenderKernelArgs(kernel);
        }

        @Override
        <G extends Game> void setUIKernelArgs(@NotNull Scene<G> scene, @NotNull KernelView kernel) {
            scene.setUIKernelArgs(kernel);
        }
    },

    /**
     * Scene was started and is currently active
     */
    STARTED {
        @Override
        <G extends Game> void onKey(@NotNull Scene<G> scene, int key, int scancode, int action, int mods) {
            scene.onKey0(key, scancode, action, mods);
        }

        @Override
        <G extends Game> void onMouseButton(@NotNull Scene<G> scene, int button, int action, int mods) {
            scene.onMouseButton0(button, action, mods);
        }

        @Override
        <G extends Game> void onTextInput(@NotNull Scene<G> scene, char[] chars, boolean supplementaryChar) {
            scene.onTextInput0(chars, supplementaryChar);
        }

        @Override
        <G extends Game> void tick(@NotNull Scene<G> scene) {
            scene.tick();
        }

        @Override
        <G extends Game> void update(@NotNull Scene<G> scene, @NotNull Engine<G> engine, @NotNull FrameInfo info) {
            scene.update0(engine, info);
        }

        @Override
        <G extends Game> void setRenderKernelArgs(@NotNull Scene<G> scene, @NotNull KernelView kernel) {
            throw new UnsupportedOperationException();
        }

        @Override
        <G extends Game> void setUIKernelArgs(@NotNull Scene<G> scene, @NotNull KernelView kernel) {
            throw new UnsupportedOperationException();
        }
    },

    /**
     * Scene is not active anymore and is currently releasing resources.
     */
    UNLOADING {

        @Override
        <G extends Game> void tick(@NotNull Scene<G> scene) {
            throw new UnsupportedOperationException();
        }

        @Override
        <G extends Game> void update(@NotNull Scene<G> scene, @NotNull Engine<G> engine, @NotNull FrameInfo info) {
            throw new UnsupportedOperationException();
        }

        @Override
        <G extends Game> void setRenderKernelArgs(@NotNull Scene<G> scene, @NotNull KernelView kernel) {
            throw new UnsupportedOperationException();
        }

        @Override
        <G extends Game> void setUIKernelArgs(@NotNull Scene<G> scene, @NotNull KernelView kernel) {
            throw new UnsupportedOperationException();
        }
    },

    /**
     * Scene is dead.
     */
    DEAD {

        @Override
        <G extends Game> void tick(@NotNull Scene<G> scene) {
            throw new UnsupportedOperationException();
        }

        @Override
        <G extends Game> void update(@NotNull Scene<G> scene, @NotNull Engine<G> engine, @NotNull FrameInfo info) {
            throw new UnsupportedOperationException();
        }

        @Override
        <G extends Game> void setRenderKernelArgs(@NotNull Scene<G> scene, @NotNull KernelView kernel) {
            throw new UnsupportedOperationException();
        }

        @Override
        <G extends Game> void setUIKernelArgs(@NotNull Scene<G> scene, @NotNull KernelView kernel) {
            throw new UnsupportedOperationException();
        }
    },

    ;

    @NonBlocking
    abstract <G extends Game> void tick(@NotNull Scene<G> scene);

    @NonBlocking
    abstract <G extends Game> void update(@NotNull Scene<G> scene, @NotNull Engine<G> engine, @NotNull FrameInfo info);

    @NonBlocking
    abstract <G extends Game> void setRenderKernelArgs(@NotNull Scene<G> scene, @NotNull KernelView kernel);

    @NonBlocking
    abstract <G extends Game> void setUIKernelArgs(@NotNull Scene<G> scene, @NotNull KernelView kernel);

    @NonBlocking
    <G extends Game> void onKey(@NotNull Scene<G> scene, int key, int scancode, int action, int mods) {

    }

    @NonBlocking
    <G extends Game> void onMouseButton(@NotNull Scene<G> scene, int button, int action, int mods) {

    }

    @NonBlocking
    <G extends Game> void onTextInput(@NotNull Scene<G> scene, char[] chars, boolean supplementaryChar) {

    }
}
