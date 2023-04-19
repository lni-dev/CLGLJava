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
import de.linusdev.clgl.api.types.bytebuffer.BBInt1;
import de.linusdev.clgl.nat.NativeUtils;
import de.linusdev.clgl.nat.cl.CL;
import de.linusdev.clgl.nat.wgl.WGL;
import de.linusdev.clgl.nat.cl.listener.ContextOnError;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static de.linusdev.clgl.nat.cl.CLStatus.check;

public class Context implements AutoCloseable {

    private static final @NotNull ArrayList<Context> contexts = new ArrayList<>(1);

    private final int id;
    private final long pointer;

    protected @Nullable ContextOnError onErrorListener;

    public static @NotNull Context createCLGLSharedContext(@NotNull Device @NotNull ... devs) {
        Map<CL.@NotNull CLContextProperties, @NotNull Long> properties = new HashMap<>();

        long currentContext = WGL._wglGetCurrentContext();
        long currentDC = WGL._wglGetCurrentDC();

        if(NativeUtils.isNull(currentContext) || NativeUtils.isNull(currentDC)) {
            throw new IllegalStateException("You must create a gl context and a window first!");
        }

        properties.put(CL.CLContextProperties.CL_GL_CONTEXT_KHR, currentContext);
        properties.put(CL.CLContextProperties.CL_WGL_HDC_KHR, currentDC);

        return new Context(properties, (info, privateInfo) -> System.err.println("OpenCL Context Error: " + info), devs);
    }

    public Context(
            @Nullable Map<CL.@NotNull CLContextProperties, @NotNull Long> properties,
            @Nullable ContextOnError onErrorListener,
            @NotNull Device @NotNull ... devs
    ) {

        this.onErrorListener = onErrorListener;

        synchronized (contexts) {
            int id = -1;
            for(int i = 0; i < contexts.size(); i++) {
                if(contexts.get(i) == null) {
                    id = i;
                    contexts.set(i, this);
                    break;
                }
            }

            if(id == -1) {
                this.id = contexts.size();
                contexts.add(this);
            } else {
                this.id = id;
            }
        }

        PrimitiveTypeArray<Long> props = null;
        if(properties != null) {
            props = new PrimitiveTypeArray<>(Long.class, properties.size() * 2 + 1, true);

            int i = 0;
            for(Map.Entry<CL.CLContextProperties, Long> entry : properties.entrySet()) {
                props.setLong(i++, entry.getKey().getValue());
                props.setLong(i++, entry.getValue());
            }

            props.setLong(i, 0L);
        }

        PrimitiveTypeArray<Long> devices = new PrimitiveTypeArray<>(Long.class, devs.length, true);
        for(int i = 0; i < devices.size(); i++)
            devices.set(i, devs[i].getPointer());

        BBInt1 errCodeRet = new BBInt1(true);
        pointer = CL.clCreateContext(props, devices, id + 1, errCodeRet);
        check(errCodeRet.get());
    }

    @SuppressWarnings("unused") //Called natively only
    private static void onErrorStatic(String errinfo, ByteBuffer private_info, long user_data) {
        contexts.get((int) user_data - 1).onError(errinfo, private_info);
    }

    public void onError(String info, ByteBuffer privateInfo) {
        if(onErrorListener != null)
            onErrorListener.onError(info, privateInfo);
    }

    public long getPointer() {
        return pointer;
    }

    @Override
    public void close() {
        //noinspection resource: contexts[id] == this
        contexts.set(id, null);
        CL.clReleaseContext(this);
    }
}
