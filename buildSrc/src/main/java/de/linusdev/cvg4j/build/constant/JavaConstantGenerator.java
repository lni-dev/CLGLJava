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

import de.linusdev.lutils.codegen.SourceGenerator;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.Directory;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;
import java.nio.file.Path;

public class JavaConstantGenerator extends DefaultTask {

    @Input
    Property<Path> generatedSourceRoot = getProject().getObjects().property(Path.class)
            .convention(getProject().provider(() ->
                    getProject().getLayout()
                            .getBuildDirectory()
                            .getAsFile().get()
                            .toPath()
                            .resolve("generated/sources/constants/")
            ));

    @Input
    Property<String> basePackage = getProject().getObjects().property(String.class);

    @Input
    Property<String> clazzName = getProject().getObjects().property(String.class)
            .convention("GeneratedConstants");

    @Internal
    Property<SourceGenerator> sourceGenerator = getProject().getObjects().property(SourceGenerator.class)
            .convention(getProject().provider(() -> {
                        SourceGenerator gen = new SourceGenerator(generatedSourceRoot.get());
                        gen.setJavaBasePackage(basePackage.get());
                        return gen;
                    }
            ));

    @OutputDirectory
    DirectoryProperty generatedJavaSource = getProject().getObjects().directoryProperty()
            .convention(
                    getProject().getLayout().dir(
                            getProject().provider(
                                    () -> sourceGenerator.get().getJavaSourcePath().toFile()
                            )
                    )
            );

    @Input
    SetProperty<Constant> constants = getProject().getObjects().setProperty(Constant.class);

    @TaskAction
    void generate() throws IOException {
        SourceGenerator generator = sourceGenerator.get();

        var clazz = generator.addJavaFile(basePackage.get());
        clazz.setName(clazzName.get());

        for (Constant constant : constants.get()) {
            constant.add(clazz);
        }

        generator.write();
    }

    public String getBasePackage() {
        return basePackage.get();
    }

    public void setBasePackage(String basePackage) {
        this.basePackage.set(basePackage);
    }

    public String getClazzName() {
        return clazzName.get();
    }

    public SourceGenerator getSourceGenerator() {
        return sourceGenerator.get();
    }

    public void setGeneratedSourceRoot(Path generatedSourceRoot) {
        this.generatedSourceRoot.set(generatedSourceRoot);
    }

    public String getGeneratedSourceRoot() {
        return generatedSourceRoot.get().toString();
    }

    public Directory getGeneratedJavaSource() {
        return generatedJavaSource.get();
    }

    public SetProperty<Constant> getConstants() {
        return constants;
    }

    public void setClazzName(String clazzName) {
        this.clazzName.set(clazzName);
    }
}
