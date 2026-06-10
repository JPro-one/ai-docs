package one.jpro.platform.aidocs.gradle;

import one.jpro.platform.aidocs.core.PomParser;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.component.ModuleComponentIdentifier;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.artifacts.result.ArtifactResult;
import org.gradle.api.artifacts.result.ComponentArtifactsResult;
import org.gradle.api.artifacts.result.ResolvedArtifactResult;
import org.gradle.api.artifacts.result.ResolvedComponentResult;
import org.gradle.api.artifacts.result.ResolvedDependencyResult;
import org.gradle.api.component.Artifact;
import org.gradle.jvm.JvmLibrary;
import org.gradle.language.base.artifact.SourcesArtifact;
import org.gradle.language.java.artifact.JavadocArtifact;

import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Resolves documentation artifacts (DOCUMENTATION.md, sources.jar, CHANGELOG.md, POM)
 * for all dependencies of a project. Runs at configuration time, so the resulting
 * {@link ModuleSpec}s can be wired into the task as plain, configuration-cache-safe inputs.
 */
class DocsResolver {

    static List<ModuleSpec> resolve(Project project, boolean includeBuildscript) {
        List<ModuleSpec> specs = new ArrayList<>();
        Set<String> seenCoordinates = new HashSet<>();

        scanConfigurations(project, project.getConfigurations(), project.getDependencies(),
                specs, seenCoordinates);
        if (includeBuildscript) {
            scanConfigurations(project, project.getBuildscript().getConfigurations(),
                    project.getBuildscript().getDependencies(), specs, seenCoordinates);
        }

        return specs;
    }

    private static void scanConfigurations(Project project,
                                           ConfigurationContainer configurations, DependencyHandler dependencies,
                                           List<ModuleSpec> specs, Set<String> seenCoordinates) {
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
                processModule(project, module[0], module[1], module[2], configurations, dependencies,
                        specs, seenCoordinates, modulesToProcess);
            }
        }
    }

    private static void processModule(Project project, String group, String name, String version,
                                      ConfigurationContainer configurations, DependencyHandler dependencies,
                                      List<ModuleSpec> specs, Set<String> seenCoordinates,
                                      Deque<String[]> modulesToProcess) {
        String coordinate = group + ":" + name + ":" + version;
        if (!seenCoordinates.add(coordinate)) return;

        File doc = resolveArtifact(configurations, dependencies, coordinate + ":DOCUMENTATION@md");
        File changelog = resolveArtifact(configurations, dependencies, coordinate + ":CHANGELOG@md");
        File pom = resolveArtifact(configurations, dependencies, coordinate + "@pom");
        // Sources/javadoc via ArtifactResolutionQuery — classifier requests on detached
        // configurations fail for modules with Gradle Module Metadata (e.g. JavaFX),
        // whose variants require OS/arch attributes
        File sources = resolveViaQuery(dependencies, group, name, version, SourcesArtifact.class);
        File javadoc = resolveViaQuery(dependencies, group, name, version, JavadocArtifact.class);

        // Walk the parent chain: queue the immediate parent for processing as its own
        // module, and collect all parent POMs for metadata fallback
        List<File> pomChain = new ArrayList<>();
        if (pom != null) {
            pomChain.add(pom);
            File current = pom;
            boolean immediate = true;
            while (pomChain.size() < 10) {
                String[] parent = PomParser.parseParent(current.toPath());
                if (parent == null) break;
                String parentCoord = parent[0] + ":" + parent[1] + ":" + parent[2];
                if (immediate && !seenCoordinates.contains(parentCoord)) {
                    modulesToProcess.add(parent);
                    project.getLogger().debug("Discovered parent POM: {}", parentCoord);
                }
                immediate = false;
                File parentPom = resolveArtifact(configurations, dependencies, parentCoord + "@pom");
                if (parentPom == null) break;
                pomChain.add(parentPom);
                current = parentPom;
            }
        }

        if (doc != null || sources != null || changelog != null || javadoc != null) {
            specs.add(new ModuleSpec(group, name, version, doc, sources, changelog, javadoc, pomChain));
        } else {
            project.getLogger().debug("No documentation artifacts for {}", coordinate);
        }
    }

    private static File resolveArtifact(ConfigurationContainer configurations,
                                        DependencyHandler dependencies, String notation) {
        try {
            var detached = configurations.detachedConfiguration(dependencies.create(notation));
            detached.setTransitive(false);
            for (File file : detached.resolve()) {
                return file;
            }
        } catch (Exception e) {
            // artifact not published — expected for most dependencies
        }
        return null;
    }

    private static File resolveViaQuery(DependencyHandler dependencies,
                                        String group, String name, String version,
                                        Class<? extends Artifact> artifactType) {
        try {
            var result = dependencies.createArtifactResolutionQuery()
                    .forModule(group, name, version)
                    .withArtifacts(JvmLibrary.class, artifactType)
                    .execute();
            for (ComponentArtifactsResult component : result.getResolvedComponents()) {
                for (ArtifactResult artifact : component.getArtifacts(artifactType)) {
                    if (artifact instanceof ResolvedArtifactResult resolved) {
                        return resolved.getFile();
                    }
                }
            }
        } catch (Exception e) {
            // artifact not published — expected for most dependencies
        }
        return null;
    }
}
