/*
 * Copyright (c) 2024-2025 Linus Andera
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

package de.linusdev.ljgel.build;

import de.linusdev.ljgel.build.vkregistry.RegistryLoader;
import de.linusdev.lutils.codegen.SourceGenerator;
import org.gradle.api.DefaultTask;
import org.gradle.api.Task;
import org.gradle.api.file.Directory;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.file.Path;

public class VulkanXMLGeneratorTask extends DefaultTask {

    Property<Path> generatedSourceRoot = getProject().getObjects().property(Path.class)
            .convention(getProject().provider(() ->
                    getProject().getLayout()
                            .getBuildDirectory()
                            .getAsFile().get()
                            .toPath()
                            .resolve("generated/sources/vulkan/")
            ));

    @Input
    Property<String> basePackage = getProject().getObjects().property(String.class);

    @InputDirectory
    final DirectoryProperty vulkanXmlFileDir = getProject().getObjects().directoryProperty();


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

    @TaskAction
    void generate() throws ParserConfigurationException, IOException, SAXException {
        RegistryLoader registry = new RegistryLoader(
                vulkanXmlFileDir.file("vk.xml").get().getAsFile().toPath(),
                vulkanXmlFileDir.file("video.xml").get().getAsFile().toPath()
        );
        SourceGenerator generator = sourceGenerator.get();

        registry.generate(generator);

        generator.write();
    }

    public void setBasePackage(String basePackage) {
        this.basePackage.set(basePackage);
    }

    public void setVulkanXmlFileDir(Task task) {
        dependsOn(task);
        vulkanXmlFileDir.set(task.getOutputs().getFiles().getSingleFile());
    }

    public Directory getVulkanXmlFileDir() {
        return vulkanXmlFileDir.get();
    }

    public void setGeneratedSourceRoot(Path generatedSourceRoot) {
        this.generatedSourceRoot.set(generatedSourceRoot);
    }

    @Input
    public String getGeneratedSourceRoot() {
        return generatedSourceRoot.get().toString();
    }

    public String getBasePackage() {
        return basePackage.get();
    }

    public Directory getGeneratedJavaSource() {
        return generatedJavaSource.get();
    }

    public SourceGenerator getSourceGenerator() {
        return sourceGenerator.get();
    }
}
