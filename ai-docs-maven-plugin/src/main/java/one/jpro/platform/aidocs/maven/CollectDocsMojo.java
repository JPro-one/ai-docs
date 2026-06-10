package one.jpro.platform.aidocs.maven;

import one.jpro.platform.aidocs.core.BuildTool;
import one.jpro.platform.aidocs.core.DocEntry;
import one.jpro.platform.aidocs.core.DocsCollector;
import one.jpro.platform.aidocs.core.PomParser;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResult;

import javax.inject.Inject;
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
 * Maven Mojo that resolves DOCUMENTATION.md, sources.jar, and CHANGELOG.md classifier
 * artifacts from all dependencies and delegates to DocsCollector for file generation.
 */
@Mojo(name = "collect-docs",
        defaultPhase = LifecyclePhase.GENERATE_RESOURCES,
        requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class CollectDocsMojo extends AbstractMojo {

    private final RepositorySystem repoSystem;

    @Inject
    public CollectDocsMojo(RepositorySystem repoSystem) {
        this.repoSystem = repoSystem;
    }

    @Parameter(defaultValue = "${project.build.directory}/ai-docs")
    private File outputDirectory;

    @Parameter(defaultValue = "15")
    private int overviewMinLines;

    @Parameter(defaultValue = "150")
    private int contextMinLines;

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    @Parameter(defaultValue = "${repositorySystemSession}", readonly = true, required = true)
    private RepositorySystemSession repoSession;

    @Parameter(defaultValue = "${project.remoteProjectRepositories}", readonly = true, required = true)
    private List<RemoteRepository> remoteRepositories;

    @Override
    public void execute() throws MojoExecutionException {
        Path outputDir = outputDirectory.toPath();

        try {
            DocsCollector.cleanOutputDir(outputDir);
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to clean output directory", e);
        }

        List<DocEntry> entries = new ArrayList<>();
        Set<String> seenCoordinates = new HashSet<>();

        // Seed the processing queue with all project artifacts
        Deque<String[]> modulesToProcess = new ArrayDeque<>();
        for (Artifact artifact : project.getArtifacts()) {
            modulesToProcess.add(new String[]{artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion()});
        }

        // Process modules, discovering and queuing parent POMs as we go
        while (!modulesToProcess.isEmpty()) {
            String[] module = modulesToProcess.poll();
            processModule(module[0], module[1], module[2], outputDir, entries, seenCoordinates, modulesToProcess);
        }

        try {
            DocsCollector.generateContextAndIndex(outputDir, entries, contextMinLines);

            Path skillDir = project.getBasedir().toPath().resolve(".claude/skills/docs");
            String relativeDocsDir = project.getBasedir().toPath().relativize(outputDir).toString();
            DocsCollector.generateSkill(skillDir, relativeDocsDir, BuildTool.MAVEN);
            getLog().info("Generated AI skill at .claude/skills/docs/SKILL.md");

            getLog().info("Collected documentation for " + entries.size() + " libraries into " + outputDir);
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to generate index", e);
        }
    }

    private void processModule(String group, String name, String version,
                               Path outputDir, List<DocEntry> entries, Set<String> seenCoordinates,
                               Deque<String[]> modulesToProcess) {
        String coordinate = group + ":" + name + ":" + version;
        if (!seenCoordinates.add(coordinate)) return;

        var entry = DocEntry.of(group, name, version);
        DocEntry enriched = entry;
        boolean hasDocs = false;

        // Try DOCUMENTATION.md
        File docFile = resolveArtifact(group, name, version, "DOCUMENTATION", "md");
        if (docFile != null) {
            try {
                enriched = DocsCollector.collectDoc(outputDir, docFile.toPath(), entry, overviewMinLines);
                hasDocs = true;
                getLog().info("Collected docs: " + group + ":" + name);
            } catch (IOException e) {
                getLog().warn("Failed to collect docs for " + group + ":" + name, e);
            }
        }

        // Try sources.jar
        File srcFile = resolveArtifact(group, name, version, "sources", "jar");
        if (srcFile != null) {
            try {
                DocsCollector.collectSources(outputDir, srcFile.toPath(), enriched);
                enriched = enriched.withHasSources(true);
                getLog().info("Collected sources: " + group + ":" + name);
            } catch (IOException e) {
                getLog().warn("Failed to collect sources for " + group + ":" + name, e);
            }
        }

        // Try CHANGELOG.md
        File changelogFile = resolveArtifact(group, name, version, "CHANGELOG", "md");
        if (changelogFile != null) {
            try {
                DocsCollector.collectChangelog(outputDir, changelogFile.toPath(), enriched, overviewMinLines);
                enriched = enriched.withHasChangelog(true);
                getLog().info("Collected changelog: " + group + ":" + name);
            } catch (IOException e) {
                getLog().warn("Failed to collect changelog for " + group + ":" + name, e);
            }
        }

        // Always resolve POM to discover parent chain
        File pomFile = resolveArtifact(group, name, version, "", "pom");
        if (pomFile != null) {
            if (hasDocs || enriched.hasSources() || enriched.hasChangelog()) {
                var pomMetadata = PomParser.parse(pomFile.toPath());
                enriched = enriched.withPomMetadata(pomMetadata);
            }

            // Queue parent for processing if not already seen
            String[] parent = PomParser.parseParent(pomFile.toPath());
            if (parent != null) {
                String parentCoord = parent[0] + ":" + parent[1] + ":" + parent[2];
                if (!seenCoordinates.contains(parentCoord)) {
                    modulesToProcess.add(parent);
                    getLog().debug("Discovered parent POM: " + parentCoord);
                }
            }
        }

        if (hasDocs || enriched.hasSources() || enriched.hasChangelog()) {
            entries.add(enriched);
        }
    }

    /**
     * Resolves a single artifact using Aether. Returns the file if found, null otherwise.
     */
    private File resolveArtifact(String group, String name, String version, String classifier, String extension) {
        try {
            var artifact = new DefaultArtifact(group, name, classifier, extension, version);
            var request = new ArtifactRequest(artifact, remoteRepositories, null);
            ArtifactResult result = repoSystem.resolveArtifact(repoSession, request);
            if (result.isResolved()) {
                return result.getArtifact().getFile();
            }
        } catch (Exception e) {
            getLog().debug("Could not resolve " + group + ":" + name + ":" + version + ":" + classifier + "@" + extension);
        }
        return null;
    }
}
