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
import de.linusdev.cvg4j.engine.obj.ModelViewProjection;
import de.linusdev.cvg4j.engine.vk.device.Extend2D;
import de.linusdev.cvg4j.engine.vk.extension.VulkanExtension;
import de.linusdev.cvg4j.engine.vk.memory.allocator.VulkanMemoryAllocator;
import de.linusdev.cvg4j.engine.vk.memory.buffer.index.IndexBuffer;
import de.linusdev.cvg4j.engine.vk.memory.buffer.uniform.UniformBuffer;
import de.linusdev.cvg4j.engine.vk.memory.buffer.vertex.SimpleVertex;
import de.linusdev.cvg4j.engine.vk.memory.buffer.vertex.VertexBuffer;
import de.linusdev.cvg4j.engine.vk.memory.buffer.vertex.VertexElement;
import de.linusdev.cvg4j.engine.vk.memory.image.sampler.Sampler2D;
import de.linusdev.cvg4j.engine.vk.pipeline.RasterizationPipelineInfo;
import de.linusdev.cvg4j.engine.vk.shader.VulkanShader;
import de.linusdev.cvg4j.nat.glfw3.custom.FrameInfo;
import de.linusdev.cvg4j.nat.vulkan.VulkanApiVersion;
import de.linusdev.cvg4j.nat.vulkan.enums.*;
import de.linusdev.cvg4j.nat.vulkan.handles.VkCommandBuffer;
import de.linusdev.cvg4j.nat.vulkan.handles.VkFramebuffer;
import de.linusdev.cvg4j.nat.vulkan.handles.VkInstance;
import de.linusdev.cvg4j.nat.vulkan.handles.VkShaderModule;
import de.linusdev.cvg4j.nat.vulkan.structs.VkClearValue;
import de.linusdev.cvg4j.nat.vulkan.structs.VkCommandBufferBeginInfo;
import de.linusdev.cvg4j.nat.vulkan.structs.VkRenderPassBeginInfo;
import de.linusdev.lutils.image.Image;
import de.linusdev.lutils.image.buffer.BufferBackedRGBAImage;
import de.linusdev.lutils.image.png.reader.PNGReader;
import de.linusdev.lutils.math.VMath;
import de.linusdev.lutils.math.matrix.abstracts.floatn.Float4x4;
import de.linusdev.lutils.math.matrix.array.floatn.ABFloat4x4;
import de.linusdev.lutils.math.special.CameraMatrix;
import de.linusdev.lutils.math.vector.abstracts.floatn.Float4;
import de.linusdev.lutils.math.vector.array.floatn.ABFloat3;
import de.linusdev.lutils.math.vector.array.floatn.ABFloat4;
import de.linusdev.lutils.math.vector.buffer.shortn.BBUShort1;
import de.linusdev.lutils.nat.memory.Stack;
import de.linusdev.lutils.nat.struct.abstracts.Structure;
import de.linusdev.lutils.nat.struct.array.StructureArray;
import de.linusdev.lutils.result.BiResult;
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

        protected final long startTime = System.currentTimeMillis();

        protected VulkanMemoryAllocator vulkanMemoryAllocator;
        protected VertexBuffer<SimpleVertex> vertexBuffer;
        protected IndexBuffer<BBUShort1> indexBuffer;
        protected StructureArray<SimpleVertex> vertexBufferAsArray;

        protected UniformBuffer<ModelViewProjection> uniformBuffer;

        public TestScene(@NotNull VulkanEngine<TestGame> engine) {
            super(engine);
        }

        @Override
        public void onLoad(@NotNull Stack stack, @NotNull VulkanRasterizationWindow window) throws EngineException {
            window.setWindowAspectRatio(1, 1);

            vulkanMemoryAllocator = new VulkanMemoryAllocator(engine.getVkInstance(), engine.getDevice());
            vertexBuffer = vulkanMemoryAllocator.createStagedVertexBuffer(
                    stack, "vertex-buffer-1", SimpleVertex.class, SimpleVertex::new,
                    VertexElement.ofComplexInfo(new SimpleVertex().getInfo()),
                    8, 0, VkVertexInputRate.VERTEX
            );

            indexBuffer = vulkanMemoryAllocator.createStagedInstanceBuffer(
                    stack, "index-buffer-1", BBUShort1.class, () -> BBUShort1.newAllocatable(null),
                    36
            );


            uniformBuffer = vulkanMemoryAllocator.createUniformBuffer(stack, "uniform-buf", ModelViewProjection::newUnAllocatedForOpenGLUniform, 2, 0);

            Image grassSide = loadGrassSide();
            Sampler2D<BufferBackedRGBAImage> grassSideSample = vulkanMemoryAllocator.createStagedSampler(
                    stack, "grass-texture", grassSide
            );

            vulkanMemoryAllocator.allocate(stack);

            vertexBufferAsArray = vertexBuffer.getInput().getBackedArray();

            vertexBufferAsArray.getOrCreate(0).position.xyz(-.5f, -.5f,-.5f);
            vertexBufferAsArray.getOrCreate(1).position.xyz(0.5f, -.5f,-.5f);
            vertexBufferAsArray.getOrCreate(2).position.xyz(-.5f, -.5f,0.5f);
            vertexBufferAsArray.getOrCreate(3).position.xyz(0.5f, -.5f,0.5f);

            vertexBufferAsArray.getOrCreate(4).position.xyz(-.5f, 0.5f,-.5f);
            vertexBufferAsArray.getOrCreate(5).position.xyz(0.5f, 0.5f,-.5f);
            vertexBufferAsArray.getOrCreate(6).position.xyz(-.5f, 0.5f,0.5f);
            vertexBufferAsArray.getOrCreate(7).position.xyz(0.5f, 0.5f,0.5f);

            vertexBufferAsArray.getOrCreate(0).color.xyz(0.5f, 0.5f,0.5f);
            vertexBufferAsArray.getOrCreate(1).color.xyz(0f, 1f,0f);
            vertexBufferAsArray.getOrCreate(2).color.xyz(0f, 0f,1f);
            vertexBufferAsArray.getOrCreate(3).color.xyz(1f, 0f,0.6f);

            vertexBufferAsArray.getOrCreate(4).color.xyz(0.5f, 0.5f,0.5f);
            vertexBufferAsArray.getOrCreate(5).color.xyz(0f, 1f,0f);
            vertexBufferAsArray.getOrCreate(6).color.xyz(0f, 0f,1f);
            vertexBufferAsArray.getOrCreate(7).color.xyz(1f, 0f,0.6f);

            vertexBuffer.getInput().setCurrentCount(8);

            var indexBufferArray = indexBuffer.getInput().getBackedArray();

            // Bottom
            indexBufferArray.getOrCreate(0).set((short) 0);
            indexBufferArray.getOrCreate(1).set((short) 1);
            indexBufferArray.getOrCreate(2).set((short) 2);

            indexBufferArray.getOrCreate(3).set((short) 0);
            indexBufferArray.getOrCreate(4).set((short) 1);
            indexBufferArray.getOrCreate(5).set((short) 3);

            // Top
            indexBufferArray.getOrCreate(6).set((short) 4);
            indexBufferArray.getOrCreate(7).set((short) 5);
            indexBufferArray.getOrCreate(8).set((short) 6);

            indexBufferArray.getOrCreate(9).set((short) 4);
            indexBufferArray.getOrCreate(10).set((short) 5);
            indexBufferArray.getOrCreate(11).set((short) 7);

            // X
            indexBufferArray.getOrCreate(12).set((short) 1);
            indexBufferArray.getOrCreate(13).set((short) 3);
            indexBufferArray.getOrCreate(14).set((short) 5);

            indexBufferArray.getOrCreate(15).set((short) 1);
            indexBufferArray.getOrCreate(16).set((short) 3);
            indexBufferArray.getOrCreate(17).set((short) 7);

            // -X
            indexBufferArray.getOrCreate(18).set((short) 0);
            indexBufferArray.getOrCreate(19).set((short) 2);
            indexBufferArray.getOrCreate(20).set((short) 4);

            indexBufferArray.getOrCreate(21).set((short) 0);
            indexBufferArray.getOrCreate(22).set((short) 2);
            indexBufferArray.getOrCreate(23).set((short) 6);

            // Z
            indexBufferArray.getOrCreate(24).set((short) 2);
            indexBufferArray.getOrCreate(25).set((short) 3);
            indexBufferArray.getOrCreate(26).set((short) 6);

            indexBufferArray.getOrCreate(27).set((short) 2);
            indexBufferArray.getOrCreate(28).set((short) 3);
            indexBufferArray.getOrCreate(29).set((short) 7);

            // -Z: Done
            indexBufferArray.getOrCreate(30).set((short) 1);
            indexBufferArray.getOrCreate(31).set((short) 0);
            indexBufferArray.getOrCreate(32).set((short) 4);

            indexBufferArray.getOrCreate(33).set((short) 1);
            indexBufferArray.getOrCreate(34).set((short) 4);
            indexBufferArray.getOrCreate(35).set((short) 5);

            indexBuffer.getInput().setCurrentCount(36);


            Image.copy(grassSide, grassSideSample.getInput().getBackedStruct());
            var samplerFuture = engine.getTransientCommandPool().submitSingleTimeCommand(stack, buf -> {
                grassSideSample.getOutput().getImage().transitionLayoutCommand(stack, buf, VkImageLayout.TRANSFER_DST_OPTIMAL);
                grassSideSample.bufferCopyCommand(stack, buf);
                grassSideSample.getOutput().getImage().transitionLayoutCommand(stack, buf, VkImageLayout.SHADER_READ_ONLY_OPTIMAL);
            });

            try {
                samplerFuture.getResult();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        public @NotNull Image loadGrassSide() {
            try {
                return PNGReader.readFromResource("textures/grass/grass_side.png");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public void updateUniformBuffer(int index) {
            ModelViewProjection mvp = uniformBuffer.getInput(index).getBackedStruct();

            double secondsPast = (System.currentTimeMillis() - startTime) / 1000d;
            mvp.model.put(3,3, 1f);
            VMath.diagonalMatrix(1f, true, mvp.model);
            VMath.rotationMatrix((float) (secondsPast * 1.57f), VMath.normalize(new ABFloat3(0f,0,1), new ABFloat3()),mvp.model);

            CameraMatrix cam = new CameraMatrix(new ABFloat4x4(), mvp.view);
            cam.position().xyz(2, 2, 2);
            cam.lookAt(new ABFloat3(0, 0, 0));
            cam.calculateViewMatrix();


            VMath.projectionMatrix(
                    (float) swapChain.getExtend().width() / swapChain.getExtend().height(),
                    4f, 4f,
                    .01f, 10f,
                    true, 1.3f, mvp.projection
            );

            // calcVerticesOnCPU(mvp.model, mvp.view, mvp.projection);
        }
        
        private void calcVerticesOnCPU(Float4x4 model, Float4x4 view, Float4x4 proj) {
            Float4 result = new ABFloat4();

            int i = 0;
            for (SimpleVertex vertex : vertexBufferAsArray) {
                ABFloat4 pos = new ABFloat4(1, 1, 1, 1);
                pos.xyz(vertex.position);
                VMath.multiply(model, pos, result);
                VMath.multiply(view, result, result);
                VMath.multiply(proj, result, result);

                System.out.println("verex[" + i++ +"]: " + result);
            }
        }

        @Override
        void render(
                @NotNull Stack stack,
                @NotNull VkInstance vkInstance,
                @NotNull Extend2D extend,
                int frameBufferIndex,
                int currentFrame,
                @NotNull VkCommandBuffer commandBuffer,
                @NotNull VkFramebuffer frameBuffer
        ) {
            updateUniformBuffer(currentFrame);

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

            vertexBuffer.bufferCopyCommand(stack, commandBuffer);
            indexBuffer.bufferCopyCommand(stack, commandBuffer);

            vkInstance.vkCmdBeginRenderPass(commandBuffer, ref(renderPassBeginInfo), VkSubpassContents.INLINE);
            vkInstance.vkCmdBindPipeline(commandBuffer, VkPipelineBindPoint.GRAPHICS, pipeLine.getVkPipeline());
            vkInstance.vkCmdBindVertexBuffers(commandBuffer, 0, 1, ref(vertexBuffer.getVkBuffer()), ref(vertexBuffer.getOffset()));
            vkInstance.vkCmdBindIndexBuffer(commandBuffer, indexBuffer.getVkBuffer(), indexBuffer.getOffset(), VkIndexType.UINT16);
            vkInstance.vkCmdSetViewport(commandBuffer, 0, 1, ref(viewport));
            vkInstance.vkCmdSetScissor(commandBuffer, 0, 1, ref(scissors));
            uniformBuffer.bindCommand(stack, commandBuffer, pipeLine, currentFrame);
            vkInstance.vkCmdDrawIndexed(commandBuffer, indexBuffer.getCurrentCount(), 1, 0, 0, 0);
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
                            Objects.requireNonNull(this.getClass().getResourceAsStream("/de/linusdev/cvg4j/vulkan/shaders/vulkanTest3.vert.spv")),
                            "main",
                            Structure.allocate(new VkShaderModule())
                    );
                }

                @Override
                public @NotNull VulkanShader loadFragmentShader() throws IOException {
                    return VulkanShader.createFromSpirVBinaryStream(
                            stack,
                            engine,
                            Objects.requireNonNull(this.getClass().getResourceAsStream("/de/linusdev/cvg4j/vulkan/shaders/vulkanTest3.frag.spv")),
                            "main",
                            Structure.allocate(new VkShaderModule())
                    );
                }

                @Override
                public @NotNull BiResult<VertexBuffer<?>, IndexBuffer<?>> getVertexAndIndexBuffer() throws EngineException {
                    return new BiResult<>(vertexBuffer, indexBuffer);
                }

                @Override
                public @NotNull UniformBuffer<?> getUniformBuffer() {
                    return uniformBuffer;
                }
            };
        }

        @Override
        public void tick() {
            if(true) return;
            float factor = 0.61f;
            for (SimpleVertex simpleVertex : vertexBufferAsArray) {
//                VMath.add(
//                        simpleVertex.color,
//                        new ABFloat3((float) ((Math.random() - 0.5f) * factor), (float) ((Math.random() - 0.5f) * factor), (float) ((Math.random() - 0.5f) * factor)),
//                        simpleVertex.color
//                );

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
            uniformBuffer.close();
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