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

import de.linusdev.clgl.api.structs.PrimitiveTypeArray;
import de.linusdev.clgl.nat.NativeUtils;
import de.linusdev.clgl.nat.cl.custom.ProgramBuild;
import de.linusdev.clgl.nat.cl.custom.ProgramBuildFuture;
import de.linusdev.clgl.nat.custom.StaticCallbackObjects;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static de.linusdev.clgl.nat.cl.CL.*;

@SuppressWarnings("unused")
public class Program implements AutoCloseable {

    public static final @NotNull StaticCallbackObjects<ProgramBuildFuture> buildFutures = new StaticCallbackObjects<>();

    protected final long pointer;

    public Program(@NotNull Context context, @NotNull String source) {
        pointer = clCreateProgramWithSource(context.getPointer(), source);
    }

    @Contract("_, false, _ -> null; _, true, _ -> !null")
    public @Nullable ProgramBuildFuture build(@NotNull String options, boolean async, @NotNull Device @NotNull ... devs) {
        PrimitiveTypeArray<Long> devices = new PrimitiveTypeArray<>(Long.class, devs.length, true);

        for(int i = 0; i < devs.length; i++)
            devices.set(i, devs[i].getPointer());

        if(!async) {
            clBuildProgram(pointer, devices, options, NativeUtils.getNullPointer());
            return null;
        }

        ProgramBuildFuture future = new ProgramBuildFuture(buildFutures, this, new ProgramBuild(this, devs));
        clBuildProgram(pointer, devices, options, future.getId());
        return future;
    }

    public String getBuildLog(@NotNull Device device) {
        return clGetProgramBuildInfoString(pointer, device.getPointer(), ProgramBuildInfo.CL_PROGRAM_BUILD_LOG);
    }

    public BuildStatus getBuildStatus(@NotNull Device device) {
        int status = clGetProgramBuildInfoInt(pointer, device.getPointer(), ProgramBuildInfo.CL_PROGRAM_BUILD_STATUS);
        return BuildStatus.fromValue(status);
    }

    @SuppressWarnings("unused") //called natively only
    private static void onProgramBuildFinishedStatic(long program, long user_data) {
        var future = buildFutures.get(user_data);
        if(future != null)
            future.complete();
    }

    public long getPointer() {
        return pointer;
    }

    @Override
    public void close() {
        clReleaseProgram(pointer);
    }
}
