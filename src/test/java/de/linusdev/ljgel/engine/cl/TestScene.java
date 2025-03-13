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

import de.linusdev.ljgel.engine.cl.structs.WorldStruct;
import de.linusdev.ljgel.engine.cl.window.args.KernelView;
import de.linusdev.ljgel.engine.cl.window.args.impl.AutoUpdateBuffer;
import de.linusdev.ljgel.engine.ticker.Ticker;
import de.linusdev.ljgel.engine.window.input.TextInput;
import de.linusdev.ljgel.nat.cl.CL;
import de.linusdev.ljgel.nat.cl.objects.Buffer;
import de.linusdev.ljgel.nat.glfw3.GLFWValues;
import de.linusdev.ljgel.nat.glfw3.custom.FrameInfo;
import de.linusdev.lutils.bitfield.LongBitfieldImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Paths;
import java.util.Random;

public class TestScene extends CLScene<TestGame> {

    private WorldStruct world;
    private Buffer worldBuffer;


    protected TestScene(@NotNull CLEngine<TestGame> engine) {
        super(engine);
        loadingPercent.set(.0f);
    }

    @Override
    protected @Nullable KernelSourceInfo getUIKernelInfo() {
        return KernelSourceInfo.ofUTF8StringFile(
                Paths.get("../src/test/resources/enginetest/ui.cl"),
                "render"
        );
    }

    @Override
    protected @Nullable KernelSourceInfo getRenderKernelInfo() {
        return KernelSourceInfo.ofUTF8StringFile(
                Paths.get("../src/test/resources/enginetest/render.cl"),
                "render"
        );
    }

    @Override
    protected void setRenderKernelArgs(@NotNull KernelView renderKernel) {
        renderKernel.setKernelArg(3, new AutoUpdateBuffer(world, worldBuffer));
    }

    @Override
    protected void setUIKernelArgs(@NotNull KernelView uiKernel) {

    }

    @Override
    protected void load() {
        System.out.println("load");
        try {
            final long step = 10L;
            final long time = 500L;
            for(long waited = 0; waited < time; waited+=step) {
                Thread.sleep(step);
                loadingPercent.set(((float) waited) / ((float) time));
                loadingPercent.modified();
            }

        } catch (Throwable t) {
            t.printStackTrace();
        }

        world = new WorldStruct(true);
        world.playerA.color.x(1.f);
        world.playerB.color.z(1.f);

        worldBuffer = new Buffer(getEngine().getClContext(),
                new LongBitfieldImpl<>(
                        CL.CLMemFlag.CL_MEM_READ_ONLY,
                        CL.CLMemFlag.CL_MEM_HOST_WRITE_ONLY,
                        CL.CLMemFlag.CL_MEM_USE_HOST_PTR
                ), world);


        System.out.println("load end");
    }

    @Override
    protected void unload() {
        System.out.println("unload");
    }

    @Override
    public void start() {
        getInputManger().getUSKey(GLFWValues.Keys_US.GLFW_KEY_F5).addPressListener(() -> {
            getEngine().loadScene(new TestScene(getEngine()));
        });


        new TextInput(getInputManger(), new TextInput.Listener() {
            @Override
            public void onAdd(@NotNull TextInput input, @NotNull StringBuffer current, char @NotNull [] added) {

            }

            @Override
            public void onRemove(@NotNull TextInput input, @NotNull StringBuffer current, char removed) {

            }

            @Override
            public void onEnter(@NotNull TextInput input, @NotNull StringBuffer current) {
                System.out.println(current);
                input.stop();
            }
        }).start();

        world.playerA.position.modified();
    }

    @Override
    public void tick(@NotNull Ticker ticker) {

        if(getEngine().getWindow().getInputManger().getUSKey(GLFWValues.Keys_US.GLFW_KEY_A).isPressed()) {
            Random random = new Random();
            world.playerA.color.xyz(random.nextFloat(), random.nextFloat(), random.nextFloat());
            world.playerB.color.xyz(random.nextFloat(), random.nextFloat(), random.nextFloat());
            world.playerA.color.modified();
            world.playerB.color.modified();
        }
    }

    @Override
    public void update(@NotNull FrameInfo frameInfo) {

    }
}
