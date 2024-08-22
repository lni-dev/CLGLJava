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

package de.linusdev.cvg4j.nengine.vulkan.selector;

import de.linusdev.cvg4j.nat.vulkan.VulkanApiVersion;
import de.linusdev.cvg4j.nengine.exception.EngineException;
import de.linusdev.cvg4j.nengine.vulkan.extension.VulkanExtension;
import de.linusdev.llog.LLog;
import de.linusdev.llog.base.LogInstance;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class VulkanRequirements {

    private final static @NotNull LogInstance LOG = LLog.getLogInstance();

    private final @NotNull VulkanApiVersion minRequiredInstanceVersion;
    private final @NotNull List<VulkanExtension> requiredInstanceExtensions;



    public VulkanRequirements(
            @NotNull VulkanApiVersion minRequiredInstanceVersion,
            @NotNull List<VulkanExtension> requiredInstanceExtensions
    ) {
        this.minRequiredInstanceVersion = minRequiredInstanceVersion;
        this.requiredInstanceExtensions = requiredInstanceExtensions;
    }

    public @NotNull VulkanApiVersion getMinRequiredInstanceVersion() {
        return minRequiredInstanceVersion;
    }

    public void checkMinRequiredInstanceVersion(@NotNull VulkanEngineInfo info) throws EngineException {
        LOG.logDebug("Checking vulkan api version. minRequiredInstanceVersion: " + minRequiredInstanceVersion + ", maxAvailableApiVersion: " + info.getMaxInstanceVulkanApiVersion());
        if(info.getMaxInstanceVulkanApiVersion().compareTo(minRequiredInstanceVersion) < 0)
            throw new EngineException(minRequiredInstanceVersion + " is not supported. Maximum supported is: " + info.getMaxInstanceVulkanApiVersion());

    }

    public void checkRequiredInstanceExtensions(@NotNull VulkanEngineInfo info) throws EngineException {
        LOG.logDebug("Checking if the required instance extensions are available. Required instance extensions: " + requiredInstanceExtensions);

        for (VulkanExtension reqExt : requiredInstanceExtensions) {
            for (VulkanExtension availableExt : info.getAvailableInstanceExtensions()) {
                if (VulkanExtension.isSufficient(reqExt, availableExt)) {
                    break;
                }
            }

            throw new EngineException("Instance extension '" +  reqExt + "' is not available. available extensions: " + Arrays.toString(info.getAvailableInstanceExtensions()));
        }
    }
}
