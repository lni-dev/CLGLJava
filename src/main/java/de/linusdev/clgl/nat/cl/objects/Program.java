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
import de.linusdev.clgl.nat.cl.custom.StaticCallbackObject;
import de.linusdev.clgl.nat.cl.custom.StaticCallbackObjects;
import de.linusdev.clgl.nat.cl.listener.ProgramOnBuildFinished;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static de.linusdev.clgl.nat.cl.CL.*;

@SuppressWarnings("unused")
public class Program implements StaticCallbackObject<Program> {

    public static final @NotNull StaticCallbackObjects<Program> programs = new StaticCallbackObjects<>();

    protected final int id;
    protected final long pointer;
    protected @Nullable ProgramOnBuildFinished buildFinishListener;

    public Program(@NotNull Context context, @NotNull String source) {
        id = programs.add(this);
        try {
            pointer = clCreateProgramWithSource(context.getPointer(), source);
        } finally {
            programs.remove(this);
        }

    }

    public void build(@NotNull String options, boolean async, @NotNull Device @NotNull ... devs) {
        PrimitiveTypeArray<Long> devices = new PrimitiveTypeArray<>(Long.class, devs.length, true);

        for(int i = 0; i < devs.length; i++)
            devices.set(i, devs[i].getPointer());

        //TODO: return ProgramBuild / async Task ( cahnge StaticCallbackObjects to ProgramBuild)

        clBuildProgram(pointer, devices, options, async ? id : NativeUtils.getNullPointer());
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
        Program p = programs.get(user_data);

        if(p.buildFinishListener != null)
            p.buildFinishListener.onProgramBuildFinished(p);
    }

    public void setBuildFinishListener(@Nullable ProgramOnBuildFinished listener) {
        this.buildFinishListener = listener;
    }

    @Override
    public int getId() {
        return id;
    }

    public long getPointer() {
        return pointer;
    }
}
