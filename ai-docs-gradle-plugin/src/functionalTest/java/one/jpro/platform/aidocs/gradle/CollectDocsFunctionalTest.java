package one.jpro.platform.aidocs.gradle;

import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.BuildResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS;

class CollectDocsFunctionalTest {

    private static final String JPRO_REPOS = """
            repositories {
                mavenCentral()
                maven {
                    url "https://sandec.jfrog.io/artifactory/repo"
                }
            }
            """;

    @TempDir
    Path projectDir;

    @BeforeEach
    void setUp() throws IOException {
        Files.writeString(projectDir.resolve("settings.gradle"), "rootProject.name = 'test-project'\n");
    }

    @Test
    void taskIsRegistered() throws IOException {
        Files.writeString(projectDir.resolve("build.gradle"), """
                plugins {
                    id 'java'
                    id 'one.jpro.aidocs'
                }
                repositories {
                    mavenCentral()
                }
                """);

        BuildResult result = GradleRunner.create()
                .withProjectDir(projectDir.toFile())
                .withArguments("tasks", "--group=documentation")
                .withPluginClasspath()
                .build();

        assertThat(result.getOutput()).contains("collectDocs");
    }

    @Test
    void collectDocsWithNoDependencies() throws IOException {
        Files.writeString(projectDir.resolve("build.gradle"), """
                plugins {
                    id 'java'
                    id 'one.jpro.aidocs'
                }
                repositories {
                    mavenCentral()
                }
                """);

        BuildResult result = GradleRunner.create()
                .withProjectDir(projectDir.toFile())
                .withArguments("collectDocs")
                .withPluginClasspath()
                .build();

        assertThat(result.task(":collectDocs").getOutcome()).isEqualTo(SUCCESS);
        assertThat(result.getOutput()).contains("Collected documentation for 0 libraries");

        Path aiDocs = projectDir.resolve("build/ai-docs");
        assertThat(aiDocs.resolve("index.md")).exists();
        assertThat(aiDocs.resolve("context.md")).exists();

        String index = Files.readString(aiDocs.resolve("index.md"));
        assertThat(index).contains("# AI Documentation Index");
        // No library entries
        assertThat(index.lines().filter(l -> l.startsWith("- ")).count()).isZero();

        String context = Files.readString(aiDocs.resolve("context.md"));
        assertThat(context).contains("# Project Documentation Context");

        // SKILL.md should be generated
        Path skillFile = projectDir.resolve(".claude/skills/docs/SKILL.md");
        assertThat(skillFile).exists();
        String skill = Files.readString(skillFile);
        assertThat(skill).contains("build/ai-docs/");
        assertThat(skill).contains("index.md");
        assertThat(skill).contains("overview.md");
        assertThat(skill).contains("DOCUMENTATION.md");
        assertThat(result.getOutput()).contains("Generated AI skill at .claude/skills/docs/SKILL.md");
    }

    @Test
    void collectDocsFromJproRoutingCore() throws IOException {
        Files.writeString(projectDir.resolve("build.gradle"), """
                plugins {
                    id 'java'
                    id 'one.jpro.aidocs'
                }
                """ + JPRO_REPOS + """
                dependencies {
                    implementation 'one.jpro.platform:jpro-routing-core:0.5.8'
                }
                """);

        BuildResult result = GradleRunner.create()
                .withProjectDir(projectDir.toFile())
                .withArguments("collectDocs")
                .withPluginClasspath()
                .build();

        assertThat(result.task(":collectDocs").getOutcome()).isEqualTo(SUCCESS);

        Path aiDocs = projectDir.resolve("build/ai-docs");

        // index.md should list the library exactly once with line range into context.md
        String index = Files.readString(aiDocs.resolve("index.md"));
        assertThat(index).contains("one.jpro.platform:jpro-routing-core");
        assertThat(index).contains("lines");
        long indexCount = index.lines().filter(l -> l.contains("jpro-routing-core")).count();
        assertThat(indexCount).isEqualTo(1);

        // DOCUMENTATION.md should be collected with real content
        Path docFile = aiDocs.resolve("one.jpro.platform/jpro-routing-core/DOCUMENTATION.md");
        assertThat(docFile).exists();
        String doc = Files.readString(docFile);
        assertThat(doc.length()).isGreaterThan(100);
        // Should contain actual routing documentation
        assertThat(doc).containsIgnoringCase("routing");

        // overview.md should have chapters parsed from the documentation
        Path overviewFile = aiDocs.resolve("one.jpro.platform/jpro-routing-core/overview.md");
        assertThat(overviewFile).exists();
        String overview = Files.readString(overviewFile);
        assertThat(overview).contains("jpro-routing-core");
        assertThat(overview).contains("## Chapters");
        assertThat(overview).contains("lines");
        // Should have summaries (at least one line with " — ")
        assertThat(overview).contains(" — ");

        // context.md should exist and contain the library
        Path contextFile = aiDocs.resolve("context.md");
        assertThat(contextFile).exists();
        String context = Files.readString(contextFile);
        assertThat(context).contains("one.jpro.platform:jpro-routing-core");
        assertThat(context).contains("Chapters");

        // sources.jar should be collected (jpro-routing-core publishes sources)
        Path sourcesJar = aiDocs.resolve("one.jpro.platform/jpro-routing-core/sources.jar");
        assertThat(sourcesJar).exists();
        assertThat(Files.size(sourcesJar)).isGreaterThan(0);

        // sources-index.md should be generated
        Path sourcesIndex = aiDocs.resolve("one.jpro.platform/jpro-routing-core/sources-index.md");
        assertThat(sourcesIndex).exists();
        String srcIndex = Files.readString(sourcesIndex);
        assertThat(srcIndex).contains("Source Index");
        assertThat(srcIndex).contains(".java");

        // CHANGELOG.md — graceful absence: if the artifact doesn't publish one, no file should exist
        Path changelogFile = aiDocs.resolve("one.jpro.platform/jpro-routing-core/CHANGELOG.md");
        Path changelogOverview = aiDocs.resolve("one.jpro.platform/jpro-routing-core/changelog-overview.md");
        if (changelogFile.toFile().exists()) {
            // If a changelog is present, the overview should also exist
            assertThat(changelogOverview).exists();
            String changelogContent = Files.readString(changelogOverview);
            assertThat(changelogContent).contains("Chapters");
            // context.md should reference the changelog
            assertThat(context).contains("changelog-overview.md");
        } else {
            // No changelog artifact — overview should not exist either
            assertThat(changelogOverview).doesNotExist();
        }

    }

    @Test
    void collectDocsFromMultipleJproDependencies() throws IOException {
        Files.writeString(projectDir.resolve("build.gradle"), """
                plugins {
                    id 'java'
                    id 'one.jpro.aidocs'
                }
                """ + JPRO_REPOS + """
                dependencies {
                    implementation 'one.jpro.platform:jpro-routing-core:0.5.8'
                    implementation 'one.jpro:jpro-webapi:2025.3.1'
                }
                """);

        BuildResult result = GradleRunner.create()
                .withProjectDir(projectDir.toFile())
                .withArguments("collectDocs")
                .withPluginClasspath()
                .build();

        assertThat(result.task(":collectDocs").getOutcome()).isEqualTo(SUCCESS);

        Path aiDocs = projectDir.resolve("build/ai-docs");

        // Both libraries should appear in the index
        String index = Files.readString(aiDocs.resolve("index.md"));
        assertThat(index).contains("jpro-routing-core");
        assertThat(index).contains("jpro-webapi");

        // Both should have documentation files
        assertThat(aiDocs.resolve("one.jpro.platform/jpro-routing-core/DOCUMENTATION.md")).exists();
        assertThat(aiDocs.resolve("one.jpro/jpro-webapi/DOCUMENTATION.md")).exists();

        // Both should have overview files
        assertThat(aiDocs.resolve("one.jpro.platform/jpro-routing-core/overview.md")).exists();
        assertThat(aiDocs.resolve("one.jpro/jpro-webapi/overview.md")).exists();

        // Both should contain real content
        String routingDoc = Files.readString(aiDocs.resolve("one.jpro.platform/jpro-routing-core/DOCUMENTATION.md"));
        String webapiDoc = Files.readString(aiDocs.resolve("one.jpro/jpro-webapi/DOCUMENTATION.md"));
        assertThat(routingDoc.length()).isGreaterThan(100);
        assertThat(webapiDoc.length()).isGreaterThan(100);
    }

    @Test
    void collectDocsSkipsDependenciesWithoutDocumentation() throws IOException {
        Files.writeString(projectDir.resolve("build.gradle"), """
                plugins {
                    id 'java'
                    id 'one.jpro.aidocs'
                }
                """ + JPRO_REPOS + """
                dependencies {
                    implementation 'org.slf4j:slf4j-api:2.0.17'
                    implementation 'one.jpro.platform:jpro-routing-core:0.5.8'
                }
                """);

        BuildResult result = GradleRunner.create()
                .withProjectDir(projectDir.toFile())
                .withArguments("collectDocs")
                .withPluginClasspath()
                .build();

        assertThat(result.task(":collectDocs").getOutcome()).isEqualTo(SUCCESS);

        Path aiDocs = projectDir.resolve("build/ai-docs");
        String index = Files.readString(aiDocs.resolve("index.md"));
        // jpro-routing-core should be there
        assertThat(index).contains("jpro-routing-core");

        // slf4j has sources but no DOCUMENTATION.md — it should still appear in the index with sources
        assertThat(index).contains("slf4j");
        Path slf4jSourcesJar = aiDocs.resolve("org.slf4j/slf4j-api/sources.jar");
        assertThat(slf4jSourcesJar).exists();
        Path slf4jSourcesIndex = aiDocs.resolve("org.slf4j/slf4j-api/sources-index.md");
        assertThat(slf4jSourcesIndex).exists();

        // slf4j POM has name/description/url/license — verify POM metadata is parsed
        // The SLF4J POM contains a human-readable name
        assertThat(index).contains("SLF4J");
    }

    @Test
    void collectDocsFromBuildscriptDependency() throws IOException {
        // Put jpro-routing-core in the buildscript classpath (not project dependencies)
        // to verify that buildscript scanning picks it up
        Files.writeString(projectDir.resolve("build.gradle"), """
                buildscript {
                """ + JPRO_REPOS + """
                    dependencies {
                        classpath 'one.jpro.platform:jpro-routing-core:0.5.8'
                    }
                }
                plugins {
                    id 'java'
                    id 'one.jpro.aidocs'
                }
                repositories {
                    mavenCentral()
                }
                """);

        BuildResult result = GradleRunner.create()
                .withProjectDir(projectDir.toFile())
                .withArguments("collectDocs")
                .withPluginClasspath()
                .build();

        assertThat(result.task(":collectDocs").getOutcome()).isEqualTo(SUCCESS);

        Path aiDocs = projectDir.resolve("build/ai-docs");

        // jpro-routing-core should appear even though it's only in the buildscript classpath
        String index = Files.readString(aiDocs.resolve("index.md"));
        assertThat(index).contains("jpro-routing-core");

        // DOCUMENTATION.md should be collected
        Path docFile = aiDocs.resolve("one.jpro.platform/jpro-routing-core/DOCUMENTATION.md");
        assertThat(docFile).exists();
        String doc = Files.readString(docFile);
        assertThat(doc.length()).isGreaterThan(100);
        assertThat(doc).containsIgnoringCase("routing");
    }

    @Test
    void collectDocsFromSubprojectDependencies() throws IOException {
        // Root project applies the plugin but has no dependencies of its own
        Files.writeString(projectDir.resolve("build.gradle"), """
                plugins {
                    id 'one.jpro.aidocs'
                }
                repositories {
                    mavenCentral()
                }
                """);

        // Settings: include a subproject
        Files.writeString(projectDir.resolve("settings.gradle"), """
                rootProject.name = 'test-multiproject'
                include 'sub'
                """);

        // Subproject has jpro-routing-core as a dependency
        Path subDir = projectDir.resolve("sub");
        Files.createDirectories(subDir);
        Files.writeString(subDir.resolve("build.gradle"), """
                plugins {
                    id 'java'
                }
                """ + JPRO_REPOS + """
                dependencies {
                    implementation 'one.jpro.platform:jpro-routing-core:0.5.8'
                }
                """);

        BuildResult result = GradleRunner.create()
                .withProjectDir(projectDir.toFile())
                .withArguments("collectDocs")
                .withPluginClasspath()
                .build();

        assertThat(result.task(":collectDocs").getOutcome()).isEqualTo(SUCCESS);
        assertThat(result.getOutput()).contains("Scanning subproject: :sub");

        Path aiDocs = projectDir.resolve("build/ai-docs");

        // jpro-routing-core comes from the subproject — should be collected
        String index = Files.readString(aiDocs.resolve("index.md"));
        assertThat(index).contains("jpro-routing-core");

        Path docFile = aiDocs.resolve("one.jpro.platform/jpro-routing-core/DOCUMENTATION.md");
        assertThat(docFile).exists();
        String doc = Files.readString(docFile);
        assertThat(doc).containsIgnoringCase("routing");
    }

    @Test
    void collectDocsIsIdempotent() throws IOException {
        Files.writeString(projectDir.resolve("build.gradle"), """
                plugins {
                    id 'java'
                    id 'one.jpro.aidocs'
                }
                """ + JPRO_REPOS + """
                dependencies {
                    implementation 'one.jpro.platform:jpro-routing-core:0.5.8'
                }
                """);

        GradleRunner runner = GradleRunner.create()
                .withProjectDir(projectDir.toFile())
                .withArguments("collectDocs")
                .withPluginClasspath();

        // Run twice — both should succeed, second run should produce same output
        runner.build();
        BuildResult second = runner.build();
        assertThat(second.task(":collectDocs")).isNotNull();

        // Output should still be valid after second run
        String index = Files.readString(projectDir.resolve("build/ai-docs/index.md"));
        assertThat(index).contains("jpro-routing-core");
    }
}
