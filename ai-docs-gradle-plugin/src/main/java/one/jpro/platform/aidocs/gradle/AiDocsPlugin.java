package one.jpro.platform.aidocs.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

import java.nio.file.Path;

/**
 * Gradle plugin that collects documentation artifacts from project dependencies
 * and generates an AI-navigable file structure.
 *
 * Applying this plugin registers a per-project {@code collectDocsPartial} task on this
 * project and every subproject, plus the aggregating {@code collectDocs} task that
 * merges all partial outputs into a single {@code build/ai-docs/} directory.
 */
public class AiDocsPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.getPluginManager().apply(AiDocsPartialPlugin.class);
        for (Project subproject : project.getSubprojects()) {
            subproject.getPluginManager().apply(AiDocsPartialPlugin.class);
        }

        project.getTasks().register("collectDocs", CollectDocsTask.class, task -> {
            task.setGroup("documentation");
            task.setDescription("Collects dependency documentation into build/ai-docs/");
            task.getOutputDirectory().convention(project.getLayout().getBuildDirectory().dir("ai-docs"));
            task.getContextMinLines().convention(150);

            String rootDir = project.getRootDir().getAbsolutePath();
            task.getSkillDirectory().set(Path.of(rootDir, ".claude", "skills", "docs").toString());
            task.getRelativeDocsDir().set(task.getOutputDirectory().getLocationOnly().map(dir ->
                    Path.of(rootDir).relativize(dir.getAsFile().toPath()).toString()));

            task.getPartialDirs().from(project.getTasks().named(AiDocsPartialPlugin.TASK_NAME, CollectDocsPartialTask.class)
                    .flatMap(CollectDocsPartialTask::getOutputDirectory));
            for (Project subproject : project.getSubprojects()) {
                task.getPartialDirs().from(subproject.getTasks().named(AiDocsPartialPlugin.TASK_NAME, CollectDocsPartialTask.class)
                        .flatMap(CollectDocsPartialTask::getOutputDirectory));
            }
        });
    }
}
