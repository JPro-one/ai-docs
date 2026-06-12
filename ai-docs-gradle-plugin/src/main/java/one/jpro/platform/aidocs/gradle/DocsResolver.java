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
import org.gradle.api.invocation.Gradle;
import org.gradle.jvm.JvmLibrary;
import org.gradle.language.base.artifact.SourcesArtifact;
import org.gradle.language.java.artifact.JavadocArtifact;

import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Resolves documentation artifacts (DOCUMENTATION.md, sources.jar, CHANGELOG.md, javadoc
 * jar, POM) for all dependencies of a project. Runs at configuration time, so the resulting
 * {@link ModuleSpec}s can be wired into the task as plain, configuration-cache-safe inputs.
 *
 * Per-coordinate resolution results are shared across all projects of one build invocation
 * — in multi-module builds most coordinates appear in many subprojects.
 */
class DocsResolver {

    /** Resolved artifacts of one module, cacheable across projects within one build. */
    private record ResolvedModule(File doc, File sources, File changelog, File javadoc,
                                  List<File> pomChain, String[] parent) {}

    /**
     * Cache keyed by the build invocation (Gradle instance), so a long-lived daemon
     * never serves stale results to a later build. Entries are weakly referenced.
     */
    private static final Map<Gradle, Map<String, ResolvedModule>> RESOLUTION_CACHE =
            Collections.synchronizedMap(new WeakHashMap<>());

    static List<ModuleSpec> resolve(Project project, boolean includeBuildscript) {
        List<ModuleSpec> specs = new ArrayList<>();
        Set<String> seenCoordinates = new HashSet<>();
        List<String> shadowedByMavenLocal = new ArrayList<>();

        // Gather coordinates first: a coordinate is test-only when it appears in
        // test classpaths but no main classpath
        Map<String, String[]> mainCoords = new LinkedHashMap<>();
        Map<String, String[]> testCoords = new LinkedHashMap<>();
        gatherCoordinates(project.getConfigurations(), mainCoords, testCoords);
        testCoords.keySet().removeAll(mainCoords.keySet());

        processQueue(project, project.getConfigurations(), project.getDependencies(),
                mainCoords.values(), false, specs, seenCoordinates, shadowedByMavenLocal);
        processQueue(project, project.getConfigurations(), project.getDependencies(),
                testCoords.values(), true, specs, seenCoordinates, shadowedByMavenLocal);

        if (includeBuildscript) {
            // Buildscript dependencies resolve against the buildscript's own repositories
            Map<String, String[]> buildscriptCoords = new LinkedHashMap<>();
            gatherCoordinates(project.getBuildscript().getConfigurations(), buildscriptCoords, new LinkedHashMap<>());
            processQueue(project, project.getBuildscript().getConfigurations(), project.getBuildscript().getDependencies(),
                    buildscriptCoords.values(), false, specs, seenCoordinates, shadowedByMavenLocal);
        }

        if (!shadowedByMavenLocal.isEmpty()) {
            project.getLogger().warn(
                    "The following dependencies were resolved from mavenLocal (~/.m2) and provided no "
                            + "documentation artifacts. An incomplete local copy (e.g. jar+pom cached by a Maven run) "
                            + "may be hiding their DOCUMENTATION/sources/javadoc artifacts — Gradle only fetches "
                            + "artifacts from the repository that supplied a module's metadata. "
                            + "Consider removing mavenLocal() or restricting it with a content filter "
                            + "(see the Troubleshooting section of the ai-docs README):\n  "
                            + String.join("\n  ", shadowedByMavenLocal));
        }

        return specs;
    }

    /**
     * Collects all module coordinates of the resolvable classpath configurations,
     * classified by whether they appear on a non-test classpath.
     */
    private static void gatherCoordinates(ConfigurationContainer configurations,
                                          Map<String, String[]> mainCoords, Map<String, String[]> testCoords) {
        for (Configuration config : configurations) {
            if (!config.isCanBeResolved()) continue;
            String configName = config.getName().toLowerCase();
            if (!configName.contains("classpath")) continue;
            boolean isTest = configName.contains("test");

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
                String[] coord = {moduleId.getGroup(), moduleId.getModule(), moduleId.getVersion()};
                String key = coord[0] + ":" + coord[1] + ":" + coord[2];
                (isTest ? testCoords : mainCoords).putIfAbsent(key, coord);
            }
        }
    }

    private static void processQueue(Project project,
                                     ConfigurationContainer configurations, DependencyHandler dependencies,
                                     java.util.Collection<String[]> coords, boolean testOnly,
                                     List<ModuleSpec> specs, Set<String> seenCoordinates,
                                     List<String> shadowedByMavenLocal) {
        Deque<String[]> modulesToProcess = new ArrayDeque<>(coords);
        while (!modulesToProcess.isEmpty()) {
            String[] module = modulesToProcess.poll();
            processModule(project, configurations, dependencies, module[0], module[1], module[2], testOnly,
                    specs, seenCoordinates, modulesToProcess, shadowedByMavenLocal);
        }
    }

    private static void processModule(Project project,
                                      ConfigurationContainer configurations, DependencyHandler dependencies,
                                      String group, String name, String version, boolean testOnly,
                                      List<ModuleSpec> specs, Set<String> seenCoordinates,
                                      Deque<String[]> modulesToProcess, List<String> shadowedByMavenLocal) {
        String coordinate = group + ":" + name + ":" + version;
        if (!seenCoordinates.add(coordinate)) return;

        Map<String, ResolvedModule> cache = RESOLUTION_CACHE
                .computeIfAbsent(project.getGradle(), g -> new ConcurrentHashMap<>());
        ResolvedModule resolved = cache.computeIfAbsent(coordinate,
                c -> resolveModule(configurations, dependencies, group, name, version));

        // Queue the immediate parent for processing as its own module (parents
        // inherit the test classification of their discoverer)
        if (resolved.parent() != null) {
            String parentCoord = resolved.parent()[0] + ":" + resolved.parent()[1] + ":" + resolved.parent()[2];
            if (!seenCoordinates.contains(parentCoord)) {
                modulesToProcess.add(resolved.parent());
                project.getLogger().debug("Discovered parent POM: {}", parentCoord);
            }
        }

        if (resolved.doc() != null || resolved.sources() != null
                || resolved.changelog() != null || resolved.javadoc() != null) {
            specs.add(new ModuleSpec(group, name, version, resolved.doc(), resolved.sources(),
                    resolved.changelog(), resolved.javadoc(), resolved.pomChain(), testOnly));
        } else {
            // The POM's location reveals which repository served the module's metadata —
            // a mavenLocal-served module with no artifacts is the classic incomplete-~/.m2 trap
            if (!resolved.pomChain().isEmpty()) {
                var pomPath = resolved.pomChain().get(0).toPath();
                if (mavenLocalRoots(project).stream().anyMatch(pomPath::startsWith)) {
                    shadowedByMavenLocal.add(coordinate);
                }
            }
            project.getLogger().debug("No documentation artifacts for {}", coordinate);
        }
    }

    private static ResolvedModule resolveModule(ConfigurationContainer configurations, DependencyHandler dependencies,
                                                String group, String name, String version) {
        String coordinate = group + ":" + name + ":" + version;

        File doc = resolveArtifact(configurations, dependencies, coordinate + ":DOCUMENTATION@md");
        File changelog = resolveArtifact(configurations, dependencies, coordinate + ":CHANGELOG@md");
        File pom = resolveArtifact(configurations, dependencies, coordinate + "@pom");
        // Sources/javadoc via ArtifactResolutionQuery — classifier requests on detached
        // configurations fail for modules with Gradle Module Metadata (e.g. JavaFX),
        // whose variants require OS/arch attributes
        File sources = resolveViaQuery(dependencies, group, name, version, SourcesArtifact.class);
        File javadoc = resolveViaQuery(dependencies, group, name, version, JavadocArtifact.class);

        // Walk the parent chain for metadata fallback
        List<File> pomChain = new ArrayList<>();
        String[] parent = null;
        if (pom != null) {
            pomChain.add(pom);
            parent = PomParser.parseParent(pom.toPath());
            File current = pom;
            String[] next = parent;
            while (next != null && pomChain.size() < 10) {
                File parentPom = resolveArtifact(configurations, dependencies,
                        next[0] + ":" + next[1] + ":" + next[2] + "@pom");
                if (parentPom == null) break;
                pomChain.add(parentPom);
                current = parentPom;
                next = PomParser.parseParent(current.toPath());
            }
        }
        return new ResolvedModule(doc, sources, changelog, javadoc, pomChain, parent);
    }

    /**
     * Returns the local roots of all mavenLocal-style repositories (file-backed Maven repos)
     * declared on the project or its buildscript.
     */
    private static List<java.nio.file.Path> mavenLocalRoots(Project project) {
        List<java.nio.file.Path> roots = new ArrayList<>();
        List<org.gradle.api.artifacts.repositories.ArtifactRepository> repos = new ArrayList<>();
        repos.addAll(project.getRepositories());
        repos.addAll(project.getBuildscript().getRepositories());
        for (var repo : repos) {
            if (repo instanceof org.gradle.api.artifacts.repositories.MavenArtifactRepository maven
                    && "file".equalsIgnoreCase(maven.getUrl().getScheme())) {
                roots.add(java.nio.file.Path.of(maven.getUrl()));
            }
        }
        return roots;
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
