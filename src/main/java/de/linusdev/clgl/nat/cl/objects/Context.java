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
import de.linusdev.clgl.nat.cl.CL;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import static de.linusdev.clgl.nat.cl.CLStatus.check;

public abstract class Context implements AutoCloseable {

    private static final @NotNull ArrayList<Context> contexts = new ArrayList<>(1);

    private final int id;
    private final long pointer;

    protected Context(Device... devs) {
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

        PrimitiveTypeArray<Long> properties = new PrimitiveTypeArray<>(Long.class, 6, true);
        //TODO: properties

        PrimitiveTypeArray<Long> devices = new PrimitiveTypeArray<>(Long.class, devs.length, true);
        for(int i = 0; i < devices.size(); i++)
            devices.set(i, devs[i].getPointer());

        BBInt1 errCodeRet = new BBInt1(true);
        pointer = CL.clCreateContext(properties, devices, id, errCodeRet);
        check(errCodeRet.get());
    }

    @SuppressWarnings("unused") //Called natively only
    private static void onErrorStatic(String errinfo, ByteBuffer private_info, long user_data) {
        contexts.get((int) user_data).onError(errinfo, private_info);
    }

    public abstract void onError(String errinfo, ByteBuffer private_info);

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
