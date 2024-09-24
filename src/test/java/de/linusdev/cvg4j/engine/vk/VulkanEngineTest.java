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
import de.linusdev.cvg4j.engine.scene.Loader;
import de.linusdev.cvg4j.engine.vk.descriptor.pool.DescriptorSet;
import de.linusdev.cvg4j.engine.vk.descriptor.pool.FixedSizeDescriptorPool;
import de.linusdev.cvg4j.engine.vk.extension.VulkanExtension;
import de.linusdev.cvg4j.engine.vk.memory.buffer.index.IndexBuffer;
import de.linusdev.cvg4j.engine.vk.memory.buffer.uniform.UniformBuffer;
import de.linusdev.cvg4j.engine.vk.memory.buffer.vertex.SimpleVertex;
import de.linusdev.cvg4j.engine.vk.memory.buffer.vertex.VertexBuffer;
import de.linusdev.cvg4j.engine.vk.memory.buffer.vertex.VertexElement;
import de.linusdev.cvg4j.engine.vk.memory.image.sampler.Sampler2D;
import de.linusdev.cvg4j.engine.vk.memory.manager.allocator.ondemand.OnDemandVulkanMemoryAllocator;
import de.linusdev.cvg4j.engine.vk.pipeline.RasterizationPipeline;
import de.linusdev.cvg4j.engine.vk.pipeline.RasterizationPipelineInfo;
import de.linusdev.cvg4j.engine.vk.renderpass.RenderPass;
import de.linusdev.cvg4j.engine.vk.scene.VkScene;
import de.linusdev.cvg4j.engine.vk.shader.VulkanShader;
import de.linusdev.cvg4j.engine.vk.swapchain.Extend2D;
import de.linusdev.cvg4j.engine.window.input.Key;
import de.linusdev.cvg4j.nat.glfw3.GLFWValues;
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
import de.linusdev.lutils.image.buffer.BBInt32Image;
import de.linusdev.lutils.image.png.reader.PNGReader;
import de.linusdev.lutils.math.VMath;
import de.linusdev.lutils.math.matrix.abstracts.floatn.Float4x4;
import de.linusdev.lutils.math.matrix.array.floatn.ABFloat4x4;
import de.linusdev.lutils.math.special.CameraMatrix;
import de.linusdev.lutils.math.vector.abstracts.floatn.Float3;
import de.linusdev.lutils.math.vector.abstracts.floatn.Float4;
import de.linusdev.lutils.math.vector.array.floatn.ABFloat3;
import de.linusdev.lutils.math.vector.array.floatn.ABFloat4;
import de.linusdev.lutils.math.vector.buffer.shortn.BBUShort1;
import de.linusdev.lutils.nat.memory.stack.Stack;
import de.linusdev.lutils.nat.memory.stack.impl.DirectMemoryStack64;
import de.linusdev.lutils.nat.size.ByteUnits;
import de.linusdev.lutils.nat.size.Size;
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
        public @NotNull VkScene<?> startScene(@NotNull VulkanEngine<?> engine) {
            return new TestScene((VulkanEngine<TestGame>) engine, 1.5f);
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

        @Override
        public boolean logValidationLayerMessages() {
            return true;
        }
    }

    static class TestScene extends VkScene<TestGame> {

        private final float rotationFactor;

        protected final long startTime = System.currentTimeMillis();

        protected OnDemandVulkanMemoryAllocator vulkanMemoryAllocator;
        protected FixedSizeDescriptorPool descriptorPool;
        protected VertexBuffer<SimpleVertex> vertexBuffer;
        protected IndexBuffer<BBUShort1> indexBuffer;
        protected StructureArray<SimpleVertex> vertexBufferAsArray;

        protected UniformBuffer<ModelViewProjection> uniformBuffer;
        protected Sampler2D<BBInt32Image> grassSideSampler;

        private final Float3 cameraPosition = new ABFloat3(2, 2, -2);

        public TestScene(@NotNull VulkanEngine<TestGame> engine, float rotationFactor) {
            super(engine);
            this.rotationFactor = rotationFactor;
        }

        private void load(@NotNull Stack stack) throws EngineException, IOException, InterruptedException {
            window.setWindowAspectRatio(1, 1);

            vulkanMemoryAllocator = new OnDemandVulkanMemoryAllocator(engine.getDevice(), "test-scene-memory-allocator");
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
            grassSideSampler = vulkanMemoryAllocator.createStagedSampler(
                    stack, "grass-texture",0, grassSide, VkImageLayout.SHADER_READ_ONLY_OPTIMAL, true, true
            );

            vulkanMemoryAllocator.allocate(stack);




            vertexBufferAsArray = vertexBuffer.getInput().getBackedArray();

            vertexBufferAsArray.get(0).position.xyz(-.5f, -.5f,-.5f);
            vertexBufferAsArray.get(1).position.xyz(0.5f, -.5f,-.5f);
            vertexBufferAsArray.get(2).position.xyz(-.5f, -.5f,0.5f);
            vertexBufferAsArray.get(3).position.xyz(0.5f, -.5f,0.5f);

            vertexBufferAsArray.get(4).position.xyz(-.5f, 0.5f,-.5f);
            vertexBufferAsArray.get(5).position.xyz(0.5f, 0.5f,-.5f);
            vertexBufferAsArray.get(6).position.xyz(-.5f, 0.5f,0.5f);
            vertexBufferAsArray.get(7).position.xyz(0.5f, 0.5f,0.5f);

            vertexBufferAsArray.get(0).color.xyz(0.5f, 0.5f,0.5f);
            vertexBufferAsArray.get(1).color.xyz(0f, 1f,0f);
            vertexBufferAsArray.get(2).color.xyz(0f, 0f,1f);
            vertexBufferAsArray.get(3).color.xyz(1f, 0f,0.6f);

            vertexBufferAsArray.get(4).color.xyz(0.5f, 0.5f,0.5f);
            vertexBufferAsArray.get(5).color.xyz(0f, 1f,0f);
            vertexBufferAsArray.get(6).color.xyz(0f, 0f,1f);
            vertexBufferAsArray.get(7).color.xyz(1f, 0f,0.6f);

            vertexBufferAsArray.get(0).texCoord.xy(1, 1); // Done
            vertexBufferAsArray.get(1).texCoord.xy(0, 1); // Done
            vertexBufferAsArray.get(2).texCoord.xy(1, 1);
            vertexBufferAsArray.get(3).texCoord.xy(0, 1);

            vertexBufferAsArray.get(4).texCoord.xy(1, 0); // Done
            vertexBufferAsArray.get(5).texCoord.xy(0, 0); // Done
            vertexBufferAsArray.get(6).texCoord.xy(1, 0);
            vertexBufferAsArray.get(7).texCoord.xy(0, 0);

            vertexBuffer.getInput().setCurrentCount(8);

            var indexBufferArray = indexBuffer.getInput().getBackedArray();

            // Bottom: Done
            indexBufferArray.get(0).set((short) 0);
            indexBufferArray.get(1).set((short) 1);
            indexBufferArray.get(2).set((short) 2);

            indexBufferArray.get(3).set((short) 2);
            indexBufferArray.get(4).set((short) 1);
            indexBufferArray.get(5).set((short) 3);

            // Top: Done
            indexBufferArray.get(6).set((short) 6);
            indexBufferArray.get(7).set((short) 5);
            indexBufferArray.get(8).set((short) 4);

            indexBufferArray.get(9).set((short) 7);
            indexBufferArray.get(10).set((short) 5);
            indexBufferArray.get(11).set((short) 6);

            // X: Done
            indexBufferArray.get(12).set((short) 5);
            indexBufferArray.get(13).set((short) 3);
            indexBufferArray.get(14).set((short) 1);

            indexBufferArray.get(15).set((short) 7);
            indexBufferArray.get(16).set((short) 3);
            indexBufferArray.get(17).set((short) 5);

            // -X: Done
            indexBufferArray.get(18).set((short) 0);
            indexBufferArray.get(19).set((short) 2);
            indexBufferArray.get(20).set((short) 4);

            indexBufferArray.get(21).set((short) 4);
            indexBufferArray.get(22).set((short) 2);
            indexBufferArray.get(23).set((short) 6);

            // Z: Done
            indexBufferArray.get(24).set((short) 2);
            indexBufferArray.get(25).set((short) 3);
            indexBufferArray.get(26).set((short) 6);

            indexBufferArray.get(27).set((short) 6);
            indexBufferArray.get(28).set((short) 3);
            indexBufferArray.get(29).set((short) 7);

            // -Z: Done
            indexBufferArray.get(30).set((short) 1);
            indexBufferArray.get(31).set((short) 0);
            indexBufferArray.get(32).set((short) 4);

            indexBufferArray.get(33).set((short) 1);
            indexBufferArray.get(34).set((short) 4);
            indexBufferArray.get(35).set((short) 5);

            indexBuffer.getInput().setCurrentCount(36);

            descriptorPool = new FixedSizeDescriptorPool(engine.getVkInstance(), engine.getDevice());
            descriptorPool.add(new DescriptorSet(vkInstance, device, 0, uniformBuffer));
            descriptorPool.add(new DescriptorSet(vkInstance, device, 1, grassSideSampler));

            descriptorPool.create(stack);

            Image.copy(grassSide, grassSideSampler.getInput().getBackedStruct());
            var samplerFuture = engine.getTransientCommandPool().submitSingleTimeCommand(buf -> {
                grassSideSampler.bufferCopyCommand(stack, buf, true);
            });

            samplerFuture.getResult();


            renderPass = RenderPass.create(stack, vkInstance, device, swapChain);
            pipeLine = RasterizationPipeline.create(stack, vkInstance, device, swapChain, renderPass, pipeline(stack));
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
            VMath.rotationMatrix((float) (secondsPast * rotationFactor), VMath.normalize(new ABFloat3(0f,0,1), new ABFloat3()),mvp.model);

            CameraMatrix cam = new CameraMatrix(new ABFloat4x4(), mvp.view);
            cam.position().xyz(cameraPosition);
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

                System.out.println("vertex[" + i++ +"]: " + result);
            }
        }

        @Override
        protected void render(
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

            var clearValueArray = stack.pushArray(2, VkClearValue.class, VkClearValue::new);

            // Color attachment clear values
            VkClearValue vkClearValue = clearValueArray.get(0);
            vkClearValue.color.float32.get(0).set(0f);
            vkClearValue.color.float32.get(1).set(0f);
            vkClearValue.color.float32.get(2).set(0f);
            vkClearValue.color.float32.get(3).set(1f);

            // Depth and Stencil clear values
            vkClearValue = clearValueArray.get(1);
            vkClearValue.depthStencil.depth.set(1f);
            vkClearValue.depthStencil.stencil.set(0);

            VkRenderPassBeginInfo renderPassBeginInfo = stack.push(new VkRenderPassBeginInfo());
            renderPassBeginInfo.sType.set(VkStructureType.RENDER_PASS_BEGIN_INFO);
            renderPassBeginInfo.renderPass.set(pipeLine.getRenderPass().getVkRenderPass());
            renderPassBeginInfo.renderArea.offset.x.set(0);
            renderPassBeginInfo.renderArea.offset.y.set(0);
            renderPassBeginInfo.renderArea.extent.width.set(extend.width());
            renderPassBeginInfo.renderArea.extent.height.set(extend.height());
            renderPassBeginInfo.clearValueCount.set(clearValueArray.length());
            renderPassBeginInfo.pClearValues.setOfArray(clearValueArray);
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
            descriptorPool.bindCommand(stack, commandBuffer, pipeLine, uniformBuffer.getVkDescriptorSet(currentFrame), grassSideSampler.getVkDescriptorSet());
            vkInstance.vkCmdDrawIndexed(commandBuffer, indexBuffer.getCurrentCount(), 1, 0, 0, 0);
            vkInstance.vkCmdEndRenderPass(commandBuffer);
            vkInstance.vkEndCommandBuffer(commandBuffer).check();

            stack.pop(); // renderPassBeginInfo
            stack.pop(); // clearValueArray
            stack.pop(); // commandBufferBeginInfo
        }

        private @NotNull RasterizationPipelineInfo pipeline(@NotNull Stack stack) {

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
                public @NotNull FixedSizeDescriptorPool getDescriptorPool() {
                    return descriptorPool;
                }
            };
        }


        @Override
        public void tick() {
            Key KEY_W = engine.getInputManger().getUSKey(GLFWValues.Keys_US.GLFW_KEY_W);
            Key KEY_A = engine.getInputManger().getUSKey(GLFWValues.Keys_US.GLFW_KEY_A);
            Key KEY_S = engine.getInputManger().getUSKey(GLFWValues.Keys_US.GLFW_KEY_S);
            Key KEY_D = engine.getInputManger().getUSKey(GLFWValues.Keys_US.GLFW_KEY_D);

            if(engine.getInputManger().isKeyPressed(KEY_W)) {
                cameraPosition.z(cameraPosition.z() - 0.05f);
            } else if(engine.getInputManger().isKeyPressed(KEY_A)) {
                cameraPosition.x(cameraPosition.x() - 0.05f);
            } else if(engine.getInputManger().isKeyPressed(KEY_S)) {
                cameraPosition.z(cameraPosition.z() + 0.05f);
            }else if(engine.getInputManger().isKeyPressed(KEY_D)) {
                cameraPosition.x(cameraPosition.x() + 0.05f);
            }


            if(true) return;
            float factor = 0.01f;
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
        public void close() {
            descriptorPool.close();
            vulkanMemoryAllocator.close();
            super.close();
        }

        @Override
        public @NotNull Loader loader() {
            return new Loader() {
                @Override
                public void start() throws EngineException, IOException, InterruptedException {
                    load(new DirectMemoryStack64(new Size(10, ByteUnits.KiB)));
                }

                @Override
                public double progress() {
                    return 0;
                }

                @Override
                public void tick() {

                }
            };
        }

        @Override
        public @NotNull Loader releaser() {
            return new Loader() {
                @Override
                public void start() {

                }

                @Override
                public double progress() {
                    return 0;
                }

                @Override
                public void tick() {

                }
            };
        }
    }

    @Test
    void test() throws EngineException, InterruptedException {
        Engine.StaticSetup.setup();

        VulkanEngine<TestGame> engine = new VulkanEngine<>(new TestGame());

        Thread.sleep(2000);
        System.out.println("TEST LOAD ANOTHER SCENE");
        engine.loadScene(new TestScene(engine, -1.5f)).getResult().activate();

        engine.getEngineDeathFuture().getResult();
    }
}