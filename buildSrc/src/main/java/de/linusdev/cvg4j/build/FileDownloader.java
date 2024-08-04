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

package de.linusdev.cvg4j.build;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

//TODO handle caching
@SuppressWarnings("unused")
//@DisableCachingByDefault(because = "File may change on remote")
public class FileDownloader extends DefaultTask {

    @Input
    Property<String> url = getProject().getObjects().property(String.class);

    @Input
    Property<String> outputFileName = getProject().getObjects().property(String.class)
            .convention(getProject().provider(() -> {
                int li = url.get().lastIndexOf("/");
                li = Math.max(li, url.get().lastIndexOf("\\"));

                return url.get().substring(li+1);
            }));

    @OutputFile
    RegularFileProperty outputFile = getProject().getObjects().fileProperty().convention(
            getProject().getLayout().file(
                    getProject().provider(() ->
                            new File(getProject().getBuildDir(), "downloads/" + outputFileName.get())
            ))
    );

    @TaskAction
    void download() throws IOException {

        try(InputStream stream = new URL(url.get()).openStream()) {
            Files.copy(
                    stream, outputFile.get().getAsFile().toPath(),
                    StandardCopyOption.REPLACE_EXISTING
            );
        }
    }

    public String getUrl() {
        return url.get();
    }

    public void setUrl(String url) {
        this.url.set(url);
    }


    public String getOutputFileName() {
        return outputFileName.get();
    }

    public void setOutputFileName(String outputFileName) {
        this.outputFileName.set(outputFileName);
    }

    public RegularFileProperty getOutputFile() {
        return outputFile;
    }
}
