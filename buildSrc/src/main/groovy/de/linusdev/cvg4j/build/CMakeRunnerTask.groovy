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

package de.linusdev.cvg4j.build

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.jetbrains.annotations.Nullable

import java.nio.file.Files
import java.nio.file.Path

class CMakeRunnerTask extends DefaultTask {

    private @Nullable CPPBuildType buildType = null

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

    @Input
    Property<Path> generatedResourcesPath = getProject().getObjects().property(Path.class)
            .convention(getProject().provider(() ->
                    getProject().getLayout()
                            .getBuildDirectory()
                            .getAsFile().get()
                            .toPath()
                            .resolve("generated/sources/cmake-build/")
            ))

    @OutputDirectory
    DirectoryProperty generatedResourcesSource = getProject().getObjects().directoryProperty()
            .convention(
                    getProject().getLayout().dir(
                            getProject().provider(
                                    () -> generatedResourcesPath.get().resolve("resources").toFile()
                            )
                    )
            )

    @TaskAction
    void run() {
        // create the output directory
        Path resources = generatedResourcesSource.getAsFile().get().toPath()
        Files.createDirectories(resources)

        if(buildType == null) {
            throw new IllegalStateException("Build type was not specified!")
        }

        ProcessBuilder builder = new ProcessBuilder("cmake", "--preset=${buildType.cmakePresetName}")
        builder.directory(project.rootDir)
        Process process = builder.start()

        var logger = project.getLogger()
        String line;
        while((line = process.inputReader().readLine()) != null) { logger.lifecycle(line) }
        while((line = process.errorReader().readLine()) != null) { logger.error(line) }

        int exitCode
        if((exitCode = process.waitFor()) != 0)
            throw new IllegalStateException("'cmake --preset=${buildType.cmakePresetName}' completed with exit code $exitCode!")


    }

}
