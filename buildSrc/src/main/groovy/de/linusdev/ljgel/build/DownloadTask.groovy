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
import org.gradle.api.file.Directory
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

class DownloadTask extends DefaultTask {

    @Input List<String> urls

    @Input List<String> outputFileNames

    @Input boolean backup = false

    @OutputDirectory Directory outputDir = project.layout.buildDirectory.dir("download").get()

    @TaskAction
    void download() throws IOException {

        for(int i = 0; i < urls.size(); i++) {
            String url = urls.get(i)
            String fileName = outputFileNames.get(i)

            Path target = outputDir.file(fileName).getAsFile().toPath()

            try(InputStream stream = new URI(url).toURL().openStream()) {

                if(backup && Files.isRegularFile(target)) {
                    Files.copy(
                            target,
                            target.parent.resolve(target.getFileName().toString() + ".backup"),
                            StandardCopyOption.REPLACE_EXISTING
                    )
                }

                Files.copy(
                        stream,
                        target,
                        StandardCopyOption.REPLACE_EXISTING
                )
            } catch (IOException e) {
                project.getLogger().warn("Could not download specified files! Using old download if possible", e)

                // If file does not already exist from previous execution, throw an exception to stop the build
                if(!Files.isRegularFile(target)) throw e
            }
        }


    }

}
