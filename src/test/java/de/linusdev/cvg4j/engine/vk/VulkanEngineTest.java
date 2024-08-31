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

package de.linusdev.cvg4j.engine.vk;

import de.linusdev.cvg4j.engine.Engine;
import de.linusdev.cvg4j.engine.exception.EngineException;
import de.linusdev.cvg4j.engine.vk.device.Extend2D;
import de.linusdev.cvg4j.engine.vk.extension.VulkanExtension;
import de.linusdev.cvg4j.engine.vk.pipeline.RasterizationPipelineInfo;
import de.linusdev.cvg4j.engine.vk.shader.VulkanShader;
import de.linusdev.cvg4j.nat.vulkan.VulkanApiVersion;
import de.linusdev.cvg4j.nat.vulkan.enums.VkPipelineBindPoint;
import de.linusdev.cvg4j.nat.vulkan.enums.VkStructureType;
import de.linusdev.cvg4j.nat.vulkan.enums.VkSubpassContents;
import de.linusdev.cvg4j.nat.vulkan.handles.VkCommandBuffer;
import de.linusdev.cvg4j.nat.vulkan.handles.VkFramebuffer;
import de.linusdev.cvg4j.nat.vulkan.handles.VkInstance;
import de.linusdev.cvg4j.nat.vulkan.handles.VkShaderModule;
import de.linusdev.cvg4j.nat.vulkan.structs.VkClearValue;
import de.linusdev.cvg4j.nat.vulkan.structs.VkCommandBufferBeginInfo;
import de.linusdev.cvg4j.nat.vulkan.structs.VkRenderPassBeginInfo;
import de.linusdev.lutils.nat.memory.Stack;
import de.linusdev.lutils.nat.struct.abstracts.Structure;
import de.linusdev.lutils.version.ReleaseType;
import de.linusdev.lutils.version.Version;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import static de.linusdev.lutils.nat.pointer.TypedPointer64.ref;

class VulkanEngineTest {

    static class TestGame implements VulkanGame {

        @Override
        public @NotNull String name() {
            return "TestGame";
        }

        @Override
        public @NotNull Version version() {
            return Version.of(ReleaseType.DEVELOPMENT_BUILD, 1, 0, 0);
        }

        @Override
        public @NotNull VulkanApiVersion minRequiredInstanceVersion() {
            return VulkanApiVersion.V_1_0_0;
        }

        @Override
        public @NotNull List<VulkanExtension> requiredInstanceExtensions() {
            return List.of();
        }

        @Override
        public @NotNull List<String> activatedVulkanLayers() {
            return List.of();
        }
    }

    static class TestScene extends VkScene<TestGame> {


        public TestScene(@NotNull VulkanEngine<TestGame> engine) {
            super(engine);
        }

        @Override
        void render(
                @NotNull Stack stack,
                @NotNull VkInstance vkInstance,
                @NotNull Extend2D extend,
                int frameBufferIndex,
                @NotNull VkCommandBuffer commandBuffer,
                @NotNull VkFramebuffer frameBuffer
        ) {
            VkCommandBufferBeginInfo commandBufferBeginInfo = stack.push(new VkCommandBufferBeginInfo());
            commandBufferBeginInfo.sType.set(VkStructureType.COMMAND_BUFFER_BEGIN_INFO);

            VkClearValue vkClearValue = stack.push(new VkClearValue());
            vkClearValue.color.float32.getOrCreate(0).set(0f);
            vkClearValue.color.float32.getOrCreate(1).set(0f);
            vkClearValue.color.float32.getOrCreate(2).set(0f);
            vkClearValue.color.float32.getOrCreate(3).set(1f);

            VkRenderPassBeginInfo renderPassBeginInfo = stack.push(new VkRenderPassBeginInfo());
            renderPassBeginInfo.sType.set(VkStructureType.RENDER_PASS_BEGIN_INFO);
            renderPassBeginInfo.renderPass.set(pipeLine.getVkRenderPass());
            renderPassBeginInfo.renderArea.offset.x.set(0);
            renderPassBeginInfo.renderArea.offset.y.set(0);
            renderPassBeginInfo.renderArea.extent.width.set(extend.width());
            renderPassBeginInfo.renderArea.extent.height.set(extend.height());
            renderPassBeginInfo.clearValueCount.set(1);
            renderPassBeginInfo.pClearValues.set(vkClearValue);
            renderPassBeginInfo.framebuffer.set(frameBuffer);

            vkInstance.vkBeginCommandBuffer(commandBuffer, ref(commandBufferBeginInfo)).check();
            vkInstance.vkCmdBeginRenderPass(commandBuffer, ref(renderPassBeginInfo), VkSubpassContents.INLINE);
            vkInstance.vkCmdBindPipeline(commandBuffer, VkPipelineBindPoint.GRAPHICS, pipeLine.getVkPipeline());
            vkInstance.vkCmdDraw(commandBuffer, 3, 1, 0, 0);
            vkInstance.vkCmdEndRenderPass(commandBuffer);
            vkInstance.vkEndCommandBuffer(commandBuffer).check();

            stack.pop(); // renderPassBeginInfo
            stack.pop(); // vkClearValue
            stack.pop(); // commandBufferBeginInfo
        }

        @Override
        @NotNull RasterizationPipelineInfo pipeline(@NotNull Stack stack) {

            return new RasterizationPipelineInfo() {
                @Override
                public @NotNull VulkanShader loadVertexShader() throws IOException {
                    return VulkanShader.createFromSpirVBinaryStream(
                            stack,
                            engine,
                            Objects.requireNonNull(this.getClass().getResourceAsStream("/de/linusdev/cvg4j/vulkan/shaders/vulkanTest1.vert.spv")),
                            "main",
                            Structure.allocate(new VkShaderModule())
                    );
                }

                @Override
                public @NotNull VulkanShader loadFragmentShader() throws IOException {
                    return VulkanShader.createFromSpirVBinaryStream(
                            stack,
                            engine,
                            Objects.requireNonNull(this.getClass().getResourceAsStream("/de/linusdev/cvg4j/vulkan/shaders/vulkanTest1.frag.spv")),
                            "main",
                            Structure.allocate(new VkShaderModule())
                    );
                }
            };
        }

        @Override
        public void tick() {

        }
    }

    @Test
    void test() throws EngineException, InterruptedException {
        Engine.StaticSetup.setup();

        VulkanEngine<TestGame> engine = new VulkanEngine<>(new TestGame());

        engine.loadScene(new TestScene(engine)).getResult();

        engine.getEngineDeathFuture().getResult();
    }
}