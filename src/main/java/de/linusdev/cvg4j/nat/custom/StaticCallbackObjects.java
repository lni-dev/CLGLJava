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

package de.linusdev.cvg4j.nat.custom;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.ArrayList;

public class StaticCallbackObjects<T extends StaticCallbackObject<T>> {

    private final @NotNull ArrayList<T> instances = new ArrayList<>(1);

    @Range(from = 1, to = Integer.MAX_VALUE)
    public synchronized int add(T object) {
        for(int i = 0; i < instances.size(); i++) {
            if(instances.get(i) == null) {
                instances.set(i, object);
                return i + 1;
            }
        }

        instances.add(object);
        return instances.size();
    }

    public synchronized void remove(T object) {
        instances.set(object.getId() - 1, null);
    }

    public synchronized T get(long id) {
        if(id <= 0) return null;
        return instances.get((int) (id - 1));
    }

}
