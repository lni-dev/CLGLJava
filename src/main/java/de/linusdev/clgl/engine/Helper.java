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

package de.linusdev.clgl.engine;

import de.linusdev.clgl.engine.kernel.source.KernelSourceInfo;
import de.linusdev.clgl.nat.cl.objects.Kernel;
import de.linusdev.lutils.async.Future;
import de.linusdev.lutils.async.Nothing;
import org.jetbrains.annotations.NonBlocking;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Helper {

    static class LoadKernelsResult {
        public Kernel renderKernel = null;
        public Kernel uiKernel = null;
    }

    @NonBlocking
    static <G extends Game> @NotNull Future<LoadKernelsResult, Engine<G>> loadKernels(
            @NotNull Engine<G> engine,
            @Nullable KernelSourceInfo renderKernelInfo,
            @Nullable KernelSourceInfo uiKernelInfo
    ) {
        return engine.runSupervised(() -> {
            LoadKernelsResult result = new LoadKernelsResult();

            Future<Kernel, Nothing> renderKernelFuture, uiKernelFuture;
            renderKernelFuture = renderKernelInfo != null ? renderKernelInfo.loadKernel(engine) : null;
            uiKernelFuture = uiKernelInfo != null ? uiKernelInfo.loadKernel(engine) : null;

            if(renderKernelFuture != null) {
                //noinspection BlockingMethodInNonBlockingContext: run in new thread
                var res = renderKernelFuture.get();
                if(res.hasError())
                    throw res.getError().asThrowable();

                result.renderKernel = res.getResult();
            }

            if(uiKernelFuture != null) {
                //noinspection BlockingMethodInNonBlockingContext: run in new thread
                var res = uiKernelFuture.get();
                if(res.hasError())
                    throw res.getError().asThrowable();

                result.uiKernel = res.getResult();
            }

            return result;
        });

    }

}
