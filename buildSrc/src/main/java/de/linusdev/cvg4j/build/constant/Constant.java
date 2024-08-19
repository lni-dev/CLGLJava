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

package de.linusdev.cvg4j.build.constant;

import de.linusdev.lutils.codegen.java.JavaClass;
import de.linusdev.lutils.codegen.java.JavaClassGenerator;
import de.linusdev.lutils.codegen.java.JavaExpression;
import de.linusdev.lutils.codegen.java.JavaVisibility;
import de.linusdev.lutils.version.Version;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

public interface Constant extends Serializable {

    @NotNull String name();

    @NotNull Class<?> clazz();

    default void add(@NotNull JavaClassGenerator clazz) {
        var v = clazz.addVariable(JavaClass.ofClass(clazz()), name());
        v.setVisibility(JavaVisibility.PUBLIC);
        v.setFinal(true);
        v.setStatic(true);
        v.setDefaultValue(value());
    }

    @NotNull JavaExpression value();

    static boolean equals(Constant con1, Object con2) {
        if(con1 == con2) return true;
        if(con2 == null) return false;

        if(con2 instanceof Constant c) {
            return c.name().equals(con1.name());
        }

        return false;
    }

    class StringConst implements Constant {
        private final @NotNull String name;
        private final @NotNull String string;

        public StringConst(@NotNull String name, @NotNull String string) {
            this.name = name;
            this.string = string;
        }

        @Override
        public @NotNull String name() {
            return name;
        }

        @Override
        public @NotNull Class<?> clazz() {
            return String.class;
        }

        @Override
        public @NotNull JavaExpression value() {
            return JavaExpression.ofString(string);
        }

        @Override
        public boolean equals(Object obj) {
            return Constant.equals(this, obj);
        }
    }

    class VersionConst implements Constant {
        private final @NotNull String name;
        private final @NotNull String version;

        public VersionConst(@NotNull String name, @NotNull Version version) {
            this.name = name;
            this.version = version.getAsUserFriendlyString();
        }

        @Override
        public @NotNull String name() {
            return name;
        }

        @Override
        public @NotNull Class<?> clazz() {
            return Version.class;
        }

        @Override
        public @NotNull JavaExpression value() {
            return JavaExpression.ofCode("Version.of(\"" + version + "\")");
        }

        @Override
        public boolean equals(Object obj) {
            return Constant.equals(this, obj);
        }
    }
}
