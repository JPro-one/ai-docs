package one.jpro.platform.aidocs.gradle;

import one.jpro.platform.aidocs.core.DocEntry;
import one.jpro.platform.aidocs.core.DocsCollector;
import org.gradle.api.DefaultTask;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.component.ModuleComponentIdentifier;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Gradle task that resolves DOCUMENTATION.md classifier artifacts from all dependencies
 * and delegates to {@link DocsCollector} for file generation.
 */
public abstract class CollectDocsTask extends DefaultTask {

    @OutputDirectory
    public abstract DirectoryProperty getOutputDirectory();

    @TaskAction
    public void collectDocs() throws IOException {
        Path outputDir = getOutputDirectory().get().getAsFile().toPath();
        DocsCollector.cleanOutputDir(outputDir);

        List<DocEntry> entries = new ArrayList<>();

        for (Configuration config : getProject().getConfigurations()) {
            if (!config.isCanBeResolved()) continue;
            if (!config.getName().toLowerCase().contains("classpath")) continue;

            for (var artifact : config.getResolvedConfiguration().getResolvedArtifacts()) {
                if (!(artifact.getId().getComponentIdentifier() instanceof ModuleComponentIdentifier id)) continue;

                String group = id.getGroup();
                String name = id.getModule();
                String version = id.getVersion();

                // Skip if we already collected this dependency
                var entry = new DocEntry(group, name, version);
                if (entries.contains(entry)) continue;

                try {
                    var docDep = getProject().getDependencies().create(
                            group + ":" + name + ":" + version + ":DOCUMENTATION@md"
                    );
                    var detached = getProject().getConfigurations().detachedConfiguration(docDep);
                    detached.setTransitive(false);

                    for (File docFile : detached.resolve()) {
                        DocsCollector.collectDoc(outputDir, docFile.toPath(), entry);
                        entries.add(entry);
                        getLogger().lifecycle("Collected docs: {}:{}", group, name);
                    }
                } catch (Exception e) {
                    getLogger().debug("No DOCUMENTATION.md for {}:{}:{}", group, name, version);
                }
            }
        }

        DocsCollector.generateIndex(outputDir, entries);
        getLogger().lifecycle("Collected documentation for {} libraries into {}", entries.size(), outputDir);
    }
}
