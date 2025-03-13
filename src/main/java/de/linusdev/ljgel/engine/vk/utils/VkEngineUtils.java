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

package de.linusdev.ljgel.engine.vk.utils;

import de.linusdev.ljgel.engine.exception.EngineException;
import de.linusdev.ljgel.engine.vk.VulkanGame;
import de.linusdev.ljgel.engine.vk.device.Device;
import de.linusdev.ljgel.engine.vk.device.GPUInfo;
import de.linusdev.ljgel.engine.vk.device.SurfaceInfo;
import de.linusdev.ljgel.engine.vk.instance.Instance;
import de.linusdev.ljgel.engine.vk.selector.gpu.GPUSelectionProgress;
import de.linusdev.ljgel.engine.vk.selector.swapchain.HasSwapChainSelectors;
import de.linusdev.ljgel.engine.vk.swapchain.Extend2D;
import de.linusdev.ljgel.engine.vk.swapchain.SwapChain;
import de.linusdev.ljgel.engine.vk.swapchain.SwapChainBuilder;
import de.linusdev.ljgel.engine.vk.window.VulkanWindow;
import de.linusdev.ljgel.nat.vulkan.handles.VkInstance;
import de.linusdev.ljgel.nat.vulkan.handles.VkPhysicalDevice;
import de.linusdev.ljgel.nat.vulkan.structs.VkExtent2D;
import de.linusdev.llog.base.impl.StandardLogLevel;
import de.linusdev.llog.base.impl.data.ThrowableLogData;
import de.linusdev.lutils.interfaces.TRunnable;
import de.linusdev.lutils.math.VMath;
import de.linusdev.lutils.math.vector.buffer.intn.BBInt2;
import de.linusdev.lutils.math.vector.buffer.intn.BBUInt1;
import de.linusdev.lutils.math.vector.buffer.intn.BBUInt2;
import de.linusdev.lutils.nat.memory.stack.Stack;
import de.linusdev.lutils.nat.struct.array.StructureArray;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static de.linusdev.ljgel.engine.vk.VulkanEngine.LOG;
import static de.linusdev.lutils.nat.pointer.TypedPointer64.ofArray;
import static de.linusdev.lutils.nat.pointer.TypedPointer64.ref;
import static de.linusdev.lutils.nat.struct.abstracts.Structure.unionWith;

public class VkEngineUtils {

    public static @NotNull Device selectAndCreateDevice(
            @NotNull Stack stack,
            @NotNull VulkanGame game,
            @NotNull Instance instance,
            @NotNull VulkanWindow window
    ) throws EngineException {
        @NotNull VkInstance vkInstance = instance.getVkInstance();

        try (var ignored = stack.popPoint()) {
            BBUInt1 integer = stack.pushUnsignedInt();
            vkInstance.vkEnumeratePhysicalDevices(ref(integer), ref(null)).check();

            StructureArray<VkPhysicalDevice> vkPhysicalDevices = stack.pushArray(integer.get(), VkPhysicalDevice.class, VkPhysicalDevice::new);
            vkInstance.vkEnumeratePhysicalDevices(ref(integer), ofArray(vkPhysicalDevices)).check();


            GPUSelectionProgress progress = game.gpuSelector().startSelection();

            GPUInfo gpuInfo = new GPUInfo(stack);
            @Nullable VkPhysicalDevice lastChecked = null;
            for (VkPhysicalDevice dev : vkPhysicalDevices) {
                if(progress.canSelectionStop()) break;
                lastChecked = dev;

                gpuInfo.fillOfDevice(stack, vkInstance, dev, window.getVkSurface());

                // calculate gpu priority
                int priority = progress.addGpu(dev, gpuInfo);
                LOG.debug("Checking gpu '"+ gpuInfo.props.deviceName.get() + "': " + priority);

            }

            VkPhysicalDevice best = progress.getBestGPU();

            if(best == null)
                throw new EngineException("No suitable gpu available.");

            // get the gpu information again (if required)...
            if(lastChecked != best) {
                gpuInfo.fillOfDevice(stack, vkInstance, best, window.getVkSurface());
            }

            LOG.debug("Selected gpu: " + gpuInfo.props.deviceName.get());

            // Create device
            Device device = Device.create(
                    stack,
                    vkInstance,
                    gpuInfo.vkPhysicalDevice,
                    game.queueFamilySelector().selectGraphicsQueue(gpuInfo.queueFamilyInfoList).result1().index(),
                    game.queueFamilySelector().selectPresentationQueue(gpuInfo.queueFamilyInfoList).result1().index(),
                    game.requiredDeviceExtensions(),
                    game.activatedVulkanLayers()
            );

            LOG.debug("Device created");

            return device;
        }
    }

    public static @NotNull SwapChain createSwapChain(
            @NotNull Stack stack,
            @NotNull HasSwapChainSelectors selectors,
            @NotNull Instance instance,
            @NotNull VulkanWindow window,
            @NotNull Device device
    ) throws EngineException {
        try (var ignored = stack.popPoint()) {
            SwapChainBuilder builder = fillSwapChainBuilder(stack, instance.getVkInstance(), device, window, selectors);
            SwapChain swapChain = builder.buildSwapChain(stack, window, instance.getVkInstance(), device);
            LOG.debug("SwapChain created");
            return swapChain;
        }
    }

    /**
     * Must be used in combination with {@link Stack#popPoint()}!
     */
    public static @NotNull SwapChainBuilder fillSwapChainBuilder(
            @NotNull Stack stack,
            @NotNull VkInstance vkInstance,
            @NotNull Device device,
            @NotNull VulkanWindow window,
            @NotNull HasSwapChainSelectors selectors
    ) throws EngineException {
        SurfaceInfo surfaceInfo = new SurfaceInfo(stack);
        surfaceInfo.fillOfDevice(stack, vkInstance, device.getVkPhysicalDevice(), window.getVkSurface());

        SwapChainBuilder builder = new SwapChainBuilder()
                .setSurfaceFormat(
                        selectors.surfaceFormatSelector().select(surfaceInfo.surfaceFormatCount, surfaceInfo.surfaceFormats).result1()
                ).setPresentMode(
                        selectors.presentModeSelector().select(surfaceInfo.presentModeCount, surfaceInfo.presentModes).result1()
                );


        // Calculate swap extend
        LOG.debug("Calculate swap extend");
        Extend2D swapChainExtend = new Extend2D(stack.push(new VkExtent2D()));
        if(surfaceInfo.surfacesCaps.currentExtent.width.get() != 0xFFFFFFFF) {
            LOG.debug("Swap extend is fixed");
            swapChainExtend.xy(
                    surfaceInfo.surfacesCaps.currentExtent.width.get(),
                    surfaceInfo.surfacesCaps.currentExtent.height.get()
            );
        } else {
            LOG.debug("Swap extend is not fixed, select it based on frame buffer size.");
            BBInt2 size = unionWith(BBInt2.newAllocatable(null), swapChainExtend);
            window.getFrameBufferSize(size);

            BBUInt2 maxImageExtend = unionWith(BBUInt2.newAllocatable(null), surfaceInfo.surfacesCaps.maxImageExtent);
            BBUInt2 minImageExtend = unionWith(BBUInt2.newAllocatable(null), surfaceInfo.surfacesCaps.minImageExtent);

            VMath.clamp(size, minImageExtend, maxImageExtend, size);
        }

        builder.setSwapExtend(swapChainExtend);

        // Swap chain image count
        int max = surfaceInfo.surfacesCaps.maxImageCount.get();
        int min = surfaceInfo.surfacesCaps.minImageCount.get();
        builder.setSwapChainImageCount(selectors.swapChainImageCountSelector().select(min, max == 0 ? Integer.MAX_VALUE : max));

        // Set surface transform (current is fine)
        builder.setSurfaceTransform(surfaceInfo.surfacesCaps.currentTransform);

        return builder;
    }

    /**
     * Runs given {@code runnable}. If the runnable throws an {@link InterruptedException},
     * the exception will be logged and the method returns early.
     * @param runnable runnable to run.
     */
    public static void ignoreInterrupts(TRunnable<InterruptedException> runnable) {
        try {
            runnable.run();
        } catch (InterruptedException e) {
            LOG.warning("InterruptedException in ignoreInterrupts:");
            LOG.log(StandardLogLevel.WARNING, new ThrowableLogData(e));
        }
    }

}
