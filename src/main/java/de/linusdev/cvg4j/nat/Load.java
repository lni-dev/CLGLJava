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

package de.linusdev.cvg4j.nat;

import de.linusdev.cvg4j.nat.loader.LibraryLoader;

import java.io.IOException;

public class Load {

    static {
        LibraryLoader loader = new LibraryLoader(
                Load.class, "native-libraries/", "CLGLJava.jnilib",
                "glfw3", "glad"
        );

        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    protected static boolean init = false;

    public static void init() {
        if(!init) {
            init = true;
            _init();
            Runtime.getRuntime().addShutdownHook(new Thread(Load::_close));
        }
    }

    @SuppressWarnings("unused") //called natively only
    protected static void printInJava(String str) {
        System.out.println(str);
    }

    protected static native void _init();

    protected static native void _close();

}
