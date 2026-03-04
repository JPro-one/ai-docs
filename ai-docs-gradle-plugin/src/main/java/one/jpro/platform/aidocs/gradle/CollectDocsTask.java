package one.jpro.platform.aidocs.gradle;

import one.jpro.platform.aidocs.core.DocEntry;
import one.jpro.platform.aidocs.core.DocsCollector;
import one.jpro.platform.aidocs.core.PomParser;
import org.gradle.api.DefaultTask;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ResolvedDependency;
import org.gradle.api.artifacts.component.ModuleComponentIdentifier;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Gradle task that resolves DOCUMENTATION.md classifier artifacts from all dependencies
 * and delegates to {@link DocsCollector} for file generation.
 */
public abstract class CollectDocsTask extends DefaultTask {

    @OutputDirectory
    public abstract DirectoryProperty getOutputDirectory();

    @Input
    public abstract Property<Integer> getOverviewMinLines();

    @Input
    public abstract Property<Integer> getContextMinLines();

    @TaskAction
    public void collectDocs() throws IOException {
        Path outputDir = getOutputDirectory().get().getAsFile().toPath();
        DocsCollector.cleanOutputDir(outputDir);

        List<DocEntry> entries = new ArrayList<>();
        Set<String> seenCoordinates = new HashSet<>();

        for (Configuration config : getProject().getConfigurations()) {
            if (!config.isCanBeResolved()) continue;
            if (!config.getName().toLowerCase().contains("classpath")) continue;

            // Walk the full dependency graph to discover all modules, including POM-only ones
            // that have no jar artifact but may publish DOCUMENTATION.md
            Deque<ResolvedDependency> queue = new ArrayDeque<>(
                    config.getResolvedConfiguration().getFirstLevelModuleDependencies());
            while (!queue.isEmpty()) {
                ResolvedDependency dep = queue.poll();
                String group = dep.getModuleGroup();
                String name = dep.getModuleName();
                String version = dep.getModuleVersion();

                String coordinate = group + ":" + name + ":" + version;
                if (!seenCoordinates.add(coordinate)) continue;

                var entry = new DocEntry(group, name, version);
                DocEntry enriched = entry;
                boolean hasDocs = false;
                try {
                    var docDep = getProject().getDependencies().create(
                            group + ":" + name + ":" + version + ":DOCUMENTATION@md"
                    );
                    var detached = getProject().getConfigurations().detachedConfiguration(docDep);
                    detached.setTransitive(false);

                    for (File docFile : detached.resolve()) {
                        enriched = DocsCollector.collectDoc(outputDir, docFile.toPath(), entry, getOverviewMinLines().get());
                        hasDocs = true;
                        getLogger().lifecycle("Collected docs: {}:{}", group, name);
                    }
                } catch (Exception e) {
                    getLogger().debug("No DOCUMENTATION.md for {}:{}:{}", group, name, version);
                }

                try {
                    var srcDep = getProject().getDependencies().create(
                            group + ":" + name + ":" + version + ":sources@jar"
                    );
                    var detached = getProject().getConfigurations().detachedConfiguration(srcDep);
                    detached.setTransitive(false);

                    for (File srcFile : detached.resolve()) {
                        DocsCollector.collectSources(outputDir, srcFile.toPath(), enriched);
                        enriched = enriched.withHasSources(true);
                        getLogger().lifecycle("Collected sources: {}:{}", group, name);
                    }
                } catch (Exception e) {
                    getLogger().debug("No sources jar for {}:{}:{}", group, name, version);
                }

                if (hasDocs || enriched.hasSources()) {
                    try {
                        var pomDep = getProject().getDependencies().create(
                                group + ":" + name + ":" + version + "@pom"
                        );
                        var detached = getProject().getConfigurations().detachedConfiguration(pomDep);
                        detached.setTransitive(false);

                        for (File pomFile : detached.resolve()) {
                            var pomMetadata = PomParser.parse(pomFile.toPath());
                            enriched = enriched.withPomMetadata(pomMetadata);
                            getLogger().debug("Parsed POM metadata for {}:{}", group, name);
                        }
                    } catch (Exception e) {
                        getLogger().debug("No POM for {}:{}:{}", group, name, version);
                    }

                    entries.add(enriched);
                }

                queue.addAll(dep.getChildren());
            }
        }

        DocsCollector.generateContextAndIndex(outputDir, entries, getContextMinLines().get());

        Path skillDir = getProject().getRootDir().toPath().resolve(".claude/skills/docs");
        String relativeDocsDir = getProject().getRootDir().toPath().relativize(outputDir).toString();
        DocsCollector.generateSkill(skillDir, relativeDocsDir);
        getLogger().lifecycle("Generated AI skill at .claude/skills/docs/SKILL.md");

        getLogger().lifecycle("Collected documentation for {} libraries into {}", entries.size(), outputDir);
    }
}
