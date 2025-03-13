/*
 * Copyright (c) 2024-2025 Linus Andera
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

package de.linusdev.ljgel.nat.glfw3.custom;

import de.linusdev.ljgel.nat.glfw3.GLFWValues;
import org.jetbrains.annotations.Nullable;

public enum RenderAPI {
    OPENGL(GLFWValues.ClientApis.GLFW_OPENGL_API),
    VULKAN(GLFWValues.ClientApis.GLFW_NO_API)
    ;

    private final @Nullable GLFWValues.ClientApis clientApi;

    RenderAPI(@Nullable GLFWValues.ClientApis clientApi) {
        this.clientApi = clientApi;
    }

    public @Nullable GLFWValues.ClientApis getClientApi() {
        return clientApi;
    }
}
