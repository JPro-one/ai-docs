package one.jpro.platform.aidocs.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

/**
 * Internal plugin that registers the per-project {@code collectDocsPartial} task.
 * Applied automatically to all subprojects by {@link AiDocsPlugin}.
 *
 * The expensive dependency resolution happens inside the task's configuration
 * action, which Gradle only runs when the task is actually realized (i.e. part
 * of the requested task graph) — other builds pay nothing.
 */
public class AiDocsPartialPlugin implements Plugin<Project> {

    public static final String TASK_NAME = "collectDocsPartial";

    @Override
    public void apply(Project project) {
        AiDocsExtension extension = project.getExtensions().create(AiDocsExtension.NAME, AiDocsExtension.class);
        extension.getIncludeBuildscript().convention(false);

        project.getTasks().register(TASK_NAME, CollectDocsPartialTask.class, task -> {
            task.setDescription("Collects dependency documentation for this project (internal)");
            task.getOutputDirectory().convention(project.getLayout().getBuildDirectory().dir("ai-docs-partial"));
            task.getOverviewMinLines().convention(15);

            for (ModuleSpec spec : DocsResolver.resolve(project, extension.getIncludeBuildscript().get())) {
                task.getModuleSpecs().add(spec.encode());
                task.getResolvedFiles().from(spec.files().toArray());
            }
        });
    }
}
