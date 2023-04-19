/*
 * Copyright (c) 2023 Linus Andera
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

package de.linusdev.clgl.nat.cl.objects;

import de.linusdev.clgl.nat.cl.CL;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("unused")
public class Platform {

    private static List<Platform> platforms = null;

    private final long pointer;

    Platform(long pointer) {

        this.pointer = pointer;
    }

    public static List<Platform> getPlatforms() {
        if(platforms == null) {
            platforms = new ArrayList<>(5);
            for(Long p : CL.getPlatformIDs())
                platforms.add(new Platform(p));
        }

        return Collections.unmodifiableList(platforms);
    }

    public @NotNull List<Device> getDevices(@NotNull CL.DeviceType type) {
        List<Device> devices = new ArrayList<>(2);

        for(long p : CL.getDeviceIDs(pointer, type))
            devices.add(new Device(p));

        return Collections.unmodifiableList(devices);
    }

    /**
     * @see CL.PlatformInfo#CL_PLATFORM_NAME
     * @return platform name
     */
    public String getPlatformName() {
        return CL.getPlatformInfoString(getPointer(), CL.PlatformInfo.CL_PLATFORM_NAME);
    }

    /**
     * @see CL.PlatformInfo#CL_PLATFORM_VERSION
     * @return platform version
     */
    public String getPlatformVersion() {
        return CL.getPlatformInfoString(getPointer(), CL.PlatformInfo.CL_PLATFORM_VERSION);
    }

    public long getPointer() {
        return pointer;
    }
}
