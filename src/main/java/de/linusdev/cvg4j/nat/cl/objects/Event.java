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

package de.linusdev.cvg4j.nat.cl.objects;

import de.linusdev.cvg4j.nat.cl.CL;
import de.linusdev.lutils.math.vector.buffer.longn.BBLong1;
import de.linusdev.lutils.nat.struct.abstracts.StructureStaticVariables;
import de.linusdev.lutils.nat.struct.annos.StructValue;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public class Event extends BBLong1 {

    /**
     * @see StructureStaticVariables#newUnallocated()
     */
    public static Event newUnallocated() {
        return new Event(false, null);
    }

    /**
     * @see StructureStaticVariables#newAllocatable(StructValue)
     */
    public static Event newAllocatable(@Nullable StructValue structValue) {
        return new Event(true, structValue);
    }

    /**
     * @see StructureStaticVariables#newAllocated(StructValue)
     */
    public static Event newAllocated(@Nullable StructValue structValue) {
        Event ret = newAllocatable(structValue);
        ret.allocate();
        return ret;
    }

    protected Event(boolean generateInfo, @Nullable StructValue structValue) {
        super(generateInfo, structValue);
    }

    public long getOpenCLObjectPointer() {
        return get();
    }

    public boolean isEmpty() {
        return getOpenCLObjectPointer() == 0L;
    }

    public void waitForEvent() {
        CL.clWaitForEvent(this);
    }

}
