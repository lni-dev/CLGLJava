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

import de.linusdev.clgl.engine.kernel.source.KernelSourceInfo;
import de.linusdev.clgl.nat.glfw3.custom.FrameInfo;
import de.linusdev.clgl.window.args.KernelView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TestScene extends Scene<TestGame> {
    protected TestScene(@NotNull Engine<TestGame> engine) {
        super(engine);
        loadingPercent.set(.0f);
    }

    @Override
    @Nullable KernelSourceInfo getUIKernelInfo() {
        return null;
    }

    @Override
    @Nullable KernelSourceInfo getRenderKernelInfo() {
        return null;
    }

    @Override
    void setRenderKernelArgs(@NotNull KernelView renderKernel) {
        System.out.println("setRenderKernelArgs");
    }

    @Override
    void setUIKernelArgs(@NotNull KernelView uiKernel) {
        System.out.println("setUIKernelArgs");
    }

    @Override
    protected void load() {
        System.out.println("load");
        try {
            final long step = 10L;
            final long time = 1_000L;
            for(long waited = 0; waited < time; waited+=step) {
                Thread.sleep(step);
                loadingPercent.set(((float) waited) / ((float) time));
                loadingPercent.modified();
            }

        } catch (Throwable t) {
            t.printStackTrace();
        }

        System.out.println("load end");
    }

    @Override
    protected void unload() {
        System.out.println("unload");
    }

    @Override
    public void start() {
        System.out.println("start");
    }

    @Override
    public void tick() {
        System.out.println("tick");
    }

    @Override
    public void update(@NotNull Engine<TestGame> engine, @NotNull FrameInfo frameInfo) {

    }
}
