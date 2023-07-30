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

import de.linusdev.clgl.api.async.GlobalAsyncManager;
import de.linusdev.clgl.nat.cl.objects.Program;
import de.linusdev.clgl.nat.custom.StaticCallbackObject;
import de.linusdev.clgl.nat.custom.StaticCallbackObjects;
import de.linusdev.lutils.async.completeable.CompletableFuture;
import de.linusdev.lutils.async.completeable.CompletableTask;
import de.linusdev.lutils.async.error.AsyncError;
import org.jetbrains.annotations.NotNull;

public class ProgramBuildFuture
        extends CompletableFuture<ProgramBuild, Program, CompletableTask<ProgramBuild, Program>>
        implements StaticCallbackObject<ProgramBuildFuture> {

    private final int id;

    private final @NotNull Program program;
    private final @NotNull ProgramBuild build;

    public ProgramBuildFuture(
            @NotNull StaticCallbackObjects<ProgramBuildFuture> buildFutures,
            @NotNull Program program,
            @NotNull ProgramBuild build
    ) {
        super(GlobalAsyncManager.getInstance(), false);
        this.id = buildFutures.add(this);
        this.program = program;
        this.build = build;
    }

    public void complete() {
        AsyncError error = BuildErrorException.check(program, build.getDevices());
        complete(build, program, error);
    }

    @Override
    public int getId() {
        return id;
    }
}
