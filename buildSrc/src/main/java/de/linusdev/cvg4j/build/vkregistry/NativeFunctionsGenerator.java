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
import de.linusdev.llog.LLog;
import de.linusdev.llog.base.LogInstance;
import de.linusdev.lutils.codegen.SourceGenerator;
import de.linusdev.lutils.codegen.c.CPPFileType;
import de.linusdev.lutils.codegen.c.CPPUtils;
import de.linusdev.lutils.codegen.java.JavaClass;
import de.linusdev.lutils.codegen.java.JavaMethod;
import de.linusdev.lutils.codegen.java.JavaVisibility;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class NativeFunctionsGenerator {

    public static final @NotNull LogInstance LOG = LLog.getLogInstance();

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

    public static final @NotNull String FUNC_POINTER_PARAM_NAME = "funcPointer";

    public void generate(
            @NotNull RegistryLoader registry,
            @NotNull SourceGenerator generator
    ) {
        LOG.debug("START GEN NativeFunctions.java and NativeFunctions.cpp");
        var cClazz = generator.addCFile();
        cClazz.setType(CPPFileType.SOURCE_CPP);
        cClazz.setName("NativeFunctions");
        cClazz.addInclude("de_linusdev_cvg4j_nat_NativeFunctions.h");
        cClazz.addInclude("<vulkan/vulkan.h>");

        var clazz = generator.addJavaFile();
        clazz.setName("NativeFunctions");
        clazz.setVisibility(JavaVisibility.PUBLIC);

        for (NativeFunction fun : functions.values()) {
            CTypes actualFunRetType = fun.returnType;
            JavaClass funJavaClass = JavaClass.ofClass(actualFunRetType.getJavaClass());

            if(actualFunRetType == CTypes.VOID)
                funJavaClass = JavaClass.ofClass(void.class);

            String cFunPointerName = "PFN_" + fun.name;
            cClazz.addCode("\n\n");
            cClazz.addCode(CPPUtils.typedefFunPointer(
                    actualFunRetType.getCName(), "VKAPI_PTR", cFunPointerName,
                    Arrays.stream(fun.paramTypes).map(CTypes::getCName).toArray(String[]::new)
            ));
            cClazz.addCode("\n");

            var method = clazz.addMethod(funJavaClass, fun.name);
            method.setStatic(true);
            method.setVisibility(JavaVisibility.PUBLIC);
            method.setNative(true);

            method.addParameter(FUNC_POINTER_PARAM_NAME, JavaClass.ofClass(long.class));

            List<String> cParamTypes = new ArrayList<>();
            List<String> cParamNames = new ArrayList<>();
            cParamTypes.add("JNIEnv*"); cParamNames.add("env");
            cParamTypes.add("jclass"); cParamNames.add("clazz");
            cParamTypes.add("jlong"); cParamNames.add(FUNC_POINTER_PARAM_NAME);
            for (int i = 0; i < fun.paramTypes.length; i++) {
                cParamTypes.add(fun.paramTypes[i].getJniName()); cParamNames.add(fun.paramNames[i]);
                method.addParameter(fun.paramNames[i], JavaClass.ofClass(fun.paramTypes[i].getJavaClass()));
            }

            cClazz.addCode(CPPUtils.funDeclaration(
                    CPPUtils.JNI_EXPORT(),
                    actualFunRetType.getJniName(),
                    CPPUtils.JNI_CALL(),
                    CPPUtils.jniJavaFunName(method),
                    cParamTypes.toArray(String[]::new),
                    cParamNames.toArray(String[]::new)
            ));

            String[] cReinterpretCastedParams = new String[cParamTypes.size()-3];
            for (int i = 3; i < cParamTypes.size(); i++) {
                CTypes paramType = fun.paramTypes[i-3];
                if(paramType == CTypes.POINTER)
                    cReinterpretCastedParams[i-3] = CPPUtils.reinterpretCast(paramType.getCName(), cParamNames.get(i));
                else if (paramType == CTypes.INT) {
                    cReinterpretCastedParams[i-3] = CPPUtils.staticCast(paramType.getCName(), cParamNames.get(i));
                } else
                    cReinterpretCastedParams[i-3] = cParamNames.get(i);
            }

            String cCallFun = CPPUtils.callLocalFun("fun", cReinterpretCastedParams);
            cClazz.addCode(CPPUtils.block(0,
                    CPPUtils.declareAndAssign("auto", "fun", CPPUtils.reinterpretCast(cFunPointerName, FUNC_POINTER_PARAM_NAME)),
                    CPPUtils.returnExpression(
                            actualFunRetType == CTypes.POINTER
                                    ?
                                    CPPUtils.reinterpretCast("jlong", cCallFun)
                                    :
                                    cCallFun
                    )
            ));
        }

        LOG.debug("END GEN NativeFunctions.java and NativeFunctions.cpp");
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

        public JavaMethod getNativeMethod(RegistryLoader registry, SourceGenerator generator) {
            return JavaMethod.of(
                    JavaClass.custom(generator.getJavaBasePackage().toString(), "NativeFunctions"),
                    returnType.getJavaClass(registry, generator),
                    name,
                    true
            );
        }
    }
}
