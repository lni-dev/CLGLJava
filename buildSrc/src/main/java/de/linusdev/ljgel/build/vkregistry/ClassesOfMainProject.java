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

package de.linusdev.ljgel.build.vkregistry;

import de.linusdev.lutils.codegen.java.JavaClass;
import de.linusdev.lutils.codegen.java.JavaMethod;
import de.linusdev.lutils.nat.pointer.Pointer64;
import org.jetbrains.annotations.NotNull;

public interface ClassesOfMainProject {

    @NotNull JavaClass VULKAN_UTILS_CLASS = JavaClass.custom("de.linusdev.ljgel.nat.vulkan.utils", "VulkanUtils");
    @NotNull JavaClass RETURNED_VK_RESULT_CLASS = JavaClass.custom("de.linusdev.ljgel.nat.vulkan", "ReturnedVkResult");
    @NotNull JavaClass GLFW3_CLASS = JavaClass.custom("de.linusdev.ljgel.nat.glfw3", "GLFW");
    @NotNull JavaClass UNKNOWN_STRUCT_CLASS = JavaClass.custom("de.linusdev.ljgel.nat", "UnknownStruct");

    @NotNull JavaClass POINTER64_CLASS = JavaClass.ofClass(Pointer64.class);
    @NotNull JavaMethod POINTER64_METHOD_REQUIRE_NOT_NULL = JavaMethod.of(POINTER64_CLASS, JavaClass.ofClass(Boolean.class), "requireNotNull", true);


}
