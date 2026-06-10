package one.jpro.platform.aidocs.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

/**
 * Gradle plugin that collects DOCUMENTATION.md artifacts from project dependencies
 * and generates an AI-navigable file structure.
 */
public class AiDocsPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.getTasks().register("collectDocs", CollectDocsTask.class, task -> {
            task.setGroup("documentation");
            task.setDescription("Collects DOCUMENTATION.md artifacts from dependencies into build/ai-docs/");
            task.getOutputDirectory().convention(project.getLayout().getBuildDirectory().dir("ai-docs"));
            task.getOverviewMinLines().convention(15);
            task.getContextMinLines().convention(150);
            // The dependency graph is not modeled as an input yet, so never skip
            task.getOutputs().upToDateWhen(t -> false);
        });
    }
}
