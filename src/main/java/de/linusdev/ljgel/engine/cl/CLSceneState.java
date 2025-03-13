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

package de.linusdev.ljgel.engine.cl;

import de.linusdev.ljgel.engine.cl.window.args.KernelView;
import de.linusdev.ljgel.engine.ticker.Ticker;
import de.linusdev.ljgel.nat.glfw3.custom.FrameInfo;
import org.jetbrains.annotations.NonBlocking;
import org.jetbrains.annotations.NotNull;

public enum CLSceneState {

    /**
     * Starting State
     */
    CREATED {

        @Override
        <G extends CLGame> void tick(@NotNull Ticker ticker, @NotNull CLScene<G> scene) {
            
        }

        @Override
        <G extends CLGame> void update(@NotNull CLScene<G> scene, @NotNull CLEngine<G> engine, @NotNull FrameInfo info) {
            
        }

        @Override
        <G extends CLGame> void setRenderKernelArgs(@NotNull CLScene<G> scene, @NotNull KernelView kernel) {
            scene.setLoadingRenderKernelArgs(kernel);
        }

        @Override
        <G extends CLGame> void setUIKernelArgs(@NotNull CLScene<G> scene, @NotNull KernelView kernel) {
            scene.setLoadingUIKernelArgs(kernel);
        }
    },

    /**
     * CLScene is currently loading.
     */
    LOADING {
        @Override
        <G extends CLGame> void onKey(@NotNull CLScene<G> scene, int key, int scancode, int action, int mods) {
            scene.onKey0(key, scancode, action, mods);
        }

        @Override
        <G extends CLGame> void onMouseButton(@NotNull CLScene<G> scene, int button, int action, int mods) {
            scene.onMouseButton0(button, action, mods);
        }

        @Override
        <G extends CLGame> void onTextInput(@NotNull CLScene<G> scene, char[] chars, boolean supplementaryChar) {
            scene.onTextInput0(chars, supplementaryChar);
        }

        @Override
        <G extends CLGame> void tick(@NotNull Ticker ticker, @NotNull CLScene<G> scene) {
            
        }

        @Override
        <G extends CLGame> void update(@NotNull CLScene<G> scene, @NotNull CLEngine<G> engine, @NotNull FrameInfo info) {
            scene.update0(info);
        }

        @Override
        <G extends CLGame> void setRenderKernelArgs(@NotNull CLScene<G> scene, @NotNull KernelView kernel) {
            throw new UnsupportedOperationException();
        }

        @Override
        <G extends CLGame> void setUIKernelArgs(@NotNull CLScene<G> scene, @NotNull KernelView kernel) {
            throw new UnsupportedOperationException();
        }
    },

    /**
     * CLScene has finished loading, but {@link CLScene#start() start} has not yet been called (The state will change after
     * the call to {@link CLScene#start() start} has returned).
     * The scene's render and ui kernels will be set during this state.
     */
    UNSTARTED {
        @Override
        <G extends CLGame> void onKey(@NotNull CLScene<G> scene, int key, int scancode, int action, int mods) {
            scene.onKey0(key, scancode, action, mods);
        }

        @Override
        <G extends CLGame> void onMouseButton(@NotNull CLScene<G> scene, int button, int action, int mods) {
            scene.onMouseButton0(button, action, mods);
        }

        @Override
        <G extends CLGame> void onTextInput(@NotNull CLScene<G> scene, char[] chars, boolean supplementaryChar) {
            scene.onTextInput0(chars, supplementaryChar);
        }

        @Override
        <G extends CLGame> void tick(@NotNull Ticker ticker, @NotNull CLScene<G> scene) {

        }

        @Override
        <G extends CLGame> void update(@NotNull CLScene<G> scene, @NotNull CLEngine<G> engine, @NotNull FrameInfo info) {
            scene.update0(info);
        }

        @Override
        <G extends CLGame> void setRenderKernelArgs(@NotNull CLScene<G> scene, @NotNull KernelView kernel) {
            scene.setRenderKernelArgs(kernel);
        }

        @Override
        <G extends CLGame> void setUIKernelArgs(@NotNull CLScene<G> scene, @NotNull KernelView kernel) {
            scene.setUIKernelArgs(kernel);
        }
    },

    /**
     * CLScene was started and is currently active
     */
    STARTED {
        @Override
        <G extends CLGame> void onKey(@NotNull CLScene<G> scene, int key, int scancode, int action, int mods) {
            scene.onKey0(key, scancode, action, mods);
        }

        @Override
        <G extends CLGame> void onMouseButton(@NotNull CLScene<G> scene, int button, int action, int mods) {
            scene.onMouseButton0(button, action, mods);
        }

        @Override
        <G extends CLGame> void onTextInput(@NotNull CLScene<G> scene, char[] chars, boolean supplementaryChar) {
            scene.onTextInput0(chars, supplementaryChar);
        }

        @Override
        <G extends CLGame> void tick(@NotNull Ticker ticker, @NotNull CLScene<G> scene) {
            scene.tick0(ticker);
        }

        @Override
        <G extends CLGame> void update(@NotNull CLScene<G> scene, @NotNull CLEngine<G> engine, @NotNull FrameInfo info) {
            scene.update0(info);
        }

        @Override
        <G extends CLGame> void setRenderKernelArgs(@NotNull CLScene<G> scene, @NotNull KernelView kernel) {
            throw new UnsupportedOperationException();
        }

        @Override
        <G extends CLGame> void setUIKernelArgs(@NotNull CLScene<G> scene, @NotNull KernelView kernel) {
            throw new UnsupportedOperationException();
        }
    },

    /**
     * CLScene is not active anymore and is currently releasing resources.
     */
    UNLOADING {

        @Override
        <G extends CLGame> void tick(@NotNull Ticker ticker, @NotNull CLScene<G> scene) {
            throw new UnsupportedOperationException();
        }

        @Override
        <G extends CLGame> void update(@NotNull CLScene<G> scene, @NotNull CLEngine<G> engine, @NotNull FrameInfo info) {
            throw new UnsupportedOperationException();
        }

        @Override
        <G extends CLGame> void setRenderKernelArgs(@NotNull CLScene<G> scene, @NotNull KernelView kernel) {
            throw new UnsupportedOperationException();
        }

        @Override
        <G extends CLGame> void setUIKernelArgs(@NotNull CLScene<G> scene, @NotNull KernelView kernel) {
            throw new UnsupportedOperationException();
        }
    },

    /**
     * CLScene is dead.
     */
    DEAD {

        @Override
        <G extends CLGame> void tick(@NotNull Ticker ticker, @NotNull CLScene<G> scene) {
            throw new UnsupportedOperationException();
        }

        @Override
        <G extends CLGame> void update(@NotNull CLScene<G> scene, @NotNull CLEngine<G> engine, @NotNull FrameInfo info) {
            throw new UnsupportedOperationException();
        }

        @Override
        <G extends CLGame> void setRenderKernelArgs(@NotNull CLScene<G> scene, @NotNull KernelView kernel) {
            throw new UnsupportedOperationException();
        }

        @Override
        <G extends CLGame> void setUIKernelArgs(@NotNull CLScene<G> scene, @NotNull KernelView kernel) {
            throw new UnsupportedOperationException();
        }
    },

    ;

    @NonBlocking
    abstract <G extends CLGame> void tick(@NotNull Ticker ticker, @NotNull CLScene<G> scene);

    @NonBlocking
    abstract <G extends CLGame> void update(@NotNull CLScene<G> scene, @NotNull CLEngine<G> engine, @NotNull FrameInfo info);

    @NonBlocking
    abstract <G extends CLGame> void setRenderKernelArgs(@NotNull CLScene<G> scene, @NotNull KernelView kernel);

    @NonBlocking
    abstract <G extends CLGame> void setUIKernelArgs(@NotNull CLScene<G> scene, @NotNull KernelView kernel);

    @NonBlocking
    <G extends CLGame> void onKey(@NotNull CLScene<G> scene, int key, int scancode, int action, int mods) {

    }

    @NonBlocking
    <G extends CLGame> void onMouseButton(@NotNull CLScene<G> scene, int button, int action, int mods) {

    }

    @NonBlocking
    <G extends CLGame> void onTextInput(@NotNull CLScene<G> scene, char[] chars, boolean supplementaryChar) {

    }
}
