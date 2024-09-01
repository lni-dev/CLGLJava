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

package de.linusdev.cvg4j.nat.vulkan;

import de.linusdev.cvg4j.nat.vulkan.custom.VulkanException;
import de.linusdev.cvg4j.nat.vulkan.enums.VkResult;
import org.jetbrains.annotations.NotNull;

public class ReturnedVkResult {

    private final int result;

    public ReturnedVkResult(int result) {
        this.result = result;
    }

    public int get() {
        return result;
    }

    public @NotNull VkResult getAsVkResult() {
        for (VkResult value : VkResult.values()) {
            if(value.getValue() == result)
                return value;
        }

        throw new Error("Unknown VkResult with value=" + result);
    }

    public void check() throws VulkanException {
        if(result != VkResult.VK_SUCCESS.getValue())
            throw new VulkanException(null, getAsVkResult());
    }

    public void checkButAllow(@NotNull VkResult allow) throws VulkanException {
        if(result != VkResult.VK_SUCCESS.getValue() && result != allow.getValue())
            throw new VulkanException(null, getAsVkResult());
    }

    public boolean is(@NotNull VkResult result) {
        return this.result == result.getValue();
    }
}
