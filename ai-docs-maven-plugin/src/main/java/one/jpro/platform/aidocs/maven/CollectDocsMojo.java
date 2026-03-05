package one.jpro.platform.aidocs.maven;

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
import java.util.ArrayList;
import java.util.List;

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

        for (Artifact artifact : project.getArtifacts()) {
            String group = artifact.getGroupId();
            String name = artifact.getArtifactId();
            String version = artifact.getVersion();

            var entry = new DocEntry(group, name, version);
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

            // If any artifact was found, resolve POM for metadata
            if (hasDocs || enriched.hasSources() || enriched.hasChangelog()) {
                File pomFile = resolveArtifact(group, name, version, "", "pom");
                if (pomFile != null) {
                    var pomMetadata = PomParser.parse(pomFile.toPath());
                    enriched = enriched.withPomMetadata(pomMetadata);
                }
                entries.add(enriched);
            }
        }

        try {
            DocsCollector.generateContextAndIndex(outputDir, entries, contextMinLines);

            Path skillDir = project.getBasedir().toPath().resolve(".claude/skills/docs");
            String relativeDocsDir = project.getBasedir().toPath().relativize(outputDir).toString();
            DocsCollector.generateSkill(skillDir, relativeDocsDir);
            getLog().info("Generated AI skill at .claude/skills/docs/SKILL.md");

            getLog().info("Collected documentation for " + entries.size() + " libraries into " + outputDir);
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to generate index", e);
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
