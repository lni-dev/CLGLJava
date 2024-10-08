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

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskProvider;
import org.jetbrains.annotations.NotNull;

public class GlslCGradlePlugin implements Plugin<Project> {

    @Override
    public void apply(@NotNull Project target) {
        TaskProvider<GlslCTask> glslcTask = target.getTasks().register("glslc", GlslCTask.class);

        target.getPlugins().withId("java", plugin -> {
            target.afterEvaluate(proj -> {
                SourceSetContainer sourceSets = target.getExtensions().getByType(SourceSetContainer.class);
                SourceSet mainSourceSet = sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME);

                // Add src
                mainSourceSet.getResources().srcDir(glslcTask);

                /*
                // Ensure the resources are generated before processing them
                target.getTasks().named(mainSourceSet.getProcessResourcesTaskName())
                        .configure(task -> task.dependsOn(glslcTask));

                // For some reason we get an error, if processTestResources doesn't depend on the glslc task.
                try {
                    target.getTasks().named("processTestResources").configure(task -> task.dependsOn(glslcTask));
                } catch (UnknownTaskException ignored) {}*/

            });
        });
    }
}
