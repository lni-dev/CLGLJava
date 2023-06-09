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

import de.linusdev.clgl.api.structs.Structure;
import de.linusdev.clgl.api.types.bytebuffer.BBLong1;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public class Event extends BBLong1 {

    public Event() {
        super(true);
    }

    public Event(@NotNull Structure parent, int offset) {
        super(false);
        useBuffer(parent, offset);
    }

    public long getPointer() {
        return get();
    }

    public boolean isEmpty() {
        return getPointer() == 0L;
    }


}
