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

package de.linusdev.clgl.engine.kernel.source;

import de.linusdev.clgl.engine.Engine;
import de.linusdev.clgl.nat.cl.CLException;
import de.linusdev.clgl.nat.cl.objects.Kernel;
import de.linusdev.clgl.nat.cl.objects.Program;
import de.linusdev.lutils.async.Future;
import de.linusdev.lutils.async.Nothing;
import de.linusdev.lutils.async.completeable.CompletableFuture;
import de.linusdev.lutils.async.error.ThrowableAsyncError;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

@SuppressWarnings("unused")
public interface KernelSourceInfo {


    /**
     *
     * @param cl class to get the resource
     * @param resourcePath path to the resource
     * @return {@link KernelSourceInfo} representing given resource
     */
    static @NotNull KernelSourceInfo ofUTF8StringResource(
            @Nullable Class<?> cl, @NotNull String resourcePath,
            @NotNull String kernelName
    ) {
        return new KernelSourceInfo() {
            final Class<?> clazz = cl == null ?  KernelSourceInfo.class : cl;

            @Override
            public boolean isUTF8Format() {
                return true;
            }

            @Override
            public String getSourceString() throws IOException {
                InputStream in = clazz.getClassLoader().getResourceAsStream(resourcePath);

                if (in == null)
                    throw new NoSuchFileException(resourcePath);

                StringBuilder src = new StringBuilder();
                try (
                        in;
                        InputStreamReader inReader = new InputStreamReader(in, StandardCharsets.UTF_8);
                        BufferedReader reader = new BufferedReader(inReader)
                ) {
                    String line;
                    while ((line = reader.readLine()) != null)
                        src.append(line).append("\n");
                }
                return src.toString();
            }

            @Override
            public @NotNull String getKernelName() {
                return kernelName;
            }
        };
    }

    /**
     *
     * @param path path to the file
     * @return {@link KernelSourceInfo} representing given resource
     */
    static @NotNull KernelSourceInfo ofUTF8StringFile(
            @NotNull Path path,
            @NotNull String kernelName
    ) {
        return new KernelSourceInfo() {

            @Override
            public boolean isUTF8Format() {
                return true;
            }

            @Override
            public String getSourceString() throws IOException {
                InputStream in = Files.newInputStream(path);

                StringBuilder src = new StringBuilder();
                try (
                        in;
                        InputStreamReader inReader = new InputStreamReader(in, StandardCharsets.UTF_8);
                        BufferedReader reader = new BufferedReader(inReader)
                ) {
                    String line;
                    while ((line = reader.readLine()) != null)
                        src.append(line).append("\n");
                }
                return src.toString();
            }

            @Override
            public @NotNull String getKernelName() {
                return kernelName;
            }
        };
    }

    /**
     *
     * @return {@code true} if and only if the source is in utf-8 format
     */
    boolean isUTF8Format();

    /**
     *
     * @return source string, if the source {@link #isUTF8Format()  is utf-8 format}. {@code null} otherwise.
     */
    String getSourceString() throws IOException;

    /**
     * Name of the kernel function, which should be executed
     * @return kernel name as {@link String}
     */
    @NotNull String getKernelName();

    default @NotNull Future<Kernel, Nothing> loadKernel(@NotNull Engine<?> engine) {
        var future = CompletableFuture.<Kernel, Nothing>create(engine.getAsyncManager(), false);

        engine.runSupervised(() -> {
            try {
                if(isUTF8Format()) {
                    Program program = new Program(engine.getWindow().getClContext(), getSourceString());
                    //TODO: build options
                    try {
                        program.build("-cl-std=CL2.0", false, engine.getWindow().getClDevice());
                    } catch (CLException clException) {
                        System.out.println(program.getBuildLog(engine.getWindow().getClDevice()));
                        future.complete(null, Nothing.INSTANCE, new ThrowableAsyncError(clException));
                        return;
                    }

                    future.complete(new Kernel(program, getKernelName()), Nothing.INSTANCE, null);
                }

            } catch (IOException e) {
                future.complete(null, Nothing.INSTANCE, new ThrowableAsyncError(e));
            }
        });

        return future;
    }



}
