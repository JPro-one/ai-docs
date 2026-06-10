package one.jpro.platform.aidocs.gradle;

import one.jpro.platform.aidocs.core.BuildTool;
import one.jpro.platform.aidocs.core.DocEntry;
import one.jpro.platform.aidocs.core.DocsCollector;
import one.jpro.platform.aidocs.core.PomParser;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.component.ModuleComponentIdentifier;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.artifacts.result.ResolvedComponentResult;
import org.gradle.api.artifacts.result.ResolvedDependencyResult;
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

        // Scan project dependencies (compile/runtime classpaths)
        scanConfigurations(getProject().getConfigurations(), getProject().getDependencies(),
                outputDir, entries, seenCoordinates);

        // Scan buildscript dependencies (plugin classpath)
        scanConfigurations(getProject().getBuildscript().getConfigurations(),
                getProject().getBuildscript().getDependencies(),
                outputDir, entries, seenCoordinates);

        // Scan all subprojects' dependencies (for multi-project builds)
        for (Project subproject : getProject().getSubprojects()) {
            getLogger().lifecycle("Scanning subproject: {}", subproject.getPath());
            scanConfigurations(subproject.getConfigurations(), subproject.getDependencies(),
                    outputDir, entries, seenCoordinates);
        }

        DocsCollector.generateContextAndIndex(outputDir, entries, getContextMinLines().get());

        Path skillDir = getProject().getRootDir().toPath().resolve(".claude/skills/docs");
        String relativeDocsDir = getProject().getRootDir().toPath().relativize(outputDir).toString();
        DocsCollector.generateSkill(skillDir, relativeDocsDir, BuildTool.GRADLE);
        getLogger().lifecycle("Generated AI skill at .claude/skills/docs/SKILL.md");

        getLogger().lifecycle("Collected documentation for {} libraries into {}", entries.size(), outputDir);
    }

    private void scanConfigurations(ConfigurationContainer configurations, DependencyHandler dependencies,
                                    Path outputDir, List<DocEntry> entries, Set<String> seenCoordinates) {
        for (Configuration config : configurations) {
            if (!config.isCanBeResolved()) continue;
            if (!config.getName().toLowerCase().contains("classpath")) continue;

            // Collect all module coordinates from the dependency graph via BFS
            Deque<String[]> modulesToProcess = new ArrayDeque<>();
            Set<Object> visitedComponents = new HashSet<>();
            ResolvedComponentResult root = config.getIncoming().getResolutionResult().getRoot();
            Deque<ResolvedComponentResult> queue = new ArrayDeque<>();
            queue.add(root);
            while (!queue.isEmpty()) {
                ResolvedComponentResult component = queue.poll();
                if (!visitedComponents.add(component.getId())) continue;

                for (var depResult : component.getDependencies()) {
                    if (depResult instanceof ResolvedDependencyResult resolved) {
                        queue.add(resolved.getSelected());
                    }
                }

                if (!(component.getId() instanceof ModuleComponentIdentifier moduleId)) {
                    continue;
                }
                String coordinate = moduleId.getGroup() + ":" + moduleId.getModule() + ":" + moduleId.getVersion();
                if (seenCoordinates.contains(coordinate)) continue;
                modulesToProcess.add(new String[]{moduleId.getGroup(), moduleId.getModule(), moduleId.getVersion()});
            }

            // Process modules, discovering and queuing parent POMs as we go
            while (!modulesToProcess.isEmpty()) {
                String[] module = modulesToProcess.poll();
                processModule(module[0], module[1], module[2], configurations, dependencies,
                        outputDir, entries, seenCoordinates, modulesToProcess);
            }
        }
    }

    private void processModule(String group, String name, String version,
                               ConfigurationContainer configurations, DependencyHandler dependencies,
                               Path outputDir, List<DocEntry> entries, Set<String> seenCoordinates,
                               Deque<String[]> modulesToProcess) {
        String coordinate = group + ":" + name + ":" + version;
        if (!seenCoordinates.add(coordinate)) return;

        var entry = DocEntry.of(group, name, version);
        DocEntry enriched = entry;
        boolean hasDocs = false;
        try {
            var docDep = dependencies.create(
                    group + ":" + name + ":" + version + ":DOCUMENTATION@md"
            );
            var detached = configurations.detachedConfiguration(docDep);
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
            var srcDep = dependencies.create(
                    group + ":" + name + ":" + version + ":sources@jar"
            );
            var detached = configurations.detachedConfiguration(srcDep);
            detached.setTransitive(false);

            for (File srcFile : detached.resolve()) {
                DocsCollector.collectSources(outputDir, srcFile.toPath(), enriched);
                enriched = enriched.withHasSources(true);
                getLogger().lifecycle("Collected sources: {}:{}", group, name);
            }
        } catch (Exception e) {
            getLogger().debug("No sources jar for {}:{}:{}", group, name, version);
        }

        try {
            var changelogDep = dependencies.create(
                    group + ":" + name + ":" + version + ":CHANGELOG@md"
            );
            var detached = configurations.detachedConfiguration(changelogDep);
            detached.setTransitive(false);

            for (File changelogFile : detached.resolve()) {
                DocsCollector.collectChangelog(outputDir, changelogFile.toPath(), enriched, getOverviewMinLines().get());
                enriched = enriched.withHasChangelog(true);
                getLogger().lifecycle("Collected changelog: {}:{}", group, name);
            }
        } catch (Exception e) {
            getLogger().debug("No CHANGELOG.md for {}:{}:{}", group, name, version);
        }

        // Always resolve POM to discover parent chain
        try {
            var pomDep = dependencies.create(
                    group + ":" + name + ":" + version + "@pom"
            );
            var detached = configurations.detachedConfiguration(pomDep);
            detached.setTransitive(false);

            for (File pomFile : detached.resolve()) {
                if (hasDocs || enriched.hasSources() || enriched.hasChangelog()) {
                    var pomMetadata = PomParser.parse(pomFile.toPath());
                    enriched = enriched.withPomMetadata(pomMetadata);
                    getLogger().debug("Parsed POM metadata for {}:{}", group, name);
                }

                // Queue parent for processing if not already seen
                String[] parent = PomParser.parseParent(pomFile.toPath());
                if (parent != null) {
                    String parentCoord = parent[0] + ":" + parent[1] + ":" + parent[2];
                    if (!seenCoordinates.contains(parentCoord)) {
                        modulesToProcess.add(parent);
                        getLogger().debug("Discovered parent POM: {}", parentCoord);
                    }
                }
            }
        } catch (Exception e) {
            getLogger().debug("No POM for {}:{}:{}", group, name, version);
        }

        if (hasDocs || enriched.hasSources() || enriched.hasChangelog()) {
            entries.add(enriched);
        }
    }
}
