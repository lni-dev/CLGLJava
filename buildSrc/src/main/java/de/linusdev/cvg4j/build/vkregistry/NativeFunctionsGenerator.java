/*
 * Copyright (c) 2024 Linus Andera
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

package de.linusdev.cvg4j.build.vkregistry;

import de.linusdev.cvg4j.build.vkregistry.types.CTypes;
import de.linusdev.cvg4j.build.vkregistry.types.abstracts.Type;
import de.linusdev.lutils.codegen.SourceGenerator;
import de.linusdev.lutils.codegen.java.JavaVisibility;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class NativeFunctionsGenerator {

    private final @NotNull RegistryLoader registry;

    private final @NotNull HashMap<String, NativeFunction> functions = new HashMap<>();

    public NativeFunctionsGenerator(@NotNull RegistryLoader registry) {
        this.registry = registry;
    }

    public @NotNull NativeFunction getNativeFunction(
            @NotNull Type returnType,
            @NotNull Type @NotNull ... params
    ) {
        CTypes retType = returnType.getAsBaseType();
        CTypes[] paramTypes = new CTypes[params.length];
        String[] paramNames = new String[params.length];

        StringBuilder functionName = new StringBuilder("callNative" + retType.getShortName() + "Function");

        int i = 0;
        for (Type param : params) {
            paramTypes[i] = param.getAsBaseType();
            paramNames[i] = paramTypes[i].getShortName().toLowerCase() + "_" + i;
            functionName.append(paramTypes[i].getShortName());
            i++;
        }

        NativeFunction fun = functions.get(functionName.toString());
        if(fun != null)
            return fun;

        fun = new NativeFunction(
                functionName.toString(),
                retType,
                paramTypes,
                paramNames
        );

        functions.put(functionName.toString(), fun);

        return fun;
    }

    public void generate(
            @NotNull RegistryLoader registry,
            @NotNull SourceGenerator generator
    ) {
        var clazz = generator.addJavaFile();
        clazz.setName("NativeFunctions");
        clazz.setVisibility(JavaVisibility.PUBLIC);

        for (NativeFunction fun : functions.values()) {
            var method = clazz.addMethod(fun.returnType.getJavaClass(registry, generator), fun.name);
            method.setStatic(true);
            method.setVisibility(JavaVisibility.PUBLIC);
            method.setNative(true);

            for (int i = 0; i < fun.paramTypes.length; i++) {
                method.addParameter(fun.paramNames[i], fun.paramTypes[i].getJavaClass(registry, generator));
            }
        }
    }

    public static class NativeFunction {
        private final @NotNull String name;
        private final @NotNull CTypes returnType;
        private final @NotNull CTypes[] paramTypes;
        private final @NotNull String[] paramNames;

        public NativeFunction(
                @NotNull String name,
                @NotNull CTypes returnType,
                @NotNull CTypes[] paramTypes,
                @NotNull String[] paramNames
        ) {
            this.name = name;
            this.returnType = returnType;
            this.paramTypes = paramTypes;
            this.paramNames = paramNames;
        }
    }
}
