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

package de.linusdev.clgl.nat.cl.custom;

import de.linusdev.clgl.api.async.AsyncErrorTypes;
import de.linusdev.clgl.nat.cl.CL;
import de.linusdev.clgl.nat.cl.objects.Device;
import de.linusdev.clgl.nat.cl.objects.Program;
import de.linusdev.lutils.async.error.AsyncError;
import de.linusdev.lutils.async.error.ErrorType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class BuildErrorException extends Exception implements AsyncError {

    private final @NotNull Device @NotNull [] devices;
    private final @NotNull String @NotNull [] buildLogs;

    public BuildErrorException(@NotNull Device @NotNull [] devices, @NotNull String @NotNull [] buildLogs) {
        this.devices = devices;
        this.buildLogs = buildLogs;
    }

    public static @Nullable BuildErrorException check(
            @NotNull Program program,
            @NotNull Device @NotNull [] devices
    ) {
        ArrayList<Device> devs = new ArrayList<>(devices.length);
        ArrayList<String> logs = new ArrayList<>(devices.length);

        for(Device device : devices) {
            if(program.getBuildStatus(device) == CL.BuildStatus.CL_BUILD_ERROR) {
                devs.add(device);
                logs.add(program.getBuildLog(device));
            }
        }

        if(devs.size() == 0)
            return null;

        return new BuildErrorException(
                devs.toArray(Device[]::new),
                logs.toArray(String[]::new)
        );
    }

    @Override
    public @NotNull String getMessage() {
        StringBuilder sb = new StringBuilder();

        for(int i = 0; i < devices.length; i++) {
            sb.append("Device: ").append(devices[i].getName()).append("\n");
            sb.append("Log:\n").append(buildLogs[i]).append("\n\n\n");
        }

        return sb.toString();
    }

    @Override
    public @NotNull Throwable getThrowable() {
        return this;
    }

    @Override
    public @NotNull ErrorType getType() {
        return AsyncErrorTypes.BUILD_ERROR;
    }


}
