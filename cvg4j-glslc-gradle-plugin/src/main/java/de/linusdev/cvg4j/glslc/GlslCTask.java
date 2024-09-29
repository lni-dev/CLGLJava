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

package de.linusdev.cvg4j.glslc;

import de.linusdev.lutils.io.FileUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.Directory;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.internal.tasks.compile.CompilationFailedException;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.tasks.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@CacheableTask
public class GlslCTask extends DefaultTask {

    @Input
    Property<String> glslCLocation = getProject().getObjects().property(String.class)
            .convention(getProject().provider(() -> "glslc"));

    @Optional
    @PathSensitive(PathSensitivity.ABSOLUTE)
    @InputDirectory
    DirectoryProperty shaderLocation = getProject().getObjects().directoryProperty()
            .convention((Directory) null);

    @Input
    SetProperty<String> allowedFileEndings = getProject().getObjects().setProperty(String.class)
            .convention(() -> List.of("vert", "frag").iterator());

    @Optional
    @Input
    Property<String> compiledShadersRootResourcesPackage = getProject().getObjects().property(String.class)
            .convention((String) null);

    @Input
    Property<Path> generatedResourcesPath = getProject().getObjects().property(Path.class)
            .convention(getProject().provider(() ->
                    getProject().getLayout()
                            .getBuildDirectory()
                            .getAsFile().get()
                            .toPath()
                            .resolve("generated/sources/glslc/")
            ));

    @OutputDirectory
    DirectoryProperty generatedResourcesSource = getProject().getObjects().directoryProperty()
            .convention(
                    getProject().getLayout().dir(
                            getProject().provider(
                                    () -> generatedResourcesPath.get().resolve("resources").toFile()
                            )
                    )
            );

    @TaskAction
    public void glslC() throws IOException, InterruptedException {

        // create the output directory
        Path resources = generatedResourcesSource.getAsFile().get().toPath();
        Files.createDirectories(resources);

        // Only run, if shaders are specified.
        if(shaderLocation.getOrNull() == null)
            return;

        if(compiledShadersRootResourcesPackage.getOrNull() == null)
            throw new IllegalStateException("compileShadersRootResourcesPackage must be set.");


        Path shadersDir = shaderLocation.get().getAsFile().toPath();

        List<Path> shaderFiles = FileUtils.collectInFileTree(
                shadersDir,
                (path, basicFileAttributes) ->
                        allowedFileEndings.get().contains(FileUtils.getFileEnding(path))
        );

        boolean error = false;
        for (Path input : shaderFiles) {
            Path output = resources.resolve(compiledShadersRootResourcesPackage.get().replace(".", "/"));
            Files.createDirectories(output);
            output = output.resolve(input.getFileName() + ".spv");

            Process process = Runtime.getRuntime().exec(new String[]{"glslc", "-o", output.toString(), input.toString()});

            try (
                    BufferedReader inReader = process.inputReader();
                    BufferedReader errReader = process.errorReader()
            ) {
                inReader.lines().forEach(System.out::println);
                errReader.lines().forEach(System.err::println);
            }

            if(process.waitFor() != 0) error = true;
        }

        if(error) {
            throw new CompilationFailedException();
        }

    }

    public void setGlslCLocation(String glslCLocation) {
        this.glslCLocation.set(glslCLocation);
    }

    public String getGlslCLocation() {
        return glslCLocation.get();
    }

    public void setShaderLocation(Path shaderLocation) {
        this.shaderLocation.set(shaderLocation.toFile());
    }

    public void setShaderLocation(String shaderLocation) {
        this.shaderLocation.set(Paths.get(shaderLocation).toFile());
    }

    public Directory getShaderLocation() {
        return shaderLocation.getOrNull();
    }

    public SetProperty<String> getAllowedFileEndings() {
        return allowedFileEndings;
    }

    public void setGeneratedResourcesPath(Path generatedResourcesPath) {
        this.generatedResourcesPath.set(generatedResourcesPath);
    }

    public void setCompiledShadersRootResourcesPackage(String compiledShadersRootResourcesPackage) {
        this.compiledShadersRootResourcesPackage.set(compiledShadersRootResourcesPackage);
    }

    public String getCompiledShadersRootResourcesPackage() {
        return compiledShadersRootResourcesPackage.getOrNull();
    }

    public String getGeneratedResourcesPath() {
        return generatedResourcesPath.get().toString();
    }

    public Directory getGeneratedResourcesSource() {
        return generatedResourcesSource.get();
    }
}
