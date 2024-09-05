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

package de.linusdev.cvg4j.engine.vk.pipeline;

import de.linusdev.cvg4j.engine.exception.EngineException;
import de.linusdev.cvg4j.engine.vk.memory.buffer.index.IndexBuffer;
import de.linusdev.cvg4j.engine.vk.memory.buffer.uniform.UniformBuffer;
import de.linusdev.cvg4j.engine.vk.memory.buffer.vertex.VertexBuffer;
import de.linusdev.cvg4j.engine.vk.shader.VulkanShader;
import de.linusdev.lutils.result.BiResult;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public interface RasterizationPipelineInfo {

    @NotNull VulkanShader loadVertexShader() throws IOException;

    @NotNull VulkanShader loadFragmentShader() throws IOException;

    @NotNull BiResult<VertexBuffer<?>, IndexBuffer<?>> getVertexAndIndexBuffer() throws EngineException;

    @NotNull UniformBuffer<?> getUniformBuffer();

}
