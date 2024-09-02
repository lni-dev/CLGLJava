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
import de.linusdev.cvg4j.engine.vk.memory.allocator.VulkanMemoryAllocator;
import de.linusdev.cvg4j.engine.vk.memory.buffer.vertex.SimpleVertex;
import de.linusdev.cvg4j.engine.vk.memory.buffer.vertex.VertexBuffer;
import de.linusdev.cvg4j.engine.vk.memory.buffer.vertex.VertexElement;
import de.linusdev.cvg4j.engine.vk.pipeline.RasterizationPipelineInfo;
import de.linusdev.cvg4j.engine.vk.shader.VulkanShader;
import de.linusdev.cvg4j.nat.glfw3.custom.FrameInfo;
import de.linusdev.cvg4j.nat.vulkan.VulkanApiVersion;
import de.linusdev.cvg4j.nat.vulkan.enums.VkPipelineBindPoint;
import de.linusdev.cvg4j.nat.vulkan.enums.VkStructureType;
import de.linusdev.cvg4j.nat.vulkan.enums.VkSubpassContents;
import de.linusdev.cvg4j.nat.vulkan.enums.VkVertexInputRate;
import de.linusdev.cvg4j.nat.vulkan.handles.VkCommandBuffer;
import de.linusdev.cvg4j.nat.vulkan.handles.VkFramebuffer;
import de.linusdev.cvg4j.nat.vulkan.handles.VkInstance;
import de.linusdev.cvg4j.nat.vulkan.handles.VkShaderModule;
import de.linusdev.cvg4j.nat.vulkan.structs.VkClearValue;
import de.linusdev.cvg4j.nat.vulkan.structs.VkCommandBufferBeginInfo;
import de.linusdev.cvg4j.nat.vulkan.structs.VkRenderPassBeginInfo;
import de.linusdev.lutils.math.VMath;
import de.linusdev.lutils.math.vector.array.floatn.ABFloat3;
import de.linusdev.lutils.nat.memory.Stack;
import de.linusdev.lutils.nat.struct.abstracts.Structure;
import de.linusdev.lutils.nat.struct.array.StructureArray;
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
        public long getMillisPerTick() {
            return 20;
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

        protected VulkanMemoryAllocator vulkanMemoryAllocator;
        protected VertexBuffer<SimpleVertex> vertexBuffer;
        protected StructureArray<SimpleVertex> vertexBufferAsArray;

        public TestScene(@NotNull VulkanEngine<TestGame> engine) {
            super(engine);
        }

        @Override
        public void onLoad(@NotNull VulkanRasterizationWindow window) {
            window.setWindowAspectRatio(1, 1);
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
            renderPassBeginInfo.renderPass.set(pipeLine.getRenderPass().getVkRenderPass());
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
            vkInstance.vkCmdBindVertexBuffers(commandBuffer, 0, 1, ref(vertexBuffer.getVkBuffer()), ref(vertexBuffer.getOffset()));
            vkInstance.vkCmdSetViewport(commandBuffer, 0, 1, ref(viewport));
            vkInstance.vkCmdSetScissor(commandBuffer, 0, 1, ref(scissors));
            vkInstance.vkCmdDraw(commandBuffer, vertexBuffer.getVertexCount(), 1, 0, 0);
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
                            Objects.requireNonNull(this.getClass().getResourceAsStream("/de/linusdev/cvg4j/vulkan/shaders/vulkanTest2.vert.spv")),
                            "main",
                            Structure.allocate(new VkShaderModule())
                    );
                }

                @Override
                public @NotNull VulkanShader loadFragmentShader() throws IOException {
                    return VulkanShader.createFromSpirVBinaryStream(
                            stack,
                            engine,
                            Objects.requireNonNull(this.getClass().getResourceAsStream("/de/linusdev/cvg4j/vulkan/shaders/vulkanTest2.frag.spv")),
                            "main",
                            Structure.allocate(new VkShaderModule())
                    );
                }

                @Override
                public @NotNull VertexBuffer<?> getVertexBuffer() throws EngineException {

                    vulkanMemoryAllocator = new VulkanMemoryAllocator(engine.getVkInstance(), engine.getDevice());
                    vertexBuffer = vulkanMemoryAllocator.createVertexBuffer(
                            stack, "vertex-buffer-1", SimpleVertex.class, SimpleVertex::new,
                            VertexElement.ofComplexInfo(new SimpleVertex().getInfo()),
                            3, 0, VkVertexInputRate.VERTEX
                    );

                    vulkanMemoryAllocator.allocate(stack);

                    vertexBufferAsArray = vertexBuffer.getVertexInput().getBackedArray();
                    vertexBuffer.getVertexInput().setCurrentVertexCount(3);

                    vertexBufferAsArray.getOrCreate(0).position.xyz(0f, -0.5f,0f);
                    vertexBufferAsArray.getOrCreate(0).color.xyz(0.5f, 0.5f,0.5f);

                    vertexBufferAsArray.getOrCreate(1).position.xyz(0.5f, 0.5f,0f);
                    vertexBufferAsArray.getOrCreate(1).color.xyz(0f, 1f,0f);

                    vertexBufferAsArray.getOrCreate(2).position.xyz(-0.5f, 0.5f,0f);
                    vertexBufferAsArray.getOrCreate(2).color.xyz(0f, 0f,1f);

                    return vertexBuffer;
                }
            };
        }

        @Override
        public void tick() {
            float factor = 0.01f;
            for (SimpleVertex simpleVertex : vertexBufferAsArray) {
                VMath.add(
                        simpleVertex.color,
                        new ABFloat3((float) ((Math.random() - 0.5f) * factor), (float) ((Math.random() - 0.5f) * factor), (float) ((Math.random() - 0.5f) * factor)),
                        simpleVertex.color
                );

                VMath.add(
                        simpleVertex.position,
                        new ABFloat3((float) ((Math.random() - 0.5f) * factor), (float) ((Math.random() - 0.5f) * factor), (float) ((Math.random() - 0.5f) * factor)),
                        simpleVertex.position
                );
            }
        }

        @Override
        public void update(@NotNull FrameInfo frameInfo) {

        }

        @Override
        public void close() {
            vulkanMemoryAllocator.close();
            super.close();
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