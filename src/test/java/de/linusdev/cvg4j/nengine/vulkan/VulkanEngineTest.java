/*
 * Copyright (c) 2024 Linus Andera
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

package de.linusdev.cvg4j.nengine.vulkan;

import de.linusdev.cvg4j.nat.vulkan.VulkanApiVersion;
import de.linusdev.cvg4j.nengine.Engine;
import de.linusdev.cvg4j.nengine.exception.EngineException;
import de.linusdev.cvg4j.nengine.info.Game;
import de.linusdev.cvg4j.nengine.vulkan.selector.VulkanGPUSelectorBuilder;
import de.linusdev.cvg4j.nengine.vulkan.selector.VulkanRequirements;
import de.linusdev.lutils.version.ReleaseType;
import de.linusdev.lutils.version.Version;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.List;

class VulkanEngineTest {

    static class TestGame implements Game {

        @Override
        public @NotNull String name() {
            return "TestGame";
        }

        @Override
        public @NotNull Version version() {
            return Version.of(ReleaseType.DEVELOPMENT_BUILD, 1, 0, 0);
        }
    }

    @Test
    void test() throws EngineException, InterruptedException {
        Engine.StaticSetup.setup();

        VulkanEngine<TestGame> engine = new VulkanEngine<>(
                new TestGame(),
                new VulkanRequirements(
                        VulkanApiVersion.V_1_0_0,
                        List.of(),
                        VulkanGPUSelectorBuilder.getDefault().build(),
                        List.of()
                )
        );

        Thread.sleep(1000);
    }
}