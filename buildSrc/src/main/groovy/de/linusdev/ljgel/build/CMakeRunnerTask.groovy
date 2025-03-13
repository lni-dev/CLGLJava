/*
 * Copyright (c) 2025 Linus Andera
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

package de.linusdev.ljgel.build

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.jetbrains.annotations.Nullable

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

class CMakeRunnerTask extends DefaultTask {

    private @Nullable CPPBuildType buildType = null

    @Input
    final Property<String> generatedResourcesPath = getProject().getObjects().property(String.class)
            .convention(getProject().provider(() ->
                    getProject().getLayout()
                            .getBuildDirectory()
                            .getAsFile().get()
                            .toPath()
                            .resolve("generated/sources/cmake-build/").toString()
            ));

    @OutputDirectory
    final DirectoryProperty generatedResourcesSource = getProject().getObjects().directoryProperty()
            .convention(
                    getProject().getLayout().dir(
                            getProject().provider(
                                    () -> Paths.get(generatedResourcesPath.get()).resolve("resources").toFile()
                            )
                    )
            );

    @Optional
    @Input
    final Property<String> resourcePackage = getProject().getObjects().property(String.class)
            .convention((String) "");

    @Input
    String nativeLibName

    @TaskAction
    void run() {
        if(buildType == null) {
            throw new IllegalStateException("Build type was not specified!")
        }

        var logger = project.getLogger();

        { // Run cmake --preset=...
            logger.lifecycle("Running command: 'cmake --preset=${buildType.cmakePresetName}'")

            ProcessBuilder builder = new ProcessBuilder("cmake", "--preset=${buildType.cmakePresetName}")
            builder.directory(project.rootDir)
            Process process = builder.start()


            String line;
            while((line = process.inputReader().readLine()) != null) { logger.lifecycle(line) }
            while((line = process.errorReader().readLine()) != null) { logger.error(line) }

            int exitCode
            if((exitCode = process.waitFor()) != 0)
                throw new IllegalStateException("'cmake --preset=${buildType.cmakePresetName}' completed with exit code $exitCode!")
        }

        { // Run cmake --build cmake-build/... --config=...
            logger.lifecycle("Running command: 'cmake --build ${buildType.buildDir} ${buildType.requiresConfigParam ? "--config ${buildType.configParam}" : ""}'")
            
            ProcessBuilder builder = buildType.requiresConfigParam
                    ?
                    new ProcessBuilder("cmake", "--build", "${buildType.buildDir}", "--config", "${buildType.configParam}")
                    :
                    new ProcessBuilder("cmake", "--build", "${buildType.buildDir}")

            builder.directory(project.rootDir)
            Process process = builder.start()
            
            String line;
            while((line = process.inputReader().readLine()) != null) { logger.lifecycle(line) }
            while((line = process.errorReader().readLine()) != null) { logger.error(line) }

            int exitCode
            if((exitCode = process.waitFor()) != 0)
                throw new IllegalStateException("'cmake --build ${buildType.buildDir} ${buildType.requiresConfigParam ? "--config ${buildType.configParam}" : ""}' completed with exit code $exitCode!")
        }

        // Copy to generated sources
        Path root = project.rootDir.toPath()
        Path libFile = root.resolve(buildType.buildDir)
        if(!buildType.libFileDir.isEmpty())
            libFile = libFile.resolve(buildType.libFileDir)

        libFile = libFile.resolve("$nativeLibName.${buildType.libFileEnding}")

        // create the output directory
        Path resources = generatedResourcesSource.getAsFile().get().toPath()
        Files.createDirectories(resources)

        Path output = resources.resolve(resourcePackage.get().replace(".", "/"))
        Files.createDirectories(output)

        Path targetLocation = output.resolve("$nativeLibName.${buildType.libFileEnding}")

        Files.copy(libFile, targetLocation, StandardCopyOption.REPLACE_EXISTING)
    }

    @Input
    CPPBuildType getBuildType() {
        return buildType
    }

    void setBuildType(Object value) {
        if (value instanceof CPPBuildType) {
            buildType = value
        } else if (value instanceof String) {
            buildType = CPPBuildType.valueOf(value.toUpperCase())  // Convert String to Enum
        } else {
            throw new IllegalArgumentException("Invalid value for buildType: $value")
        }
    }

}
