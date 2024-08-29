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

package de.linusdev.cvg4j.engine.vk.extension;

import de.linusdev.lutils.nat.pointer.BBTypedPointer64;
import de.linusdev.lutils.nat.string.NullTerminatedUTF8String;
import de.linusdev.lutils.nat.struct.array.StructureArray;
import de.linusdev.lutils.nat.struct.array.StructureArraySupplier;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.function.Function;

public class VulkanExtensionList {

    HashMap<String, VulkanExtension> map = new HashMap<>();

    public VulkanExtensionList() {

    }

    /**
     * Adds the extension if it does not exist. If it already exists, its version will
     * be to the highest of the two.
     */
    public void add(@NotNull VulkanExtension extension) {
        VulkanExtension inMap = map.computeIfAbsent(extension.extensionName(), string -> extension);

        if(inMap.version() < extension.version())
            map.put(extension.extensionName(), extension);
    }

    public void addAll(@NotNull Collection<VulkanExtension> extensions) {
        for (VulkanExtension ext : extensions) {
            add(ext);
        }
    }

    public void addAll(@NotNull VulkanExtension @NotNull [] extensions) {
        for (VulkanExtension ext : extensions) {
            add(ext);
        }
    }

    /**
     *
     * @return {@code true} if it contains given extension and its version is
     * {@link VulkanExtension#isSufficient(VulkanExtension, VulkanExtension) sufficient}, {@code false} otherwise.
     */
    public boolean contains(@NotNull VulkanExtension extension) {
        VulkanExtension inMap = map.get(extension.extensionName());

       return VulkanExtension.isSufficient(extension, inMap);
    }

    public StructureArray<BBTypedPointer64<NullTerminatedUTF8String>> toNativeArray(
            @NotNull StructureArraySupplier<BBTypedPointer64<NullTerminatedUTF8String>> arraySupplier,
            @NotNull Function<String, NullTerminatedUTF8String> stringSupplier
    ) {
        StructureArray<BBTypedPointer64<NullTerminatedUTF8String>> natArray = arraySupplier.supply(this.map.size(), BBTypedPointer64.class, () -> BBTypedPointer64.newAllocatable1(null));

        int i = 0;
        for (String ext : this.map.keySet()) {
            natArray.getOrCreate(i++).set(stringSupplier.apply(ext));
        }

        return natArray;
    }

}
